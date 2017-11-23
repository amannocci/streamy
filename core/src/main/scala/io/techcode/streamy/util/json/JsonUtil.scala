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
package io.techcode.streamy.util.json

import akka.util.ByteString
import org.apache.commons.lang3.StringUtils

import scala.collection.mutable


/**
  * Some helpers to work with Json values.
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
  def size(el: Json): Long = {
    if (el.isObject) {
      el.asObject.map(x => 1 + x.underlying.keys.map(_.length + 2).sum + x.values.map(size).sum + (x.values.size * 2)).get // {"element": $el}
    } else if (el.isArray) {
      el.asArray.map(x => x.underlying.map(size).sum + 1 + x.underlying.size).get // ["element", 1, 1.0]
    } else if (el.isBoolean) {
      el.asBoolean.map(x => if (x) TrueLength else FalseLength).get
    } else if (el.isNull) {
      NullLength
    } else if (el.isInt) {
      el.asInt.get.toString.length
    } else if (el.isLong) {
      el.asLong.get.toString.length
    } else if (el.isFloat) {
      el.asFloat.get.toString.length
    } else if (el.isDouble) {
      el.asDouble.get.toString.length
    } else if (el.isNumber) {
      el.asNumber.map(_.toString.length).get // 2.0
    } else {
      el.asString.get.length + 2 // "element"
    }
  }

  /**
    * Convert a json value to dot notation.
    *
    * @param js json object to flatten.
    * @return json value with dot notation.
    */
  def flatten(js: Json): Option[Json] = js.asObject.map(flatten(_))

  /**
    * Convert a json object to dot notation.
    *
    * @param js     json object to flatten.
    * @param prefix accumultator.
    * @return json object with dot notation.
    */
  def flatten(js: JsObject, prefix: String = StringUtils.EMPTY): JsObject = js.fields.foldLeft(Json.obj()) {
    case (acc, (k, v: Json)) =>
      if (v.isObject) {
        // Deep merge will always successed
        if (prefix.isEmpty) {
          acc.deepMerge(flatten(v.asObject.get, k)).get
        } else {
          acc.deepMerge(flatten(v.asObject.get, s"$prefix.$k")).get
        }
      } else {
        if (prefix.isEmpty) {
          acc.asObject.get.put(k, v)
        } else {
          acc.asObject.get.put(s"$prefix.$k", v)
        }
      }
  }

  /**
    * Convert a map to json.
    *
    * @param map map with any values.
    * @return json object.
    */
  def fromJsonMap(map: mutable.Map[String, Json]): JsObject = {
    val json = Json.obj()
    val builder = json.underlying
    map.foreach {
      case (key: String, value: Json) => builder.put(key, value)
    }
    json
  }

  /**
    * Convert a map to json.
    *
    * @param map map with any values.
    * @return json object.
    */
  def fromMap(map: mutable.Map[String, Any]): JsObject = {
    val json = Json.obj()
    val builder = json.underlying
    map.foreach {
      case (key: String, value: Int) => builder.put(key, intToJson(value))
      case (key: String, value: Long) => builder.put(key, longToJson(value))
      case (key: String, value: Double) => builder.put(key, doubleToJson(value))
      case (key: String, value: Float) => builder.put(key, floatToJson(value))
      case (key: String, value: BigDecimal) => builder.put(key, bigDecimalToJson(value))
      case (key: String, value: Boolean) => builder.put(key, booleanToJson(value))
      case (key: String, value: ByteString) => builder.put(key, byteStringToJson(value))
      case (key: String, value: Any) => builder.put(key, stringToJson(value.toString))
    }
    json
  }

}
