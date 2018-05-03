/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017-2018
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

import akka.util.ByteString
import io.techcode.streamy.util.json._
import org.scalatest._
import java.lang.{StringBuilder => JStringBuilder}

/**
  * Binder spec.
  */
class BinderSpec extends WordSpecLike with Matchers {

  "String binder" should {
    "convert correctly a boolean in mapping" in {
      val builder = Json.objectBuilder()
      StringBinder("foobar").bind(builder, value = true)
      builder.result() should equal(Json.obj("foobar" -> JsString("true")))
    }

    "convert correctly an int in mapping" in {
      val builder = Json.objectBuilder()
      StringBinder("foobar").bind(builder, 1)
      builder.result() should equal(Json.obj("foobar" -> JsString("1")))
    }

    "convert correctly a long in mapping" in {
      val builder = Json.objectBuilder()
      StringBinder("foobar").bind(builder, 1L)
      builder.result() should equal(Json.obj("foobar" -> JsString("1")))
    }

    "convert correctly a float in mapping" in {
      val builder = Json.objectBuilder()
      StringBinder("foobar").bind(builder, 1.0F)
      builder.result() should equal(Json.obj("foobar" -> JsString("1.0")))
    }

    "convert correctly a double in mapping" in {
      val builder = Json.objectBuilder()
      StringBinder("foobar").bind(builder, 1.0D)
      builder.result() should equal(Json.obj("foobar" -> JsString("1.0")))
    }

    "convert correctly a string in mapping" in {
      val builder = Json.objectBuilder()
      StringBinder("foobar").bind(builder, "test")
      builder.result() should equal(Json.obj("foobar" -> JsString("test")))
    }

    "convert correctly a bytestring in mapping" in {
      val builder = Json.objectBuilder()
      StringBinder("foobar").bind(builder, ByteString("test"))
      builder.result() should equal(Json.obj("foobar" -> JsString("test")))
    }

    "convert correctly a json value in bytestring" in {
      val builder = ByteString.newBuilder
      StringBinder("foobar").bind(builder, Json.obj("foobar" -> "test"))()
      builder.result() should equal(ByteString("test"))
    }

    "fail to convert a json value in bytestring" in {
      val builder = ByteString.newBuilder
      StringBinder("foobar").bind(builder, Json.obj("foobar" -> 1))()
      builder.result() should equal(ByteString.empty)
    }

    "convert correctly a json value in string" in {
      val builder = new JStringBuilder
      StringBinder("foobar").bind(builder, Json.obj("foobar" -> "test"))()
      builder.toString should equal("test")
    }

    "fail to convert a json value in string" in {
      val builder = new JStringBuilder
      StringBinder("foobar").bind(builder, Json.obj("foobar" -> 1))()
      builder.toString should equal("")
    }
  }

  "Int binder" should {
    "convert correctly a boolean in mapping" in {
      val builder = Json.objectBuilder()
      IntBinder("foobar").bind(builder, value = true)
      builder.result() should equal(Json.obj("foobar" -> JsInt(1)))
    }

    "convert correctly an int in mapping" in {
      val builder = Json.objectBuilder()
      IntBinder("foobar").bind(builder, 1)
      builder.result() should equal(Json.obj("foobar" -> JsInt(1)))
    }

    "convert correctly a long in mapping" in {
      val builder = Json.objectBuilder()
      IntBinder("foobar").bind(builder, 1L)
      builder.result() should equal(Json.obj("foobar" -> JsInt(1)))
    }

    "convert correctly a float in mapping" in {
      val builder = Json.objectBuilder()
      IntBinder("foobar").bind(builder, 1.0F)
      builder.result() should equal(Json.obj("foobar" -> JsInt(1)))
    }

    "convert correctly a double in mapping" in {
      val builder = Json.objectBuilder()
      IntBinder("foobar").bind(builder, 1.0D)
      builder.result() should equal(Json.obj("foobar" -> JsInt(1)))
    }

    "convert correctly a string in mapping" in {
      val builder = Json.objectBuilder()
      IntBinder("foobar").bind(builder, "1")
      builder.result() should equal(Json.obj("foobar" -> JsInt(1)))
    }

    "fail to convert a string in mapping" in {
      val builder = Json.objectBuilder()
      IntBinder("foobar").bind(builder, "notInt")
      builder.result() should equal(Json.obj())
    }

    "convert correctly a bytestring in mapping" in {
      val builder = Json.objectBuilder()
      IntBinder("foobar").bind(builder, ByteString("1"))
      builder.result() should equal(Json.obj("foobar" -> JsInt(1)))
    }

    "fail to convert a bytestring in mapping" in {
      val builder = Json.objectBuilder()
      IntBinder("foobar").bind(builder, ByteString("notInt"))
      builder.result() should equal(Json.obj())
    }

    "convert correctly a json value in bytestring" in {
      val builder = ByteString.newBuilder
      IntBinder("foobar").bind(builder, Json.obj("foobar" -> JsInt(1)))()
      builder.result() should equal(ByteString("1"))
    }

    "fail to convert a json value in bytestring" in {
      val builder = ByteString.newBuilder
      IntBinder("foobar").bind(builder, Json.obj("foobar" -> "1"))()
      builder.result() should equal(ByteString.empty)
    }

    "convert correctly a json value in string" in {
      val builder = new JStringBuilder
      IntBinder("foobar").bind(builder, Json.obj("foobar" -> JsInt(1)))()
      builder.toString should equal("1")
    }

    "fail to convert a json value in string" in {
      val builder = new JStringBuilder
      IntBinder("foobar").bind(builder, Json.obj("foobar" -> "1"))()
      builder.toString should equal("")
    }

  }

  "Long binder" should {
    "convert correctly a boolean in mapping" in {
      val builder = Json.objectBuilder()
      LongBinder("foobar").bind(builder, value = true)
      builder.result() should equal(Json.obj("foobar" -> JsLong(1)))
    }

    "convert correctly an int in mapping" in {
      val builder = Json.objectBuilder()
      LongBinder("foobar").bind(builder, 1)
      builder.result() should equal(Json.obj("foobar" -> JsLong(1)))
    }

    "convert correctly a long in mapping" in {
      val builder = Json.objectBuilder()
      LongBinder("foobar").bind(builder, 1L)
      builder.result() should equal(Json.obj("foobar" -> JsLong(1)))
    }

    "convert correctly a float in mapping" in {
      val builder = Json.objectBuilder()
      LongBinder("foobar").bind(builder, 1.0F)
      builder.result() should equal(Json.obj("foobar" -> JsLong(1)))
    }

    "convert correctly a double in mapping" in {
      val builder = Json.objectBuilder()
      LongBinder("foobar").bind(builder, 1.0D)
      builder.result() should equal(Json.obj("foobar" -> JsLong(1)))
    }

    "convert correctly a string in mapping" in {
      val builder = Json.objectBuilder()
      LongBinder("foobar").bind(builder, "1")
      builder.result() should equal(Json.obj("foobar" -> JsLong(1)))
    }

    "fail to convert a string in mapping" in {
      val builder = Json.objectBuilder()
      LongBinder("foobar").bind(builder, "notLong")
      builder.result() should equal(Json.obj())
    }

    "convert correctly a bytestring in mapping" in {
      val builder = Json.objectBuilder()
      LongBinder("foobar").bind(builder, ByteString("1"))
      builder.result() should equal(Json.obj("foobar" -> JsLong(1)))
    }

    "fail to convert a bytestring in mapping" in {
      val builder = Json.objectBuilder()
      LongBinder("foobar").bind(builder, ByteString("notLong"))
      builder.result() should equal(Json.obj())
    }

    "convert correctly a json value in bytestring" in {
      val builder = ByteString.newBuilder
      LongBinder("foobar").bind(builder, Json.obj("foobar" -> JsLong(1)))()
      builder.result() should equal(ByteString("1"))
    }

    "fail to convert a json value in bytestring" in {
      val builder = ByteString.newBuilder
      LongBinder("foobar").bind(builder, Json.obj("foobar" -> "1"))()
      builder.result() should equal(ByteString.empty)
    }

    "convert correctly a json value in string" in {
      val builder = new JStringBuilder
      LongBinder("foobar").bind(builder, Json.obj("foobar" -> JsLong(1)))()
      builder.toString should equal("1")
    }

    "fail to convert a json value in string" in {
      val builder = new JStringBuilder
      LongBinder("foobar").bind(builder, Json.obj("foobar" -> "1"))()
      builder.toString should equal("")
    }

  }

  "Float binder" should {
    "convert correctly a boolean in mapping" in {
      val builder = Json.objectBuilder()
      FloatBinder("foobar").bind(builder, value = true)
      builder.result() should equal(Json.obj("foobar" -> JsFloat(1)))
    }

    "convert correctly an int in mapping" in {
      val builder = Json.objectBuilder()
      FloatBinder("foobar").bind(builder, 1)
      builder.result() should equal(Json.obj("foobar" -> JsFloat(1)))
    }

    "convert correctly a long in mapping" in {
      val builder = Json.objectBuilder()
      FloatBinder("foobar").bind(builder, 1L)
      builder.result() should equal(Json.obj("foobar" -> JsFloat(1)))
    }

    "convert correctly a float in mapping" in {
      val builder = Json.objectBuilder()
      FloatBinder("foobar").bind(builder, 1.0F)
      builder.result() should equal(Json.obj("foobar" -> JsFloat(1.0F)))
    }

    "convert correctly a double in mapping" in {
      val builder = Json.objectBuilder()
      FloatBinder("foobar").bind(builder, 1.0D)
      builder.result() should equal(Json.obj("foobar" -> JsFloat(1.0F)))
    }

    "convert correctly a string in mapping" in {
      val builder = Json.objectBuilder()
      FloatBinder("foobar").bind(builder, "1")
      builder.result() should equal(Json.obj("foobar" -> JsFloat(1)))
    }

    "fail to convert a string in mapping" in {
      val builder = Json.objectBuilder()
      FloatBinder("foobar").bind(builder, "notFloat")
      builder.result() should equal(Json.obj())
    }

    "convert correctly a bytestring in mapping" in {
      val builder = Json.objectBuilder()
      FloatBinder("foobar").bind(builder, ByteString("1.0"))
      builder.result() should equal(Json.obj("foobar" -> JsFloat(1)))
    }

    "fail to convert a bytestring in mapping" in {
      val builder = Json.objectBuilder()
      FloatBinder("foobar").bind(builder, ByteString("notFloat"))
      builder.result() should equal(Json.obj())
    }

    "convert correctly a json value in bytestring" in {
      val builder = ByteString.newBuilder
      FloatBinder("foobar").bind(builder, Json.obj("foobar" -> JsFloat(1)))()
      builder.result() should equal(ByteString("1.0"))
    }

    "fail to convert a json value in bytestring" in {
      val builder = ByteString.newBuilder
      FloatBinder("foobar").bind(builder, Json.obj("foobar" -> "1"))()
      builder.result() should equal(ByteString.empty)
    }

    "convert correctly a json value in string" in {
      val builder = new JStringBuilder
      FloatBinder("foobar").bind(builder, Json.obj("foobar" -> JsFloat(1)))()
      builder.toString should equal("1.0")
    }

    "fail to convert a json value in string" in {
      val builder = new JStringBuilder
      FloatBinder("foobar").bind(builder, Json.obj("foobar" -> "1"))()
      builder.toString should equal("")
    }

  }

  "Double binder" should {
    "convert correctly a boolean in mapping" in {
      val builder = Json.objectBuilder()
      DoubleBinder("foobar").bind(builder, value = true)
      builder.result() should equal(Json.obj("foobar" -> JsDouble(1)))
    }

    "convert correctly an int in mapping" in {
      val builder = Json.objectBuilder()
      DoubleBinder("foobar").bind(builder, 1)
      builder.result() should equal(Json.obj("foobar" -> JsDouble(1)))
    }

    "convert correctly a long in mapping" in {
      val builder = Json.objectBuilder()
      DoubleBinder("foobar").bind(builder, 1L)
      builder.result() should equal(Json.obj("foobar" -> JsDouble(1)))
    }

    "convert correctly a float in mapping" in {
      val builder = Json.objectBuilder()
      DoubleBinder("foobar").bind(builder, 1.0F)
      builder.result() should equal(Json.obj("foobar" -> JsDouble(1.0F)))
    }

    "convert correctly a double in mapping" in {
      val builder = Json.objectBuilder()
      DoubleBinder("foobar").bind(builder, 1.0D)
      builder.result() should equal(Json.obj("foobar" -> JsDouble(1.0F)))
    }

    "convert correctly a string in mapping" in {
      val builder = Json.objectBuilder()
      DoubleBinder("foobar").bind(builder, "1.0")
      builder.result() should equal(Json.obj("foobar" -> JsDouble(1)))
    }

    "fail to convert a string in mapping" in {
      val builder = Json.objectBuilder()
      DoubleBinder("foobar").bind(builder, "notDouble")
      builder.result() should equal(Json.obj())
    }

    "convert correctly a bytestring in mapping" in {
      val builder = Json.objectBuilder()
      DoubleBinder("foobar").bind(builder, ByteString("1"))
      builder.result() should equal(Json.obj("foobar" -> JsDouble(1)))
    }

    "fail to convert a bytestring in mapping" in {
      val builder = Json.objectBuilder()
      DoubleBinder("foobar").bind(builder, "notDouble")
      builder.result() should equal(Json.obj())
    }

    "convert correctly a json value in bytestring" in {
      val builder = ByteString.newBuilder
      DoubleBinder("foobar").bind(builder, Json.obj("foobar" -> JsDouble(1)))()
      builder.result() should equal(ByteString("1.0"))
    }

    "fail to convert a json value in bytestring" in {
      val builder = ByteString.newBuilder
      DoubleBinder("foobar").bind(builder, Json.obj("foobar" -> "1"))()
      builder.result() should equal(ByteString.empty)
    }

    "convert correctly a json value in string" in {
      val builder = new JStringBuilder
      DoubleBinder("foobar").bind(builder, Json.obj("foobar" -> JsDouble(1)))()
      builder.toString should equal("1.0")
    }

    "fail to convert a json value in string" in {
      val builder = new JStringBuilder
      DoubleBinder("foobar").bind(builder, Json.obj("foobar" -> "1"))()
      builder.toString should equal("")
    }

  }

  "Bytes binder" should {
    "convert correctly a boolean in mapping" in {
      val builder = Json.objectBuilder()
      BytesBinder("foobar").bind(builder, value = true)
      builder.result() should equal(Json.obj("foobar" -> JsBytes(ByteString("true"))))
    }

    "convert correctly an int in mapping" in {
      val builder = Json.objectBuilder()
      BytesBinder("foobar").bind(builder, 1)
      builder.result() should equal(Json.obj("foobar" -> JsBytes(ByteString("1"))))
    }

    "convert correctly a long in mapping" in {
      val builder = Json.objectBuilder()
      BytesBinder("foobar").bind(builder, 1L)
      builder.result() should equal(Json.obj("foobar" -> JsBytes(ByteString("1"))))
    }

    "convert correctly a float in mapping" in {
      val builder = Json.objectBuilder()
      BytesBinder("foobar").bind(builder, 1.0F)
      builder.result() should equal(Json.obj("foobar" -> JsBytes(ByteString("1.0"))))
    }

    "convert correctly a double in mapping" in {
      val builder = Json.objectBuilder()
      BytesBinder("foobar").bind(builder, 1.0D)
      builder.result() should equal(Json.obj("foobar" -> JsBytes(ByteString("1.0"))))
    }

    "convert correctly a string in mapping" in {
      val builder = Json.objectBuilder()
      BytesBinder("foobar").bind(builder, "test")
      builder.result() should equal(Json.obj("foobar" -> JsBytes(ByteString("test"))))
    }

    "convert correctly a bytestring in mapping" in {
      val builder = Json.objectBuilder()
      BytesBinder("foobar").bind(builder, ByteString("test"))
      builder.result() should equal(Json.obj("foobar" -> JsBytes(ByteString("test"))))
    }

    "convert correctly a json value in bytestring" in {
      val builder = ByteString.newBuilder
      BytesBinder("foobar").bind(builder, Json.obj("foobar" -> JsBytes(ByteString("test"))))()
      builder.result() should equal(ByteString("test"))
    }

    "fail to convert a json value in bytestring" in {
      val builder = ByteString.newBuilder
      BytesBinder("foobar").bind(builder, Json.obj("foobar" -> "1"))()
      builder.result() should equal(ByteString.empty)
    }

    "convert correctly a json value in string" in {
      val builder = new JStringBuilder
      BytesBinder("foobar").bind(builder, Json.obj("foobar" -> JsBytes(ByteString("test"))))()
      builder.toString should equal("test")
    }

    "fail to convert a json value in string" in {
      val builder = new JStringBuilder
      BytesBinder("foobar").bind(builder, Json.obj("foobar" -> "1"))()
      builder.toString should equal("")
    }

  }

}
