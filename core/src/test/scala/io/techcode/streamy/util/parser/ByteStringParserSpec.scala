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
package io.techcode.streamy.util.parser

import akka.util.ByteString
import io.techcode.streamy.util.json.{JsObjectBuilder, Json}
import io.techcode.streamy.util.printer.PrintException
import io.techcode.streamy.util.{IntBinder, NoneBinder, StringBinder}
import org.scalatest._

/**
  * ByteString parser spec.
  */
// scalastyle:off
class ByteStringParserSpec extends WordSpecLike with Matchers {

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

    "return bytestring partition" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = {
          mark()
          str("foo")
          partition().asString() should equal("foo")
          true
        }
      }
      parser.parse(ByteString("foobar"))
    }

    "return current cursor position" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = true
      }
      parser.cursor() should equal(0)
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
          capture(
            str("foo"),
            StringBinder("test")(_)
          )
        }
      }
      parser.parse(ByteString("foobar")) should equal(Right(Json.obj("test" -> "foo")))
    }

    "not capture a value if undefined" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = {
          capture(
            str("foo"),
            NoneBinder(_),
          )
        }
      }
      parser.parse(ByteString("foobar")) should equal(Right(Json.obj()))
    }

    "not capture properly an optional value if present" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = {
          capture(
            zeroOrMore(str("1")),
            IntBinder("foobar")(_),
            optional = true
          )
        }
      }
      parser.parse(ByteString("foobar")) should equal(Right(Json.obj()))
    }

    "not capture properly a value if absent" in {
      val parser = new ByteStringParserImpl() {
        override def root(): Boolean = {
          capture(
            zeroOrMore(str("1")),
            IntBinder("foobar")(_)
          )
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
          times(1) {
            str("foo")
          }
        }
      }
      parser.parse(ByteString("foo")).isRight should equal(true)
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
          override def root(): Boolean = capture(str("foo"), StringBinder("foo")(_))
        }

        override def root(): Boolean = subParser[ByteStringParserImpl](subParsing)(_.root()) && capture(str("bar"), StringBinder("bar")(_))
      }
      parser.parse(ByteString("foobar")) should equal(Right(Json.obj("foo" -> "foo", "bar" -> "bar")))
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

abstract class ByteStringParserImpl extends ByteStringParser[Json] {

  implicit var builder: JsObjectBuilder = Json.objectBuilder()

  def run(): Json = {
    if (root()) {
      builder.result()
    } else {
      throw new ParseException(s"Unexpected input at index ${cursor()}")
    }
  }

  override def cleanup(): Unit = {
    super.cleanup()
    builder = Json.objectBuilder()
  }

  override def merge[T <: Parser[ByteString, Json]](parser: T): Unit = {
    super.merge(parser)
    parser match {
      case p: ByteStringParserImpl => builder ++= p.builder.result().fields
    }
  }

}
