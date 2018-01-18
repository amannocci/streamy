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
import io.techcode.streamy.util.json.Json
import org.scalatest._

/**
  * ByteString parser spec.
  */
class ByteStringParserSpec extends WordSpecLike with Matchers {

  "ByteString parser" should {
    "compute correctly error message for empty input" in {
      val parser = new ByteStringParser(ByteString("")) {
        override def process(): Boolean = true
      }
      parser.error() should equal("Unexpected empty input")
    }

    "compute correctly error message for input" in {
      val parser = new ByteStringParser(ByteString("foobar")) {
        override def process(): Boolean = true
      }
      parser.error() should equal("Unexpected 'f' at index 0")
    }

    "return current character based on cursor" in {
      val parser = new ByteStringParser(ByteString("foobar")) {
        override def process(): Boolean = true
      }
      parser.current() should equal('f')
    }

    "return bytestring partition" in {
      val parser = new ByteStringParser(ByteString("foobar")) {
        override def process(): Boolean = {
          mark()
          str("foo")
        }
      }
      parser.process()
      parser.partition().asString() should equal("foo")
    }

    "return current cursor position" in {
      val parser = new ByteStringParser(ByteString("foobar")) {
        override def process(): Boolean = true
      }
      parser.cursor() should equal(0)
    }

    "detect end of input" in {
      val parser = new ByteStringParser(ByteString("")) {
        override def process(): Boolean = true
      }
      parser.eoi() should equal(true)
    }

    "capture properly a value if present" in {
      val parser = new ByteStringParser(ByteString("foobar")) {
        override def process(): Boolean = {
          capture(Some(StringBinder("test"))) {
            str("foo")
          }
        }
      }
      parser.parse() should equal(Some(Json.obj("test" -> "foo")))
    }

    "not capture a value if undefined" in {
      val parser = new ByteStringParser(ByteString("foobar")) {
        override def process(): Boolean = {
          capture(None) {
            str("foo")
          }
        }
      }
      parser.parse() should equal(Some(Json.obj()))
    }

    "not capture properly an optional value if present" in {
      val parser = new ByteStringParser(ByteString("foobar")) {
        override def process(): Boolean = {
          capture(Some(IntBinder("foobar")), optional = true) {
            zeroOrMore(str("1"))
          }
        }
      }
      parser.parse() should equal(Some(Json.obj()))
    }

    "not capture properly a value if absent" in {
      val parser = new ByteStringParser(ByteString("foobar")) {
        override def process(): Boolean = {
          capture(Some(IntBinder("foobar"))) {
            zeroOrMore(str("1"))
          }
        }
      }
      parser.parse() should equal(None)
    }

    "process a character if possible" in {
      val parser = new ByteStringParser(ByteString("foobar")) {
        override def process(): Boolean = ch('f')
      }
      parser.parse().isDefined should equal(true)
    }

    "process a character if impossible" in {
      val parser = new ByteStringParser(ByteString("foobar")) {
        override def process(): Boolean = ch('c')
      }
      parser.parse().isDefined should equal(false)
    }

    "process a characters sequence if possible" in {
      val parser = new ByteStringParser(ByteString("foobar")) {
        override def process(): Boolean = str("foo")
      }
      parser.parse().isDefined should equal(true)
    }

    "process a characters sequence if impossible" in {
      val parser = new ByteStringParser(ByteString("foobar")) {
        override def process(): Boolean = str("foa")
      }
      parser.parse().isDefined should equal(false)
    }

    "process a number of time a char matcher if possible" in {
      val parser = new ByteStringParser(ByteString("foobar")) {
        override def process(): Boolean = {
          times(1, CharMatchers.All)
        }
      }
      parser.parse().isDefined should equal(true)
    }

    "process a number of time a char matcher if impossible" in {
      val parser = new ByteStringParser(ByteString("1foobar")) {
        override def process(): Boolean = {
          times(2, CharMatchers.Digit)
        }
      }
      parser.parse().isDefined should equal(false)
    }

    "process a number of time an inner rule if possible" in {
      val parser = new ByteStringParser(ByteString("foobar")) {
        override def process(): Boolean = {
          times(1) {
            str("foo")
          }
        }
      }
      parser.parse().isDefined should equal(true)
    }

    "process a number of time an inner rule if impossible" in {
      val parser = new ByteStringParser(ByteString("foobar")) {
        override def process(): Boolean = {
          times(2) {
            str("foo")
          }
        }
      }
      parser.parse().isDefined should equal(false)
    }
    "process a minimum number of time a char matcher if possible" in {
      val parser = new ByteStringParser(ByteString("f1oobar")) {
        override def process(): Boolean = {
          times(1, 2, CharMatchers.LowerAlpha)
        }
      }
      parser.parse().isDefined should equal(true)
    }

    "process a maximum number of time a char matcher if possible" in {
      val parser = new ByteStringParser(ByteString("fo1obar")) {
        override def process(): Boolean = {
          times(1, 2, CharMatchers.LowerAlpha)
        }
      }
      parser.parse().isDefined should equal(true)
    }

    "process a number of time in a range a char matcher if impossible" in {
      val parser = new ByteStringParser(ByteString("1foobar")) {
        override def process(): Boolean = {
          times(1, 2, CharMatchers.LowerAlpha)
        }
      }
      parser.parse().isDefined should equal(false)
    }

    "process zero or more character using a char matcher if possible" in {
      val parser = new ByteStringParser(ByteString("foobar")) {
        override def process(): Boolean = {
          zeroOrMore(CharMatchers.LowerAlpha)
        }
      }
      parser.parse().isDefined should equal(true)
    }

    "process zero or more character using a char matcher if impossible" in {
      val parser = new ByteStringParser(ByteString("foobar")) {
        override def process(): Boolean = {
          zeroOrMore(CharMatchers.Digit)
        }
      }
      parser.parse().isDefined should equal(true)
    }

    "process zero or more character using a inner rule if possible" in {
      val parser = new ByteStringParser(ByteString("foobar")) {
        override def process(): Boolean = {
          zeroOrMore {
            str("foo")
          }
        }
      }
      parser.parse().isDefined should equal(true)
    }

    "process zero or more character using an inner rule if impossible" in {
      val parser = new ByteStringParser(ByteString("foobar")) {
        override def process(): Boolean = {
          zeroOrMore {
            str("123")
          }
        }
      }
      parser.parse().isDefined should equal(true)
    }

    "process one or more character using a char matcher if possible" in {
      val parser = new ByteStringParser(ByteString("foobar")) {
        override def process(): Boolean = {
          oneOrMore(CharMatchers.LowerAlpha)
        }
      }
      parser.parse().isDefined should equal(true)
    }

    "process one or more character using a char matcher if impossible" in {
      val parser = new ByteStringParser(ByteString("foobar")) {
        override def process(): Boolean = {
          oneOrMore(CharMatchers.Digit)
        }
      }
      parser.parse().isDefined should equal(false)
    }

    "process one or more character using an inner rule if possible" in {
      val parser = new ByteStringParser(ByteString("foobar")) {
        override def process(): Boolean = {
          oneOrMore {
            str("foo")
          }
        }
      }
      parser.parse().isDefined should equal(true)
    }

    "process one or more character using an inner rule if impossible" in {
      val parser = new ByteStringParser(ByteString("foobar")) {
        override def process(): Boolean = {
          oneOrMore {
            str("123")
          }
        }
      }
      parser.parse().isDefined should equal(false)
    }

    "process an optional inner rule if possible" in {
      val parser = new ByteStringParser(ByteString("foobar")) {
        override def process(): Boolean = {
          optional {
            str("foo")
          }
        }
      }
      parser.parse().isDefined should equal(true)
    }

    "process an optional inner rule if impossible" in {
      val parser = new ByteStringParser(ByteString("foobar")) {
        override def process(): Boolean = {
          optional {
            str("123")
          }
        }
      }
      parser.parse().isDefined should equal(true)
    }

    "process correctly first inner rule if possible" in {
      val parser = new ByteStringParser(ByteString("foobar")) {
        override def process(): Boolean = or(
          str("foo"),
          str("123")
        )
      }
      parser.parse().isDefined should equal(true)
    }

    "process correctly second inner rule if possible" in {
      val parser = new ByteStringParser(ByteString("foobar")) {
        override def process(): Boolean = or(
          str("123"),
          str("foo")
        )
      }
      parser.parse().isDefined should equal(true)
    }

    "process correctly when or rule is impossible" in {
      val parser = new ByteStringParser(ByteString("foobar")) {
        override def process(): Boolean = or(
          str("123"),
          str("123")
        )
      }
      parser.parse().isDefined should equal(false)
    }

    "process correctly when using sub parser when possible" in {
      val parser = new ByteStringParser(ByteString("foobar")) {
        val subParsing: ByteStringParser {
          def process(): Boolean
        } = new ByteStringParser(bytes) {
          override def process(): Boolean = str("foo")
        }

        override def process(): Boolean = subParser[ByteStringParser](subParsing, _.process()) && str("bar")
      }
      parser.parse().isDefined should equal(true)
    }

    "process correctly when using sub parser when impossible" in {
      val parser = new ByteStringParser(ByteString("foobar")) {
        val subParsing: ByteStringParser {
          def process(): Boolean
        } = new ByteStringParser(bytes) {
          override def process(): Boolean = str("bar")
        }

        override def process(): Boolean = subParser[ByteStringParser](subParsing, _.process()) && str("bar")
      }
      parser.parse().isDefined should equal(false)
    }

  }

}