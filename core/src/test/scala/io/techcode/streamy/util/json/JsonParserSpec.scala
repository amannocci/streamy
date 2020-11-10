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
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

import scala.util.control.NonFatal

/**
  * Json parser spec.
  */
class JsonParserSpec extends AnyWordSpecLike with Matchers {

  "Json parser" should {
    "parse an json boolean set to false correctly" in {
      except("false", JsFalse)
    }

    "parse an json boolean set to true correctly" in {
      except("true", JsTrue)
    }

    "parse an json null correctly" in {
      except("null", JsNull)
    }

    "parse an json int zero correctly" in {
      except("0", JsInt(0))
    }

    "parse an json int correctly" in {
      except("-12345678", JsInt(-12345678))
    }

    "parse an json long correctly" in {
      except(Long.MinValue.toString, JsLong(Long.MinValue))
    }

    "parse an json float correctly" in {
      except(Float.MinValue.toString, JsBigDecimal(BigDecimal(Float.MinValue.toString)))
    }

    "parse an json float 1.23 correctly" in {
      except("1.23", JsBigDecimal(BigDecimal("1.23")))
    }

    "parse an json double correctly" in {
      except(Double.MinValue.toString, JsBigDecimal(BigDecimal(Double.MinValue.toString)))
    }

    "parse an json big decimal correctly" in {
      except("1e20", JsBigDecimal(BigDecimal("1e20")))
    }

    "parse an json big decimal -1E10 correctly" in {
      except("-1E10", JsBigDecimal(BigDecimal("-1E10")))
    }

    "parse an json big decimal 12.34e-10 correctly" in {
      except("12.34e-10", JsBigDecimal(BigDecimal("1.234E-9")))
    }

    "parse an json string correctly" in {
      except(""""foobar"""", JsString("foobar"))
    }

    "parse an json string from encoded bytes correctly" in {
      except("\"4-byte UTF-8 chars like \uD801\uDC37\"", JsString("4-byte UTF-8 chars like \uD801\uDC37"))
    }

    "parse an json string escape correctly" in {
      except(""""\"\\/\b\f\n\r\t"""", JsString("\"\\/\b\f\n\r\t"))
      except("\"L\\" + "u00e4nder\"", JsString("Länder"))
    }

    "parse an json string of the slash (SOLIDUS) character correctly" in {
      except("\"" + "/\\/\\u002f" + "\"", JsString("///"))
    }

    "parse a json object correctly" in {
      except("""{"string":"string","int":10,"float":1.0}""", Json.obj(
        "string" -> "string",
        "int" -> 10,
        "float" -> BigDecimal(1.0F)
      ))
    }

    "parse a json array correctly" in {
      except("""[null, 1.23, {"key":true }]""", Json.arr(
        JsNull,
        JsBigDecimal.fromLiteral(BigDecimal("1.23")),
        Json.obj("key" -> JsTrue)
      ))
    }

    "parse directly from UTF-8 encoded bytes" in {
      val json = Json.obj(
        "7-bit" -> "This is regular 7-bit ASCII text.",
        "2-bytes" -> "2-byte UTF-8 chars like £, æ or Ö",
        "3-bytes" -> "3-byte UTF-8 chars like ﾖ, ᄅ or ᐁ.",
        "4-bytes" -> "4-byte UTF-8 chars like \uD801\uDC37, \uD852\uDF62 or \uD83D\uDE01."
      )
      except(json.toString, json)
    }

    "parse directly from UTF-8 encoded bytes when string starts with a multi-byte character" in {
      except(""""£0.99"""", JsString("£0.99"))
      except(""""飞机因此受到损伤"""", JsString("飞机因此受到损伤"))
      except(""""เอสds飞机hu เอฟ到a ซ'ีเ$นม่า เอ็#ม损บีเ00因0ค เซ็นเbตอร์"""", JsString("เอสds飞机hu เอฟ到a ซ'ีเ$นม่า เอ็#ม损บีเ00因0ค เซ็นเbตอร์"))
    }

    "parse a json object with whitespace correctly" in {
      except("""           {   "foo" :        "bar" , "key"    :   true   }""", Json.obj(
        "foo" -> "bar",
        "key" -> JsTrue
      ))
    }

    "parse a json array with whitespace correctly" in {
      except("""               [   {"foo" : "bar"} ,  null   ]""", Json.arr(
        Json.obj("foo" -> "bar"),
        JsNull
      ))
    }

    "be reentrant" in {
      val byteStringParser = JsonParser.byteStringParser()
      for (_ <- 0 to 5) {
        byteStringParser.parse(ByteString("""{"string":"string","int":10,"float":1.0}""")) should equal(Right(Json.obj(
          "string" -> "string",
          "int" -> 10,
          "float" -> BigDecimal(1.0F)
        )))
      }
      val stringParser = JsonParser.stringParser()
      for (_ <- 0 to 5) {
        stringParser.parse("""{"string":"string","int":10,"float":1.0}""") should equal(Right(Json.obj(
          "string" -> "string",
          "int" -> 10,
          "float" -> BigDecimal(1.0F)
        )))
      }
    }

    "fail gracefully for deeply nested structures" in {
      val queue = new java.util.ArrayDeque[String]()

      // testing revealed that each recursion will need approx. 280 bytes of stack space
      val depth = 1500
      val runnable = new Runnable {
        override def run(): Unit =
          try {
            val nested = "[{\"key\":" * (depth / 2)
            val result = JsonParser.stringParser().parse(nested)
            result match {
              case Left(ex) => queue.push(s"nonfatal: ${ex.getMessage}")
              case Right(_) => queue.push("didn't fail")
            }
          } catch {
            case _: StackOverflowError => queue.push("stackoverflow")
            case NonFatal(e) => queue.push(s"nonfatal: ${e.getMessage}")
          }
      }

      val thread = new Thread(null, runnable, "parser-test", 655360)
      thread.start()
      thread.join()
      queue.peek() === "nonfatal: JSON input was nested more deeply than the configured limit of maxNesting = 1000"
    }

    "fail to parse invalid json object" in {
      assertThrows[ParseException] {
        except("""{"query":{"match_all"}""", JsNull)
      }
    }

    "fail to parse control char" in {
      exceptError(""""\u0000"""")
      exceptError(""""\u0001"""")
      exceptError(""""\u0010"""")
      exceptError(""""\u001e"""")
    }

    "fail to parse bad escape" in {
      exceptError(""""\a"""")
    }
  }

  /**
    * Except all parsers to parse an input into an output.
    *
    * @param input  input to parse.
    * @param output excepted output.
    */
  private def except(input: String, output: Json): Assertion = {
    Json.parseByteStringUnsafe(ByteString(input)) should equal(output)
    Json.parseStringUnsafe(input) should equal(output)
  }

  /**
    * Except all parsers to parse an input and throw an parsing exception.
    *
    * @param input input to parse.
    */
  private def exceptError(input: String): Assertion = {
    assertThrows[ParseException] {
      Json.parseByteStringUnsafe(ByteString(input))
    }
    assertThrows[ParseException] {
      Json.parseStringUnsafe(input)
    }
  }

}