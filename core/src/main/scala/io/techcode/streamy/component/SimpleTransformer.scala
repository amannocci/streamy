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

import io.techcode.streamy.component.SimpleTransformer.SuccessBehaviour.SuccessBehaviour
import io.techcode.streamy.component.SimpleTransformer.{Config, SuccessBehaviour}
import io.techcode.streamy.component.Transformer.ErrorBehaviour
import io.techcode.streamy.component.Transformer.ErrorBehaviour.ErrorBehaviour
import play.api.libs.json._

/**
  * Simple transformer abstract implementation that provide
  * a convenient way to process an update on json object.
  */
abstract class SimpleTransformer(config: Config) extends Transformer[JsObject, JsObject](config) {

  // Attempt to convert a string into a json structure inplace
  private lazy val sourceInplace: Reads[JsObject] = parseInplace(config.source)

  // Attempt to convert a string into a json structrue from a source path to a target path
  private lazy val sourceToTarget: Reads[JsObject] = parseInplace(config.source)
    .andThen(config.target.get.json.copyFrom(config.source.json.pick))

  // Choose right transform function
  private val function: ((JsObject) => JsObject) = {
    if (config.target.isEmpty || config.source == config.target.get) {
      // Parse inplace
      (pkt: JsObject) => {
        val result = pkt.transform(sourceInplace).asOpt
        if (result.isDefined) {
          result.get
        } else {
          // An error will be raised if we error handler return everything but a json object
          onError(state = pkt).as[JsObject]
        }
      }
    } else {
      // Parse inplace and then copy to target
      (pkt: JsObject) => {
        // Transform target and extract
        val branch = pkt.transform(sourceToTarget).asOpt
        if (branch.isDefined) {
          // Remove source if needed
          {
            if (config.onSuccess == SuccessBehaviour.Remove) {
              pkt.transform(config.source.json.prune).get
            } else {
              pkt
            }
          }.deepMerge(branch.get)
        } else {
          // An error will be raised if we error handler return everything but a json object
          onError(state = pkt).as[JsObject]
        }
      }
    }
  }

  /**
    * Parse a string field into a json structure.
    *
    * @param path path to string field.
    * @return json structure.
    */
  def parseInplace(path: JsPath): Reads[JsObject]

  /**
    * Apply transform component on packet.
    *
    * @param pkt packet involved.
    * @return packet transformed.
    */
  def apply(pkt: JsObject): JsObject = function(pkt)

}

/**
  * Simple transformer companion.
  */
object SimpleTransformer {

  // Component configuration
  class Config(
    val source: JsPath,
    val target: Option[JsPath] = None,
    val onSuccess: SuccessBehaviour = SuccessBehaviour.Skip,
    override val onError: ErrorBehaviour = ErrorBehaviour.Skip
  ) extends Transformer.Config(onError)

  // Behaviour on error
  object SuccessBehaviour extends Enumeration {
    type SuccessBehaviour = Value
    val Remove, Skip = Value
  }

}
