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

import gnieh.diffson.Pointer
import gnieh.diffson.circe._
import io.circe._
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

  private val errHandler: PartialFunction[(Json, String, Pointer), Json] = {
    case _ => Json.Null
  }

  private implicit val existPointer: JsonPointer = {
    val p = new JsonPointer()
    p.handler_=(errHandler)
    p
  }

  val root: Pointer = Pointer.root

  @inline def exist(pkt: Json, path: Pointer): Boolean = existPointer.evaluate(pkt, path) != Json.Null

  @inline def evaluate(pkt: Json, path: Pointer): Json = pointer.evaluate(pkt, path)

  def patch(pkt: Json, ops: Operation*): Json = JsonPatch(ops.toList)(pkt)

  /**
    * Size of an element in Json.
    *
    * @param el element to evaluate.
    * @return size of the element.
    */
  def size(el: Json): Long = {
    if (el.isObject) {
      el.asObject.map(x => 1 + x.fields.map(_.length + 2).sum + x.values.map(size).sum + (x.values.size * 2)).get // {"element": $el}
    } else if (el.isArray) {
      el.asArray.map(x => x.map(size).sum + 1 + x.size).get // ["element", 1, 1.0]
    } else if (el.isBoolean) {
      el.asBoolean.map(x => if (x) TrueLength else FalseLength).get
    } else if (el.isNull) {
      NullLength
    } else if (el.isNumber) {
      el.asNumber.map(x => x.toString.length).get // 2.0
    } else {
      el.asString.map(x => x.length + 2).get // "element"
    }
  }

  /**
    * Convert a map to json.
    *
    * @param map map with any values.
    * @return json object.
    */
  def fromMap(map: mutable.Map[String, Any]): Json = Json.fromFields(map.mapValues[Json] {
    case value: Int => Json.fromInt(value)
    case value: Long => Json.fromLong(value)
    case value: Double => Json.fromDoubleOrNull(value)
    case value: Float => Json.fromFloatOrNull(value)
    case value => Json.fromString(value.toString)
  })

  /**
    * Convert a json object to dot notation.
    *
    * @return json object with doc notation.
    */
  def flatten(js: Json, prefix: String = StringUtils.EMPTY): Json = js.asObject.get.toVector.foldLeft(Json.obj()) {
    case (acc, (k, v: Json)) =>
      if (v.isObject) {
        if (prefix.isEmpty) {
          acc.deepMerge(flatten(v, k))
        } else {
          acc.deepMerge(flatten(v, s"$prefix.$k"))
        }
      } else {
        if (prefix.isEmpty) {
          Json.fromJsonObject(acc.asObject.get.add(k, v))
        } else {
          Json.fromJsonObject(acc.asObject.get.add(s"$prefix.$k", v))
        }
      }
  }

}
