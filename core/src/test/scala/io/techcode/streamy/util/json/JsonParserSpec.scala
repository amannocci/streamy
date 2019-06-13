/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2018-2019
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
import io.techcode.streamy.util.parser.ParseException
import org.scalatest._

/**
  * Json parser spec.
  */
class JsonParserSpec extends WordSpecLike with Matchers {

  "Json parser" should {
    "parse an json boolean set to false correctly" in {
      Json.parseByteStringUnsafe(ByteString("false")) should equal(JsFalse)
    }

    "parse an json boolean set to true correctly" in {
      Json.parseByteStringUnsafe(ByteString("true")) should equal(JsTrue)
    }

    "parse an json null correctly" in {
      Json.parseByteStringUnsafe(ByteString("null")) should equal(JsNull)
    }

    "parse an json int zero correctly" in {
      Json.parseByteStringUnsafe(ByteString("0")) should equal(JsInt(0))
    }

    "parse an json int correctly" in {
      Json.parseByteStringUnsafe(ByteString(Int.MinValue.toString)) should equal(JsInt(Int.MinValue))
    }

    "parse an json long correctly" in {
      Json.parseByteStringUnsafe(ByteString(Long.MinValue.toString)) should equal(JsLong(Long.MinValue))
    }

    "parse an json float correctly" in {
      Json.parseByteStringUnsafe(ByteString(Float.MinValue.toString)) should equal(JsBigDecimal(BigDecimal(Float.MinValue.toString)))
    }

    "parse an json float 1.23 correctly" in {
      Json.parseByteStringUnsafe(ByteString("1.23")) should equal(JsBigDecimal(BigDecimal("1.23")))
    }

    "parse an json double correctly" in {
      Json.parseByteStringUnsafe(ByteString(Double.MinValue.toString)) should equal(JsBigDecimal(BigDecimal(Double.MinValue.toString)))
    }

    "parse an json big decimal correctly" in {
      Json.parseByteStringUnsafe(ByteString("1e20")) should equal(JsBigDecimal(BigDecimal("1e20")))
    }

    "parse an json big decimal -1E10 correctly" in {
      Json.parseByteStringUnsafe(ByteString("-1E10")) should equal(JsBigDecimal(BigDecimal("-1E10")))
    }

    "parse an json big decimal 12.34e-10 correctly" in {
      Json.parseByteStringUnsafe(ByteString("12.34e-10")) should equal(JsBigDecimal(BigDecimal("1.234E-9")))
    }

    "parse an json string correctly" in {
      Json.parseByteStringUnsafe(ByteString(""""foobar"""")) should equal(JsString("foobar"))
    }

    "parse an json string from encoded bytes correctly" in {
      Json.parseByteStringUnsafe(ByteString("\"4-byte UTF-8 chars like \uD801\uDC37\"")) should equal(JsString("4-byte UTF-8 chars like \uD801\uDC37"))
    }

    "parse an json string escape correctly" in {
      Json.parseByteStringUnsafe(ByteString(""""\"\\/\b\f\n\r\t"""")) should equal(JsString("\"\\/\b\f\n\r\t"))
      Json.parseByteStringUnsafe(ByteString("\"L\\" + "u00e4nder\"")) should equal(JsString("Länder"))
    }

    "parse an json string of the slash (SOLIDUS) character correctly" in {
      Json.parseByteStringUnsafe(ByteString("\"" + "/\\/\\u002f" + "\"")) should equal(JsString("///"))
    }

    "parse a json object correctly" in {
      Json.parseByteStringUnsafe(ByteString(
        """{"string":"string","int":10,"float":1.0}"""
      )) should equal(Json.obj(
        "string" -> "string",
        "int" -> 10,
        "float" -> BigDecimal(1.0F)
      ))
    }

    "parse directly from UTF-8 encoded bytes" in {
      val json = Json.obj(
        "7-bit" -> "This is regular 7-bit ASCII text.",
        "2-bytes" -> "2-byte UTF-8 chars like £, æ or Ö",
        "3-bytes" -> "3-byte UTF-8 chars like ﾖ, ᄅ or ᐁ.",
        "4-bytes" -> "4-byte UTF-8 chars like \uD801\uDC37, \uD852\uDF62 or \uD83D\uDE01."
      )
      Json.parseStringUnsafe(json.toString) should equal(json)
    }

    "parse directly from UTF-8 encoded bytes when string starts with a multi-byte character" in {
      Json.parseByteStringUnsafe(ByteString(""""£0.99"""")) should equal(JsString("£0.99"))
    }

    "fail to parse invalid json object" in {
      assertThrows[ParseException] {
        Json.parseStringUnsafe("""{"query":{"match_all"}""")
      }
    }
  }

}