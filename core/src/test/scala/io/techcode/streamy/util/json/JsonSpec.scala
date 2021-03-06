/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017-2019
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
import com.google.common.math.{IntMath, LongMath}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

/**
  * Json spec.
  */
class JsonSpec extends AnyWordSpecLike with Matchers {

  "Json object" should {
    "equals JsObject independently of field order" in {
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

    "not be equals when there is a deep difference" in {
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

    "be create empty" in {
      Json.obj().eq(Json.obj()) should equal(true)
    }

    "return if empty" in {
      Json.obj().isEmpty should equal(true)
      Json.obj("foo" -> "bar").isEmpty should equal(false)
    }

    "be iterate using a foreach" in {
      var founded = false
      Json.obj("test" -> "test").foreach(el => founded |= el._2.equals(JsString.fromLiteral("test")))
      founded should equal(true)
    }

    "return field set" in {
      Json.obj("test" -> "test").fieldSet should equal(Set("test" -> JsString.fromLiteral("test")))
    }

    "return if contains key" in {
      val input = Json.obj("foo" -> "bar")
      input.contains("foo") should equal(true)
      input.contains("missing") should equal(false)
    }

    "return a filtered json object" in {
      val input = Json.obj("foo" -> "bar")
      input.filter(_ => false) should equal(Json.obj())
      input should equal(Json.obj("foo" -> "bar"))
      input.filterNot { case (_: String, _: Json) => false } should equal(Json.obj("foo" -> "bar"))
      input should equal(Json.obj("foo" -> "bar"))
    }

    "return size of instance" in {
      Json.obj().size() should equal(0)
      Json.obj("foo" -> "bar").size() should equal(1)
    }

    "return values as iterable" in {
      Json.obj("test" -> "test").values.head should equal(Seq(JsString.fromLiteral("test")).head)
    }

    "flatten correctly a json object" in {
      Json.obj(
        "foobar" -> 0,
        "test" -> Json.obj(
          "test" -> "foobar",
          "foobar" -> Json.obj("test" -> 0)
        )
      ).flatten() should equal(Json.obj(
        "foobar" -> 0,
        "test.test" -> "foobar",
        "test.foobar.test" -> 0
      ))
    }

    "not be equals when there is a difference" in {
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

    "be update by adding a unique tuple" in {
      val input = Json.obj("test" -> "foobar")
      val result = input.put("add" -> "foobar")
      result should equal(Json.obj(
        "test" -> "foobar",
        "add" -> "foobar"
      ))
      result should not equal input
    }

    "be update by adding a tuple" in {
      val input = Json.obj("test" -> "foobar")
      val result = input.put("test" -> "updated")
      result should equal(Json.obj("test" -> "updated"))
      result should not equal input
    }

    "be update by removing an existing tuple" in {
      val input = Json.obj("test" -> "foobar")
      val result = input.remove("test")
      result should equal(Json.obj())
      result should not equal input
    }

    "be update by removing a tuple" in {
      val input = Json.obj("test" -> "foobar")
      val result = input.remove("foobar")
      result should equal(Json.obj("test" -> "foobar"))
    }

    "be merge with another json object" in {
      val input = Json.obj("test" -> "foobar")
      val toMerge = Json.obj("foobar" -> "test")
      val result = input.merge(toMerge)
      result should equal(Json.obj(
        "test" -> "foobar",
        "foobar" -> "test"
      ))
      result should not equal input
    }

    "be merge with another empty json object" in {
      val input = Json.obj("test" -> "foobar")
      val toMerge = Json.obj()
      val result = input.merge(toMerge)
      result should equal(Json.obj("test" -> "foobar"))
    }

    "return value if present" in {
      val input = Json.obj("test" -> "foobar")
      input("test") should equal(JsString.fromLiteral("foobar"))
    }

    "return undefined if absent" in {
      val input = Json.obj("test" -> "foobar")
      input("missing") should equal(JsUndefined)
    }

    "not fail to deep merge when the objects are empty" in {
      Json.obj().deepMerge(Json.obj()) should equal(Json.obj())
    }

    "deep merge correctly when the source object is empty" in {
      def populatedObj = Json.obj(
        "field1" -> 123,
        "field2" -> "abc",
        "field3" -> JsNull
      )

      populatedObj.deepMerge(Json.obj()) should equal(populatedObj)
    }

    "deep merge correctly when the incoming object is empty" in {
      val populatedObj = Json.obj(
        "field1" -> 123,
        "field2" -> "abc",
        "field3" -> JsNull
      )

      Json.obj().deepMerge(populatedObj) should equal(populatedObj)
    }

    "should keep existing attributes where there is no collision and overwrite existing attributes on collision when value is not a JsArray or JsObject" in {
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
      )) should equal(Json.obj(
        "field1" -> 123,
        "field2" -> "abc",
        "field3" -> JsNull,
        "field4" -> 789,
        "field5" -> "xyz",
        "field6" -> JsNull
      ))
    }

    "should keep existing attributes where there is no collision and recursively merge where elements are both of type JsArray or both of type JsObject" in {
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
      )) should equal(Json.obj(
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
      ))
    }

    "should keep existing attributes where there is no collision and properly merge a deep structure" in {
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
      )) should equal(Json.obj(
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
      ))
    }

    "return correct size" in {
      Json.obj("test" -> "test").sizeHint should equal(15)
    }

    "be identified as object" in {
      Json.obj().isObject should equal(true)
    }

    "be filter" in {
      Json.obj("test" -> "test").filter { case (_: String, v: Json) => v.isString } should equal(Json.obj("test" -> "test"))
      Json.obj("test" -> "test").filter { case (_: String, v: Json) => v.isArray } should equal(Json.obj())
      Json.obj("test" -> "test").filterNot { case (_: String, v: Json) => v.isString } should equal(Json.obj())
      Json.obj("test" -> "test").filterNot { case (_: String, v: Json) => v.isArray } should equal(Json.obj("test" -> "test"))
    }

    "be execute function if exists" in {
      var value = false
      Json.obj("test" -> "test").ifExists[JsObject] { v => value = v.isObject }
      value should equal(true)
    }

    "be fold" in {
      Json.obj("test" -> "test").fold(JsNull) { v => v } should equal(Json.obj("test" -> "test"))
      JsUndefined.fold(JsNull) { v => v } should equal(JsNull)
    }
  }

  "Json array" should {
    "stringify list correctly" in {
      val input = Json.arr("123", 123, BigDecimal("2e128"))
      input.toString should equal("""["123",123,2E+128]""")
    }

    "be converted to iterator" in {
      Json.arr("foobar").iterator.next() should equal(JsString.fromLiteral("foobar"))
    }

    "be converted to seq" in {
      Json.arr("foobar").toSeq.head should equal(JsString.fromLiteral("foobar"))
    }

    "map json array correctly" in {
      Json.arr().map[JsArray](_ => JsNull) should equal(JsNull)
      JsNull.map[JsArray](_ => JsNull) should equal(JsUndefined)
    }

    "return value if present" in {
      val input = Json.arr("test", "foobar")
      input(1) should equal(JsString.fromLiteral("foobar"))
    }

    "return undefined if absent" in {
      val input = Json.arr("test", "foobar")
      input(2) should equal(JsUndefined)
    }

    "be identified as array" in {
      Json.arr().isArray should equal(true)
    }

    "be create empty" in {
      Json.arr().eq(Json.arr()) should equal(true)
    }

    "return correctly if empty" in {
      Json.arr().isEmpty should equal(true)
      Json.arr("test").isEmpty should equal(false)
    }

    "return head of json array if present" in {
      val input = Json.arr("test", "foobar")
      input.head() should equal(JsString.fromLiteral("test"))
    }

    "return head of json array if not present" in {
      val input = Json.arr()
      input.head() should equal(JsUndefined)
    }

    "return last of json array if present" in {
      val input = Json.arr("test", "foobar")
      input.last() should equal(JsString.fromLiteral("foobar"))
    }

    "return last of json array if not present" in {
      val input = Json.arr()
      input.last() should equal(JsUndefined)
    }

    "append json array correctly" in {
      val input = Json.arr("test01", "test02")
      input.append(Json.arr("test03")) should equal(Json.arr("test01", "test02", "test03"))
    }

    "append json value correctly" in {
      val input = Json.arr("test01", "test02")
      input.append("test03") should equal(Json.arr("test01", "test02", "test03"))
    }

    "prepend json array correctly" in {
      val input = Json.arr("test01", "test02")
      input.prepend(Json.arr("test03")) should equal(Json.arr("test03", "test01", "test02"))
    }

    "prepend json value correctly" in {
      val input = Json.arr("test01", "test02")
      input.prepend("test03") should equal(Json.arr("test03", "test01", "test02"))
    }

    "be iterable using foreach" in {
      Json.arr("test").foreach(_ => false)
    }

    "be filtrable" in {
      val input = Json.arr("test", 1)
      input.filter(el => el.isString) should equal(Json.arr("test"))
      input should equal(Json.arr("test", 1))
      input.filterNot(el => el.isString) should equal(Json.arr(1))
      input should equal(Json.arr("test", 1))
    }

    "return correctly if a element is present" in {
      Json.arr("test").contains("test") should equal(true)
      Json.arr().contains("test") should equal(false)
    }

    "return correct size" in {
      Json.arr().size() should equal(0)
      Json.arr("test").size() should equal(1)
    }

    "return correct size hint" in {
      Json.arr("test", 2, Json.obj("test" -> "test"), 4.0).sizeHint should equal(30) // ["test",2,{"test":"test"},4.0]
    }
  }

  "Json number" should {
    "stringify float correctly" in {
      JsFloat.fromLiteral(1.0F).toString should equal("1.0")
    }

    "map json number correctly" in {
      JsFloat.fromLiteral(10F).map[JsNumber](_ => JsNull) should equal(JsNull)
      JsNull.map[Float](_ => JsNull) should equal(JsUndefined)
    }

    "flatMap json number correctly" in {
      JsFloat.fromLiteral(10F).flatMap[JsNumber](_ => JsNull) should equal(JsNull)
      JsNull.flatMap[JsNumber](_ => JsNull) should equal(JsUndefined)
    }

    "get json number correctly" in {
      JsFloat.fromLiteral(10F).get[JsNumber] should equal(JsFloat.fromLiteral(10F))
    }

    "getOrElse json number correctly" in {
      JsFloat.fromLiteral(10F).getOrElse[JsNumber](JsFloat.fromLiteral(0F)) should equal(JsFloat.fromLiteral(10F))
      JsNull.getOrElse[JsNumber](JsFloat.fromLiteral(0F)) should equal(JsFloat.fromLiteral(0F))
    }

    "be identified as number" in {
      JsFloat.fromLiteral(0F).isNumber should equal(true)
    }

    "be identified as not null" in {
      JsFloat.fromLiteral(0F).isNull should equal(false)
    }
  }

  "Json float" should {
    "stringify float correctly" in {
      JsFloat(1.0F).toString should equal("1.0")
      JsFloat.fromLiteral(1.0F).toString should equal("1.0")
      JsFloat.fromByteStringUnsafe(ByteString("1.0")).toString should equal("1.0")
      JsFloat.fromStringUnsafe("1.0").toString should equal("1.0")
    }

    "map json float correctly" in {
      JsFloat.fromLiteral(10F).map[Float](_ => JsNull) should equal(JsNull)
      JsNull.map[Float](_ => JsNull) should equal(JsUndefined)
    }

    "return int conversion for float" in {
      JsFloat.fromLiteral(2.0F).toInt should equal(2)
      JsFloat.fromByteStringUnsafe(ByteString("2.0")).toInt should equal(2)
      JsFloat.fromStringUnsafe("2.0").toInt should equal(2)
    }

    "return long conversion for float" in {
      JsFloat.fromLiteral(2.0F).toLong should equal(2L)
      JsFloat.fromByteStringUnsafe(ByteString("2.0")).toLong should equal(2L)
      JsFloat.fromStringUnsafe("2.0").toLong should equal(2L)
    }

    "return float conversion for float" in {
      JsFloat.fromLiteral(6.0F).toFloat should equal(6.0F)
      JsFloat.fromByteStringUnsafe(ByteString("6.0")).toFloat should equal(6.0F)
      JsFloat.fromStringUnsafe("6.0").toFloat should equal(6.0F)
    }

    "return double conversion for float" in {
      JsFloat.fromLiteral(2.0F).toDouble should equal(2.0D)
      JsFloat.fromByteStringUnsafe(ByteString("2.0")).toDouble should equal(2.0D)
      JsFloat.fromStringUnsafe("2.0").toDouble should equal(2.0D)
    }

    "return big decimal conversion for float" in {
      JsFloat.fromLiteral(2.0F).toBigDecimal should equal(BigDecimal(2.0F))
      JsFloat.fromByteStringUnsafe(ByteString("2.0")).toBigDecimal should equal(BigDecimal(2.0F))
      JsFloat.fromStringUnsafe("2.0").toBigDecimal should equal(BigDecimal(2.0F))
    }

    "return correct size for float" in {
      JsFloat.fromLiteral(2.0F).sizeHint should equal(3)
      JsFloat.fromByteStringUnsafe(ByteString("2.0")).sizeHint should equal(3)
      JsFloat.fromStringUnsafe("2.0").sizeHint should equal(3)
    }

    "support pattern matching" in {
      JsFloat.fromLiteral(2.0F) match {
        case JsFloat(x) => x should equal(2.0F)
        case x: JsFloat => x.value should equal(2.0F)
      }
      JsFloat.fromByteStringUnsafe(ByteString("2.0")) match {
        case JsFloat(x) => x should equal(2.0F)
        case x: JsFloat => x.value should equal(2.0F)
      }
      JsFloat.fromStringUnsafe("2.0") match {
        case JsFloat(x) => x should equal(2.0F)
        case x: JsFloat => x.value should equal(2.0F)
      }
    }

    "be equal to float" in {
      JsFloat.fromLiteral(2.0F) should equal(JsFloat.fromLiteral(2.0F))
      JsFloat.fromLiteral(2.0F) should equal(JsFloat.fromByteStringUnsafe(ByteString("2.0")))
      JsFloat.fromLiteral(2.0F) should equal(JsFloat.fromStringUnsafe("2.0"))
      JsFloat.fromLiteral(0.0F) should not equal (JsFloat.fromLiteral(2.0F))
      JsFloat.fromLiteral(0.0F) should not equal (JsInt.fromLiteral(2))
    }

    "be equal to hashCode float" in {
      JsFloat.fromLiteral(2.0F).hashCode() should equal(JsFloat.fromLiteral(2.0F).hashCode())
      JsFloat.fromLiteral(2.0F).hashCode() should equal(JsFloat.fromByteStringUnsafe(ByteString("2.0")).hashCode())
      JsFloat.fromLiteral(2.0F).hashCode() should equal(JsFloat.fromStringUnsafe("2.0").hashCode())
    }

    "be identified as number" in {
      JsFloat.fromLiteral(0F).isNumber should equal(true)
      JsFloat.fromByteStringUnsafe(ByteString("0")).isNumber should equal(true)
      JsFloat.fromStringUnsafe("0").isNumber should equal(true)
    }

    "be identified as float" in {
      JsFloat.fromLiteral(0F).isFloat should equal(true)
      JsFloat.fromByteStringUnsafe(ByteString("0")).isFloat should equal(true)
      JsFloat.fromStringUnsafe("0").isFloat should equal(true)
    }
  }

  "Json double" should {
    "stringify double correctly" in {
      JsDouble(1.0D).toString should equal("1.0")
      JsDouble.fromLiteral(1.0D).toString should equal("1.0")
      JsDouble.fromByteStringUnsafe(ByteString("1.0")).toString should equal("1.0")
      JsDouble.fromStringUnsafe("1.0").toString should equal("1.0")
    }

    "map json double correctly" in {
      JsDouble.fromLiteral(10D).map[Double](_ => JsNull) should equal(JsNull)
      JsNull.map[Double](_ => JsNull) should equal(JsUndefined)
    }

    "return correct size for double" in {
      JsDouble.fromLiteral(2.0D).sizeHint should equal(3)
      JsDouble.fromByteStringUnsafe(ByteString("2.0")).sizeHint should equal(3)
      JsDouble.fromStringUnsafe("2.0").sizeHint should equal(3)
    }

    "return int conversion for double" in {
      JsDouble.fromLiteral(2.0D).toInt should equal(2)
      JsDouble.fromByteStringUnsafe(ByteString("2.0")).toInt should equal(2)
      JsDouble.fromStringUnsafe("2.0").toInt should equal(2)
    }

    "return long conversion for double" in {
      JsDouble.fromLiteral(2.0D).toLong should equal(2L)
      JsDouble.fromByteStringUnsafe(ByteString("2.0")).toLong should equal(2L)
      JsDouble.fromStringUnsafe("2.0").toLong should equal(2L)
    }

    "return float conversion for double" in {
      JsDouble.fromLiteral(2.0D).toFloat should equal(2.0F)
      JsDouble.fromByteStringUnsafe(ByteString("2.0")).toFloat should equal(2.0F)
      JsDouble.fromStringUnsafe("2.0").toFloat should equal(2.0F)
    }

    "return double conversion for double" in {
      JsDouble.fromLiteral(2.0D).toDouble should equal(2.0D)
      JsDouble.fromByteStringUnsafe(ByteString("2.0")).toDouble should equal(2.0D)
      JsDouble.fromStringUnsafe("2.0").toDouble should equal(2.0D)
    }

    "return big decimal conversion for double" in {
      JsDouble.fromLiteral(2.0D).toBigDecimal should equal(BigDecimal(2.0D))
      JsDouble.fromByteStringUnsafe(ByteString("2.0")).toBigDecimal should equal(BigDecimal(2.0D))
      JsDouble.fromStringUnsafe("2.0").toBigDecimal should equal(BigDecimal(2.0D))
    }

    "support pattern matching" in {
      JsDouble.fromLiteral(2.0D) match {
        case JsDouble(x) => x should equal(2.0D)
        case x: JsDouble => x.value should equal(2.0D)
      }
      JsDouble.fromByteStringUnsafe(ByteString("2.0")) match {
        case JsDouble(x) => x should equal(2.0D)
        case x: JsDouble => x.value should equal(2.0D)
      }
      JsDouble.fromStringUnsafe("2.0") match {
        case JsDouble(x) => x should equal(2.0D)
        case x: JsDouble => x.value should equal(2.0D)
      }
    }

    "be equal to double" in {
      JsDouble.fromLiteral(0.0D) should equal(JsDouble.fromLiteral(0.0F))
      JsDouble.fromLiteral(0.0D) should equal(JsDouble.fromByteStringUnsafe(ByteString("0.0")))
      JsDouble.fromLiteral(0.0D) should equal(JsDouble.fromStringUnsafe("0.0"))
      JsDouble.fromLiteral(0.0D) should not equal (JsDouble.fromLiteral(2.0D))
      JsDouble.fromLiteral(0.0D) should not equal (JsInt.fromLiteral(0))
    }

    "be equal to hashCode double" in {
      JsDouble.fromLiteral(0.0D).hashCode() should equal(JsDouble.fromLiteral(0.0F).hashCode())
      JsDouble.fromLiteral(0.0D).hashCode() should equal(JsDouble.fromByteStringUnsafe(ByteString("0.0")).hashCode())
      JsDouble.fromLiteral(0.0D).hashCode() should equal(JsDouble.fromStringUnsafe("0.0").hashCode())
    }

    "be identified as number" in {
      JsDouble.fromLiteral(0.0D).isNumber should equal(true)
      JsDouble.fromByteStringUnsafe(ByteString("0.0")).isNumber should equal(true)
      JsDouble.fromStringUnsafe("0.0").isNumber should equal(true)
    }

    "be identified as double" in {
      JsDouble.fromLiteral(0.0D).isDouble should equal(true)
      JsDouble.fromByteStringUnsafe(ByteString("0.0")).isDouble should equal(true)
      JsDouble.fromStringUnsafe("0.0").isDouble should equal(true)
    }
  }

  "Json int" should {
    "stringify int correctly" in {
      JsInt(0).toString should equal("0")
      JsInt.fromLiteral(0).toString should equal("0")
      JsInt.fromByteStringUnsafe(ByteString("0")).toString should equal("0")
      JsInt.fromStringUnsafe("0").toString should equal("0")
    }

    "return correct size for int" in {
      // Positive cases
      var size = 1
      for (i <- 0 until String.valueOf(Int.MaxValue).length) {
        JsInt(IntMath.pow(10, i)).sizeHint should equal(size)
        JsInt.fromStringUnsafe(IntMath.pow(10, i).toString).sizeHint should equal(size)
        JsInt.fromByteStringUnsafe(ByteString(IntMath.pow(10, i).toString)).sizeHint should equal(size)
        size += 1
      }

      // Negative cases
      size = 2
      for (i <- 0 until String.valueOf(Int.MaxValue).length) {
        JsInt(-1 * IntMath.pow(10, i)).sizeHint should equal(size)
        JsInt.fromStringUnsafe((-1 * IntMath.pow(10, i)).toString).sizeHint should equal(size)
        JsInt.fromByteStringUnsafe(ByteString((-1 * IntMath.pow(10, i)).toString)).sizeHint should equal(size)
        size += 1
      }
    }

    "return int conversion for int" in {
      JsInt.fromLiteral(1).toInt should equal(1)
      JsInt.fromByteStringUnsafe(ByteString("1")).toInt should equal(1)
      JsInt.fromStringUnsafe("1").toInt should equal(1)
    }

    "return long conversion for int" in {
      JsInt.fromLiteral(1).toLong should equal(1L)
      JsInt.fromByteStringUnsafe(ByteString("1")).toLong should equal(1L)
      JsInt.fromStringUnsafe("1").toLong should equal(1L)
    }

    "return float conversion for int" in {
      JsInt.fromLiteral(1).toFloat should equal(1.0F)
      JsInt.fromByteStringUnsafe(ByteString("1")).toFloat should equal(1.0F)
      JsInt.fromStringUnsafe("1").toFloat should equal(1.0F)
    }

    "return double conversion for int" in {
      JsInt.fromLiteral(1).toDouble should equal(1.0D)
      JsInt.fromByteStringUnsafe(ByteString("1")).toDouble should equal(1.0D)
      JsInt.fromStringUnsafe("1").toDouble should equal(1.0D)
    }

    "return big decimal conversion for int" in {
      JsInt.fromLiteral(1).toBigDecimal should equal(BigDecimal(1))
      JsInt.fromByteStringUnsafe(ByteString("1")).toBigDecimal should equal(BigDecimal(1))
      JsInt.fromStringUnsafe("1").toBigDecimal should equal(BigDecimal(1))
    }

    "support pattern matching" in {
      JsInt.fromLiteral(1) match {
        case JsInt(x) => x should equal(1)
        case x: JsInt => x.value should equal(1)
      }
      JsInt.fromByteStringUnsafe(ByteString("1")) match {
        case JsInt(x) => x should equal(1)
        case x: JsInt => x.value should equal(1)
      }
      JsInt.fromStringUnsafe("1") match {
        case JsInt(x) => x should equal(1)
        case x: JsInt => x.value should equal(1)
      }
    }

    "be equal to int" in {
      JsInt.fromLiteral(0) should equal(JsInt.fromLiteral(0))
      JsInt.fromLiteral(0) should equal(JsInt.fromByteStringUnsafe(ByteString("0")))
      JsInt.fromLiteral(0) should equal(JsInt.fromStringUnsafe("0"))
      JsInt.fromLiteral(0) should not equal (JsInt.fromLiteral(2))
      JsInt.fromLiteral(0) should not equal (JsDouble.fromLiteral(0.0D))
    }

    "be equal to hashCode int" in {
      JsInt.fromLiteral(0).hashCode() should equal(JsInt.fromLiteral(0).hashCode())
      JsInt.fromLiteral(0).hashCode() should equal(JsInt.fromByteStringUnsafe(ByteString("0")).hashCode())
      JsInt.fromLiteral(0).hashCode() should equal(JsInt.fromStringUnsafe("0").hashCode())
    }

    "be identified as number" in {
      JsInt.fromLiteral(0).isNumber should equal(true)
      JsInt.fromByteStringUnsafe(ByteString("0")).isNumber should equal(true)
      JsInt.fromStringUnsafe("0").isNumber should equal(true)
    }

    "be identified as int" in {
      JsInt.fromLiteral(0).isInt should equal(true)
      JsInt.fromByteStringUnsafe(ByteString("0")).isInt should equal(true)
      JsInt.fromStringUnsafe("0").isInt should equal(true)
    }
  }

  "Json long" should {
    "stringify long integers correctly" in {
      JsLong(1330950829160L).toString should equal("1330950829160")
      JsLong.fromLiteral(1330950829160L).toString should equal("1330950829160")
      JsLong.fromByteStringUnsafe(ByteString("1330950829160")).toString should equal("1330950829160")
      JsLong.fromStringUnsafe("1330950829160").toString should equal("1330950829160")
    }

    "return correct size for long" in {
      // Positive cases
      var size = 1
      for (i <- 0 until String.valueOf(Long.MaxValue).length) {
        JsLong.fromLiteral(LongMath.pow(10, i)).sizeHint should equal(size)
        JsLong.fromStringUnsafe(LongMath.pow(10, i).toString).sizeHint should equal(size)
        JsLong.fromByteStringUnsafe(ByteString(LongMath.pow(10, i).toString)).sizeHint should equal(size)
        size += 1
      }

      // Negative cases
      size = 2
      for (i <- 0 until String.valueOf(Long.MaxValue).length) {
        JsLong.fromLiteral(-1 * LongMath.pow(10, i)).sizeHint should equal(size)
        JsLong.fromStringUnsafe((-1 * LongMath.pow(10, i)).toString).sizeHint should equal(size)
        JsLong.fromByteStringUnsafe(ByteString((-1 * LongMath.pow(10, i)).toString)).sizeHint should equal(size)
        size += 1
      }
    }

    "return int conversion for long" in {
      JsLong.fromLiteral(1L).toInt should equal(1)
      JsLong.fromByteStringUnsafe(ByteString("1")).toInt should equal(1)
      JsLong.fromStringUnsafe("1").toInt should equal(1)
    }

    "return long conversion for long" in {
      JsLong.fromLiteral(1L).toLong should equal(1L)
      JsLong.fromByteStringUnsafe(ByteString("1")).toLong should equal(1L)
      JsLong.fromStringUnsafe("1").toLong should equal(1L)
    }

    "return float conversion for long" in {
      JsLong.fromLiteral(1L).toFloat should equal(1.0F)
      JsLong.fromByteStringUnsafe(ByteString("1")).toFloat should equal(1.0F)
      JsLong.fromStringUnsafe("1").toFloat should equal(1.0F)
    }

    "return double conversion for long" in {
      JsLong.fromLiteral(1L).toDouble should equal(1.0D)
      JsLong.fromByteStringUnsafe(ByteString("1")).toDouble should equal(1.0D)
      JsLong.fromStringUnsafe("1").toDouble should equal(1.0D)
    }

    "return big decimal conversion for long" in {
      JsLong.fromLiteral(1L).toBigDecimal should equal(BigDecimal(1))
      JsLong.fromByteStringUnsafe(ByteString("1")).toBigDecimal should equal(BigDecimal(1))
      JsLong.fromStringUnsafe("1").toBigDecimal should equal(BigDecimal(1))
    }

    "support pattern matching" in {
      JsLong.fromLiteral(1L) match {
        case JsLong(x) => x should equal(1L)
        case x: JsLong => x.value should equal(1L)
      }
      JsLong.fromByteStringUnsafe(ByteString("1")) match {
        case JsLong(x) => x should equal(1L)
        case x: JsLong => x.value should equal(1L)
      }
      JsLong.fromStringUnsafe("1") match {
        case JsLong(x) => x should equal(1L)
        case x: JsLong => x.value should equal(1L)
      }
    }

    "be equal to long" in {
      JsLong.fromLiteral(0L) should equal(JsLong.fromLiteral(0L))
      JsLong.fromLiteral(0L) should equal(JsLong.fromByteStringUnsafe(ByteString("0")))
      JsLong.fromLiteral(0L) should equal(JsLong.fromStringUnsafe("0"))
      JsLong.fromLiteral(0L) should not equal (JsLong.fromLiteral(2))
      JsLong.fromLiteral(0) should not equal (JsDouble.fromLiteral(0.0D))
    }

    "be equal to hashCode long" in {
      JsLong.fromLiteral(0).hashCode() should equal(JsLong.fromLiteral(0).hashCode())
      JsLong.fromLiteral(0).hashCode() should equal(JsLong.fromByteStringUnsafe(ByteString("0")).hashCode())
      JsLong.fromLiteral(0).hashCode() should equal(JsLong.fromStringUnsafe("0").hashCode())
    }

    "be identified as number" in {
      JsLong.fromLiteral(0).isNumber should equal(true)
      JsLong.fromByteStringUnsafe(ByteString("0")).isNumber should equal(true)
      JsLong.fromStringUnsafe("0").isNumber should equal(true)
    }

    "be identified as long" in {
      JsLong.fromLiteral(0).isLong should equal(true)
      JsLong.fromByteStringUnsafe(ByteString("0")).isLong should equal(true)
      JsLong.fromStringUnsafe("0").isLong should equal(true)
    }
  }

  "Json big decimal" should {
    "return correct size hint for big decimal" in {
      JsBigDecimal(BigDecimal("2e128")).sizeHint should equal(6)
      JsBigDecimal.fromLiteral(BigDecimal("2e128")).sizeHint should equal(6)
      JsBigDecimal.fromByteStringUnsafe(ByteString("2e128")).sizeHint should equal(5)
      JsBigDecimal.fromStringUnsafe("2e128").sizeHint should equal(5)
    }

    "return int conversion for big decimal" in {
      JsBigDecimal.fromLiteral(BigDecimal(6.0D)).toInt should equal(6)
      JsBigDecimal.fromByteStringUnsafe(ByteString("6.0")).toInt should equal(6)
      JsBigDecimal.fromStringUnsafe("6.0").toInt should equal(6)
    }

    "return long conversion for big decimal" in {
      JsBigDecimal.fromLiteral(BigDecimal(6.0D)).toLong should equal(6L)
      JsBigDecimal.fromByteStringUnsafe(ByteString("6.0")).toLong should equal(6L)
      JsBigDecimal.fromStringUnsafe("6.0").toLong should equal(6L)
    }

    "return float conversion for big decimal" in {
      JsBigDecimal.fromLiteral(BigDecimal(6.0D)).toFloat should equal(6.0D)
      JsBigDecimal.fromByteStringUnsafe(ByteString("6.0")).toFloat should equal(6.0D)
      JsBigDecimal.fromStringUnsafe("6.0").toFloat should equal(6.0D)
    }

    "return double conversion for big decimal" in {
      JsBigDecimal.fromLiteral(BigDecimal(6.0D)).toDouble should equal(6D)
      JsBigDecimal.fromByteStringUnsafe(ByteString("6.0")).toInt should equal(6D)
      JsBigDecimal.fromStringUnsafe("6.0").toInt should equal(6D)
    }

    "return big decimal conversion for big decimal" in {
      JsBigDecimal.fromLiteral(BigDecimal("2e128")).toBigDecimal should equal(BigDecimal("2e128"))
      JsBigDecimal.fromByteStringUnsafe(ByteString("2e128")).toBigDecimal should equal(BigDecimal("2e128"))
      JsBigDecimal.fromStringUnsafe("2e128").toBigDecimal should equal(BigDecimal("2e128"))
    }

    "support pattern matching" in {
      JsBigDecimal.fromLiteral(BigDecimal("2e128")) match {
        case JsBigDecimal(x) => x should equal(BigDecimal("2e128"))
        case x: JsBigDecimal => x.value should equal(BigDecimal("2e128"))
      }
      JsBigDecimal.fromByteStringUnsafe(ByteString("2e128")) match {
        case JsBigDecimal(x) => x should equal(BigDecimal("2e128"))
        case x: JsBigDecimal => x.value should equal(BigDecimal("2e128"))
      }
      JsBigDecimal.fromStringUnsafe("2e128") match {
        case JsBigDecimal(x) => x should equal(BigDecimal("2e128"))
        case x: JsBigDecimal => x.value should equal(BigDecimal("2e128"))
      }
    }

    "be equal to big decimal" in {
      JsBigDecimal.fromLiteral(BigDecimal(0)) should equal(JsBigDecimal.fromLiteral(BigDecimal(0)))
      JsBigDecimal.fromLiteral(BigDecimal(0)) should equal(JsBigDecimal.fromByteStringUnsafe(ByteString("0")))
      JsBigDecimal.fromLiteral(BigDecimal(0)) should equal(JsBigDecimal.fromStringUnsafe("0"))
      JsBigDecimal.fromLiteral(BigDecimal(0)) should not equal (JsBigDecimal.fromLiteral(BigDecimal(2)))
      JsBigDecimal.fromLiteral(BigDecimal(0)) should not equal (JsInt.fromLiteral(0))
    }

    "be equal to hashCode big decimal" in {
      JsBigDecimal.fromLiteral(0).hashCode() should equal(JsBigDecimal.fromLiteral(0).hashCode())
      JsBigDecimal.fromLiteral(0).hashCode() should equal(JsBigDecimal.fromByteStringUnsafe(ByteString("0")).hashCode())
      JsBigDecimal.fromLiteral(0).hashCode() should equal(JsBigDecimal.fromStringUnsafe("0").hashCode())
    }

    "be identified as big decimal" in {
      JsBigDecimal.fromLiteral(BigDecimal(0F)).isBigDecimal should equal(true)
      JsBigDecimal.fromByteStringUnsafe(ByteString("0")).isBigDecimal should equal(true)
      JsBigDecimal.fromStringUnsafe("0").isBigDecimal should equal(true)
    }
  }

  "Json string" should {
    "return correct size hint" in {
      JsString("test").sizeHint should equal(6) // "test"
      JsString.fromLiteral("test").sizeHint should equal(6) // "test"
      JsString.fromByteStringUnsafe(ByteString("test")).sizeHint should equal(6) // "test"
    }

    "support pattern matching" in {
      JsString.fromLiteral("test") match {
        case JsString(x) => x should equal("test")
        case x: JsString => x.value should equal("test")
      }
      JsString.fromByteStringUnsafe(ByteString("test")) match {
        case JsString(x) => x should equal("test")
        case x: JsString => x.value should equal("test")
      }
    }

    "return correct length" in {
      JsString("test").length() should equal(4)
      JsString.fromLiteral("test").length() should equal(4)
      JsString.fromByteStringUnsafe(ByteString("test")).length should equal(4)
    }

    "be equal to string" in {
      JsString.fromLiteral("foobar") should equal(JsString.fromLiteral("foobar"))
      JsString.fromLiteral("foobar") should equal(JsString.fromByteStringUnsafe(ByteString("foobar")))
      JsString.fromLiteral("foobar") should not equal (JsString.fromLiteral("foo"))
      JsString.fromLiteral("foobar") should not equal (JsInt.fromLiteral(0))
    }

    "be equal to hashCode string" in {
      JsString.fromLiteral("foobar").hashCode() should equal(JsString.fromLiteral("foobar").hashCode())
      JsString.fromLiteral("foobar").hashCode() should equal(JsString.fromByteStringUnsafe(ByteString("foobar")).hashCode())
    }

    "be identified as string" in {
      JsString.fromLiteral("").isString should equal(true)
      JsString.fromByteStringUnsafe(ByteString("")).isString should equal(true)
    }
  }

  "Json bytes" should {
    "stringify bytestring correctly" in {
      JsBytes(ByteString("test")).toString should equal("\"dGVzdA==\"")
      JsBytes.fromLiteral(ByteString("test")).toString should equal("\"dGVzdA==\"")
      JsBytes.fromStringUnsafe("test").toString should equal("\"dGVzdA==\"")
    }

    "support pattern matching" in {
      JsBytes.fromLiteral(ByteString("test")) match {
        case JsBytes(x) => x should equal(ByteString("test"))
        case x: JsBytes => x.value should equal(ByteString("test"))
      }
      JsBytes.fromStringUnsafe("test") match {
        case JsBytes(x) => x should equal(ByteString("test"))
        case x: JsBytes => x.value should equal(ByteString("test"))
      }
    }

    "be equal to bytes" in {
      JsBytes.fromLiteral(ByteString("foobar")) should equal(JsBytes.fromLiteral(ByteString("foobar")))
      JsBytes.fromLiteral(ByteString("foobar")) should equal(JsBytes.fromStringUnsafe("foobar"))
      JsBytes.fromLiteral(ByteString("foobar")) should not equal (JsBytes.fromLiteral(ByteString("foo")))
      JsBytes.fromLiteral(ByteString("foobar")) should not equal (JsInt.fromLiteral(0))
    }

    "be equal to hashCode bytes" in {
      JsBytes.fromLiteral(ByteString("foobar")).hashCode() should equal(JsBytes.fromLiteral(ByteString("foobar")).hashCode())
      JsBytes.fromLiteral(ByteString("foobar")).hashCode() should equal(JsBytes.fromStringUnsafe("foobar").hashCode())
    }

    "be identified as bytes" in {
      JsBytes.fromLiteral(ByteString.empty).isBytes should equal(true)
      JsBytes.fromStringUnsafe("").isBytes should equal(true)
    }

    "return correct the size" in {
      JsBytes(ByteString("test")).size() should equal(4)
      JsBytes.fromStringUnsafe("test").size() should equal(4)
    }

    "return correct the size hint" in {
      JsBytes(ByteString("test")).sizeHint() should equal(10)
      JsBytes.fromStringUnsafe("test").sizeHint() should equal(4)
    }
  }

  "Json null" should {
    "return correct size for null" in {
      JsNull.sizeHint should equal(4)
    }

    "be identified as null" in {
      JsNull.isNull should equal(true)
    }
  }

  "Json undefined" should {
    "be evaluate at any path" in {
      JsUndefined.evaluate(Root) should equal(JsUndefined)
    }

    "be mutate with identify function" in {
      JsUndefined.mutate[Json](Root)(identity[Json]) should equal(JsUndefined)
    }

    "be patch with simple operation" in {
      JsUndefined.patch(Add(Root, JsNull)) should equal(JsUndefined)
    }

    "be patch with operations" in {
      JsUndefined.patch(Add(Root, JsNull), Add(Root, JsNull)) should equal(JsUndefined)
    }

    "be patch with iterable operations" in {
      JsUndefined.patch(Seq(Add(Root, JsNull), Add(Root, JsNull))) should equal(JsUndefined)
    }

    "be used with a predicate for validation" in {
      JsUndefined.exists(v => v.isDefined) should equal(false)
    }

    "be identified as undefined" in {
      JsUndefined.isDefined should equal(false)
      JsUndefined.isEmpty should equal(true)
    }

    "be convert to option" in {
      JsUndefined.toOption should equal(None)
    }
  }

  "Json boolean" should {
    "get boolean correctly" in {
      JsTrue.get[Boolean] should equal(true)
      JsFalse.get[Boolean] should equal(false)
      assertThrows[ClassCastException] {
        JsNull.get[Boolean]
      }
    }

    "be filtrable" in {
      JsTrue.filter(_.isBoolean) should equal(JsTrue)
      JsTrue.filterNot(_.isObject) should equal(JsTrue)
      JsTrue.filter(_.isObject) should equal(JsUndefined)
      JsTrue.filterNot(_.isBoolean) should equal(JsUndefined)
    }

    "return correct size for boolean" in {
      JsTrue.sizeHint should equal(4)
      JsFalse.sizeHint should equal(5)
    }

    "be identified as boolean" in {
      JsTrue.isBoolean should equal(true)
    }
  }

  "Json" should {
    "stringify short integers correctly" in {
      val s: Short = 1234
      val input = Json.obj("s" -> s)
      Json.printStringUnsafe(input) should equal("""{"s":1234}""")
    }

    "stringify byte integers correctly" in {
      val b: Byte = 123
      val input = Json.obj("b" -> b)
      input.toString should equal("""{"b":123}""")
    }

    "stringify boolean correctly" in {
      JsTrue.toString should equal("true")
    }

    "stringify null correctly" in {
      JsNull.toString should equal("null")
    }

    "stringify big decimal correctly" in {
      val n = BigDecimal("12345678901234567890.42")
      val input = Json.obj("bd" -> n)
      input.toString should equal("""{"bd":12345678901234567890.42}""")
    }

    "stringify big decimal with large exponents in scientific notation correctly" in {
      val input = Json.obj("bd" -> BigDecimal("1.2e1000"))
      input.toString should equal("""{"bd":1.2E+1000}""")
    }

    "stringify big decimal with large negative exponents in scientific notation correctly" in {
      val input = Json.obj("bd" -> BigDecimal("6.75e-1000"))
      input.toString should equal("""{"bd":6.75E-1000}""")
    }

    "stringify big decimal with small exponents in scientific notation correctly" in {
      val input = Json.obj("bd" -> BigDecimal("1.234e3"))
      input.toString should equal("""{"bd":1234}""")
    }

    "stringify big decimal with small negative exponents in scientific notation correctly" in {
      val input = Json.obj("bd" -> BigDecimal("1.234e-3"))
      input.toString should equal("""{"bd":0.001234}""")
    }

    "stringify big decimal with integer base correctly" in {
      val input = Json.obj("bd" -> BigDecimal("2e128"))
      input.toString should equal("""{"bd":2E+128}""")
    }

    "map json correctly" in {
      val input = Json.parseStringUnsafe("1330950829160")
      input.map[Json](_ => JsNull) should equal(JsNull)
      JsUndefined.map[Json](_ => JsNull) should equal(JsUndefined)
    }

    "map json object correctly" in {
      Json.obj().map[JsObject](_ => JsNull) should equal(JsNull)
      JsNull.map[JsObject](_ => JsNull) should equal(JsUndefined)
    }

    "map json boolean correctly" in {
      JsTrue.map[Boolean](_ => JsNull) should equal(JsNull)
      JsNull.map[Boolean](_ => JsNull) should equal(JsUndefined)
    }

    "map json big decimal correctly" in {
      JsBigDecimal.fromLiteral(BigDecimal(10D)).map[BigDecimal](_ => JsNull) should equal(JsNull)
      JsNull.map[BigDecimal](_ => JsNull) should equal(JsUndefined)
    }

    "map json string correctly" in {
      JsString.fromLiteral("test").map[String](_ => JsNull) should equal(JsNull)
      JsNull.map[String](_ => JsNull) should equal(JsUndefined)
    }

    "map json byte string correctly" in {
      JsBytes.fromLiteral(ByteString("test")).map[ByteString](_ => JsNull) should equal(JsNull)
      JsNull.map[ByteString](_ => JsNull) should equal(JsUndefined)
    }

    "map json undefined correctly" in {
      JsUndefined.map[Json](_ => JsNull) should equal(JsUndefined)
    }

    "flatMap json correctly" in {
      val input = Json.parseStringUnsafe("1330950829160")
      input.flatMap[Json](_ => JsNull) should equal(JsNull)
      JsUndefined.flatMap[Json](_ => JsNull) should equal(JsUndefined)
    }

    "flatMap json array correctly" in {
      Json.arr().flatMap[JsArray](_ => JsNull) should equal(JsNull)
      JsNull.flatMap[JsArray](_ => JsNull) should equal(JsUndefined)
    }

    "flatMap json object correctly" in {
      Json.obj().flatMap[JsObject](_ => JsNull) should equal(JsNull)
      JsNull.flatMap[JsObject](_ => JsNull) should equal(JsUndefined)
    }

    "flatMap json boolean correctly" in {
      JsTrue.flatMap[Boolean](_ => JsNull) should equal(JsNull)
      JsNull.flatMap[Boolean](_ => JsNull) should equal(JsUndefined)
    }

    "flatMap json int correctly" in {
      JsInt.fromLiteral(10).flatMap[Int](_ => JsNull) should equal(JsNull)
      JsNull.flatMap[Int](_ => JsNull) should equal(JsUndefined)
    }

    "flatMap json long correctly" in {
      JsLong.fromLiteral(10L).flatMap[Long](_ => JsNull) should equal(JsNull)
      JsNull.flatMap[Long](_ => JsNull) should equal(JsUndefined)
    }

    "flatMap json float correctly" in {
      JsFloat.fromLiteral(10F).flatMap[Float](_ => JsNull) should equal(JsNull)
      JsNull.flatMap[Float](_ => JsNull) should equal(JsUndefined)
    }

    "flatMap json double correctly" in {
      JsDouble.fromLiteral(10D).flatMap[Double](_ => JsNull) should equal(JsNull)
      JsNull.flatMap[Double](_ => JsNull) should equal(JsUndefined)
    }

    "flatMap json big decimal correctly" in {
      JsBigDecimal.fromLiteral(BigDecimal(10D)).flatMap[BigDecimal](_ => JsNull) should equal(JsNull)
      JsNull.flatMap[BigDecimal](_ => JsNull) should equal(JsUndefined)
    }

    "flatMap json string correctly" in {
      JsString.fromLiteral("test").flatMap[String](_ => JsNull) should equal(JsNull)
      JsNull.flatMap[String](_ => JsNull) should equal(JsUndefined)
    }

    "flatMap json byte string correctly" in {
      JsBytes.fromLiteral(ByteString("test")).flatMap[ByteString](_ => JsNull) should equal(JsNull)
      JsNull.flatMap[ByteString](_ => JsNull) should equal(JsUndefined)
    }

    "flatMap json undefined correctly" in {
      JsUndefined.flatMap[Json](_ => JsNull) should equal(JsUndefined)
    }

    "be convert to option" in {
      Json.obj().toOption should equal(Some(Json.obj()))
    }

    "get json correctly" in {
      val input = Json.parseStringUnsafe("1330950829160")
      input.get[Json] should equal(JsLong.fromLiteral(1330950829160L))
    }

    "get json array correctly" in {
      Json.arr().get[JsArray] should equal(Json.arr())
    }

    "get json object correctly" in {
      Json.obj().get[JsObject] should equal(Json.obj())
    }

    "get json number correctly" in {
      JsInt.fromLiteral(10).get[JsNumber] should equal(JsInt.fromLiteral(10))
    }

    "get boolean correctly" in {
      JsTrue.get[Boolean] should equal(true)
      JsFalse.get[Boolean] should equal(false)
    }

    "get int correctly" in {
      JsInt.fromLiteral(10).get[Int] should equal(10)
    }

    "get long correctly" in {
      JsLong.fromLiteral(10L).get[Long] should equal(10)
    }

    "get float correctly" in {
      JsFloat.fromLiteral(10F).get[Float] should equal(10F)
    }

    "get double correctly" in {
      JsDouble.fromLiteral(10D).get[Double] should equal(10D)
    }

    "get big decimal correctly" in {
      JsBigDecimal.fromLiteral(BigDecimal(10D)).get[BigDecimal] should equal(BigDecimal(10D))
    }

    "get string correctly" in {
      JsString.fromLiteral("test").get[String] should equal("test")
    }

    "get byte string correctly" in {
      JsBytes.fromLiteral(ByteString("test")).get[ByteString] should equal(ByteString("test"))
    }

    "get json undefined correctly" in {
      assertThrows[NoSuchElementException] {
        JsUndefined.get[Json] should equal(JsUndefined)
      }
    }

    "getOrElse json correctly" in {
      JsLong.fromLiteral(1330950829160L).getOrElse[Json](JsNull) should equal(JsLong.fromLiteral(1330950829160L))
      JsUndefined.getOrElse[Json](JsNull) should equal(JsNull)
    }

    "getOrElse json array correctly" in {
      Json.arr().getOrElse[JsArray](Json.arr("foobar")) should equal(Json.arr())
      JsNull.getOrElse[JsArray](Json.arr("foobar")) should equal(Json.arr("foobar"))
    }

    "getOrElse json object correctly" in {
      Json.obj().getOrElse[JsObject](Json.obj("foo" -> "bar")) should equal(Json.obj())
      JsNull.getOrElse[JsObject](Json.obj("foo" -> "bar")) should equal(Json.obj("foo" -> "bar"))
    }

    "getOrElse json number correctly" in {
      JsInt.fromLiteral(10).getOrElse[JsNumber](JsInt.fromLiteral(0)) should equal(JsInt.fromLiteral(10))
      JsNull.getOrElse[JsNumber](JsInt.fromLiteral(0)) should equal(JsInt.fromLiteral(0))
    }

    "getOrElse boolean correctly" in {
      JsTrue.getOrElse[Boolean](false) should equal(true)
      JsFalse.getOrElse[Boolean](true) should equal(false)
      JsNull.getOrElse[Boolean](true) should equal(true)
    }

    "getOrElse int correctly" in {
      JsInt.fromLiteral(10).getOrElse[Int](0) should equal(10)
      JsNull.getOrElse[Int](0) should equal(0)
    }

    "getOrElse long correctly" in {
      JsLong.fromLiteral(10L).getOrElse[Long](0) should equal(10)
      JsNull.getOrElse[Long](0) should equal(0)
    }

    "getOrElse float correctly" in {
      JsFloat.fromLiteral(10F).getOrElse[Float](0F) should equal(10F)
      JsNull.getOrElse[Float](0F) should equal(0F)
    }

    "getOrElse double correctly" in {
      JsDouble.fromLiteral(10D).getOrElse[Double](0D) should equal(10D)
      JsNull.getOrElse[Double](0D) should equal(0D)
    }

    "getOrElse big decimal correctly" in {
      JsBigDecimal.fromLiteral(BigDecimal(10D)).getOrElse[BigDecimal](BigDecimal(0)) should equal(BigDecimal(10D))
      JsNull.getOrElse[BigDecimal](0) should equal(BigDecimal(0))
    }

    "getOrElse string correctly" in {
      JsString.fromLiteral("test").getOrElse[String]("") should equal("test")
      JsNull.getOrElse[String]("") should equal("")
    }

    "getOrElse byte string correctly" in {
      JsBytes.fromLiteral(ByteString("test")).getOrElse[ByteString](ByteString.empty) should equal(ByteString("test"))
      JsNull.getOrElse[ByteString](ByteString.empty) should equal(ByteString.empty)
    }

    "getOrElse json undefined correctly" in {
      JsNull.getOrElse[Json](JsNull) should equal(JsNull)
    }

    "process if exists json correctly" in {
      Json.obj().ifExists[Json](_ => JsNull)
    }

    "process if exists json array correctly" in {
      Json.arr().ifExists[JsArray](_ => JsNull)
    }

    "process if exists json object correctly" in {
      Json.obj().ifExists[JsObject](_ => JsNull)
    }

    "process if exists json boolean correctly" in {
      JsTrue.ifExists[Boolean](_ => JsNull)
    }

    "process if exists json int correctly" in {
      JsInt.fromLiteral(10).ifExists[Int](_ => JsNull)
    }

    "process if exists json long correctly" in {
      JsLong.fromLiteral(10L).ifExists[Long](_ => JsNull)
    }

    "process if exists json float correctly" in {
      JsFloat.fromLiteral(10F).ifExists[Float](_ => JsNull)
    }

    "process if exists json double correctly" in {
      JsDouble.fromLiteral(10D).ifExists[Double](_ => JsNull)
    }

    "process if exists json big decimal correctly" in {
      JsBigDecimal.fromLiteral(BigDecimal(10D)).ifExists[BigDecimal](_ => JsNull)
    }

    "process if exists json string correctly" in {
      JsString.fromLiteral("test").ifExists[String](_ => JsNull)
    }

    "process if exists json byte string correctly" in {
      JsBytes.fromLiteral(ByteString("test")).ifExists[ByteString](_ => JsNull)
    }

    "process if exists json undefined correctly" in {
      JsUndefined.ifExists[Json](_ => JsNull)
    }

    "be used with a predicate for validation" in {
      val input = Json.parseStringUnsafe("1330950829160")
      input.exists(v => v.isDefined) should equal(true)
    }

    "parse long integers correctly" in {
      val input = Json.parseStringUnsafe("1330950829160")
      input should equal(JsLong.fromLiteral(1330950829160L))
    }

    "parse short integers correctly" in {
      val input = Json.parseStringUnsafe("1234")
      input should equal(JsInt.fromLiteral(1234))
    }

    "parse byte integers correctly" in {
      val input = Json.parseStringUnsafe("123")
      input should equal(JsInt.fromLiteral(123))
    }

    "parse big decimal correctly" in {
      val input = Json.parseStringUnsafe("12345678901234567890.42")
      input should equal(JsBigDecimal.fromLiteral(BigDecimal("12345678901234567890.42")))
    }

    "parse big decimal with large exponents in scientific notation correctly" in {
      val input = Json.parseStringUnsafe("1.2e1000")
      input should equal(JsBigDecimal.fromLiteral(BigDecimal("1.2e1000")))
    }

    "parse big decimal with large negative exponents in scientific notation correctly" in {
      val input = Json.parseStringUnsafe("6.75e-1000")
      input should equal(JsBigDecimal.fromLiteral(BigDecimal("6.75e-1000")))
    }

    "parse big decimal with small exponents in scientific notation correctly" in {
      val input = Json.parseStringUnsafe("1.234e3")
      input should equal(JsBigDecimal.fromLiteral(BigDecimal("1.234e3")))
    }

    "parse big decimal with small negative exponents in scientific notation correctly" in {
      val input = Json.parseStringUnsafe("1.234e-3")
      input should equal(JsBigDecimal.fromLiteral(BigDecimal("1.234e-3")))
    }

    "parse big decimal with integer base correctly" in {
      val input = Json.parseStringUnsafe("2e128")
      input should equal(JsBigDecimal.fromLiteral(BigDecimal("2e128")))
    }

    "parse list correctly" in {
      val input = Json.parseStringUnsafe("""["123",123,2E+128]""")
      input should equal(Json.arr("123", 123, BigDecimal("2e128")))
    }

    "parse null values in object" in {
      val input = Json.parseStringUnsafe("""{"foo": null}""")
      input should equal(Json.obj("foo" -> JsNull))
    }

    "parse null values in array" in {
      val input = Json.parseStringUnsafe("""[null]""")
      input should equal(Json.arr(JsNull))
    }

    "parse null as JsNull" in {
      val input = Json.parseStringUnsafe("""null""")
      input should equal(JsNull)
    }

    "parse json object from bytes" in {
      val input = Json.parseBytesUnsafe("""{"test":"test"}""".getBytes)
      input should equal(Json.obj("test" -> "test"))
    }

    "handle parsing failure from bytes" in {
      val input = Json.parseBytes("""test:"test"""".getBytes).getOrElse(JsUndefined)
      input should equal(JsUndefined)
    }

    "parse json object from bytestring" in {
      val input = Json.parseByteStringUnsafe(ByteString("""{"test":"test"}"""))
      input should equal(Json.obj("test" -> "test"))
    }

    "handle parsing failure from bytestring" in {
      val input = Json.parseByteString(ByteString("""test:"test"""")).getOrElse(JsNull)
      input should equal(JsNull)
    }

    "handle patch with operations" in {
      val input = Json.obj(
        "simple" -> "test",
        "obj" -> Json.obj(
          "test" -> "test"
        ),
        "arr" -> Json.arr("1")
      )

      val result = input.patch(
        Add(Root / "simple", "foobar"),
        Add(Root / "obj" / "test", "foobar"),
        Replace(Root / "arr" / 0, "foobar"),
      )

      input should equal(Json.obj(
        "simple" -> "test",
        "obj" -> Json.obj(
          "test" -> "test"
        ),
        "arr" -> Json.arr("1")
      ))
      result should equal(Json.obj(
        "simple" -> "foobar",
        "obj" -> Json.obj(
          "test" -> "foobar"
        ),
        "arr" -> Json.arr("foobar")
      ))
    }

    "handle patch with seq operations" in {
      val input = Json.obj("foobar" -> "foobar")
      val result = input.patch(Seq(
        Remove(Root / "foobar" / "test" / "test", mustExist = false),
        Replace(Root / "foobar", "test")
      ))
      result should equal(Json.obj(
        "foobar" -> "test"
      ))
    }

    "handle mutate on json object" in {
      val input = Json.obj(
        "simple" -> "test",
        "obj" -> Json.obj(
          "test" -> "test"
        ),
        "arr" -> Json.arr("1")
      )

      val result = input.mutate[JsObject](Root / "obj") { _ =>
        Json.obj("foo" -> "bar", "test" -> "test")
      }.mutate[JsArray](Root / "arr") { _ =>
        Json.arr("foobar")
      }.mutate[Json](Root / "simple") { _ =>
        "foobar"
      }

      input should equal(Json.obj(
        "simple" -> "test",
        "obj" -> Json.obj(
          "test" -> "test"
        ),
        "arr" -> Json.arr("1")
      ))
      result should equal(Json.obj(
        "simple" -> "foobar",
        "obj" -> Json.obj(
          "test" -> "test",
          "foo" -> "bar"
        ),
        "arr" -> Json.arr("foobar")
      ))
    }

    "handle mutate on json value" in {
      Json.obj().mutate[Json](Root)(identity[Json]) should equal(Json.obj())
    }

    "provide a way to build json object" in {
      val builder = Json.objectBuilder()
      builder += "foobar" -> 1
      builder ++= Seq(
        "foobar" -> 3,
        "foobar2" -> 2
      )
      builder.result() should equal(Json.obj(
        "foobar" -> 3,
        "foobar2" -> 2
      ))
      builder.merge(Json.objectBuilder().addOne("foobar3" -> 1)).result() should equal(Json.obj(
        "foobar" -> 3,
        "foobar2" -> 2,
        "foobar3" -> 1
      ))
      if (builder.knownSize >= 0) {
        builder.clear()
      }
    }

    "provide a way to build json array" in {
      val builder = Json.arrayBuilder()
      builder += "foobar"
      builder ++= Seq(1, 2)
      builder.result() should equal(Json.arr("foobar", 1, 2))
      builder.merge(Json.arrayBuilder().addOne("foobar3")).result() should equal(Json.arr(
        "foobar",
        1, 2,
        "foobar3"
      ))
      if (builder.knownSize >= 0) {
        builder.clear()
      }
      builder.result() should equal(Json.arr())
    }
  }

}