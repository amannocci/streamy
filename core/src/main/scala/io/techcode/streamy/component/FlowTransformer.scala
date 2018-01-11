/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017-2018
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

import io.techcode.streamy.component.FlowTransformer.SuccessBehaviour.SuccessBehaviour
import io.techcode.streamy.component.FlowTransformer.{Config, SuccessBehaviour}
import io.techcode.streamy.component.Transformer.ErrorBehaviour
import io.techcode.streamy.component.Transformer.ErrorBehaviour.ErrorBehaviour
import io.techcode.streamy.util.json._

import scala.language.postfixOps

/**
  * Flow transformer abstract implementation that provide
  * a convenient way to process an update on [[Json]].
  */
abstract class FlowTransformer(config: Config) extends Transformer[Json, Json](config) {

  // Choose right transform function
  private val function: (Json => Json) = {
    if (config.target.isEmpty || config.source == config.target.get) {
      // Transform inplace and report error if needed
      (pkt: Json) =>
        pkt.evaluate(config.source)
          .flatMap(transform(_, pkt))
          .flatMap(x => pkt.patch(Replace(config.source, x)))
          .getOrElse(onError(Transformer.GenericErrorMsg, pkt))
    } else {
      // Transform inplace and then copy to target
      (pkt: Json) =>
        pkt.evaluate(config.source)
          .flatMap(transform(_, pkt))
          .flatMap { v =>
            val operated: Option[Json] = {
              if (config.target.get == Root) {
                pkt.deepMerge(v)
              } else {
                Some(pkt)
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
              operated.flatMap(_.patch(operations))
            }
          }.getOrElse(onError(Transformer.GenericErrorMsg, pkt))
    }
  }

  /**
    * Transform only value of given packet.
    *
    * @param value value to transform.
    * @param pkt   original packet.
    * @return json structure.
    */
  @inline def transform(value: Json, pkt: Json): Option[Json] =
    transform(value)

  /**
    * Transform only value of given packet.
    *
    * @param value value to transform.
    * @return json structure.
    */
  def transform(value: Json): Option[Json]

  /**
    * Apply transform component on packet.
    *
    * @param pkt packet involved.
    * @return packet transformed.
    */
  @inline def apply(pkt: Json): Json = function(pkt)

}

/**
  * Simple transformer companion.
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
