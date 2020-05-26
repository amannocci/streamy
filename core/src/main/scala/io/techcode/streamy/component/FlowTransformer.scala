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
  * a convenient way to process an update on [[Json]].
  */
abstract class FlowTransformerLogic(val config: Config) extends (Json => MaybeJson) {

  // Generic error for this transformer logic
  protected[component] val errorMsg: String = Transformer.GenericErrorMsg

  // Compute transform function
  private val function: Json => MaybeJson = {
    // Base of all transformation
    val baseTransform: Json => MaybeJson = { payload: Json =>
      payload.evaluate(config.source).flatMap[Json](transform(_, payload))
    }

    if (config.target.isEmpty || config.source == config.target.get) {
      // Only transform inplace
      payload: Json =>
        baseTransform(payload)
          .flatMap[Json](x => payload.patch(Replace(config.source, x)))
    } else if (config.target.get == Root) {
      // Result of transformation is merged at root
      val mergeTransform: Json => MaybeJson = { payload: Json =>
        baseTransform(payload)
          .flatMap[Json] { v =>
            payload.flatMap[JsObject] { x =>
              v.map[JsObject] { y =>
                x.merge(y)
              }
            }.orElse(payload)
          }
      }

      payload: Json =>
        if (config.onSuccess == SuccessBehaviour.Remove) {
          mergeTransform(payload).flatMap[Json](_.patch(Remove(config.source)))
        } else {
          mergeTransform(payload)
        }
    } else {
      // Result of transformation is added at target
      payload: Json =>
        val copyTransform: Json => MaybeJson = { payload: Json =>
          baseTransform(payload)
            .flatMap[Json](v => payload.patch(Add(config.target.get, v)))
        }

        if (config.onSuccess == SuccessBehaviour.Remove) {
          copyTransform(payload).flatMap[Json](_.patch(Remove(config.source)))
        } else {
          copyTransform(payload)
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
    * @param payload payload involved.
    * @return payload transformed.
    */
  @inline def apply(payload: Json): MaybeJson = function(payload)

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
          newPayload = logic(event.payload)
          newPayload.ifExists[Json] { p =>
            push(out, mutate(event.mutate(p)))
          }
        } catch {
          case ex@StreamException(_, _, _) =>
            // We need to re-add source event
            handleFailure(event, ex)
        }

        // We need to handle empty value
        if (newPayload.isEmpty) {
          handleMissing(event)
        }
      } catch {
        case NonFatal(ex) => decider(ex) match {
          case Supervision.Stop => failStage(ex)
          case _ => pull(in)
        }
      }
    }

    /**
      * Handle missing value in case of transform failure.
      *
      * @param event event involved.
      */
    def handleMissing(event: StreamEvent): Unit = logic.config.onError match {
      case ErrorBehaviour.Discard | ErrorBehaviour.DiscardAndReport => event.discard(logic.errorMsg)
      case ErrorBehaviour.Skip => push(out, event)
    }

    /**
      * Handle failure throw by an exception.
      *
      * @param event event involved.
      * @param ex    exception fired.
      */
    def handleFailure(event: StreamEvent, ex: StreamException): Unit = logic.config.onError match {
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
