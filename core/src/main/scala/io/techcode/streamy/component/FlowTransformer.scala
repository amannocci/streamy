/*
 * The MIT License (MIT)
 * <p>
 * Copyright (C) 2017-2019
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

import com.google.common.base.Throwables
import io.techcode.streamy.component.FlowTransformer.SuccessBehaviour.SuccessBehaviour
import io.techcode.streamy.component.FlowTransformer.{Config, SuccessBehaviour}
import io.techcode.streamy.component.Transformer.ErrorBehaviour
import io.techcode.streamy.component.Transformer.ErrorBehaviour.ErrorBehaviour
import io.techcode.streamy.util.StreamException
import io.techcode.streamy.util.json._

import scala.language.postfixOps

/**
  * Flow transformer abstract implementation that provide
  * a convenient way to process an update on [[Json]].
  */
abstract class FlowTransformer(config: Config) extends (Json => Json) {

  // Choose right transform function
  private val function: Json => Json = {
    if (config.target.isEmpty || config.source == config.target.get) {
      // Transform inplace and report error if needed
      pkt: Json =>
        pkt.evaluate(config.source)
          .flatMap[Json](transform(_, pkt))
          .flatMap[Json](x => pkt.patch(Replace(config.source, x)))
          .getOrElse[Json](error(Transformer.GenericErrorMsg, pkt))
    } else {
      // Transform inplace and then copy to target
      pkt: Json =>
        pkt.evaluate(config.source)
          .flatMap[Json](transform(_, pkt))
          .flatMap[Json] { v =>
          val operated: MaybeJson = {
            if (config.target.get == Root) {
              pkt.flatMap[JsObject] { x =>
                v.map[JsObject] { y =>
                  x.merge(y)
                }
              }
            } else {
              pkt
            }
          }

          // Combine operations if needed
          var operations = List[JsonOperation]()
          if (config.target.get != Root) {
            operations = operations :+ Add(config.target.get, v)
          }
          if (config.onSuccess == SuccessBehaviour.Remove) {
            operations = operations :+ Remove(config.source)
          }

          // Perform operations if needed
          if (operations.isEmpty) {
            operated
          } else {
            operated.flatMap[Json](_.patch(operations))
          }
        }.getOrElse[Json](error(Transformer.GenericErrorMsg, pkt))
    }
  }

  /**
    * Handle parsing error by discarding or wrapping or skipping.
    *
    * @param cause exception raised.
    * @param state value of field when error is raised.
    * @return result json value.
    */
  def error(cause: Throwable, state: Json): Json = error(cause, state, Map.empty[String, String])

  /**
    * Handle parsing error by discarding or wrapping or skipping.
    *
    * @param cause exception raised.
    * @param state value of field when error is raised.
    * @param meta  extra meta data.
    * @return result json value.
    */
  def error(cause: Throwable, state: Json, meta: Map[String, String]): Json = config.onError match {
    case ErrorBehaviour.Discard =>
      throw new StreamException(cause.getMessage, state, meta)
    case ErrorBehaviour.DiscardAndReport =>
      throw new StreamException(cause.getMessage, state, meta + ("stacktrace" -> Throwables.getStackTraceAsString(cause)))
    case ErrorBehaviour.Skip => state
  }

  /**
    * Handle parsing error by discarding or wrapping or skipping.
    *
    * @param msg   message exception raised.
    * @param state value of field when error is raised.
    * @return result json value.
    */
  def error(msg: String, state: Json): Json = error(msg, state, Map.empty[String, String])

  /**
    * Handle parsing error by discarding or wrapping or skipping.
    *
    * @param msg   message exception raised.
    * @param state value of field when error is raised.
    * @param meta  extra meta data.
    * @return result json value.
    */
  def error(msg: String, state: Json, meta: Map[String, String]): Json = config.onError match {
    case ErrorBehaviour.Discard | ErrorBehaviour.DiscardAndReport =>
      throw new StreamException(msg, state, meta)
    case ErrorBehaviour.Skip => state
  }

  /**
    * Transform only value of given packet.
    *
    * @param value value to transform.
    * @param pkt   original packet.
    * @return json structure.
    */
  @inline def transform(value: Json, pkt: Json): MaybeJson = transform(value)

  /**
    * Transform only value of given packet.
    *
    * @param value value to transform.
    * @return json structure.
    */
  def transform(value: Json): MaybeJson = JsUndefined

  /**
    * Apply transform component on packet.
    *
    * @param pkt packet involved.
    * @return packet transformed.
    */
  @inline def apply(pkt: Json): Json = function(pkt)

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
