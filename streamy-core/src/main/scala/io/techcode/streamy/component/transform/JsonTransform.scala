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

import play.api.libs.json._

import scala.util.{Failure, Success, Try}

/**
  * Json transform implementation.
  */
class JsonTransform(field: JsPath, wrapped: Boolean = true) extends Transform[JsObject, JsObject] {

  // Transformation function
  val function: Reads[JsObject] = field.json.update(__.read[String].map(msg => {
    if (msg.startsWith("{") && msg.endsWith("}")) {
      Try(Json.parse(msg)) match {
        case Success(succ) =>
          succ
        case Failure(_) =>
          if (wrapped) {
            Json.obj("message" -> msg)
          } else {
            JsString(msg)
          }
      }
    } else {
      if (wrapped) {
        Json.obj("message" -> msg)
      } else {
        JsString(msg)
      }
    }
  }))

  override def apply(pkt: JsObject): JsObject = {
    pkt.transform(function).get
  }

}