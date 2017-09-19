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

import io.techcode.streamy.component.SimpleTransformer
import io.techcode.streamy.component.SimpleTransformer.SuccessBehaviour
import io.techcode.streamy.component.SimpleTransformer.SuccessBehaviour.SuccessBehaviour
import io.techcode.streamy.component.Transformer.ErrorBehaviour
import io.techcode.streamy.component.Transformer.ErrorBehaviour.ErrorBehaviour
import io.techcode.streamy.component.transform.JsonTransformer.Config
import play.api.libs.json.Reads._
import play.api.libs.json._

import scala.util.{Failure, Success, Try}

/**
  * Json transform implementation.
  */
class JsonTransformer(config: Config) extends SimpleTransformer(config) {

  override def parseInplace(path: JsPath): Reads[JsObject] = path.json.update(
    of[JsString].map { field =>
      // Try to avoid parsing of wrong json
      if (field.value.startsWith("{") && field.value.endsWith("}")) {
        // Try to parse
        Try(Json.parse(field.value)) match {
          case Success(succ) => succ
          case Failure(ex) => onError(state = field, ex = Some(ex))
        }
      } else {
        onError(state = field)
      }
    }
  )

}

/**
  * Json transform companion.
  */
object JsonTransformer {

  // Component configuration
  case class Config(
    override val source: JsPath,
    override val target: Option[JsPath] = None,
    override val onSuccess: SuccessBehaviour = SuccessBehaviour.Skip,
    override val onError: ErrorBehaviour = ErrorBehaviour.Skip
  ) extends SimpleTransformer.Config(source, target, onSuccess, onError)

}
