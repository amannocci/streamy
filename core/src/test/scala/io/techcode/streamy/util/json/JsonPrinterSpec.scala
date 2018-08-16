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
      JsonPrinter().print(JsFalse).getOrElse("") should equal("false")
    }

    "print an json boolean set to true correctly" in {
      JsonPrinter().print(JsTrue).getOrElse("") should equal("true")
    }

    "print an json null correctly" in {
      JsonPrinter().print(JsNull).getOrElse("") should equal("null")
    }

    "print an json int correctly" in {
      JsonPrinter().print(JsInt(Int.MinValue)).getOrElse("") should equal(Int.MinValue.toString)
    }

    "print an json long correctly" in {
      JsonPrinter().print(JsLong(Long.MinValue)).getOrElse("") should equal(Long.MinValue.toString)
    }

    "print an json float correctly" in {
      JsonPrinter().print(JsFloat(Float.MinValue)).getOrElse("") should equal(Float.MinValue.toString)
    }

    "print an json double correctly" in {
      JsonPrinter().print(JsDouble(Double.MinValue)).getOrElse("") should equal(Double.MinValue.toString)
    }

    "print an json big decimal correctly" in {
      JsonPrinter().print(JsBigDecimal(BigDecimal("1e20"))).getOrElse("") should equal("1E+20")
    }

    "print an json bytes correctly" in {
      JsonPrinter().print(JsBytes(ByteString("foobar"))).getOrElse("") should equal("\"Zm9vYmFy\"")
    }

    "print an json string without unicode char correctly" in {
      JsonPrinter().print(JsString("foobar")).getOrElse("") should equal("\"foobar\"")
    }

    "print an json string with \" char correctly" in {
      JsonPrinter().print(JsString("foob\"ar")).getOrElse("") should equal(""""foob\"ar"""")
    }

    "print an json string with \\ char correctly" in {
      JsonPrinter().print(JsString("foob\\\\ar")).getOrElse("") should equal(""""foob\\\\ar"""")
    }

    "print an json string with control chars correctly" in {
      JsonPrinter().print(JsString("\b\f\n\r\t")).getOrElse("") should equal(""""\b\f\n\r\t"""")
    }

  }

}
