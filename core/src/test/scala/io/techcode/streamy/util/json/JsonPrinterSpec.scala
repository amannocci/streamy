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
      JsonPrinter(JsFalse).print().get should equal("false")
    }

    "print an json boolean set to true correctly" in {
      JsonPrinter(JsTrue).print().get should equal("true")
    }

    "print an json null correctly" in {
      JsonPrinter(JsNull).print().get should equal("null")
    }

    "print an json int correctly" in {
      JsonPrinter(JsInt(Int.MinValue)).print().get should equal(Int.MinValue.toString)
    }

    "print an json long correctly" in {
      JsonPrinter(JsLong(Long.MinValue)).print().get should equal(Long.MinValue.toString)
    }

    "print an json float correctly" in {
      JsonPrinter(JsFloat(Float.MinValue)).print().get should equal(Float.MinValue.toString)
    }

    "print an json double correctly" in {
      JsonPrinter(JsDouble(Double.MinValue)).print().get should equal(Double.MinValue.toString)
    }

    "print an json big decimal correctly" in {
      JsonPrinter(JsBigDecimal(BigDecimal("1e20"))).print().get should equal("1E+20")
    }

    "print an json bytes correctly" in {
      JsonPrinter(JsBytes(ByteString("foobar"))).print().get should equal("\"Zm9vYmFy\"")
    }

    "print an json string without unicode char correctly" in {
      JsonPrinter(JsString("foobar")).print().get should equal("\"foobar\"")
    }

    "print an json string with \" char correctly" in {
      JsonPrinter(JsString("foob\"ar")).print().get should equal(""""foob\"ar"""")
    }

    "print an json string with \\ char correctly" in {
      JsonPrinter(JsString("foob\\\\ar")).print().get should equal(""""foob\\\\ar"""")
    }

    "print an json string with control chars correctly" in {
      JsonPrinter(JsString("\b\f\n\r\t")).print().get should equal(""""\b\f\n\r\t"""")
    }

  }

}
