/*
 * The MIT License (MIT)
 * <p>
 * Copyright (C) 2017-2019
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
import io.techcode.streamy.util.lang.CharBuilder
import org.scalatest._

/**
  * Binder spec.
  */
class BinderSpec extends WordSpecLike with Matchers {

  "String binder" should {
    "convert correctly a boolean in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      StringBinder("foobar")(true)
      builder.result() should equal(Json.obj("foobar" -> JsString("true")))
    }

    "convert correctly an int in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      StringBinder("foobar")(1)
      builder.result() should equal(Json.obj("foobar" -> JsString("1")))
    }

    "convert correctly a long in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      StringBinder("foobar")(1L)
      builder.result() should equal(Json.obj("foobar" -> JsString("1")))
    }

    "convert correctly a float in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      StringBinder("foobar")(1.0F)
      builder.result() should equal(Json.obj("foobar" -> JsString("1.0")))
    }

    "convert correctly a double in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      StringBinder("foobar")(1.0D)
      builder.result() should equal(Json.obj("foobar" -> JsString("1.0")))
    }

    "convert correctly a string in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      StringBinder("foobar")("test")
      builder.result() should equal(Json.obj("foobar" -> JsString("test")))
    }

    "convert correctly a bytestring in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      StringBinder("foobar")(ByteString("test"))
      builder.result() should equal(Json.obj("foobar" -> JsString("test")))
    }

    "convert correctly a json value in bytestring" in {
      implicit val builder = ByteString.newBuilder
      StringBinder("foobar").applyByteString(Json.obj("foobar" -> "test"))
      builder.result() should equal(ByteString("test"))
    }

    "fail to convert a json value in bytestring" in {
      implicit val builder = ByteString.newBuilder
      StringBinder("foobar").applyByteString(Json.obj("foobar" -> 1))
      builder.result() should equal(ByteString.empty)
    }

    "convert correctly a json value in string" in {
      implicit val builder = new CharBuilder
      StringBinder("foobar").applyString(Json.obj("foobar" -> "test"))
      builder.toString should equal("test")
    }

    "fail to convert a json value in string" in {
      implicit val builder = new CharBuilder
      StringBinder("foobar").applyString(Json.obj("foobar" -> 1))
      builder.toString should equal("")
    }
  }

  "Int binder" should {
    "convert correctly a boolean in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      IntBinder("foobar")(true)
      builder.result() should equal(Json.obj("foobar" -> JsInt(1)))
    }

    "convert correctly an int in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      IntBinder("foobar")(1)
      builder.result() should equal(Json.obj("foobar" -> JsInt(1)))
    }

    "convert correctly a long in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      IntBinder("foobar")(1L)
      builder.result() should equal(Json.obj("foobar" -> JsInt(1)))
    }

    "convert correctly a float in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      IntBinder("foobar")(1.0F)
      builder.result() should equal(Json.obj("foobar" -> JsInt(1)))
    }

    "convert correctly a double in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      IntBinder("foobar")(1.0D)
      builder.result() should equal(Json.obj("foobar" -> JsInt(1)))
    }

    "convert correctly a string in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      IntBinder("foobar")("1")
      builder.result() should equal(Json.obj("foobar" -> JsInt(1)))
    }

    "fail to convert a string in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      IntBinder("foobar")("notInt")
      builder.result() should equal(Json.obj())
    }

    "convert correctly a bytestring in mapping" in {
      implicit val builder = Json.objectBuilder()
      IntBinder("foobar")(ByteString("1"))
      builder.result() should equal(Json.obj("foobar" -> JsInt(1)))
    }

    "fail to convert a bytestring in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      IntBinder("foobar")(ByteString("notInt"))
      builder.result() should equal(Json.obj())
    }

    "convert correctly a json value in bytestring" in {
      implicit val builder = ByteString.newBuilder
      IntBinder("foobar").applyByteString(Json.obj("foobar" -> JsInt(1)))
      builder.result() should equal(ByteString("1"))
    }

    "fail to convert a json value in bytestring" in {
      implicit val builder = ByteString.newBuilder
      IntBinder("foobar").applyByteString(Json.obj("foobar" -> "1"))
      builder.result() should equal(ByteString.empty)
    }

    "convert correctly a json value in string" in {
      implicit val builder = new CharBuilder
      IntBinder("foobar").applyString(Json.obj("foobar" -> JsInt(1)))
      builder.toString should equal("1")
    }

    "fail to convert a json value in string" in {
      implicit val builder = new CharBuilder
      IntBinder("foobar").applyString(Json.obj("foobar" -> "1"))
      builder.toString should equal("")
    }

  }

  "Long binder" should {
    "convert correctly a boolean in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      LongBinder("foobar")(true)
      builder.result() should equal(Json.obj("foobar" -> JsLong(1)))
    }

    "convert correctly an int in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      LongBinder("foobar")(1)
      builder.result() should equal(Json.obj("foobar" -> JsLong(1)))
    }

    "convert correctly a long in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      LongBinder("foobar")(1L)
      builder.result() should equal(Json.obj("foobar" -> JsLong(1)))
    }

    "convert correctly a float in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      LongBinder("foobar")(1.0F)
      builder.result() should equal(Json.obj("foobar" -> JsLong(1)))
    }

    "convert correctly a double in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      LongBinder("foobar")(1.0D)
      builder.result() should equal(Json.obj("foobar" -> JsLong(1)))
    }

    "convert correctly a string in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      LongBinder("foobar")("1")
      builder.result() should equal(Json.obj("foobar" -> JsLong(1)))
    }

    "fail to convert a string in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      LongBinder("foobar")("notLong")
      builder.result() should equal(Json.obj())
    }

    "convert correctly a bytestring in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      LongBinder("foobar")(ByteString("1"))
      builder.result() should equal(Json.obj("foobar" -> JsLong(1)))
    }

    "fail to convert a bytestring in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      LongBinder("foobar")(ByteString("notLong"))
      builder.result() should equal(Json.obj())
    }

    "convert correctly a json value in bytestring" in {
      implicit val builder = ByteString.newBuilder
      LongBinder("foobar").applyByteString(Json.obj("foobar" -> JsLong(1)))
      builder.result() should equal(ByteString("1"))
    }

    "fail to convert a json value in bytestring" in {
      implicit val builder = ByteString.newBuilder
      LongBinder("foobar").applyByteString(Json.obj("foobar" -> "1"))
      builder.result() should equal(ByteString.empty)
    }

    "convert correctly a json value in string" in {
      implicit val builder = new CharBuilder
      LongBinder("foobar").applyString(Json.obj("foobar" -> JsLong(1)))
      builder.toString should equal("1")
    }

    "fail to convert a json value in string" in {
      implicit val builder = new CharBuilder
      LongBinder("foobar").applyString(Json.obj("foobar" -> "1"))
      builder.toString should equal("")
    }

  }

  "Float binder" should {
    "convert correctly a boolean in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      FloatBinder("foobar")(true)
      builder.result() should equal(Json.obj("foobar" -> JsFloat(1)))
    }

    "convert correctly an int in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      FloatBinder("foobar")(1)
      builder.result() should equal(Json.obj("foobar" -> JsFloat(1)))
    }

    "convert correctly a long in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      FloatBinder("foobar")(1L)
      builder.result() should equal(Json.obj("foobar" -> JsFloat(1)))
    }

    "convert correctly a float in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      FloatBinder("foobar")(1.0F)
      builder.result() should equal(Json.obj("foobar" -> JsFloat(1.0F)))
    }

    "convert correctly a double in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      FloatBinder("foobar")(1.0D)
      builder.result() should equal(Json.obj("foobar" -> JsFloat(1.0F)))
    }

    "convert correctly a string in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      FloatBinder("foobar")("1")
      builder.result() should equal(Json.obj("foobar" -> JsFloat(1)))
    }

    "fail to convert a string in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      FloatBinder("foobar")("notFloat")
      builder.result() should equal(Json.obj())
    }

    "convert correctly a bytestring in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      FloatBinder("foobar")(ByteString("1.0"))
      builder.result() should equal(Json.obj("foobar" -> JsFloat(1)))
    }

    "fail to convert a bytestring in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      FloatBinder("foobar")(ByteString("notFloat"))
      builder.result() should equal(Json.obj())
    }

    "convert correctly a json value in bytestring" in {
      implicit val builder = ByteString.newBuilder
      FloatBinder("foobar").applyByteString(Json.obj("foobar" -> JsFloat(1)))
      builder.result() should equal(ByteString("1.0"))
    }

    "fail to convert a json value in bytestring" in {
      implicit val builder = ByteString.newBuilder
      FloatBinder("foobar").applyByteString(Json.obj("foobar" -> "1"))
      builder.result() should equal(ByteString.empty)
    }

    "convert correctly a json value in string" in {
      implicit val builder = new CharBuilder
      FloatBinder("foobar").applyString(Json.obj("foobar" -> JsFloat(1)))
      builder.toString should equal("1.0")
    }

    "fail to convert a json value in string" in {
      implicit val builder = new CharBuilder
      FloatBinder("foobar").applyString(Json.obj("foobar" -> "1"))
      builder.toString should equal("")
    }

  }

  "Double binder" should {
    "convert correctly a boolean in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      DoubleBinder("foobar")(true)
      builder.result() should equal(Json.obj("foobar" -> JsDouble(1)))
    }

    "convert correctly an int in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      DoubleBinder("foobar")(1)
      builder.result() should equal(Json.obj("foobar" -> JsDouble(1)))
    }

    "convert correctly a long in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      DoubleBinder("foobar")(1L)
      builder.result() should equal(Json.obj("foobar" -> JsDouble(1)))
    }

    "convert correctly a float in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      DoubleBinder("foobar")(1.0F)
      builder.result() should equal(Json.obj("foobar" -> JsDouble(1.0F)))
    }

    "convert correctly a double in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      DoubleBinder("foobar")(1.0D)
      builder.result() should equal(Json.obj("foobar" -> JsDouble(1.0F)))
    }

    "convert correctly a string in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      DoubleBinder("foobar")("1.0")
      builder.result() should equal(Json.obj("foobar" -> JsDouble(1)))
    }

    "fail to convert a string in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      DoubleBinder("foobar")("notDouble")
      builder.result() should equal(Json.obj())
    }

    "convert correctly a bytestring in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      DoubleBinder("foobar")(ByteString("1"))
      builder.result() should equal(Json.obj("foobar" -> JsDouble(1)))
    }

    "fail to convert a bytestring in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      DoubleBinder("foobar")("notDouble")
      builder.result() should equal(Json.obj())
    }

    "convert correctly a json value in bytestring" in {
      implicit val builder = ByteString.newBuilder
      DoubleBinder("foobar").applyByteString(Json.obj("foobar" -> JsDouble(1)))
      builder.result() should equal(ByteString("1.0"))
    }

    "fail to convert a json value in bytestring" in {
      implicit val builder = ByteString.newBuilder
      DoubleBinder("foobar").applyByteString(Json.obj("foobar" -> "1"))
      builder.result() should equal(ByteString.empty)
    }

    "convert correctly a json value in string" in {
      implicit val builder = new CharBuilder
      DoubleBinder("foobar").applyString(Json.obj("foobar" -> JsDouble(1)))
      builder.toString should equal("1.0")
    }

    "fail to convert a json value in string" in {
      implicit val builder = new CharBuilder
      DoubleBinder("foobar").applyString(Json.obj("foobar" -> "1"))
      builder.toString should equal("")
    }

  }

  "Bytes binder" should {
    "convert correctly a boolean in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      BytesBinder("foobar")(true)
      builder.result() should equal(Json.obj("foobar" -> JsBytes(ByteString("true"))))
    }

    "convert correctly an int in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      BytesBinder("foobar")(1)
      builder.result() should equal(Json.obj("foobar" -> JsBytes(ByteString("1"))))
    }

    "convert correctly a long in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      BytesBinder("foobar")(1L)
      builder.result() should equal(Json.obj("foobar" -> JsBytes(ByteString("1"))))
    }

    "convert correctly a float in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      BytesBinder("foobar")(1.0F)
      builder.result() should equal(Json.obj("foobar" -> JsBytes(ByteString("1.0"))))
    }

    "convert correctly a double in mapping" in {
      implicit val builder = Json.objectBuilder()
      BytesBinder("foobar")(1.0D)
      builder.result() should equal(Json.obj("foobar" -> JsBytes(ByteString("1.0"))))
    }

    "convert correctly a string in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      BytesBinder("foobar")("test")
      builder.result() should equal(Json.obj("foobar" -> JsBytes(ByteString("test"))))
    }

    "convert correctly a bytestring in mapping" in {
      implicit val builder: JsObjectBuilder = Json.objectBuilder()
      BytesBinder("foobar")(ByteString("test"))
      builder.result() should equal(Json.obj("foobar" -> JsBytes(ByteString("test"))))
    }

    "convert correctly a json value in bytestring" in {
      implicit val builder = ByteString.newBuilder
      BytesBinder("foobar").applyByteString(Json.obj("foobar" -> JsBytes(ByteString("test"))))
      builder.result() should equal(ByteString("test"))
    }

    "fail to convert a json value in bytestring" in {
      implicit val builder = ByteString.newBuilder
      BytesBinder("foobar").applyByteString(Json.obj("foobar" -> "1"))
      builder.result() should equal(ByteString.empty)
    }

    "convert correctly a json value in string" in {
      implicit val builder = new CharBuilder
      BytesBinder("foobar").applyString(Json.obj("foobar" -> JsBytes(ByteString("test"))))
      builder.toString should equal("test")
    }

    "fail to convert a json value in string" in {
      implicit val builder = new CharBuilder
      BytesBinder("foobar").applyString(Json.obj("foobar" -> "1"))
      builder.toString should equal("")
    }

  }

}
