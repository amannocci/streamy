/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017-2019
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

import akka.stream.ActorAttributes.SupervisionStrategy
import akka.stream.Supervision.Decider
import akka.stream._
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import io.techcode.streamy.component.FlowTransformer.SuccessBehaviour.SuccessBehaviour
import io.techcode.streamy.component.FlowTransformer.{Config, SuccessBehaviour}
import io.techcode.streamy.component.Transformer.ErrorBehaviour
import io.techcode.streamy.component.Transformer.ErrorBehaviour.ErrorBehaviour
import io.techcode.streamy.event.StreamEvent
import io.techcode.streamy.util.StreamException
import io.techcode.streamy.util.json._

import scala.language.postfixOps
import scala.util.control.NonFatal

/**
  * Flow transformer logic abstract implementation that provide
  * a convenient way to process an update on [[StreamEvent]].
  */
abstract class FlowTransformerLogic(val config: Config) extends (StreamEvent => MaybeJson) {

  // Generic error for this transformer logic
  protected[component] val errorMsg: String = Transformer.GenericErrorMsg

  // Compute transform function
  private val function: StreamEvent => MaybeJson = {
    // Base of all transformation
    val baseTransform: StreamEvent => MaybeJson = { evt: StreamEvent =>
      evt.payload.evaluate(config.source).flatMap[Json](transform(_, evt))
    }

    if (config.target.isEmpty || config.source == config.target.get) {
      // Only transform inplace
      evt: StreamEvent =>
        baseTransform(evt)
          .flatMap[Json](x => evt.payload.patch(Replace(config.source, x)))
    } else if (config.target.get == Root) {
      // Result of transformation is merged at root
      val mergeTransform: StreamEvent => MaybeJson = { evt: StreamEvent =>
        baseTransform(evt)
          .flatMap[Json] { v =>
            evt.payload.flatMap[JsObject] { x =>
              v.map[JsObject] { y =>
                x.merge(y)
              }
            }.orElse(evt.payload)
          }
      }

      evt: StreamEvent =>
        if (config.onSuccess == SuccessBehaviour.Remove) {
          mergeTransform(evt).flatMap[Json](_.patch(Remove(config.source)))
        } else {
          mergeTransform(evt)
        }
    } else {
      // Result of transformation is added at target
      evt: StreamEvent =>
        val copyTransform: StreamEvent => MaybeJson = { evt: StreamEvent =>
          baseTransform(evt)
            .flatMap[Json](v => evt.payload.patch(Add(config.target.get, v)))
        }

        if (config.onSuccess == SuccessBehaviour.Remove) {
          copyTransform(evt).flatMap[Json](_.patch(Remove(config.source)))
        } else {
          copyTransform(evt)
        }
    }
  }

  /**
    * Handle transform error by discarding or skipping.
    *
    * @param cause exception raised.
    * @return JsUndefined or throw a stream exception.
    */
  def error(cause: Throwable): MaybeJson = config.onError match {
    case ErrorBehaviour.Discard | ErrorBehaviour.Skip => error(cause.getMessage)
    case ErrorBehaviour.DiscardAndReport => throw new StreamException(StreamEvent.Empty, cause)
  }

  /**
    * Handle transform error by discarding or skipping.
    *
    * @param msg message exception raised.
    * @return JsUndefined or throw a stream exception.
    */
  def error(msg: String): MaybeJson = throw new StreamException(StreamEvent.Empty, msg)

  /**
    * Transform only value of given payload.
    *
    * @param value   value to transform.
    * @param evt original event.
    * @return json structure.
    */
  @inline def transform(value: Json, evt: StreamEvent): MaybeJson = transform(value, evt.payload)

  /**
    * Transform only value of given payload.
    *
    * @param value   value to transform.
    * @param payload original payload.
    * @return json structure.
    */
  @inline def transform(value: Json, payload: Json): MaybeJson = transform(value)

  /**
    * Transform only value of given payload.
    *
    * @param value value to transform.
    * @return json structure.
    */
  def transform(value: Json): MaybeJson = JsUndefined

  /**
    * Apply transform component on event payload.
    *
    * @param evt stream event involved.
    * @return payload transformed.
    */
  @inline def apply(evt: StreamEvent): MaybeJson = function(evt)

}

/**
  * Flow transformer abstract implementation that provide
  * a convenient way to process an update on [[StreamEvent]].
  */
abstract class FlowTransformer extends GraphStage[FlowShape[StreamEvent, StreamEvent]] {

  // Inlet
  val in: Inlet[StreamEvent] = Inlet[StreamEvent]("flowTransformer.in")

  // Outlet
  val out: Outlet[StreamEvent] = Outlet[StreamEvent]("flowTransformer.out")

  override val shape: FlowShape[StreamEvent, StreamEvent] = FlowShape(in, out)

  /**
    * Factory to create a flow transformer logic.
    *
    * @return a flow transformer logic.
    */
  def factory(): FlowTransformerLogic

  /**
    * Mutate current event after logic.
    *
    * @param evt current event after process.
    * @return new event.
    */
  def mutate(evt: StreamEvent): StreamEvent = evt

  override def createLogic(attrs: Attributes): GraphStageLogic = new FlowTransformerInternal(attrs)

  // Internal flow transformer
  private class FlowTransformerInternal(
    attrs: Attributes
  ) extends GraphStageLogic(shape) with InHandler with OutHandler {

    // Set handler
    setHandlers(in, out, this)

    // Logic created from factory
    private val logic = factory()

    private def decider: Decider = attrs.mandatoryAttribute[SupervisionStrategy].decider

    override def onPull(): Unit = pull(in)

    override def onPush(): Unit = {
      val event = grab(in)
      try {
        var newPayload: MaybeJson = JsUndefined
        try {
          newPayload = logic(event)
          if (newPayload.isDefined) {
            push(out, mutate(event.mutate(newPayload.get[Json])))
          } else {
            event.discard(logic.errorMsg)
          }
        } catch {
          case ex@StreamException(_, _, _) =>
            // We need to re-add source event
            handleFailure(event, ex)
        }
      } catch {
        case NonFatal(ex) => decider(ex) match {
          case Supervision.Stop => failStage(ex)
          case _ => pull(in)
        }
      }
    }

    /**
      * Handle failure throw by an exception.
      *
      * @param event event involved.
      * @param ex    exception fired.
      */
    private def handleFailure(event: StreamEvent, ex: StreamException): Unit = logic.config.onError match {
      case ErrorBehaviour.Discard | ErrorBehaviour.DiscardAndReport =>
        if (Option(ex.cause).isDefined) {
          event.discard(ex.cause)
        } else {
          event.discard(ex.msg)
        }
      case ErrorBehaviour.Skip => push(out, event)
    }

  }

}

/**
  * Flow transformer companion.
  */
object FlowTransformer {

  // Component configuration
  class Config(
    val source: JsonPointer,
    val target: Option[JsonPointer] = None,
    val onSuccess: SuccessBehaviour = SuccessBehaviour.Skip,
    override val onError: ErrorBehaviour = ErrorBehaviour.Skip
  ) extends Transformer.Config(onError)

  // Behaviour on error
  object SuccessBehaviour extends Enumeration {
    type SuccessBehaviour = Value
    val Remove, Skip = Value
  }

}
