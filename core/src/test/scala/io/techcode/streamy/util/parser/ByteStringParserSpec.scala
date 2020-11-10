/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017-2020
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
import io.techcode.streamy.util.json.{JsInt, JsObjectBuilder, JsString, Json}
import io.techcode.streamy.util.printer.PrintException
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

/**
  * ByteString parser spec.
  */
// scalastyle:off
class ByteStringParserSpec extends AnyWordSpecLike with Matchers {

  "ByteString parser" should {
    "compute correctly error message for input" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = false
      }
      parser.parse(ByteString("foobar")) should equal(Left(new ParseException("Unexpected input at index 0")))
    }

    "return current character based on cursor" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = {
          mark()
          ch('f')
          unmark()
          current() should equal('f')
          true
        }
      }
      parser.parse(ByteString("foobar"))
    }

    "return current cursor position" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = true
      }
      parser.cursor should equal(0)
    }

    "return current cursor position after skip and unskip" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = skip(2) && skip() && unskip(2) && unskip()
      }
      parser.cursor should equal(0)
    }

    "return slice of data" in {
      var result = ByteString.empty
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = {
          mark()
          str("foo")
          result = slice()
          true
        }
      }
      parser.parse(ByteString("foobar"))
      result should equal(ByteString("foo"))
    }

    "detect end of input" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = eoi()
      }
      parser.parse(ByteString("")).isRight should equal(true)
    }

    "capture properly a value if present" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = {
          capture(str("foo")) { value =>
            builder.bind("test", JsString.fromByteStringUnsafe(value))
          }
        }
      }
      parser.parse(ByteString("foobar")) should equal(Right(Json.obj("test" -> "foo")))
    }

    "capture properly a value if present and bind is defined" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = {
          capture(Option("test"), str("foo")) { (key, value) =>
            builder.bind(key, JsString.fromByteStringUnsafe(value))
          }
        }
      }
      parser.parse(ByteString("foobar")) should equal(Right(Json.obj("test" -> "foo")))
    }

    "capture properly an optional value if present and bind is defined" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = {
          captureOptional(Option("test"), str("foo")) { (key, value) =>
            builder.bind(key, JsString.fromByteStringUnsafe(value))
          }
        }
      }
      parser.parse(ByteString("foobar")) should equal(Right(Json.obj("test" -> "foo")))
    }

    "not capture a value if undefined" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = {
          capture(str("foo")) { _ =>
            true
          }
        }
      }
      parser.parse(ByteString("foobar")) should equal(Right(Json.obj()))
    }

    "not capture properly an optional value if present" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = {
          captureOptional(oneOrMore(str("1"))) { value =>
            builder.bind("foobar", JsInt.fromByteStringUnsafe(value))
          }
        }
      }
      parser.parse(ByteString("foobar")) should equal(Right(Json.obj()))
    }

    "not capture properly an optional value if present and bind is defined" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = {
          captureOptional(Option("bind"), oneOrMore(str("1"))) { (key, value) =>
            builder.bind(key, JsInt.fromByteStringUnsafe(value))
          }
        }
      }
      parser.parse(ByteString("foobar")) should equal(Right(Json.obj()))
    }

    "not capture properly a value if absent" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = {
          capture(zeroOrMore(str("1"))) { _ =>
            false
          }
        }
      }
      parser.parse(ByteString("foobar")).toOption should equal(None)
    }

    "process a character if possible" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = ch('f')
      }
      parser.parse(ByteString("foobar")).isRight should equal(true)
    }

    "process a character if impossible" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = ch('c')
      }
      parser.parse(ByteString("foobar")).isRight should equal(false)
    }

    "process a characters sequence if possible" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = str("foo")
      }
      parser.parse(ByteString("foobar")).isRight should equal(true)
    }

    "process a characters sequence if impossible" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = str("foa")
      }
      parser.parse(ByteString("foobar")).isRight should equal(false)
    }

    "process a characters sequence by skipping if not enough characters are remaining" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = str("foobart")
      }
      parser.parse(ByteString("foobar")).isRight should equal(false)
    }

    "process an utf-8 characters sequence if possible" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = utf8 {
          str("ABCDEFGHIJKLMNOPQRSTUVWXYZ /0123456789abcdefghijklmnopqrstuvwxyz £©µÀÆÖÞßéöÿ–—‘“”„†•…‰™œŠŸž€ ΑΒΓΔΩαβγδω АБВГДабвгд∀∂∈ℝ∧∪≡∞ ↑↗↨↻⇣ ┐┼╔╘░►☺♀ ﬁ�⑀₂ἠḂӥẄɐː⍎אԱა\uD841\uDF0E")
        }
      }
      parser.parse(ByteString("ABCDEFGHIJKLMNOPQRSTUVWXYZ /0123456789abcdefghijklmnopqrstuvwxyz £©µÀÆÖÞßéöÿ–—‘“”„†•…‰™œŠŸž€ ΑΒΓΔΩαβγδω АБВГДабвгд∀∂∈ℝ∧∪≡∞ ↑↗↨↻⇣ ┐┼╔╘░►☺♀ ﬁ�⑀₂ἠḂӥẄɐː⍎אԱა\uD841\uDF0E")).isRight should equal(true)
    }

    "process an utf-8 characters sequence if impossible" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = utf8 {
          str("�")
        }
      }
      parser.parse(ByteString(0xf8, 0xa1, 0xa1, 0xa1, 0xa1)).isRight should equal(true)
    }

    "process a number of time a char matcher if possible" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = {
          times(1, CharMatchers.All)
        }
      }
      parser.parse(ByteString("f")).isRight should equal(true)
    }

    "process a number of time a char matcher if impossible" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = {
          times(2, CharMatchers.Digit)
        }
      }
      parser.parse(ByteString("1foobar")).isRight should equal(false)
    }

    "process a number of time a char matcher by skipping if not enough characters are remaining" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = {
          times(4, CharMatchers.Digit)
        }
      }
      parser.parse(ByteString("foo")).isRight should equal(false)
    }

    "process a number of time an inner rule if possible" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = {
          times(2) {
            str("foo")
          }
        }
      }
      parser.parse(ByteString("foofoo")).isRight should equal(true)
    }

    "process a number of time an inner rule if impossible" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = {
          times(2) {
            str("foo")
          }
        }
      }
      parser.parse(ByteString("foobar")).isRight should equal(false)
    }

    "process a minimum number of time a char matcher if possible" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = {
          times(1, 2, CharMatchers.LowerAlpha)
        }
      }
      parser.parse(ByteString("f1oobar")).isRight should equal(true)
    }

    "process a maximum number of time a char matcher if possible" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = {
          times(1, 2, CharMatchers.LowerAlpha)
        }
      }
      parser.parse(ByteString("fo1obar")).isRight should equal(true)
    }

    "process a number of time in a range a char matcher if impossible" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = {
          times(1, 2, CharMatchers.LowerAlpha)
        }
      }
      parser.parse(ByteString("1foobar")).isRight should equal(false)
    }

    "process a number of time in a range a char matcher by skipping if there isn't enough remaining characters" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = {
          times(1, 2, CharMatchers.LowerAlpha)
        }
      }
      parser.parse(ByteString.empty).isRight should equal(false)
    }

    "process any character" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = {
          any()
        }
      }
      parser.parse(ByteString("foobar")).isRight should equal(true)
    }

    "process zero or more character using a char matcher if possible" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = {
          zeroOrMore(CharMatchers.LowerAlpha)
        }
      }
      parser.parse(ByteString("foobar")).isRight should equal(true)
    }

    "process zero or more character using a char matcher if impossible" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = {
          zeroOrMore(CharMatchers.Digit)
        }
      }
      parser.parse(ByteString("foobar")).isRight should equal(true)
    }

    "process zero or more character using a inner rule if possible" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = {
          zeroOrMore {
            str("foo")
          }
        }
      }
      parser.parse(ByteString("foobar")).isRight should equal(true)
    }

    "process zero or more character using an inner rule if impossible" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = {
          zeroOrMore {
            str("123")
          }
        }
      }
      parser.parse(ByteString("foobar")).isRight should equal(true)
    }

    "process one or more character using a char matcher if possible" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = {
          oneOrMore(CharMatchers.LowerAlpha)
        }
      }
      parser.parse(ByteString("foobar")).isRight should equal(true)
    }

    "process one or more character using a char matcher if impossible" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = {
          oneOrMore(CharMatchers.Digit)
        }
      }
      parser.parse(ByteString("foobar")).isRight should equal(false)
    }

    "process one or more character using an inner rule if possible" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = {
          oneOrMore {
            str("foo")
          }
        }
      }
      parser.parse(ByteString("foobar")).isRight should equal(true)
    }

    "process one or more character using an inner rule if impossible" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = {
          oneOrMore {
            str("123")
          }
        }
      }
      parser.parse(ByteString("foobar")).isRight should equal(false)
    }

    "process an optional inner rule if possible" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = {
          optional {
            str("foo")
          }
        }
      }
      parser.parse(ByteString("foobar")).isRight should equal(true)
    }

    "process an optional inner rule if impossible" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = {
          optional {
            str("123")
          }
        }
      }
      parser.parse(ByteString("foobar")).isRight should equal(true)
    }

    "process correctly first inner rule if possible" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = or(
          str("foo"),
          str("123")
        )
      }
      parser.parse(ByteString("foobar")).isRight should equal(true)
    }

    "process correctly second inner rule if possible" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = or(
          str("123"),
          str("foo")
        )
      }
      parser.parse(ByteString("foobar")).isRight should equal(true)
    }

    "process correctly when or rule is impossible" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = or(
          str("123"),
          str("123")
        )
      }
      parser.parse(ByteString("foobar")).isRight should equal(false)
    }

    "process correctly when using sub parser when possible" in {
      val parser: ByteStringParserImpl = new ByteStringParserImpl() {
        val subParsing: ByteStringParserImpl {
          def root(): Boolean
        } = new ByteStringParserImpl {
          override def root(): Boolean = str("foo")
        }

        override def root(): Boolean = subParser[ByteStringParserImpl](subParsing)(_.root()) && str("bar")
      }
      parser.parse(ByteString("foobar")).isRight should equal(true)
    }

    "process correctly when using sub parser when impossible" in {
      val parser: ByteStringParserImpl = new ByteStringParserImpl() {
        val subParsing: ByteStringParserImpl {
          def root(): Boolean
        } = new ByteStringParserImpl {
          override def root(): Boolean = str("bar")
        }

        override def root(): Boolean = subParser[ByteStringParserImpl](subParsing)(_.root()) && str("bar")
      }
      parser.parse(ByteString("foobar")).isRight should equal(false)
    }

    "process correctly when using sub parser and capture" in {
      val parser: ByteStringParserImpl = new ByteStringParserImpl() {
        val subParsing: ByteStringParserImpl {
          def root(): Boolean
        } = new ByteStringParserImpl {
          override def root(): Boolean = capture(str("foo")) { value =>
            builder.bind("foo", JsString.fromByteStringUnsafe(value))
          }
        }

        override def root(): Boolean = subParser[ByteStringParserImpl](subParsing)(_.root()) && capture(str("bar")) { value =>
          builder.bind("bar", JsString.fromByteStringUnsafe(value))
        }
      }
      parser.parse(ByteString("foobar")) should equal(Right(Json.obj("foo" -> "foo", "bar" -> "bar")))
    }

    "process correctly when using stack" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = {
          capture {
            or(
              str("123"),
              str("123")
            )
          } { x =>
            stack.push(x)
            true
          }
        }
      }
      parser.parse(ByteString("123123")).isRight should equal(true)
    }

    "read byte correctly" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = readByte() == 1
      }
      parser.parse(ByteString(1)).isRight should equal(true)
    }

    "read byte padded correctly" in {
      var output: Byte = -1
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = readBytePadded() == output
      }
      parser.parse(ByteString.empty).isRight should equal(true)
      output = 1
      parser.parse(ByteString(1)).isRight should equal(true)
    }

    "read double bytes correctly" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = readDoubleByte() == (1 << 8).toChar
      }
      parser.parse(ByteString(1, 0)).isRight should equal(true)
    }

    "read double bytes padded correctly" in {
      var output: Char = '\uffff'
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = readDoubleBytePadded() == output
      }
      parser.parse(ByteString.empty).isRight should equal(true)
      output = 511.toChar
      parser.parse(ByteString(1)).isRight should equal(true)
      output = (1 << 8).toChar
      parser.parse(ByteString(1, 0)).isRight should equal(true)
    }

    "read quad bytes correctly" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = readQuadByte() == (1 << 24)
      }
      parser.parse(ByteString(1, 0, 0, 0)).isRight should equal(true)
    }

    "read quad bytes padded correctly" in {
      var output: Int = 0xFFFFFFFF
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = readQuadBytePadded() == output
      }
      parser.parse(ByteString.empty).isRight should equal(true)
      output = 33554431
      parser.parse(ByteString(1)).isRight should equal(true)
      output = 16842751
      parser.parse(ByteString(1, 0)).isRight should equal(true)
      output = 16777471
      parser.parse(ByteString(1, 0, 0)).isRight should equal(true)
      output = 16777216
      parser.parse(ByteString(1, 0, 0, 0)).isRight should equal(true)
    }

    "read octa bytes correctly" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = readOctaByte() == (1L << 56)
      }
      parser.parse(ByteString(1, 0, 0, 0, 0, 0, 0, 0)).isRight should equal(true)
    }

    "read octa bytes padded correctly" in {
      var output: Long = 0xFFFFFFFFFFFFFFFFL
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = readOctaBytePadded() == output
      }
      parser.parse(ByteString.empty).isRight should equal(true)
      output = 144115188075855871L
      parser.parse(ByteString(1)).isRight should equal(true)
      output = 72339069014638591L
      parser.parse(ByteString(1, 0)).isRight should equal(true)
      output = 72058693549555711L
      parser.parse(ByteString(1, 0, 0)).isRight should equal(true)
      output = 72057598332895231L
      parser.parse(ByteString(1, 0, 0, 0)).isRight should equal(true)
      output = 72057594054705151L
      parser.parse(ByteString(1, 0, 0, 0, 0)).isRight should equal(true)
      output = 72057594037993471L
      parser.parse(ByteString(1, 0, 0, 0, 0, 0)).isRight should equal(true)
      output = 72057594037928191L
      parser.parse(ByteString(1, 0, 0, 0, 0, 0, 0)).isRight should equal(true)
      output = 72057594037927936L
      parser.parse(ByteString(1, 0, 0, 0, 0, 0, 0, 0)).isRight should equal(true)
    }
  }

  "Parser exception" should {
    "implement correctly equality" in {
      new ParseException("foobar") should equal(new ParseException("foobar"))
      new ParseException("foobar") should not equal(new PrintException("foobar"))
    }
  }

}

// scalastyle:on

abstract class ByteStringParserImpl extends ByteStringParser[Json] with Utf8ByteStringParser[Json] {

  implicit var builder: JsObjectBuilder = Json.objectBuilder()

  def run(): Json = {
    if (root()) {
      builder.result()
    } else {
      throw new ParseException(s"Unexpected input at index ${cursor}")
    }
  }

  override def cleanup(): Unit = {
    super.cleanup()
    builder = Json.objectBuilder()
  }

  override def merge[T <: Parser[ByteString, Json]](parser: T): Boolean = {
    super.merge(parser)
    parser match {
      case p: ByteStringParserImpl => builder ++= p.builder.result().fields
    }
    true
  }

}
