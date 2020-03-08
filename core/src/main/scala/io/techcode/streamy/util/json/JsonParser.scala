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
import io.techcode.streamy.util.lang.CharBuilder
import io.techcode.streamy.util.parser._

import scala.annotation.{switch, tailrec}

/**
  * Json parser companion.
  */
object JsonParser {

  // Default values
  private val DefaultMaxDepth: Int = 1000

  // Special stuff
  private[json] val IntPivot = Int.MinValue.toString.length - 2

  // Default configuration
  val DefaultConfig: Config = Config(DefaultMaxDepth)

  /**
    * Create a json parser that transform incoming [[ByteString]] to [[Json]].
    * This parser is Rfc4627 compliant.
    *
    * @return new json parser Rfc4627 compliant.
    */
  def byteStringParser(config: JsonParser.Config = DefaultConfig): ByteStringParser[Json] = new ByteStringJsonParser(config)

  /**
    * Create a json parser that transform incoming [[String]] to [[Json]].
    * This parser is Rfc4627 compliant.
    *
    * @return new json parser Rfc4627 compliant.
    */
  def stringParser(config: JsonParser.Config = DefaultConfig): StringParser[Json] = new StringJsonParser(config)

  // Json parser configuration
  case class Config(
    maxDepth: Int = DefaultMaxDepth
  )

}

/**
  * A JsonParser unserializes an [[In]] to a JSON AST.
  * It's an adaptation of the amazing spray-json project.
  * All credits goes to it's initial contributor.
  */
private trait AbstractJsonParser[In] extends Parser[In, Json] {

  protected val builder = new CharBuilder

  protected var jsValue: Json = null.asInstanceOf[Json]

  private var hasReachMaxNesting = false

  def config: JsonParser.Config

  def run(): Json = {
    if (root()) {
      jsValue
    } else {
      if (hasReachMaxNesting) {
        throw new ParseException(s"JSON input was nested more deeply than the configured limit of maxNesting = ${config.maxDepth}")
      } else {
        throw new ParseException(s"Unexpected input at index ${cursor()}")
      }
    }
  }

  override def cleanup(): Unit = {
    super.cleanup()
    hasReachMaxNesting = false
    builder.reset()
    jsValue = null.asInstanceOf[Json]
  }

  override def root(): Boolean = `value`(config.maxDepth) && eoi()

  // scalastyle:off method.name
  protected def `false`(): Boolean = str("false")

  protected def `true`(): Boolean = str("true")

  protected def `null`(): Boolean = str("null")

  protected def simpleValue(matched: Boolean, value: Json): Boolean =
    if (matched) {
      jsValue = value
      true
    } else {
      false
    }

  // https://tools.ietf.org/html/rfc4627#section-2.1
  protected def `value`(remainingNesting: Int): Boolean = ws() && {
    if (remainingNesting != 0) {
      times(1) {
        (current(): @switch) match {
          case 'f' => simpleValue(`false`(), JsFalse)
          case 'n' => simpleValue(`null`(), JsNull)
          case 't' => simpleValue(`true`(), JsTrue)
          case '{' => advance(); `object`(remainingNesting - 1)
          case '[' => advance(); `array`(remainingNesting - 1)
          case '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9' | '-' => `number`()
          case '"' => `string`()
          case _ => false
        }
      }
    } else {
      hasReachMaxNesting = true
      false
    }
  } && ws()

  // https://tools.ietf.org/html/rfc4627#section-2.2
  protected def `object`(remainingNesting: Int): Boolean = {
    if (current() != '}') {
      val obj = Json.objectBuilder()

      @tailrec def members(): Boolean = {
        var key: String = ""
        ws() &&
          `string`() &&
          ws() && ch(':') && {
          key = builder.toString
          `value`(remainingNesting)
        } && {
          obj += (key -> jsValue)
          if (current() == ',') {
            advance()
            members()
          } else {
            true
          }
        }
      }

      members() && simpleValue(ch('}'), obj.result())
    } else {
      jsValue = Json.obj()
      advance()
    }
  }

  // https://tools.ietf.org/html/rfc4627#section-2.3
  protected def `array`(remainingNesting: Int): Boolean = {
    if (current() != ']') {
      val arr = Json.arrayBuilder()

      @tailrec def values(): Boolean = {
        `value`(remainingNesting) && {
          arr += jsValue
          if (current() == ',') {
            advance()
            values()
          } else {
            true
          }
        }
      }

      values() && simpleValue(ch(']'), arr.result())
    } else {
      jsValue = Json.arr()
      advance()
    }
  }

  // https://tools.ietf.org/html/rfc4627#section-2.4
  protected def `number`(): Boolean

  protected def `int`(): Boolean = or(ch('0'), times(1, CharMatchers.Digit19) && zeroOrMore(CharMatchers.Digit))

  protected def `frac`(): Boolean = ch('.') && oneOrMore(CharMatchers.Digit)

  protected def `exp`(): Boolean = or(ch('e'), ch('E')) && optional(or(ch('-'), ch('+'))) && oneOrMore(CharMatchers.Digit)

  // https://tools.ietf.org/html/rfc4627#section-2.5
  protected def `string`(): Boolean

  protected def `char`(): Boolean = {
    val cursorChar = current()
    // Simple bloom-filter that quick-matches the most frequent case of characters that are ok to append
    // (it doesn't match control chars, EOI, '"', '?', '\', 'b' and certain higher, non-ASCII chars)
    if (((1L << cursorChar) & ((31 - cursorChar) >> 31) & 0x7ffffffbefffffffL) != 0L) {
      appendAndAdvance(cursorChar)
    } else {
      cursorChar match {
        case '"' | ByteStringParser.EOI => false
        case '\\' => advance(); `escaped`()
        case c => (c >= ' ') && appendAndAdvance(c)
      }
    }
  }

  protected def `escaped`(): Boolean = {
    def hexValue(c: Char): Int =
      if ('0' <= c && c <= '9') {
        c - '0'
      } else if ('a' <= c && c <= 'f') {
        c - 87
      } else if ('A' <= c && c <= 'F') {
        c - 55
      } else {
        throw new ParseException("hex digit")
      }

    def unicode() = {
      var value = hexValue(current())
      advance()
      value = (value << 4) + hexValue(current())
      advance()
      value = (value << 4) + hexValue(current())
      advance()
      value = (value << 4) + hexValue(current())
      appendAndAdvance(value.toChar)
    }

    val cursorChar = current()
    (cursorChar: @switch) match {
      case '"' | '/' | '\\' => appendAndAdvance(cursorChar)
      case 'b' => appendAndAdvance('\b')
      case 'f' => appendAndAdvance('\f')
      case 'n' => appendAndAdvance('\n')
      case 'r' => appendAndAdvance('\r')
      case 't' => appendAndAdvance('\t')
      case 'u' => advance(); unicode()
      case _ => false
    }
  }

  // Fast test whether cursorChar is one of " \n\r\t"
  def ws(): Boolean = zeroOrMore {
    val cursorChar = current()
    if (((1L << cursorChar) & ((cursorChar - 64) >> 31) & 0x100002600L) != 0L) {
      advance()
    } else {
      false
    }
  }

  protected def appendAndAdvance(c: Char): Boolean = {
    builder.append(c)
    advance()
  }

}

/**
  * Json parser that transform incoming [[ByteString]] to [[Json]].
  * This parser is Rfc4627 compliant.
  */
private class ByteStringJsonParser(conf: JsonParser.Config) extends ByteStringParser[Json] with AbstractJsonParser[ByteString] {

  def config: JsonParser.Config = conf

  // https://tools.ietf.org/html/rfc4627#section-2.4
  protected def `number`(): Boolean = {
    var isInt = false

    def hookIsInt(rule: => Boolean, state: Boolean): Boolean = {
      if (rule) {
        isInt = state
        true
      } else {
        false
      }
    }

    capture {
      hookIsInt(optional(ch('-')) && `int`(), state = true) &&
        optional(hookIsInt(`frac`(), state = false)) &&
        optional(hookIsInt(`exp`(), state = false))
    } { value =>
      if (isInt) {
        if (value.length <= JsonParser.IntPivot) {
          jsValue = JsInt.fromByteStringUnsafe(value)
        } else {
          jsValue = JsLong.fromByteStringUnsafe(value)
        }
      } else {
        jsValue = JsBigDecimal.fromByteStringUnsafe(value)
      }
      true
    }
  }

  // https://tools.ietf.org/html/rfc4627#section-2.5
  protected def `string`(): Boolean =
    ch('"') && {
      builder.reset()
      utf8 {
        zeroOrMore(`char`())
      }
      jsValue = JsString.fromLiteral(builder.toString)
      true
    } &&
      ch('"')

}

/**
  * Json parser that transform incoming [[String]] to [[Json]].
  * This parser is Rfc4627 compliant.
  */
private class StringJsonParser(conf: JsonParser.Config) extends StringParser[Json] with AbstractJsonParser[String] {

  def config: JsonParser.Config = conf

  // https://tools.ietf.org/html/rfc4627#section-2.4
  protected def `number`(): Boolean = {
    var isInt = false

    def hookIsInt(rule: => Boolean, state: Boolean): Boolean = {
      if (rule) {
        isInt = state
        true
      } else {
        false
      }
    }

    capture {
      hookIsInt(optional(ch('-')) && `int`(), state = true) &&
        optional(hookIsInt(`frac`(), state = false)) &&
        optional(hookIsInt(`exp`(), state = false))
    } { value =>
      if (isInt) {
        if (value.length <= JsonParser.IntPivot) {
          jsValue = JsInt.fromStringUnsafe(value)
        } else {
          jsValue = JsLong.fromStringUnsafe(value)
        }
      } else {
        jsValue = JsBigDecimal.fromStringUnsafe(value)
      }
      true
    }
  }

  // https://tools.ietf.org/html/rfc4627#section-2.5
  protected def `string`(): Boolean =
    ch('"') && {
      builder.reset()
      zeroOrMore(`char`())
      jsValue = JsString.fromLiteral(builder.toString)
      true
    } &&
      ch('"')

}
