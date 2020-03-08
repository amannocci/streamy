/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2018
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

/**
  * Json printer spec.
  */
class JsonPrinterSpec extends WordSpecLike with Matchers {

  "Json printer" should {
    "print an json boolean set to false correctly" in {
      Json.printStringUnsafe(JsFalse) should equal("false")
    }

    "print an json boolean set to true correctly" in {
      Json.printStringUnsafe(JsTrue) should equal("true")
    }

    "print an json null correctly" in {
      Json.printStringUnsafe(JsNull) should equal("null")
    }

    "print an json int literal correctly" in {
      Json.printStringUnsafe(JsInt.fromLiteral(Int.MinValue)) should equal(Int.MinValue.toString)
    }

    "print an json int string representation correctly" in {
      Json.printStringUnsafe(JsInt.fromStringUnsafe(Int.MinValue.toString)) should equal(Int.MinValue.toString)
    }

    "print an json int bytes representation correctly" in {
      Json.printStringUnsafe(JsInt.fromByteStringUnsafe(ByteString(Int.MinValue.toString))) should equal(Int.MinValue.toString)
    }

    "print an json long literal correctly" in {
      Json.printStringUnsafe(JsLong.fromLiteral(Long.MinValue)) should equal(Long.MinValue.toString)
    }

    "print an json long string representation correctly" in {
      Json.printStringUnsafe(JsLong.fromStringUnsafe(Long.MinValue.toString)) should equal(Long.MinValue.toString)
    }

    "print an json long bytes representation correctly" in {
      Json.printStringUnsafe(JsLong.fromByteStringUnsafe(ByteString(Long.MinValue.toString))) should equal(Long.MinValue.toString)
    }

    "print an json float literal correctly" in {
      Json.printStringUnsafe(JsFloat.fromLiteral(Float.MinValue)) should equal(Float.MinValue.toString)
    }

    "print an json float string representation correctly" in {
      Json.printStringUnsafe(JsFloat.fromStringUnsafe(Float.MinValue.toString)) should equal(Float.MinValue.toString)
    }

    "print an json float bytes representation correctly" in {
      Json.printStringUnsafe(JsFloat.fromByteStringUnsafe(ByteString(Float.MinValue.toString))) should equal(Float.MinValue.toString)
    }

    "print an json double literal correctly" in {
      Json.printStringUnsafe(JsDouble.fromLiteral(Double.MinValue)) should equal(Double.MinValue.toString)
    }

    "print an json double string representation correctly" in {
      Json.printStringUnsafe(JsDouble.fromStringUnsafe(Double.MinValue.toString)) should equal(Double.MinValue.toString)
    }

    "print an json double bytes representation correctly" in {
      Json.printStringUnsafe(JsDouble.fromByteStringUnsafe(ByteString(Double.MinValue.toString))) should equal(Double.MinValue.toString)
    }

    "print an json big decimal literal correctly" in {
      Json.printStringUnsafe(JsBigDecimal.fromLiteral(BigDecimal("1e20"))) should equal("1E+20")
    }

    "print an json big decimal string representation correctly" in {
      Json.printStringUnsafe(JsBigDecimal.fromStringUnsafe(BigDecimal("1e20").toString())) should equal("1E+20")
    }

    "print an json big decimal bytes representation correctly" in {
      Json.printStringUnsafe(JsBigDecimal.fromByteStringUnsafe(ByteString(BigDecimal("1e20").toString()))) should equal("1E+20")
    }

    "print an json bytes literal correctly" in {
      Json.printStringUnsafe(JsBytes.fromLiteral(ByteString("foobar"))) should equal("\"Zm9vYmFy\"")
    }

    "print an json bytes string representation correctly" in {
      Json.printStringUnsafe(JsBytes.fromStringUnsafe("foobar")) should equal("\"Zm9vYmFy\"")
    }

    "print an json string literal without unicode char correctly" in {
      Json.printStringUnsafe(JsString.fromLiteral("foobar")) should equal("\"foobar\"")
    }

    "print an json string literal with \" char correctly" in {
      Json.printStringUnsafe(JsString.fromLiteral("foob\"ar")) should equal(""""foob\"ar"""")
    }

    "print an json string literal with \\ char correctly" in {
      Json.printStringUnsafe(JsString.fromLiteral("foob\\\\ar")) should equal(""""foob\\\\ar"""")
    }

    "print an json string literal with control chars correctly" in {
      Json.printStringUnsafe(JsString.fromLiteral("\b\f\n\r\t")) should equal(""""\b\f\n\r\t"""")
    }

    "print an json string bytes representation without unicode char correctly" in {
      Json.printStringUnsafe(JsString.fromByteStringUnsafe(ByteString("foobar"))) should equal("\"foobar\"")
    }

    "print an json string bytes representation with \" char correctly" in {
      Json.printStringUnsafe(JsString.fromByteStringUnsafe(ByteString("foob\"ar"))) should equal(""""foob\"ar"""")
    }

    "print an json string bytes representation with \\ char correctly" in {
      Json.printStringUnsafe(JsString.fromByteStringUnsafe(ByteString("foob\\\\ar"))) should equal(""""foob\\\\ar"""")
    }

    "print an json string bytes representation with control chars correctly" in {
      Json.printStringUnsafe(JsString.fromByteStringUnsafe(ByteString("\b\f\n\r\t"))) should equal(""""\b\f\n\r\t"""")
    }

  }

}
