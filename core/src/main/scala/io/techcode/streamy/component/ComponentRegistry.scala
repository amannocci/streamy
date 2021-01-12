/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017-2020
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
package io.techcode.streamy.component

import akka.NotUsed
import akka.actor.{ActorSystem, ExtendedActorSystem, Extension, ExtensionId}
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.typesafe.config.Config
import io.techcode.streamy.event.StreamEvent

import java.util.concurrent.ConcurrentHashMap

/**
  * Component registry system.
  *
  * @param system actor system.
  */
class ComponentRegistry(system: ActorSystem) extends Extension {

  // All registries
  private val sourceRegistry = new ConcurrentHashMap[String, Config => Source[StreamEvent, _]]

  private val flowRegistry = new ConcurrentHashMap[String, Config => Flow[StreamEvent, StreamEvent, _]]

  private val sinkRegistry = new ConcurrentHashMap[String, Config => Sink[StreamEvent, _]]

  /**
    * Register a source factory.
    *
    * @param ref           id reference of the source factory.
    * @param sourceFactory source factory.
    */
  def registerSource(ref: String, sourceFactory: Config => Source[StreamEvent, _]): Unit = {
    sourceRegistry.putIfAbsent(ref, sourceFactory)
  }

  /**
    * Register a flow factory.
    *
    * @param ref         id reference of the flow factory.
    * @param flowFactory flow factory.
    */
  def registerFlow(ref: String, flowFactory: Config => Flow[StreamEvent, StreamEvent, _]): Unit = {
    flowRegistry.putIfAbsent(ref, flowFactory)
  }

  /**
    * Register a sink factory.
    *
    * @param ref         id reference of the sink factory.
    * @param sinkFactory sink factory.
    */
  def registerSink(ref: String, sinkFactory: Config => Sink[StreamEvent, _]): Unit = {
    sinkRegistry.putIfAbsent(ref, sinkFactory)
  }

  /**
    * Retrieve a source by ref.
    *
    * @param ref ref of the source.
    * @return optional source.
    */
  def source(ref: String): Option[Config => Source[StreamEvent, _]] =
    Option(sourceRegistry.get(ref))

  /**
    * Retrieve a flow by ref.
    *
    * @param ref ref of the flow.
    * @return optional flow.
    */
  def flow(ref: String): Option[Config => Flow[StreamEvent, StreamEvent, _]] =
    Option(flowRegistry.get(ref))

  /**
    * Retrieve a sink by ref.
    *
    * @param ref ref of the sink.
    * @return optional sink.
    */
  def sink(ref: String): Option[Config => Sink[StreamEvent, _]] =
    Option(sinkRegistry.get(ref))

}

object ComponentRegistry extends ExtensionId[ComponentRegistry] {

  def createExtension(system: ExtendedActorSystem): ComponentRegistry = new ComponentRegistry(system)

}
