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
package io.techcode.streamy.syslog.util.parser

import java.nio.charset.StandardCharsets

import akka.util.ByteString
import com.google.common.base.CharMatcher
import com.google.common.primitives.Ints
import io.techcode.streamy.syslog.component.SyslogTransformer._
import io.techcode.streamy.util.json._
import io.techcode.streamy.util.parser.{ByteStringParser, CharMatchers, ParseException}

/**
  * Syslog parser companion.
  */
object SyslogParser {

  // Struct data param value matcher for Rfc5424
  private[parser] val ParamValueMatcher: CharMatcher = CharMatchers.PrintUsAscii.and(CharMatcher.noneOf("\\\"]")).precomputed()

  // Struct data name matcher for Rfc5424
  private[parser] val SdNameMatcher: CharMatcher = CharMatchers.PrintUsAscii.and(CharMatcher.noneOf("= \"]")).precomputed()

  // App name matcher for Rfc3164
  private[parser] val AppNameMatcher: CharMatcher = CharMatchers.PrintUsAscii.and(CharMatcher.noneOf("[")).precomputed()

  // Proc id matcher for Rfc3164
  private[parser] val ProcIdMatcher: CharMatcher = CharMatchers.PrintUsAscii.and(CharMatcher.noneOf("]")).precomputed()

  /**
    * Create a syslog parser that transform incoming [[ByteString]] to [[Json]].
    * This parser is Rfc5424 compliant.
    *
    * @param config parser configuration.
    * @return new syslog parser Rfc5424 compliant.
    */
  def rfc5424(config: Rfc5424.Config): ByteStringParser[Json] = new Rfc5424Parser(config)

  /**
    * Create a syslog parser that transform incoming [[ByteString]] to [[Json]].
    * This parser is Rfc3164 compliant.
    *
    * @param config parser configuration.
    * @return new syslog parser Rfc3164 compliant.
    */
  def rfc3164(config: Rfc3164.Config): ByteStringParser[Json] = new Rfc3164Parser(config)

}

/**
  * Parser helpers containing various shortcut for character matching.
  */
private trait ParserHelpers {
  this: ByteStringParser[Json] =>

  @inline def openQuote(): Boolean = ch('<')

  @inline def closeQuote(): Boolean = ch('>')

  @inline def openBracket(): Boolean = ch('[')

  @inline def closeBracket(): Boolean = ch(']')

  @inline def sp(): Boolean = ch(' ')

  @inline def ws(): Boolean = oneOrMore(ch(' '))

  @inline def nilValue(): Boolean = dash()

  @inline def dash(): Boolean = ch('-')

  @inline def colon(): Boolean = ch(':')

  @inline def point(): Boolean = ch('.')

  @inline def doubleQuote(): Boolean = ch('"')

  @inline def equal(): Boolean = ch('=')

}

/**
  * Syslog parser that transform incoming [[ByteString]] to [[Json]].
  * This parser is Rfc5424 compliant.
  *
  * @param config parser configuration.
  */
// scalastyle:off
private class Rfc5424Parser(config: Rfc5424.Config) extends ByteStringParser[Json] with ParserHelpers {

  private val binding = config.binding

  private val mode = config.mode

  private implicit val builder: JsObjectBuilder = Json.objectBuilder()
  private val structDataBuilder: JsObjectBuilder = Json.objectBuilder()

  def run(): Json = {
    if (root()) {
      if (!structDataBuilder.result().isEmpty) {
        binding.structData.foreach(bind => builder += bind -> structDataBuilder.result())
      }
      builder.result()
    } else {
      throw new ParseException(s"Unexpected input at index ${cursor}:\n${data.utf8String}\n${" " * cursor}^")
    }
  }

  override def root(): Boolean =
    header() &&
      sp() &&
      structuredData() &&
      optional(msg()) &&
      eoi()

  // scalastyle:off
  def header(): Boolean =
    pri() && version() && timestamp() && hostname() && appName() && procId() && msgId()

  def pri(): Boolean =
    openQuote() && capturePrival(priVal()) && closeQuote()

  def priVal(): Boolean = times(1, 3, CharMatchers.Digit)

  private def capturePrival(rule: => Boolean): Boolean = {
    capture(rule) { value =>
      if (binding.facility.isDefined || binding.severity.isDefined) {
        val prival = Ints.tryParse(value.decodeString(StandardCharsets.US_ASCII))

        // Read severity or facility
        binding.facility.foreach(k => builder += k -> (prival >> 3))
        binding.severity.foreach(k => builder += k -> (prival & 7))
      }
      true
    }
  }

  def version(): Boolean =
    times(1, CharMatchers.Digit19) && optional(times(1, 2, CharMatchers.Digit))

  def hostname(): Boolean =
    sp() && or(
      nilValue(),
      capture(binding.hostname, times(1, mode.hostname, CharMatchers.PrintUsAscii)) { (key, value) =>
        // Unsafe can be use because hostname is validate
        builder.bind(key, JsString.fromByteStringUnsafe(value))
      }
    )

  def appName(): Boolean =
    sp() && or(
      nilValue(),
      capture(binding.appName, times(1, mode.appName, CharMatchers.PrintUsAscii)) { (key, value) =>
        // Unsafe can be use because appName is validate
        builder.bind(key, JsString.fromByteStringUnsafe(value))
      }
    )

  def procId(): Boolean =
    sp() && or(
      nilValue(),
      capture(binding.procId, times(1, mode.procId, CharMatchers.PrintUsAscii)) { (key, value) =>
        // Unsafe can be use because procId is validate
        builder.bind(key, JsString.fromByteStringUnsafe(value))
      }
    )

  def msgId(): Boolean =
    sp() && or(
      nilValue(),
      capture(binding.msgId, times(1, mode.msgId, CharMatchers.PrintUsAscii)) { (key, value) =>
        // Unsafe can be use because msgId is validate
        builder.bind(key, JsString.fromByteStringUnsafe(value))
      }
    )

  def timestamp(): Boolean =
    sp() && or(
      nilValue(),
      capture(binding.timestamp, fullDate() && ch('T') && fullTime()) { (key, value) =>
        // Unsafe can be use because timestamp is validate
        builder.bind(key, JsString.fromByteStringUnsafe(value))
      }
    )

  def fullDate(): Boolean =
    dateFullYear() && dash() && dateMonth() && dash() && dateMDay()

  def dateFullYear(): Boolean = times(4, CharMatchers.Digit)

  def dateMonth(): Boolean = times(2, CharMatchers.Digit)

  def dateMDay(): Boolean = times(2, CharMatchers.Digit)

  def fullTime(): Boolean = partialTime() && timeOffset()

  def partialTime(): Boolean =
    timeHour() && colon() && timeMinute() && colon() && timeSecond() && optional(timeSecFrac())

  def timeSecond(): Boolean = times(2, CharMatchers.Digit)

  def timeSecFrac(): Boolean = point() && times(1, 6, CharMatchers.Digit)

  def timeOffset(): Boolean = or(
    ch('Z'),
    timeNumOffset()
  )

  def timeNumOffset(): Boolean =
    or(ch('+'), ch('-')) && timeHour() && colon() && timeMinute()

  def timeHour(): Boolean = times(2, CharMatchers.Digit)

  def timeMinute(): Boolean = times(2, CharMatchers.Digit)

  def structuredData(): Boolean = or(
    nilValue(),
    sdElement()
  )

  def sdElement(): Boolean =
    openBracket() && sdName() && zeroOrMore(sp() && sdParam()) && closeBracket()

  def sdParam(): Boolean =
    capture(binding.structData, sdName()) { (_, value) =>
      stack.push(value.utf8String)
      true
    } && equal() && doubleQuote() &&
      capture(binding.structData, paramValue()) { (_, value) =>
        structDataBuilder.bind(stack.pop().asInstanceOf[String], JsString.fromByteStringUnsafe(value))
      } && doubleQuote()

  def paramValue(): Boolean = zeroOrMore(SyslogParser.ParamValueMatcher)

  def sdName(): Boolean = times(1, 32, SyslogParser.SdNameMatcher)

  def msg(): Boolean =
    sp() && capture(binding.message, any()) { (key, value) =>
      // Unsafe can be use because message is validate
      builder.bind(key, {
        if (config.bypassMessageParsing) {
          JsBytes(value)
        } else {
          JsString.fromByteStringUnsafe(value)
        }
      })
    }

  override def cleanup(): Unit = {
    super.cleanup()
    builder.clear()
    structDataBuilder.clear()
  }

}

// scalastyle:on

/**
  * Syslog parser that transform incoming [[ByteString]] to [[Json]].
  * This parser is Rfc3164 compliant.
  *
  * @param config parser configuration.
  */
private class Rfc3164Parser(config: Rfc3164.Config) extends ByteStringParser[Json] with ParserHelpers {

  private val binding = config.binding

  private val mode = config.mode

  private implicit var builder: JsObjectBuilder = Json.objectBuilder()

  def run(): Json = {
    if (root()) {
      builder.result()
    } else {
      throw new ParseException(s"Unexpected input at index $cursor:\n${data.utf8String}\n${" " * cursor}^")
    }
  }

  override def root(): Boolean =
    header() &&
      colon() &&
      optional(msg()) &&
      eoi()

  // scalastyle:off
  def header(): Boolean =
    pri() && timestamp() && sp() && hostname() && sp() && appName() && procId()

  def pri(): Boolean =
    openQuote() && capturePrival(priVal()) && closeQuote()

  def priVal(): Boolean = times(1, 3, CharMatchers.Digit)

  private def capturePrival(rule: => Boolean): Boolean = {
    capture(rule) { value =>
      if (binding.facility.isDefined || binding.severity.isDefined) {
        val prival = Ints.tryParse(value.decodeString(StandardCharsets.US_ASCII))

        // Read severity or facility
        binding.facility.foreach(k => builder += k -> (prival >> 3))
        binding.severity.foreach(k => builder += k -> (prival & 7))
      }
      true
    }
  }

  def hostname(): Boolean =
    capture(binding.hostname, times(1, mode.hostname, CharMatchers.PrintUsAscii)) { (key, value) =>
      // Unsafe can be use because hostname is validate
      builder.bind(key, JsString.fromByteStringUnsafe(value))
    }

  def appName(): Boolean =
    capture(binding.appName, times(1, mode.appName, SyslogParser.AppNameMatcher)) { (key, value) =>
      // Unsafe can be use because appName is validate
      builder.bind(key, JsString.fromByteStringUnsafe(value))
    }

  def procId(): Boolean =
    openBracket() &&
      capture(binding.procId, times(1, mode.procId, SyslogParser.ProcIdMatcher)) { (key, value) =>
        // Unsafe can be use because procId is validate
        builder.bind(key, JsString.fromByteStringUnsafe(value))
      } &&
      closeBracket()

  def timestamp(): Boolean =
    capture(binding.timestamp, fullDate()) { (key, value) =>
      // Unsafe can be use because timestamp is validate
      builder.bind(key, JsString.fromByteStringUnsafe(value))
    }

  def fullDate(): Boolean =
    dateMonth() && ws() && dateMDay() && sp() && fullTime()

  def dateMonth(): Boolean = times(3, CharMatchers.Alpha)

  def dateMDay(): Boolean = times(1, 2, CharMatchers.Digit)

  def fullTime(): Boolean = timeHour() && colon() && timeMinute() && colon() && timeSecond()

  def timeHour(): Boolean = times(2, CharMatchers.Digit)

  def timeMinute(): Boolean = times(2, CharMatchers.Digit)

  def timeSecond(): Boolean = times(2, CharMatchers.Digit)

  def msg(): Boolean =
    sp() && capture(binding.message, any()) { (key, value) =>
      if (config.bypassMessageParsing) {
        builder.bind(key, JsBytes(value))
      } else {
        // Unsafe can be use because message will be validate on conversion
        builder.bind(key, JsString.fromByteStringUnsafe(value))
      }
    }

  override def cleanup(): Unit = {
    super.cleanup()
    builder.clear()
  }

  // scalastyle:on

}
