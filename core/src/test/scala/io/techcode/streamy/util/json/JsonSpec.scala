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

import java.io.ByteArrayInputStream

import akka.util.ByteString
import org.scalatest._

/**
  * Json spec.
  */
class JsonSpec extends FlatSpec with Matchers {

  "Json object" should "equals JsObject independently of field order" in {
    Json.obj(
      "field1" -> 123,
      "field2" -> "beta",
      "field3" -> Json.obj(
        "field31" -> true,
        "field32" -> 123.45,
        "field33" -> Json.arr("blabla", 456L, JsNull)
      )
    ) should equal(Json.obj(
      "field2" -> "beta",
      "field3" -> Json.obj(
        "field31" -> true,
        "field33" -> Json.arr("blabla", 456L, JsNull),
        "field32" -> 123.45
      ),
      "field1" -> 123))
  }

  it should "not be equals when there is a deep difference" in {
    Json.obj(
      "field1" -> 123,
      "field2" -> "beta",
      "field3" -> Json.obj(
        "field31" -> true,
        "field32" -> 123.45,
        "field33" -> Json.arr("blabla", JsNull)
      )
    ) should not equal Json.obj(
      "field2" -> "beta",
      "field3" -> Json.obj(
        "field31" -> true,
        "field33" -> Json.arr("blabla", 456L),
        "field32" -> 123.45
      ),
      "field1" -> 123)
  }

  it should "be create from builder" in {
    val builder = Json.objectBuilder()
    builder.put("test" -> "test")
    builder.remove("test")
    builder.put("foobar" -> "test")
    builder.result()
    builder.put("foobar" -> "notModified")
    builder.result() should equal(Json.obj("foobar" -> "test"))
  }

  it should "return field set" in {
    Json.obj("test" -> "test").fieldSet should equal(Set("test" -> JsString("test")))
  }

  it should "not be equals when there is a difference" in {
    Json.obj(
      "field1" -> 123,
      "field2" -> "beta",
      "field3" -> Json.obj(
        "field31" -> true,
        "field32" -> 123.45,
        "field33" -> Json.arr("blabla", 456L, JsNull)
      )
    ) should not equal Json.obj(
      "field3" -> Json.obj(
        "field31" -> true,
        "field33" -> Json.arr("blabla", 456L, JsNull),
        "field32" -> 123.45
      ),
      "field1" -> 123)
  }

  it should "be update by adding a unique tuple" in {
    val input = Json.obj("test" -> "foobar")
    val result = input.put("add" -> "foobar")
    result should equal(Json.obj(
      "test" -> "foobar",
      "add" -> "foobar"
    ))
    result should not equal input
  }

  it should "be update by adding a tuple" in {
    val input = Json.obj("test" -> "foobar")
    val result = input.put("test" -> "updated")
    result should equal(Json.obj("test" -> "updated"))
    result should not equal input
  }

  it should "be update by removing an existing tuple" in {
    val input = Json.obj("test" -> "foobar")
    val result = input.remove("test")
    result should equal(Json.obj())
    result should not equal input
  }

  it should "be update by removing a tuple" in {
    val input = Json.obj("test" -> "foobar")
    val result = input.remove("foobar")
    result should equal(Json.obj("test" -> "foobar"))
  }

  it should "be merge with another json object" in {
    val input = Json.obj("test" -> "foobar")
    val toMerge = Json.obj("foobar" -> "test")
    val result = input.merge(toMerge)
    result should equal(Json.obj(
      "test" -> "foobar",
      "foobar" -> "test"
    ))
    result should not equal input
  }

  it should "be merge with another empty json object" in {
    val input = Json.obj("test" -> "foobar")
    val toMerge = Json.obj()
    val result = input.merge(toMerge)
    result should equal(Json.obj("test" -> "foobar"))
  }

  it should "return value if present" in {
    val input = Json.obj("test" -> "foobar")
    input("test") should equal(Some(JsString("foobar")))
  }

  it should "return none if absent" in {
    val input = Json.obj("test" -> "foobar")
    input("missing") should equal(None)
  }

  it should "not fail to deep merge when the objects are empty" in {
    Json.obj().deepMerge(Json.obj()) should equal(Some(Json.obj()))
  }

  it should "deep merge correctly when the source object is empty" in {
    def populatedObj = Json.obj(
      "field1" -> 123,
      "field2" -> "abc",
      "field3" -> JsNull
    )

    populatedObj.deepMerge(Json.obj()) should equal(Some(populatedObj))
  }

  it should "deep merge correctly when the incoming object is empty" in {
    val populatedObj = Json.obj(
      "field1" -> 123,
      "field2" -> "abc",
      "field3" -> JsNull
    )

    Json.obj().deepMerge(populatedObj) should equal(Some(populatedObj))
  }

  it should "fail to deep merge a json value" in {
    JsString("1").deepMerge(Json.obj()) should equal(None)
  }

  it should "should keep existing attributes where there is no collision and overwrite existing attributes on collision when value is not a JsArray or JsObject" in {
    Json.obj(
      "field1" -> 123,
      "field2" -> "abc",
      "field3" -> JsNull,
      "field4" -> 456,
      "field5" -> "abc",
      "field6" -> "def"
    ).deepMerge(Json.obj(
      "field4" -> 789,
      "field5" -> "xyz",
      "field6" -> JsNull
    )) should equal(Some(Json.obj(
      "field1" -> 123,
      "field2" -> "abc",
      "field3" -> JsNull,
      "field4" -> 789,
      "field5" -> "xyz",
      "field6" -> JsNull
    )))
  }

  it should "should keep existing attributes where there is no collision and recursively merge where elements are both of type JsArray or both of type JsObject" in {
    Json.obj(
      "field1" -> 123,
      "field2" -> "abc",
      "field3" -> Json.arr(
        "abc", "def", "ghi"
      ),
      "field4" -> Json.obj(
        "field1a" -> 888,
        "field2b" -> "xxx",
        "field3c" -> JsNull
      )
    ).deepMerge(Json.obj(
      "field3" -> Json.arr(
        "jkl", "mno", "pqr"
      ),
      "field4" -> Json.obj(
        "field1a" -> 999,
        "field2b" -> "yyy",
        "field3c" -> "zzz"
      )
    )) should equal(Some(Json.obj(
      "field1" -> 123,
      "field2" -> "abc",
      "field3" -> Json.arr(
        "jkl", "mno", "pqr"
      ),
      "field4" -> Json.obj(
        "field1a" -> 999,
        "field2b" -> "yyy",
        "field3c" -> "zzz"
      )
    )))
  }

  it should "should keep existing attributes where there is no collision and properly merge a deep structure" in {
    Json.obj(
      "field1a" -> Json.obj(
        "field2a" -> Json.obj(
          "field3a" -> Json.obj(
            "field4a" -> Json.obj(
              "field5a" -> "abc",
              "field5b" -> Json.arr("111", "222"),
              "field5d" -> Json.arr(Json.obj("a" -> 1), Json.obj("b" -> 2))
            )
          ),
          "field2b" -> Json.arr("aaa", "bbb"),
          "field2c" -> Json.obj(
            "hello" -> "world"
          )
        ),
        "field2b" -> "xxx",
        "field2c" -> JsNull
      )
    ).deepMerge(Json.obj(
      "field1a" -> Json.obj(
        "field2a" -> Json.obj(
          "field3a" -> Json.obj(
            "field4a" -> Json.obj(
              "field5b" -> Json.arr("333", "444"),
              "field5c" -> "deep",
              "field5d" -> Json.arr(Json.obj("c" -> 3), Json.obj("d" -> 4))
            )
          ),
          "field2b" -> Json.arr("ccc", "ddd"),
          "field2c" -> Json.obj(
            "hello" -> "new world"
          )
        ),
        "field2b" -> "yyy",
        "field2d" -> "zzz"
      )
    )) should equal(Some(Json.obj(
      "field1a" -> Json.obj(
        "field2a" -> Json.obj(
          "field3a" -> Json.obj(
            "field4a" -> Json.obj(
              "field5a" -> "abc",
              "field5b" -> Json.arr("333", "444"),
              "field5c" -> "deep",
              "field5d" -> Json.arr(Json.obj("c" -> 3), Json.obj("d" -> 4))
            )
          ),
          "field2b" -> Json.arr("ccc", "ddd"),
          "field2c" -> Json.obj(
            "hello" -> "new world"
          )
        ),
        "field2b" -> "yyy",
        "field2c" -> JsNull,
        "field2d" -> "zzz"
      )
    )))
  }

  it should "deep merge only JsObject" in {
    Json.obj().deepMerge(JsNull) should equal(None)
  }

  "Json array" should "return value if present" in {
    val input = Json.arr("test", "foobar")
    input(1) should equal(Some(JsString("foobar")))
  }

  it should "return none if absent" in {
    val input = Json.arr("test", "foobar")
    input(2) should equal(None)
  }

  it should "be create from builder" in {
    val builder = Json.arrayBuilder()
    builder.add("test")
    builder.remove()
    builder.add("foobar")
    builder.result()
    builder.remove()
    builder.add("notModified")
    builder.result() should equal(Json.arr("foobar"))
  }

  it should "append json array correctly" in {
    val input = Json.arr("test01", "test02")
    input.append(Json.arr("test03")) should equal(Json.arr("test01", "test02", "test03"))
  }

  it should "append json value correctly" in {
    val input = Json.arr("test01", "test02")
    input.append("test03") should equal(Json.arr("test01", "test02", "test03"))
  }

  it should "prepend json array correctly" in {
    val input = Json.arr("test01", "test02")
    input.prepend(Json.arr("test03")) should equal(Json.arr("test03", "test01", "test02"))
  }

  it should "prepend json value correctly" in {
    val input = Json.arr("test01", "test02")
    input.prepend("test03") should equal(Json.arr("test03", "test01", "test02"))
  }

  "Json" should "stringify long integers correctly" in {
    val input = Json.obj("l" -> 1330950829160L)
    input.toString should equal("""{"l":1330950829160}""")
  }

  it should "stringify short integers correctly" in {
    val s: Short = 1234
    val input = Json.obj("s" -> s)
    input.toString should equal("""{"s":1234}""")
  }

  it should "stringify byte integers correctly" in {
    val b: Byte = 123
    val input = Json.obj("b" -> b)
    input.toString should equal("""{"b":123}""")
  }

  it should "stringify boolean correctly" in {
    JsTrue.toString should equal("true")
  }

  it should "stringify float correctly" in {
    JsFloat(1.0F).toString should equal("1.0")
  }

  it should "stringify double correctly" in {
    JsDouble(1.0D).toString should equal("1.0")
  }

  it should "stringify null correctly" in {
    JsNull.toString should equal("null")
  }

  it should "stringify bytestring correctly" in {
    JsBytes(ByteString("test")).toString should equal("\"dGVzdA==\"")
  }

  it should "stringify big decimal correctly" in {
    val n = BigDecimal("12345678901234567890.42")
    val input = Json.obj("bd" -> n)
    input.toString should equal("""{"bd":12345678901234567890.42}""")
  }

  it should "stringify big decimal with large exponents in scientific notation correctly" in {
    val input = Json.obj("bd" -> BigDecimal("1.2e1000"))
    input.toString should equal("""{"bd":1.2E+1000}""")
  }

  it should "stringify big decimal with large negative exponents in scientific notation correctly" in {
    val input = Json.obj("bd" -> BigDecimal("6.75e-1000"))
    input.toString should equal("""{"bd":6.75E-1000}""")
  }

  it should "stringify big decimal with small exponents in scientific notation correctly" in {
    val input = Json.obj("bd" -> BigDecimal("1.234e3"))
    input.toString should equal("""{"bd":1234}""")
  }

  it should "stringify big decimal with small negative exponents in scientific notation correctly" in {
    val input = Json.obj("bd" -> BigDecimal("1.234e-3"))
    input.toString should equal("""{"bd":0.001234}""")
  }

  it should "stringify big decimal with integer base correctly" in {
    val input = Json.obj("bd" -> BigDecimal("2e128"))
    input.toString should equal("""{"bd":2E+128}""")
  }

  it should "stringify list correctly" in {
    val input = Json.arr("123", 123, BigDecimal("2e128"))
    input.toString should equal("""["123",123,2E+128]""")
  }

  it should "parse long integers correctly" in {
    val input = Json.parse("1330950829160").getOrElse(JsNull)
    input should equal(JsLong(1330950829160L))
  }

  it should "parse short integers correctly" in {
    val input = Json.parse("1234").getOrElse(JsNull)
    input should equal(JsInt(1234))
  }

  it should "parse byte integers correctly" in {
    val input = Json.parse("123").getOrElse(JsNull)
    input should equal(JsInt(123))
  }

  it should "parse big decimal correctly" in {
    val input = Json.parse("12345678901234567890.42").getOrElse(JsNull)
    input should equal(JsBigDecimal(BigDecimal("12345678901234567890.42")))
  }

  it should "parse big decimal with large exponents in scientific notation correctly" in {
    val input = Json.parse("1.2e1000").getOrElse(JsNull)
    input should equal(JsBigDecimal(BigDecimal("1.2e1000")))
  }

  it should "parse big decimal with large negative exponents in scientific notation correctly" in {
    val input = Json.parse("6.75e-1000").getOrElse(JsNull)
    input should equal(JsBigDecimal(BigDecimal("6.75e-1000")))
  }

  it should "parse big decimal with small exponents in scientific notation correctly" in {
    val input = Json.parse("1.234e3").getOrElse(JsNull)
    input should equal(JsBigDecimal(BigDecimal("1.234e3")))
  }

  it should "parse big decimal with small negative exponents in scientific notation correctly" in {
    val input = Json.parse("1.234e-3").getOrElse(JsNull)
    input should equal(JsBigDecimal(BigDecimal("1.234e-3")))
  }

  it should "parse big decimal with integer base correctly" in {
    val input = Json.parse("2e128").getOrElse(JsNull)
    input should equal(JsBigDecimal(BigDecimal("2e128")))
  }

  it should "parse list correctly" in {
    val input = Json.parse("""["123",123,2E+128]""").getOrElse(JsNull)
    input should equal(Json.arr("123", 123, BigDecimal("2e128")))
  }

  it should "parse null values in object" in {
    val input = Json.parse("""{"foo": null}""").getOrElse(JsNull)
    input should equal(Json.obj("foo" -> JsNull))
  }

  it should "parse null values in array" in {
    val input = Json.parse("""[null]""").getOrElse(JsNull)
    input should equal(Json.arr(JsNull))
  }

  it should "parse null as JsNull" in {
    val input = Json.parse("""null""").getOrElse(JsNull)
    input should equal(JsNull)
  }

  it should "parse json object from bytes" in {
    val input = Json.parse("""{"test":"test"}""".getBytes).getOrElse(JsNull)
    input should equal(Json.obj("test" -> "test"))
  }


  it should "parse json object from input stream" in {
    val input = Json.parse(new ByteArrayInputStream("""{"test":"test"}""".getBytes)).getOrElse(JsNull)
    input should equal(Json.obj("test" -> "test"))
  }

  it should "asciiStringify should escape non-ascii characters" in {
    def jo = Json.obj(
      "key1" -> "\u2028\u2029\u2030",
      "key2" -> "\u00E1\u00E9\u00ED\u00F3\u00FA",
      "key3" -> "\u00A9\u00A3",
      "key4" -> "\u6837\u54C1"
    )

    Json.asciiStringify(jo) should equal(
      "{\"key1\":\"\\u2028\\u2029\\u2030\"," +
        "\"key2\":\"\\u00E1\\u00E9\\u00ED\\u00F3\\u00FA\"," +
        "\"key3\":\"\\u00A9\\u00A3\"," + "" +
        "\"key4\":\"\\u6837\\u54C1\"}"
    )
  }

  it should "asciiStringify should escape ascii characters properly" in {
    def jo = Json.obj(
      "key1" -> "ab\n\tcd",
      "key2" -> "\"\r"
    )

    Json.asciiStringify(jo) should equal("""{"key1":"ab\n\tcd","key2":"\"\r"}""")
  }

  it should "be convert to json object when possible" in {
    Json.obj().asObject should equal(Some(Json.obj()))
  }

  it should "fail to be convert to json object when not possible" in {
    JsString("10").asObject should equal(None)
  }

  it should "return true when it can be convert to json object" in {
    Json.obj().isObject should equal(true)
  }

  it should "return false when it can be convert to json object" in {
    JsString("10").isObject should equal(false)
  }

  it should "be convert to json array when possible" in {
    Json.arr().asArray should equal(Some(Json.arr()))
  }

  it should "fail to be convert to json array when not possible" in {
    JsString("10").asArray should equal(None)
  }

  it should "return true when it can be convert to json array" in {
    Json.arr().isArray should equal(true)
  }

  it should "return false when it can be convert to json array" in {
    JsString("10").isArray should equal(false)
  }

  it should "be convert to boolean when possible" in {
    JsTrue.asBoolean should equal(Some(true))
  }

  it should "fail to be convert to boolean when not possible" in {
    JsString("10").asBoolean should equal(None)
  }

  it should "return true when it can be convert to boolean" in {
    JsTrue.isBoolean should equal(true)
  }

  it should "return false when it can be convert to boolean" in {
    JsString("10").isBoolean should equal(false)
  }

  it should "be convert to string when possible" in {
    JsString("10").asString should equal(Some("10"))
  }

  it should "fail to be convert to string when not possible" in {
    JsTrue.asString should equal(None)
  }

  it should "return true when it can be convert to string" in {
    JsString("10").isString should equal(true)
  }

  it should "return false when it can be convert to string" in {
    JsTrue.isString should equal(false)
  }

  it should "be convert to number when possible" in {
    JsBigDecimal(BigDecimal("1e20")).asNumber should equal(Some(BigDecimal("1e20")))
  }

  it should "fail to be convert to number when not possible" in {
    JsTrue.asNumber should equal(None)
  }

  it should "return true when it can be convert to number" in {
    JsBigDecimal(BigDecimal("1e20")).isNumber should equal(true)
  }

  it should "return false when it can be convert to number" in {
    JsTrue.isNumber should equal(false)
  }

  it should "be convert to null when possible" in {
    JsNull.asNull should equal(Some(()))
  }

  it should "fail to be convert to null when not possible" in {
    JsTrue.asNull should equal(None)
  }

  it should "return true when it can be convert to null" in {
    JsNull.isNull should equal(true)
  }

  it should "return false when it can be convert to null" in {
    JsTrue.isNull should equal(false)
  }

  it should "be convert to int when possible" in {
    JsInt(1).asInt should equal(Some(1))
  }

  it should "fail to be convert to int when not possible" in {
    JsTrue.asInt should equal(None)
  }

  it should "return true when it can be convert to int" in {
    JsInt(1).isInt should equal(true)
  }

  it should "return false when it can be convert to int" in {
    JsTrue.isInt should equal(false)
  }

  it should "be convert to long when possible" in {
    JsLong(1).asLong should equal(Some(1))
  }

  it should "fail to be convert to long when not possible" in {
    JsTrue.asLong should equal(None)
  }

  it should "return true when it can be convert to long" in {
    JsLong(1).isLong should equal(true)
  }

  it should "return false when it can be convert to long" in {
    JsTrue.isLong should equal(false)
  }

  it should "be convert to float when possible" in {
    JsFloat(1.0F).asFloat should equal(Some(1.0F))
  }

  it should "fail to be convert to float when not possible" in {
    JsTrue.asFloat should equal(None)
  }

  it should "return true when it can be convert to float" in {
    JsFloat(1.0F).isFloat should equal(true)
  }

  it should "return false when it can be convert to float" in {
    JsTrue.isFloat should equal(false)
  }

  it should "be convert to double when possible" in {
    JsDouble(1.0D).asDouble should equal(Some(1.0D))
  }

  it should "fail to be convert to double when not possible" in {
    JsTrue.asDouble should equal(None)
  }

  it should "return true when it can be convert to double" in {
    JsDouble(1.0D).isDouble should equal(true)
  }

  it should "return false when it can be convert to double" in {
    JsTrue.isDouble should equal(false)
  }

  it should "be convert to bytes when possible" in {
    JsBytes(ByteString("test")).asBytes should equal(Some(ByteString("test")))
  }

  it should "fail to be convert to bytes when not possible" in {
    JsTrue.asBytes should equal(None)
  }

  it should "return true when it can be convert to bytes" in {
    JsBytes(ByteString("test")).isBytes should equal(true)
  }

  it should "return false when it can be convert to bytes" in {
    JsTrue.isBytes should equal(false)
  }

}