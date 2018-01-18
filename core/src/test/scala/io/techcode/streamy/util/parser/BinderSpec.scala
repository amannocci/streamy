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
package io.techcode.streamy.util.parser

import akka.util.ByteString
import io.techcode.streamy.util.json.{JsBytes, JsDouble, JsFloat, JsInt, JsLong, JsString}
import org.scalatest._

/**
  * Binder spec.
  */
class BinderSpec extends WordSpecLike with Matchers {

  "String binder" should {
    "convert correctly a boolean in mapping" in {
      StringBinder("foobar").bind(true) should equal(JsString("true"))
    }

    "convert correctly an int in mapping" in {
      StringBinder("foobar").bind(1) should equal(JsString("1"))
    }

    "convert correctly a long in mapping" in {
      StringBinder("foobar").bind(1L) should equal(JsString("1"))
    }

    "convert correctly a float in mapping" in {
      StringBinder("foobar").bind(1.0F) should equal(JsString("1.0"))
    }

    "convert correctly a double in mapping" in {
      StringBinder("foobar").bind(1.0D) should equal(JsString("1.0"))
    }

    "convert correctly a string in mapping" in {
      StringBinder("foobar").bind("test") should equal(Some(JsString("test")))
    }

    "convert correctly a bytestring in mapping" in {
      StringBinder("foobar").bind(ByteString("test")) should equal(Some(JsString("test")))
    }

    "convert correctly a json value in bytestring" in {
      StringBinder("foobar").bind(JsString("test")) should equal(ByteString("test"))
    }
  }

  "Int binder" should {
    "convert correctly a boolean in mapping" in {
      IntBinder("foobar").bind(true) should equal(JsInt(1))
    }

    "convert correctly an int in mapping" in {
      IntBinder("foobar").bind(1) should equal(JsInt(1))
    }

    "convert correctly a long in mapping" in {
      IntBinder("foobar").bind(1L) should equal(JsInt(1))
    }

    "convert correctly a float in mapping" in {
      IntBinder("foobar").bind(1.0F) should equal(JsInt(1))
    }

    "convert correctly a double in mapping" in {
      IntBinder("foobar").bind(1.0D) should equal(JsInt(1))
    }

    "convert correctly a string in mapping" in {
      IntBinder("foobar").bind("1") should equal(Some(JsInt(1)))
    }

    "fail to convert a string in mapping" in {
      IntBinder("foobar").bind("notInt") should equal(None)
    }

    "convert correctly a bytestring in mapping" in {
      IntBinder("foobar").bind(ByteString("1")) should equal(Some(JsInt(1)))
    }

    "fail to convert a bytestring in mapping" in {
      IntBinder("foobar").bind(ByteString("notInt")) should equal(None)
    }

    "convert correctly a json value in bytestring" in {
      IntBinder("foobar").bind(JsInt(1)) should equal(ByteString("1"))
    }
  }

  "Long binder" should {
    "convert correctly a boolean in mapping" in {
      LongBinder("foobar").bind(true) should equal(JsLong(1))
    }

    "convert correctly an int in mapping" in {
      LongBinder("foobar").bind(1) should equal(JsLong(1))
    }

    "convert correctly a long in mapping" in {
      LongBinder("foobar").bind(1L) should equal(JsLong(1))
    }

    "convert correctly a float in mapping" in {
      LongBinder("foobar").bind(1.0F) should equal(JsLong(1))
    }

    "convert correctly a double in mapping" in {
      LongBinder("foobar").bind(1.0D) should equal(JsLong(1))
    }

    "convert correctly a string in mapping" in {
      LongBinder("foobar").bind("1") should equal(Some(JsLong(1)))
    }

    "fail to convert a string in mapping" in {
      LongBinder("foobar").bind("notLong") should equal(None)
    }

    "convert correctly a bytestring in mapping" in {
      LongBinder("foobar").bind(ByteString("1")) should equal(Some(JsLong(1)))
    }

    "fail to convert a bytestring in mapping" in {
      LongBinder("foobar").bind(ByteString("notLong")) should equal(None)
    }

    "convert correctly a json value in bytestring" in {
      LongBinder("foobar").bind(JsLong(1)) should equal(ByteString("1"))
    }
  }

  "Float binder" should {
    "convert correctly a boolean in mapping" in {
      FloatBinder("foobar").bind(true) should equal(JsFloat(1))
    }

    "convert correctly an int in mapping" in {
      FloatBinder("foobar").bind(1) should equal(JsFloat(1))
    }

    "convert correctly a long in mapping" in {
      FloatBinder("foobar").bind(1L) should equal(JsFloat(1))
    }

    "convert correctly a float in mapping" in {
      FloatBinder("foobar").bind(1.0F) should equal(JsFloat(1.0F))
    }

    "convert correctly a double in mapping" in {
      FloatBinder("foobar").bind(1.0D) should equal(JsFloat(1.0F))
    }

    "convert correctly a string in mapping" in {
      FloatBinder("foobar").bind("1") should equal(Some(JsFloat(1)))
    }

    "fail to convert a string in mapping" in {
      FloatBinder("foobar").bind("notFloat") should equal(None)
    }

    "convert correctly a bytestring in mapping" in {
      FloatBinder("foobar").bind(ByteString("1.0")) should equal(Some(JsFloat(1)))
    }

    "fail to convert a bytestring in mapping" in {
      FloatBinder("foobar").bind(ByteString("notFloat")) should equal(None)
    }

    "convert correctly a json value in bytestring" in {
      FloatBinder("foobar").bind(JsFloat(1)) should equal(ByteString("1.0"))
    }
  }

  "Double binder" should {
    "convert correctly a boolean in mapping" in {
      DoubleBinder("foobar").bind(true) should equal(JsDouble(1))
    }

    "convert correctly an int in mapping" in {
      DoubleBinder("foobar").bind(1) should equal(JsDouble(1))
    }

    "convert correctly a long in mapping" in {
      DoubleBinder("foobar").bind(1L) should equal(JsDouble(1))
    }

    "convert correctly a float in mapping" in {
      DoubleBinder("foobar").bind(1.0F) should equal(JsDouble(1.0F))
    }

    "convert correctly a double in mapping" in {
      DoubleBinder("foobar").bind(1.0D) should equal(JsDouble(1.0F))
    }

    "convert correctly a string in mapping" in {
      DoubleBinder("foobar").bind("1.0") should equal(Some(JsDouble(1)))
    }

    "fail to convert a string in mapping" in {
      DoubleBinder("foobar").bind("notDouble") should equal(None)
    }

    "convert correctly a bytestring in mapping" in {
      DoubleBinder("foobar").bind(ByteString("1")) should equal(Some(JsDouble(1)))
    }

    "fail to convert a bytestring in mapping" in {
      DoubleBinder("foobar").bind("notDouble") should equal(None)
    }

    "convert correctly a json value in bytestring" in {
      DoubleBinder("foobar").bind(JsDouble(1)) should equal(ByteString("1.0"))
    }
  }

  "Bytes binder" should {
    "convert correctly a boolean in mapping" in {
      BytesBinder("foobar").bind(true) should equal(JsBytes(ByteString("true")))
    }

    "convert correctly an int in mapping" in {
      BytesBinder("foobar").bind(1) should equal(JsBytes(ByteString("1")))
    }

    "convert correctly a long in mapping" in {
      BytesBinder("foobar").bind(1L) should equal(JsBytes(ByteString("1")))
    }

    "convert correctly a float in mapping" in {
      BytesBinder("foobar").bind(1.0F) should equal(JsBytes(ByteString("1.0")))
    }

    "convert correctly a double in mapping" in {
      BytesBinder("foobar").bind(1.0D) should equal(JsBytes(ByteString("1.0")))
    }

    "convert correctly a string in mapping" in {
      BytesBinder("foobar").bind("test") should equal(Some(JsBytes(ByteString("test"))))
    }

    "convert correctly a bytestring in mapping" in {
      BytesBinder("foobar").bind(ByteString("test")) should equal(Some(JsBytes(ByteString("test"))))
    }

    "convert correctly a json value in bytestring" in {
      BytesBinder("foobar").bind(JsBytes(ByteString("test"))) should equal(ByteString("test"))
    }
  }

}
