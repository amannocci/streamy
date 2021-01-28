/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017-2021
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package io.techcode.streamy.pipeline

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Keep, Merge, RunnableGraph, Sink, Source}
import akka.stream._
import com.google.common.graph.{GraphBuilder, MutableGraph}
import com.typesafe.config.{Config => TConfig}
import io.techcode.streamy.component.ComponentRegistry
import io.techcode.streamy.event.StreamEvent
import io.techcode.streamy.pipeline.Pipeline.{FlowComponent, SinkComponent, SourceComponent}
import pureconfig._
import pureconfig.generic.auto._

import scala.jdk.javaapi.CollectionConverters._
import scala.util.control.NoStackTrace

/**
  * Represents a full graph pipeline.
  *
  * @param system actor system.
  * @param conf   pipeline configuration.
  */
class Pipeline(system: ActorSystem, pipelineId: String, conf: Pipeline.Config) {

  // Component registry
  private val componentRegistry = ComponentRegistry(system)

  // Implicit actor system
  private implicit val sys: ActorSystem = system

  // Kill switch
  private val killSwitch: SharedKillSwitch = KillSwitches.shared(s"kill-switch-$pipelineId")

  /**
    * Hook to fire on start.
    */
  def onStart(): Unit = {
    // Attempt to build a graph
    val graphView: MutableGraph[String] = GraphBuilder.directed().allowsSelfLoops(false).build[String]

    // Build initial graph without fanIn & fanOut
    var sourceComponents = buildSourceGraphView(graphView)
    var flowComponents = buildFlowGraphView(graphView, sourceComponents.keySet)
    var sinkComponents = buildSinkGraphView(graphView, sourceComponents.keySet | flowComponents.keySet)

    // Recreate with fanIn & fanOut
    sourceComponents = sourceComponents.map { case (componentId: String, component: SourceComponent) =>
      val fanOutCount = graphView.successors(componentId).size()
      (componentId, component.withFanOut(fanOutCount))
    }
    flowComponents = flowComponents.map { case (componentId: String, component: FlowComponent) =>
      val fanInCount = graphView.predecessors(componentId).size()
      val fanOutCount = graphView.successors(componentId).size()
      (componentId, component.withFanIn(fanInCount).withFanOut(fanOutCount))
    }
    sinkComponents = sinkComponents.map { case (componentId: String, component: SinkComponent) =>
      val fanInCount = graphView.predecessors(componentId).size()
      (componentId, component.withFanIn(fanInCount))
    }

    val g = generateGraph(graphView, sourceComponents, flowComponents, sinkComponents)
    g.run()
  }

  /**
    * Hook to fire on stop.
    */
  def onStop(): Unit = {
    killSwitch.shutdown()
  }

  /**
    * Generate final runnable graph.
    *
    * @param graphView        graph view of components.
    * @param sourceComponents source components.
    * @param flowComponents   flow components.
    * @param sinkComponents   sink components.
    * @return runnable graph.
    */
  private def generateGraph(
    graphView: MutableGraph[String],
    sourceComponents: Map[String, SourceComponent],
    flowComponents: Map[String, FlowComponent],
    sinkComponents: Map[String, SinkComponent]
  ): RunnableGraph[NotUsed] =
    RunnableGraph.fromGraph(GraphDSL.create() { implicit builder: GraphDSL.Builder[NotUsed] =>
      sinkComponents.foreach { case (componentId: String, component: SinkComponent) =>
        for (in <- component.inputs) {
          if (flowComponents.contains(in)) {
            component.connectTo(flowComponents(in))
          } else {
            component.connectTo(sourceComponents(in))
          }
          graphView.removeEdge(componentId, in)
        }
      }

      flowComponents.foreach { case (componentId: String, component: FlowComponent) =>
        for (in <- component.inputs) {
          if (flowComponents.contains(in)) {
            component.connectTo(flowComponents(in))
          } else {
            component.connectTo(sourceComponents(in))
          }
          graphView.removeEdge(componentId, in)
        }
      }

      ClosedShape
    })

  /**
    * Build a source graph view.
    *
    * @param graph graph view.
    * @return mapping of component id and source component.
    */
  private def buildSourceGraphView(graph: MutableGraph[String]): Map[String, SourceComponent] = {
    val components = Map.newBuilder[String, SourceComponent]

    for (componentId <- asScala(conf.sources.root().entrySet()).map(_.getKey)) {
      val componentRawConf = conf.sources.getConfig(componentId)
      val componentConf = ConfigSource.fromConfig(componentRawConf).loadOrThrow[Pipeline.PartialComponentConfig]

      componentRegistry.getSource(componentConf.`type`) match {
        case Some(f) => components += componentId -> SourceComponent(
          source = f(componentRawConf).via(killSwitch.flow)
        )
        case None => throw PipelineException(s"Components type '${componentConf.`type`}' is unknown")
      }
      graph.addNode(componentId)
    }
    components.result()
  }

  /**
    * Build a flow graph view.
    *
    * @param graph        graph view.
    * @param componentIds set of components ids.
    * @return mapping of component id and flow component.
    */
  private def buildFlowGraphView(graph: MutableGraph[String], componentIds: Set[String]): Map[String, FlowComponent] = {
    val flowComponentIds = asScala(conf.flows.root().entrySet()).map(_.getKey)
    val components = Map.newBuilder[String, FlowComponent]

    for (componentId <- flowComponentIds) {
      val componentRawConf = conf.flows.getConfig(componentId)
      val componentConf = ConfigSource.fromConfig(componentRawConf).loadOrThrow[Pipeline.PartialComponentConfig]

      val unknownInputs = componentConf.inputs.diff(componentIds | flowComponentIds)
      if (unknownInputs.nonEmpty) {
        throw PipelineException(s"Components input '${unknownInputs.mkString(",")}' are unknowns")
      } else {
        componentRegistry.getFlow(componentConf.`type`) match {
          case Some(f) => components += componentId -> FlowComponent(
            inputs = componentConf.inputs,
            flow = f(componentRawConf)
          )
          case None => throw PipelineException(s"Components type '${componentConf.`type`}' is unknown")
        }

        for (input <- componentConf.inputs) {
          graph.putEdge(input, componentId)
        }
      }
    }

    components.result()
  }

  /**
    * Build a sink graph view.
    *
    * @param graph        graph view.
    * @param componentIds set of components ids.
    * @return mapping of component id and sink component.
    */
  private def buildSinkGraphView(graph: MutableGraph[String], componentIds: Set[String]): Map[String, SinkComponent] = {
    val components = Map.newBuilder[String, SinkComponent]

    for (componentId <- asScala(conf.sinks.root().entrySet()).map(_.getKey)) {
      val componentRawConf = conf.sinks.getConfig(componentId)
      val componentConf = ConfigSource.fromConfig(componentRawConf).loadOrThrow[Pipeline.PartialComponentConfig]

      val unknownInputs = componentConf.inputs.diff(componentIds)
      if (unknownInputs.nonEmpty) {
        throw PipelineException(s"Components input '${unknownInputs.mkString(",")}' are unknowns")
      } else {
        componentRegistry.getSink(componentConf.`type`) match {
          case Some(f) => components += componentId -> SinkComponent(componentConf.inputs, sink = f(componentRawConf))
          case None => throw PipelineException(s"Components type '${componentConf.`type`}' is unknown")
        }

        for (input <- componentConf.inputs) {
          graph.putEdge(input, componentId)
        }
      }
    }

    components.result()
  }

}

/**
  * Pipeline exception.
  *
  * @param msg reason of exception.
  */
case class PipelineException(msg: String) extends Exception(msg) with NoStackTrace

/**
  * Pipeline companions.
  */
object Pipeline {

  /**
    * Pipeline configuration.
    *
    * @param sources all defined sources.
    * @param flows   all defined flows.
    * @param sinks   all defined sinks.
    */
  case class Config(
    sources: TConfig,
    flows: TConfig,
    sinks: TConfig
  )

  /**
    * Partial component configuration.
    *
    * @param `type` type of component.
    * @param inputs list of input.
    */
  private[pipeline] case class PartialComponentConfig(
    `type`: String,
    inputs: Set[String] = Set.empty
  )

  /**
    * Source pipeline component.
    *
    * @param source source shape.
    * @param fanOut fan out if needed.
    */
  private[pipeline] case class SourceComponent(
    source: Source[StreamEvent, _],
    fanOut: Option[Broadcast[StreamEvent]] = None
  ) {

    import GraphDSL.Implicits._

    // Handle shapes
    private[pipeline] var sourceShape: SourceShape[StreamEvent] = _
    private[pipeline] var fanOutShape: Option[UniformFanOutShape[StreamEvent, StreamEvent]] = None
    private[pipeline] var fanOutPort: Int = 0

    /**
      * Hook to fire before any connect happen.
      */
    def onConnect()(implicit builder: GraphDSL.Builder[NotUsed]): Unit = {
      if (sourceShape == null) sourceShape = builder.add(source)
      if (fanOutShape.isEmpty && fanOut.isDefined) {
        fanOutShape = Some(builder.add(fanOut.get))
        sourceShape.out ~> fanOutShape.get.in
      }
    }

    /**
      * Recreate source component with fan out.
      *
      * @param fanOutCount number of port for fan out.
      * @return new source component.
      */
    def withFanOut(fanOutCount: Int): SourceComponent = fanOutCount match {
      case x if x > 1 => copy(fanOut = Some(Broadcast[StreamEvent](fanOutCount)))
      case _ => this
    }

  }

  /**
    * Flow pipeline component.
    *
    * @param inputs list of inputs to connect with.
    * @param fanIn  fan in if needed.
    * @param flow   flow shape.
    * @param fanOut fan out if needed.
    */
  private[pipeline] case class FlowComponent(
    inputs: Set[String],
    fanIn: Option[Merge[StreamEvent]] = None,
    flow: Flow[StreamEvent, StreamEvent, _],
    fanOut: Option[Broadcast[StreamEvent]] = None
  ) {

    import GraphDSL.Implicits._

    // Handle shapes
    private[pipeline] var flowShape: FlowShape[StreamEvent, StreamEvent] = _
    private[pipeline] var fanInShape: Option[UniformFanInShape[StreamEvent, StreamEvent]] = None
    private[pipeline] var fanInPort: Int = 0
    private[pipeline] var fanOutShape: Option[UniformFanOutShape[StreamEvent, StreamEvent]] = None
    private[pipeline] var fanOutPort: Int = 0

    /**
      * Hook to fire before any connect happen.
      */
    def onConnect()(implicit builder: GraphDSL.Builder[NotUsed]): Unit = {
      if (flowShape == null) flowShape = builder.add(flow)
      if (fanInShape.isEmpty && fanIn.isDefined) {
        fanInShape = Some(builder.add(fanIn.get))
        fanInShape.get.out ~> flowShape.in
      }
      if (fanOutShape.isEmpty && fanOut.isDefined) {
        fanOutShape = Some(builder.add(fanOut.get))
        flowShape.out ~> fanOutShape.get.in
      }
    }

    /**
      * Recreate flow component with fan in.
      *
      * @param fanInCount number of port for fan in.
      * @return new flow component.
      */
    def withFanIn(fanInCount: Int): FlowComponent = fanInCount match {
      case x if x > 1 => copy(fanIn = Some(Merge[StreamEvent](fanInCount)))
      case _ => this
    }

    /**
      * Recreate flow component with fan out.
      *
      * @param fanOutCount number of port for fan out.
      * @return new flow component.
      */
    def withFanOut(fanOutCount: Int): FlowComponent = fanOutCount match {
      case x if x > 1 => copy(fanOut = Some(Broadcast[StreamEvent](fanOutCount)))
      case _ => this
    }

    /**
      * Connect to a source component.
      *
      * @param component source component to connect with.
      */
    def connectTo(component: SourceComponent)(implicit builder: GraphDSL.Builder[NotUsed]): Unit = {
      // Prepare for connect
      onConnect()
      component.onConnect()

      // Do connection
      (fanInShape, component.fanOutShape) match {
        case (Some(fanIn), Some(fanOut)) =>
          fanOut.out(component.fanOutPort) ~> fanIn.in(fanInPort)
          component.fanOutPort += 1
          fanInPort += 1
        case (Some(fanIn), None) =>
          component.sourceShape.out ~> fanIn.in(fanInPort)
          fanInPort += 1
        case (None, Some(fanOut)) =>
          fanOut.out(component.fanOutPort) ~> flowShape.in
          component.fanOutPort += 1
        case _ =>
          component.sourceShape.out ~> flowShape.in
      }
    }

    /**
      * Connect to a flow component.
      *
      * @param component flow component to connect with.
      */
    def connectTo(component: FlowComponent)(implicit builder: GraphDSL.Builder[NotUsed]): Unit = {
      // Prepare for connect
      onConnect()
      component.onConnect()

      // Do connection
      (fanInShape, component.fanOutShape) match {
        case (Some(fanIn), Some(fanOut)) =>
          fanOut.out(component.fanOutPort) ~> fanIn.in(fanInPort)
          component.fanOutPort += 1
          fanInPort += 1
        case (Some(fanIn), None) =>
          component.flowShape.out ~> fanIn.in(fanInPort)
          fanInPort += 1
        case (None, Some(fanOut)) =>
          fanOut.out(component.fanOutPort) ~> flowShape.in
          component.fanOutPort += 1
        case _ =>
          component.flowShape.out ~> flowShape.in
      }
    }
  }

  /**
    * Sink pipeline component.
    *
    * @param inputs list of inputs to connect with.
    * @param fanIn  fan in if needed.
    * @param sink   sink shape.
    */
  private[pipeline] case class SinkComponent(
    inputs: Set[String],
    fanIn: Option[Merge[StreamEvent]] = None,
    sink: Sink[StreamEvent, _]
  ) {

    import GraphDSL.Implicits._

    // Handle shapes
    private[pipeline] var sinkShape: SinkShape[StreamEvent] = _
    private[pipeline] var fanInShape: Option[UniformFanInShape[StreamEvent, StreamEvent]] = None
    private[pipeline] var fanInPort: Int = 0

    /**
      * Hook to fire before any connect happen.
      */
    def onConnect()(implicit builder: GraphDSL.Builder[NotUsed]): Unit = {
      if (sinkShape == null) sinkShape = builder.add(sink)
      if (fanInShape.isEmpty && fanIn.isDefined) {
        fanInShape = Some(builder.add(fanIn.get))
        fanInShape.get.out ~> sinkShape.in
      }
    }

    /**
      * Recreate sink component with fan in.
      *
      * @param fanInCount number of port for fan in.
      * @return new sink component.
      */
    def withFanIn(fanInCount: Int): SinkComponent = fanInCount match {
      case x if x > 1 => copy(fanIn = Some(Merge[StreamEvent](fanInCount)))
      case _ => this
    }

    /**
      * Connect to a source component.
      *
      * @param component source component to connect with.
      */
    def connectTo(component: SourceComponent)(implicit builder: GraphDSL.Builder[NotUsed]): Unit = {
      // Prepare for connect
      onConnect()
      component.onConnect()

      // Do connection
      (fanInShape, component.fanOutShape) match {
        case (Some(fanIn), Some(fanOut)) =>
          fanOut.out(component.fanOutPort) ~> fanIn.in(fanInPort)
          component.fanOutPort += 1
          fanInPort += 1
        case (Some(fanIn), None) =>
          component.sourceShape.out ~> fanIn.in(fanInPort)
          fanInPort += 1
        case (None, Some(fanOut)) =>
          fanOut.out(component.fanOutPort) ~> sinkShape.in
          component.fanOutPort += 1
        case _ =>
          component.sourceShape.out ~> sinkShape.in
      }
    }

    /**
      * Connect to a flow component.
      *
      * @param component flow component to connect with.
      */
    def connectTo(component: FlowComponent)(implicit builder: GraphDSL.Builder[NotUsed]): Unit = {
      // Prepare for connect
      onConnect()
      component.onConnect()

      // Do connection
      (fanInShape, component.fanOutShape) match {
        case (Some(fanIn), Some(fanOut)) =>
          fanOut.out(component.fanOutPort) ~> fanIn.in(fanInPort)
          component.fanOutPort += 1
          fanInPort += 1
        case (Some(fanIn), None) =>
          component.flowShape.out ~> fanIn.in(fanInPort)
          fanInPort += 1
        case (None, Some(fanOut)) =>
          fanOut.out(component.fanOutPort) ~> sinkShape.in
          component.fanOutPort += 1
        case _ =>
          component.flowShape.out ~> sinkShape.in
      }
    }
  }

}
