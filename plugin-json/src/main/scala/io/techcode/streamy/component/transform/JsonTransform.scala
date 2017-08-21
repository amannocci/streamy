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
package io.techcode.streamy.component.transform

import io.techcode.streamy.component.Transform
import io.techcode.streamy.component.transform.JsonTransform.Behaviour.Behaviour
import io.techcode.streamy.component.transform.JsonTransform.{Behaviour, Config}
import io.techcode.streamy.stream.StreamException
import play.api.libs.json.Reads._
import play.api.libs.json._

import scala.util.{Failure, Success, Try}

/**
  * Json transform implementation.
  */
class JsonTransform(config: Config) extends Transform[JsObject, JsObject] {

  // Attempt to convert a string into a json structure inplace
  private lazy val sourceInplace: Reads[JsObject] = parseInplace(config.source)

  // Attempt to convert a string into a json structrue from a source path to a target path
  private lazy val sourceToTarget: Reads[JsObject] = parseInplace(config.source)
    .andThen(config.target.get.json.copyFrom(config.source.json.pick))

  // Choose right transform function
  private val function: ((JsObject) => JsObject) = {
    if (config.target.isEmpty || config.source == config.target.get) {
      // Parse inplace
      (pkt: JsObject) => pkt.transform(sourceInplace).get
    } else {
      // Parse inplace and then copy to target
      (pkt: JsObject) => {
        // Transform target and extract
        val branch = pkt.transform(sourceToTarget).get

        // Remove source if needed
        {
          if (config.removeSource) {
            pkt.transform(config.source.json.prune).get
          } else {
            pkt
          }
        }.deepMerge(branch)
      }
    }
  }

  /**
    * Parse a string field into a json structure.
    *
    * @param path path to string field.
    * @return json structure.
    */
  private def parseInplace(path: JsPath): Reads[JsObject] = path.json.update(
    __.read[String].map(msg =>
      // Try to avoid parsing of wrong json
      if (msg.startsWith("{") && msg.endsWith("}")) {
        // Try to parse
        Try(Json.parse(msg)) match {
          case Success(succ) => succ
          case Failure(_) => onError(msg)
        }
      } else {
        onError(msg)
      }
    )
  )

  /**
    * Handle parsing error by discarding or wrapping or skipping.
    *
    * @param msg value of field when error is raised.
    * @return result json value.
    */
  private def onError(msg: String): JsValue = {
    config.onError match {
      case Behaviour.Discard =>
        throw new StreamException("Source field can't be parse as Json")
      case Behaviour.Skip =>
        JsString(msg)
    }
  }

  override def apply(pkt: JsObject): JsObject = function(pkt)

}

/**
  * Json transform companion.
  */
object JsonTransform {

  // Component configuration
  case class Config(
    source: JsPath,
    target: Option[JsPath] = None,
    removeSource: Boolean = false,
    onError: Behaviour = Behaviour.Skip
  )

  // Behaviour on error
  object Behaviour extends Enumeration {
    type Behaviour = Value
    val Discard, Skip = Value
  }

}
