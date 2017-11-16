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

import io.circe._
import org.scalatest._

import scala.collection.mutable

/**
  * JsonUtil spec.
  */
class JsonUtilSpec extends FlatSpec with Matchers {

  "JsonUtil" must "return correct size for an int" in {
    JsonUtil.size(4) should equal(1)
  }

  it must "return correct size for a float" in {
    JsonUtil.size(2.0) should equal(3)
  }

  it must "return correct size for an object" in {
    JsonUtil.size(Json.obj("test" -> "test")) should equal(15) // {"test":"test"}
  }

  it must "return correct size for a boolean" in {
    JsonUtil.size(Json.True) should equal(4)
    JsonUtil.size(Json.False) should equal(5)
  }

  it must "return correct size for an array" in {
    JsonUtil.size(Json.arr("test", 2, Json.obj("test" -> "test"), 4.0)) should equal(30) // ["test",2,{"test":"test"},4.0]
  }

  it must "return correct size for a null" in {
    JsonUtil.size(Json.Null) should equal(4)
  }

  it must "return correct size for a string" in {
    JsonUtil.size("test") should equal(6) // "test"
  }

  it must "flatten correctly a json object" in {
    JsonUtil.flatten(Json.obj(
      "foobar" -> 0,
      "test" -> Json.obj(
        "test" -> "foobar",
        "foobar" -> Json.obj("test" -> 0)
      )
    )) should equal(Json.obj(
      "foobar" -> 0,
      "test.test" -> "foobar",
      "test.foobar.test" -> 0
    ))
  }

  it must "convert correctly a map to json object" in {
    val map: mutable.Map[String, Any] = new mutable.LinkedHashMap()
    map.put("string", "string")
    map.put("int", 10)
    map.put("long", 10L)
    map.put("float", 1.0F)
    map.put("double", 1.0D)
    JsonUtil.fromMap(map) should equal(Json.obj(
      "string" -> "string",
      "int" -> 10,
      "long" -> 10L,
      "float" -> 1.0F,
      "double" -> 1.0D
    ))
  }

}