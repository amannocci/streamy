/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017
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

import gnieh.diffson.Pointer
import gnieh.diffson.Pointer._
import gnieh.diffson.circe._
import io.circe._
import io.techcode.streamy.component.SimpleTransformer.SuccessBehaviour.SuccessBehaviour
import io.techcode.streamy.component.SimpleTransformer.{Config, SuccessBehaviour}
import io.techcode.streamy.component.Transformer.ErrorBehaviour
import io.techcode.streamy.component.Transformer.ErrorBehaviour.ErrorBehaviour

import scala.language.postfixOps

/**
  * Simple transformer abstract implementation that provide
  * a convenient way to process an update on json object.Test
  */
abstract class SimpleTransformer(config: Config) extends Transformer[Json, Json](config) {

  // Choose right transform function
  private val function: (Json => Json) = {
    if (config.target.isEmpty || config.source == config.target.get) {
      // Transform inplace and report error if needed
      (pkt: Json) => {
        val result = transform(pointer.evaluate(pkt, config.source))
        result match {
          case Some(v) => JsonPatch(Replace(config.source, v))(pkt)
          case None => onError(Transformer.GenericErrorMsg, pkt)
        }
      }
    } else {
      // Transform inplace and then copy to target
      (pkt: Json) => {
        val result = transform(pointer.evaluate(pkt, config.source))
        result match {
          case Some(v) =>
            val operated: Json = {
              if (config.target.get == root) {
                pkt.deepMerge(v)
              } else {
                pkt
              }
            }

            // Combine operations if needed
            var operations = List[Operation]()
            if (config.target.get != root) {
              operations = operations :+ Add(config.target.get, v)
            }
            if (config.onSuccess == SuccessBehaviour.Remove) {
              operations = operations :+ Remove(config.source)
            }

            // Perform operations if needed
            if (operations.isEmpty) {
              operated
            } else {
              JsonPatch(operations)(operated)
            }
          case None => onError(Transformer.GenericErrorMsg, pkt)
        }
      }
    }
  }


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
object SimpleTransformer {

  // Component configuration
  class Config(
    val source: Pointer,
    val target: Option[Pointer] = None,
    val onSuccess: SuccessBehaviour = SuccessBehaviour.Skip,
    override val onError: ErrorBehaviour = ErrorBehaviour.Skip
  ) extends Transformer.Config(onError)

  // Behaviour on error
  object SuccessBehaviour extends Enumeration {
    type SuccessBehaviour = Value
    val Remove, Skip = Value
  }

}
