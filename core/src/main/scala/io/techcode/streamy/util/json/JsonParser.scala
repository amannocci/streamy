/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2018-2020
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

import java.lang.{Long => JLong}
import java.util

import akka.util.ByteString
import io.techcode.streamy.util.json.JsonParser.{EmptyStr, HexDigits}
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

  private[json] val EmptyStr: String = ""

  private[json] val HexDigits: Array[Int] = {
    val array = new Array[Int](256)
    util.Arrays.fill(array, -1)
    array('0'.toInt) = 0x00
    array('1'.toInt) = 0x01
    array('2'.toInt) = 0x02
    array('3'.toInt) = 0x03
    array('4'.toInt) = 0x04
    array('5'.toInt) = 0x05
    array('6'.toInt) = 0x06
    array('7'.toInt) = 0x07
    array('8'.toInt) = 0x08
    array('9'.toInt) = 0x09
    array('A'.toInt) = 0x0A
    array('B'.toInt) = 0x0B
    array('C'.toInt) = 0x0C
    array('D'.toInt) = 0x0D
    array('E'.toInt) = 0x0E
    array('F'.toInt) = 0x0F
    array('a'.toInt) = 0x0A
    array('b'.toInt) = 0x0B
    array('c'.toInt) = 0x0C
    array('d'.toInt) = 0x0D
    array('e'.toInt) = 0x0E
    array('f'.toInt) = 0x0F
    array
  }

  // Default configuration
  val DefaultConfig: Config = Config(DefaultMaxDepth)

  /**
    * Create a json parser that transform incoming [[ByteString]] to [[Json]].
    * This parser is Rfc8259 compliant.
    *
    * @return new json parser Rfc8259 compliant.
    */
  def byteStringParser(config: JsonParser.Config = DefaultConfig): ByteStringParser[Json] =
    new ByteStringJsonParser(config)

  /**
    * Create a json parser that transform incoming [[String]] to [[Json]].
    * This parser is Rfc8259 compliant.
    *
    * @return new json parser Rfc8259 compliant.
    */
  def stringParser(config: JsonParser.Config = DefaultConfig): StringParser[Json] =
    new StringJsonParser(config)

  // Json parser configuration
  case class Config(
    maxDepth: Int = DefaultMaxDepth
  )

}

/**
  * A JsonParser unserializes an [[In]] to a JSON AST.
  * It's an adaptation of the amazing spray-json and borer project.
  * All credits goes to initials contributors.
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
        throw new ParseException(s"Unexpected input at index $cursor")
      }
    }
  }

  override def cleanup(): Unit = {
    super.cleanup()
    hasReachMaxNesting = false
    builder.reset()
    jsValue = null.asInstanceOf[Json]
  }

  override def root(): Boolean = value(config.maxDepth) && eoi()

  // scalastyle:off method.name
  protected def `false`(): Boolean

  protected def `true`(): Boolean

  protected def `null`(): Boolean

  protected def simpleValue(matched: Boolean, value: Json): Boolean = matched && {
    jsValue = value
    true
  }

  // https://tools.ietf.org/html/rfc4627#section-2.1
  protected def value(remainingNesting: Int): Boolean = ws() && {
    if (remainingNesting != 0 && hasRemaining) {
      (current(): @switch) match {
        case 'f' => simpleValue(`false`(), JsFalse)
        case 'n' => simpleValue(`null`(), JsNull)
        case 't' => simpleValue(`true`(), JsTrue)
        case '{' => skip(); obj(remainingNesting - 1)
        case '[' => skip(); arr(remainingNesting - 1)
        case '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9' | '-' => number()
        case '"' => string()
        case _ => false
      }
    } else {
      hasReachMaxNesting = remainingNesting == 0
      false
    }
  } && ws()

  // https://tools.ietf.org/html/rfc4627#section-2.2
  protected def obj(remainingNesting: Int): Boolean =
    simpleValue(ch('}'), Json.obj()) || {
      val obj = Json.objectBuilder()
      objMembers(remainingNesting, obj) && simpleValue(ch('}'), obj.result())
    }

  @tailrec private def objMembers(
    remainingNesting: Int,
    obj: JsObjectBuilder
  ): Boolean = {
    var key: String = EmptyStr
    ws() && string() && ws() && ch(':') && {
      key = builder.toString
      value(remainingNesting)
    } && {
      obj += (key -> jsValue)
      if (ch(',')) {
        objMembers(remainingNesting, obj)
      } else {
        true
      }
    }
  }

  // https://tools.ietf.org/html/rfc4627#section-2.3
  protected def arr(remainingNesting: Int): Boolean =
    simpleValue(ch(']'), Json.arr()) || {
      val arr = Json.arrayBuilder()
      arrValues(remainingNesting, arr) && simpleValue(ch(']'), arr.result())
    }

  @tailrec private def arrValues(
    remainingNesting: Int,
    arr: JsArrayBuilder
  ): Boolean = value(remainingNesting) && {
    arr += jsValue
    if (ch(',')) {
      arrValues(remainingNesting, arr)
    } else {
      true
    }
  }

  // https://tools.ietf.org/html/rfc4627#section-2.4
  protected def number(): Boolean

  protected def int(): Boolean = or(ch('0'), times(1, CharMatchers.Digit19) && zeroOrMore(CharMatchers.Digit))

  protected def frac(): Boolean = ch('.') && oneOrMore(CharMatchers.Digit)

  protected def exp(): Boolean = or(ch('e'), ch('E')) && optional(or(ch('-'), ch('+'))) && oneOrMore(CharMatchers.Digit)

  // https://tools.ietf.org/html/rfc4627#section-2.5
  protected def string(): Boolean

  protected def ws(): Boolean

  protected def appendAndAdvance(c: Char): Boolean = {
    builder.append(c)
    skip()
  }

  protected def append(c: Char): Boolean = {
    builder.append(c)
    true
  }

}

/**
  * Json parser that transform incoming [[ByteString]] to [[Json]].
  * This parser is Rfc8259 compliant.
  */
private class ByteStringJsonParser(
  conf: JsonParser.Config
) extends ByteStringParser[Json] with AbstractJsonParser[ByteString] {

  def config: JsonParser.Config = conf

  protected def `false`(): Boolean = {
    val mark = cursor
    ((readOctaBytePadded() >>> 24) == 0x66616C7365L) && unskip(cursor - mark - 5)
  }

  protected def `true`(): Boolean = {
    val mark = cursor
    (readQuadBytePadded() == 0x74727565) && unskip(cursor - mark - 4)
  }

  protected def `null`(): Boolean = {
    val mark = cursor
    (readQuadBytePadded() == 0x6E756C6C) && unskip(cursor - mark - 4)
  }

  // https://tools.ietf.org/html/rfc4627#section-2.4
  protected def number(): Boolean = {
    var isInt = false

    def hookIsInt(rule: => Boolean, state: Boolean): Boolean = rule && {
      isInt = state
      true
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
  protected def string(): Boolean = {
    // Clear builder before usage
    builder.reset()

    ch('"') && chars() && {
      jsValue = JsString(builder.toString)
      true
    } && ch('"')
  }

  // scalastyle:off method.length
  @tailrec private def chars(): Boolean = {
    // Mark current position
    val mark = cursor

    // Fetch 8 bytes (chars) at the same time with the first becoming the (left-most) MSB of the `octa` long
    val octa = readOctaBytePadded()

    // Mask '"' characters: of all 7-bit chars only '"' gets its high-bit set
    val qMask = (octa ^ 0x5D5D5D5D5D5D5D5DL) + 0x0101010101010101L

    // Mask '\' characters: of all 7-bit chars only '\' gets its high-bit set
    val bMask = (octa ^ 0x2323232323232323L) + 0x0101010101010101L

    // Mask ctrl characters (0 - 0x1F): of all 7-bit chars only ctrl chars get their high-bit set
    val cMask = (octa | 0x1F1F1F1F1F1F1F1FL) - 0x2020202020202020L

    // The special chars '"', '\', 8-bit (> 127) and ctrl chars become 0x80, all normal chars zero
    val mask = (qMask | bMask | octa | cMask) & 0x8080808080808080L

    // JVM intrinsic compiling to an LZCNT instr. on x86
    val nlz = JLong.numberOfLeadingZeros(mask)

    // The number of "good" normal chars before a special char [0..8]
    val charCount = nlz >> 3

    // In order to decrease instruction dependencies we always speculatively write all 8 chars to the char buffer,
    // independently of how many are actually "good" chars, this keeps CPU pipelines maximally busy
    builder.sizeHint(8)
    val x = octa & 0xFF00FF00FF00FF00L
    val y = octa & 0x00FF00FF00FF00FFL
    builder.append(
      (octa >>> 56).toChar,
      (y >>> 48).toChar,
      (x >>> 40).toChar,
      (y >>> 32).toChar,
      (x >>> 24).toChar,
      (y >>> 16).toChar,
      (x >>> 8).toChar,
      y.toChar
    )

    builder.dropRight(8 - charCount)
    if (nlz < 64) {
      // The first special char after `charCount` good chars
      val stopChar = (octa << nlz >>> 56).toInt
      val unreadCount = cursor - mark - charCount

      (stopChar: @switch) match {
        case '"' => unskip(unreadCount)
        case '\\' =>
          // Move the cursor to the char after the backslash
          unskip(unreadCount - 1)
          escaped() && chars()
        case c if c > 127 =>
          // Move the cursor to the char after the first 8-bit char
          unskip(unreadCount - 1)
          parseMultiByteUtf8Char(stopChar.toByte.toInt) && chars()
        case _ => false // stopChar is a ctrl char
      }
    } else {
      chars()
    }
  }

  // scalastyle:on method.length

  // scalastyle:off cyclomatic.complexity
  @tailrec private def parseMultiByteUtf8Char(b1: Int): Boolean = {
    val mark = cursor
    val byteCount = Integer.numberOfLeadingZeros(~b1) - 25
    val in = readQuadBytePadded()
    val b2 = in >> 24

    def fail() = throw new ParseException("Illegal UTF-8")

    (byteCount | 0x80) ^ (b2 & 0xC0) match {
      case 1 =>
        if ((b1 & 0x1E) == 0) fail()
        builder.append(((b1 << 6) ^ b2 ^ 0xF80).toChar)
      case 2 =>
        val b3 = in << 8 >> 24
        val c = (b1 << 12) ^ (b2 << 6) ^ b3 ^ 0xFFFE1F80
        if ((b1 == 0xE0 && (b2 & 0xE0) == 0x80) || (b3 & 0xC0) != 0x80 || ((c >> 11) == 0x1B)) fail()
        builder.append(c.toChar)
      case 3 =>
        val b3 = in << 8 >> 24
        val b4 = in << 16 >> 24
        val c = (b1 << 18) ^ (b2 << 12) ^ (b3 << 6) ^ b4 ^ 0x381F80
        if ((b3 & 0xC0) != 0x80 || (b4 & 0xC0) != 0x80 || c < 0x010000 || c > 0x10FFFF) fail()
        builder.append((0xD7C0 + (c >> 10)).toChar) // high surrogate
        builder.append((0xDC00 + (c & 0x3FF)).toChar) // low surrogate
      case _ => fail()
    }

    val unreadCount = cursor - mark - byteCount

    // If the next byte is also an 8-bit character (which is not that unlikely) we decode that as well right away
    val nextByte = in << (byteCount << 3) >> 24
    if (nextByte >= 0) { // No 8-bit character
      unskip(unreadCount) // "unskip" also the nextByte
    } else { // nextByte is an 8-bit character, so recurse
      unskip(unreadCount - 1)
      parseMultiByteUtf8Char(nextByte)
    }
  }

  // scalastyle:on cyclomatic.complexity

  override def ws(): Boolean = {
    // Skip until 8 whitespace
    @tailrec def skip8(): Boolean = {
      // Mark current position
      val mark = cursor

      // Fetch 8 bytes (chars) at the same time with the first becoming the (left-most) MSByte of the `octa` long
      val octa = readOctaBytePadded()

      // Bytes containing [0..0x20] or [0x80-0xA0] get their MSBit unset (< 0x80), all others have it set (>= 0x80)
      var mask = (octa & 0x7F7F7F7F7F7F7F7FL) + 0x5F5F5F5F5F5F5F5FL

      // Bytes containing [0..0x20] become zero, all others 0x80
      mask = (octa | mask) & 0x8080808080808080L

      val nlz = JLong.numberOfLeadingZeros(mask)
      if (nlz < 64) {
        unskip(cursor - mark - (nlz >> 3))
      } else {
        skip8()
      }
    }

    // Skip 1 whitespace
    def skip1(): Boolean = hasRemaining && {
      val c = readByte() & 0xFF
      if (c <= 0x20) {
        skip8()
      } else {
        unskip()
      }
    }

    skip1()
    true
  }

  private def escaped(): Boolean = (readBytePadded(): @switch) match {
    case '"' => append('"')
    case '/' => append('/')
    case '\\' => append('\\')
    case 'b' => append('\b')
    case 'f' => append('\f')
    case 'n' => append('\n')
    case 'r' => append('\r')
    case 't' => append('\t')
    case 'u' =>
      @inline def hd(c: Int): Int = HexDigits(c)

      def fail(): Boolean = throw new ParseException("Illegal escape sequence")

      var q = readQuadBytePadded()
      var x = (hd(q >>> 24) << 12) | (hd(q << 8 >>> 24) << 8) | (hd(q << 16 >>> 24) << 4) | hd(q & 0xFF)

      if (x < 0) fail()
      else builder.append(x.toChar)

      // immediately check whether there is another `u` sequence following and decode that as well if so
      val mark = cursor
      if (readDoubleBytePadded() == 0x5C75) {
        q = readQuadBytePadded()
        x = (hd(q >>> 24) << 12) | (hd(q << 8 >>> 24) << 8) | (hd(q << 16 >>> 24) << 4) | hd(q & 0xFF)

        if (x < 0) fail()
        else append(x.toChar)
      } else {
        unskip(cursor - mark)
      }
    case _ => false
  }

}

/**
  * Json parser that transform incoming [[String]] to [[Json]].
  * This parser is Rfc8259 compliant.
  */
private class StringJsonParser(
  conf: JsonParser.Config
) extends StringParser[Json] with AbstractJsonParser[String] {

  def config: JsonParser.Config = conf

  protected def `false`(): Boolean = str("false")

  protected def `true`(): Boolean = str("true")

  protected def `null`(): Boolean = str("null")

  // https://tools.ietf.org/html/rfc4627#section-2.4
  protected def number(): Boolean = {
    var isInt = false

    def hookIsInt(rule: => Boolean, state: Boolean): Boolean = rule && {
      isInt = state
      true
    }

    capture {
      hookIsInt(optional(ch('-')) && int(), state = true) &&
        optional(hookIsInt(frac(), state = false)) &&
        optional(hookIsInt(exp(), state = false))
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
  protected def string(): Boolean = {
    builder.reset()

    ch('"') && chars() && {
      jsValue = JsString(builder.toString)
      true
    } &&
      ch('"')
  }

  @tailrec private def chars(): Boolean = hasRemaining && {
    val start = _cursor
    var cursorChar = '\uFFFF'
    do {
      cursorChar = current()
    } while (hasRemaining && cursorChar != '"' && cursorChar >= ' ' && cursorChar != '\\' && skip())
    builder.append(data, start, cursor)
    cursorChar match {
      case '"' => true
      case c if c < ' ' => false
      case '\\' => skip() && escaped() && chars()
    }
  }

  private def escaped(): Boolean = {
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
      skip()
      value = (value << 4) + hexValue(current())
      skip()
      value = (value << 4) + hexValue(current())
      skip()
      value = (value << 4) + hexValue(current())
      appendAndAdvance(value.toChar)
    }

    hasRemaining && {
      val cursorChar = current()
      (cursorChar: @switch) match {
        case '"' | '/' | '\\' => appendAndAdvance(cursorChar)
        case 'b' => appendAndAdvance('\b')
        case 'f' => appendAndAdvance('\f')
        case 'n' => appendAndAdvance('\n')
        case 'r' => appendAndAdvance('\r')
        case 't' => appendAndAdvance('\t')
        case 'u' => skip(); unicode()
        case _ => false
      }
    }
  }

  protected def ws(): Boolean = {
    @tailrec def inner(): Boolean = hasRemaining && {
      (current(): @switch) match {
        case ' ' | '\n' | '\b' | '\t' => skip() && inner()
        case _ => false
      }
    }

    inner()
    true
  }

}
