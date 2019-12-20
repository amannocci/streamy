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
import com.typesafe.config.ConfigFactory
import org.scalatest._
import pureconfig._
import pureconfig.generic.auto._

/**
  * JsonImplicit spec.
  */
class JsonImplicitSpec extends WordSpecLike with Matchers {

  "JsonImplicit" should {
    "support pureconfig integration" in {
      case class Test(
        doc: Json,
        pointer: JsonPointer
      )
      val test = loadConfigOrThrow[Test](ConfigFactory.parseString("""{"doc":"{\"test\":\"test\"}", "pointer":"/key"}"""))
      test.doc should equal(Json.obj("test" -> "test"))
      test.pointer should equal(Root / "key")
    }

    "provide a shortcut to convert json in string" in {
      jsonToString(Json.obj("test" -> "test")) should equal("""{"test":"test"}""")
    }

    "provide a shortcut to convert string in json" in {
      stringToJson("""{"test":"test"}""") should equal(JsString("""{"test":"test"}"""))
    }

    "provide a shortcut to convert float in json" in {
      floatToJson(2.0F) should equal(JsFloat(2.0F))
    }

    "provide a shortcut to convert double in json" in {
      doubleToJson(2.0D) should equal(JsDouble(2.0D))
    }

    "provide a shortcut to convert byte in json" in {
      byteToJson(2) should equal(JsInt(2))
    }

    "provide a shortcut to convert short in json" in {
      shortToJson(2) should equal(JsInt(2))
    }

    "provide a shortcut to convert int in json" in {
      intToJson(2) should equal(JsInt(2))
    }

    "provide a shortcut to convert long in json" in {
      longToJson(2L) should equal(JsLong(2L))
    }

    "provide a shortcut to convert boolean in json" in {
      booleanToJson(true) should equal(JsTrue)
    }

    "provide a shortcut to convert byte string in json" in {
      byteStringToJson(ByteString.empty) should equal(JsBytes(ByteString.empty))
    }

    "provide a shortcut to convert big decimal in json" in {
      bigDecimalToJson(BigDecimal.valueOf(0)) should equal(JsBigDecimal(BigDecimal.valueOf(0)))
    }
  }

}
