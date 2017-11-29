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
import org.scalatest._

import scala.collection.mutable

/**
  * JsonUtil spec.
  */
class JsonUtilSpec extends WordSpecLike with Matchers {

  "JsonUtil" should {
    "return correct size for an int" in {
      JsonUtil.size(4) should equal(1)
    }

    "return correct size for a long" in {
      JsonUtil.size(4L) should equal(1)
    }

    "return correct size for a float" in {
      JsonUtil.size(2.0F) should equal(3)
    }

    "return correct size for a double" in {
      JsonUtil.size(2.0D) should equal(3)
    }

    "return correct size for a number" in {
      JsonUtil.size(BigDecimal("2e128")) should equal(6)
    }

    "return correct size for an object" in {
      JsonUtil.size(Json.obj("test" -> "test")) should equal(15) // {"test":"test"}
    }

    "return correct size for a boolean" in {
      JsonUtil.size(JsTrue) should equal(4)
      JsonUtil.size(JsFalse) should equal(5)
    }

    "return correct size for an array" in {
      JsonUtil.size(Json.arr("test", 2, Json.obj("test" -> "test"), 4.0)) should equal(30) // ["test",2,{"test":"test"},4.0]
    }

    "return correct size for a null" in {
      JsonUtil.size(JsNull) should equal(4)
    }

    "return correct size for a string" in {
      JsonUtil.size("test") should equal(6) // "test"
    }

    "flatten correctly a json object" in {
      JsonUtil.flatten(Json.obj(
        "foobar" -> 0,
        "test" -> Json.obj(
          "test" -> "foobar",
          "foobar" -> Json.obj("test" -> 0)
        )
      )) should equal(Some(Json.obj(
        "foobar" -> 0,
        "test.test" -> "foobar",
        "test.foobar.test" -> 0
      )))
    }

    "convert correctly a map to json object" in {
      val map: mutable.Map[String, Any] = new mutable.LinkedHashMap()
      map.put("string", "string")
      map.put("boolean", true)
      map.put("int", 10)
      map.put("long", 10L)
      map.put("float", 1.0F)
      map.put("double", 1.0D)
      map.put("bigDecimal", BigDecimal(1))
      map.put("byteString", ByteString("test"))
      JsonUtil.fromRawMap(map) should equal(Json.obj(
        "string" -> "string",
        "boolean" -> true,
        "int" -> 10,
        "long" -> 10L,
        "float" -> 1.0F,
        "double" -> 1.0D,
        "bigDecimal" -> BigDecimal(1),
        "byteString" -> ByteString("test")
      ))
    }

    "convert correctly a raw map to json object" in {
      val map: mutable.Map[String, Any] = new mutable.LinkedHashMap()
      map.put("string", "string")
      map.put("boolean", true)
      map.put("int", 10)
      map.put("long", 10L)
      map.put("float", 1.0F)
      map.put("double", 1.0D)
      map.put("bigDecimal", BigDecimal(1))
      map.put("byteString", ByteString("test"))
      JsonUtil.fromRawMap(map) should equal(Json.obj(
        "string" -> "string",
        "boolean" -> true,
        "int" -> 10,
        "long" -> 10L,
        "float" -> 1.0F,
        "double" -> 1.0D,
        "bigDecimal" -> BigDecimal(1),
        "byteString" -> ByteString("test")
      ))
    }

    "convert correctly a json map to json object" in {
      val map: mutable.Map[String, Json] = new mutable.LinkedHashMap()
      map.put("string", "string")
      map.put("boolean", true)
      map.put("int", 10)
      map.put("long", 10L)
      map.put("float", 1.0F)
      map.put("double", 1.0D)
      map.put("bigDecimal", BigDecimal(1))
      map.put("byteString", ByteString("test"))
      JsonUtil.fromJsonMap(map) should equal(Json.obj(
        "string" -> "string",
        "boolean" -> true,
        "int" -> 10,
        "long" -> 10L,
        "float" -> 1.0F,
        "double" -> 1.0D,
        "bigDecimal" -> BigDecimal(1),
        "byteString" -> ByteString("test")
      ))
    }
  }

}