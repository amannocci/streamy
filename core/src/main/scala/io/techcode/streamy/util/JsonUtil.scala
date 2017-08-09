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
package io.techcode.streamy.util

import play.api.libs.json._


/**
  * Helper for duration conversion.
  */
object JsonUtil {

  // Various length
  private val TrueLength: Int = 4
  private val FalseLength: Int = 5
  private val NullLength: Int = 4

  /**
    * Size of an element in Json.
    *
    * @param el element to evaluate.
    * @return size of the element.
    */
  def size(el: JsValue): Long = {
    el match {
      case x: JsObject => 1 + x.keys.map(_.length + 2).sum + x.values.map(size).sum + (x.value.size * 2) // {"element": $el}
      case x: JsString => x.value.length + 2 // "element"
      case x: JsArray => x.value.map(size).sum + 1 + x.value.size // ["element", 1, 1.0]
      case x: JsNumber => x.value.toString().length // 2.0
      case JsTrue => TrueLength
      case JsFalse => FalseLength
      case JsNull => NullLength
    }
  }

  /**
    * Convert a json value to string.
    *
    * @param value js value.
    * @return string.
    */
  implicit def asString(value: JsValue): String = value.toString()

  /**
    * Convert a json object to dot notation.
    *
    * @return json object with doc notation.
    */
  def flatten(js: JsValue, prefix: String = ""): JsObject = js.as[JsObject].fields.foldLeft(Json.obj()) {
    case (acc, (k, v: JsObject)) =>
      if (prefix.isEmpty) {
        acc.deepMerge(flatten(v, k))
      } else {
        acc.deepMerge(flatten(v, s"$prefix.$k"))
      }
    case (acc, (k, v)) =>
      if (prefix.isEmpty) {
        acc + (k -> v)
      } else {
        acc + (s"$prefix.$k" -> v)
      }
  }

}
