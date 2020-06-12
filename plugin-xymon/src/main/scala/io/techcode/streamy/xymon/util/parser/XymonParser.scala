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
package io.techcode.streamy.xymon.util.parser

import com.google.common.base.CharMatcher
import io.techcode.streamy.util.json._
import io.techcode.streamy.util.parser.{ByteStringParser, CharMatchers, ParseException}
import io.techcode.streamy.xymon.component.XymonTransformer

object XymonParser {
  private[parser] val GroupNameMatcher: CharMatcher = CharMatchers.AlphaNum.or(CharMatchers.PrintUsAscii)

  private[parser] val HostNameMatcher: CharMatcher = CharMatchers.Alpha.or(CharMatcher.is(',')).precomputed()

  private[parser] val TestNameMatcher: CharMatcher = CharMatcher.noneOf(". ").precomputed()

  def parser(conf: XymonTransformer.Parser.Config): ByteStringParser[Json] = new XymonParser(conf)
}

/**
  * Parser helpers containing various shortcut for character matching.
  */
private[parser] trait ParserHelpers {
  this: ByteStringParser[Json] =>

  @inline def sp(): Boolean = ch(' ')

  @inline def plus(): Boolean = ch('+')

  @inline def slash(): Boolean = ch('/')

  @inline def colon(): Boolean = ch(':')

  @inline def dot(): Boolean = ch('.')

}

/**
  * Xymon parser for the status command. The message syntax is as follows:
  * status[+LIFETIME][/group:GROUP] HOSTNAME.TESTNAME COLOR <additional_text>
  * For more information on the parameters, see the xymon man page
  */
private[parser] class XymonParser(config: XymonTransformer.Parser.Config)
  extends ByteStringParser[Json] with ParserHelpers {

  private val binding = config.binding

  private implicit var builder: JsObjectBuilder = Json.objectBuilder()

  def run(): Json = {
    if (root()) {
      builder.result()
    } else {
      throw new ParseException(s"Unexpected input at index ${_cursor}")
    }
  }

  override def root(): Boolean =
    str(XymonTransformer.Id.Status) &&
      lifetimeAndGroup() && sp() &&
      hostAndService() && sp() &&
      color() &&
      additionalText() &&
      eoi()

  def lifetimeAndGroup(): Boolean =
    lifetime() && group()

  def lifetime(): Boolean =
    optional(
      plus() &&
        capture(duration() && optional(durationUnit())) { value =>
          // Unsafe can be use because duration is validate
          binding.lifetime.foreach(bind => builder += bind -> JsString.fromByteStringUnsafe(value))
          true
        }
    )

  def duration(): Boolean = oneOrMore(CharMatchers.Digit)

  def durationUnit(): Boolean = times(1, CharMatchers.LowerAlpha)

  def group(): Boolean =
    optional(
      slash() &&
        oneOrMore(CharMatchers.LowerAlpha) &&
        colon() &&
        capture(oneOrMore(XymonParser.GroupNameMatcher)) { value =>
          // Unsafe can be use because group is validate
          binding.group.foreach(bind => builder += bind -> JsString.fromByteStringUnsafe(value))
          true
        }
    )

  def hostAndService(): Boolean =
    host && dot() && service()

  def host(): Boolean =
    capture(oneOrMore(XymonParser.HostNameMatcher)) { value =>
      // Unsafe can be use because group is validate
      binding.host.foreach(bind => builder += bind -> JsString.fromByteStringUnsafe(value))
      true
    }

  def service(): Boolean =
    capture(oneOrMore(XymonParser.TestNameMatcher)) { value =>
      // Unsafe can be use because service is validate
      binding.service.foreach(bind => builder += bind -> JsString.fromByteStringUnsafe(value))
      true
    }

  def color(): Boolean =
    capture(oneOrMore(CharMatchers.LowerAlpha)) { value =>
      // Unsafe can be use because service is validate
      binding.color.foreach(bind => builder += bind -> JsString.fromByteStringUnsafe(value))
      true
    }

  def additionalText(): Boolean =
    optional(
      sp() &&
        capture(any()) { value =>
          // Unsafe can be use because message is validate
          binding.message.foreach(bind => builder += bind -> JsString.fromByteStringUnsafe(value))
          true
        }
    )

  override def cleanup(): Unit = {
    super.cleanup()
    builder = Json.objectBuilder()
  }

}
