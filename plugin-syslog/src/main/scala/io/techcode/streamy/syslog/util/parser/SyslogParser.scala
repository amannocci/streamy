/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017
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

import akka.util.ByteString
import com.google.common.base.CharMatcher
import io.techcode.streamy.syslog.component.transformer.SyslogTransformer._
import io.techcode.streamy.util.json._
import io.techcode.streamy.util.parser.{ByteStringParser, CharMatchers}

/**
  * Syslog parser companion.
  */
object SyslogParser {

  // Struct data param value matcher
  private[parser] val ParamValueMatcher: CharMatcher = CharMatchers.PrintUsAscii.and(CharMatcher.noneOf("\\\"]")).precomputed()

  // Struct data name matcher
  private[parser] val SdNameMatcher: CharMatcher = CharMatchers.PrintUsAscii.and(CharMatcher.noneOf("= \"]")).precomputed()

  /**
    * Create a syslog parser that transform incoming [[ByteString]] to [[Json]].
    * This parser is Rfc5424 compliant.
    *
    * @param bytes   data to parse.
    * @param binding binding parser configuration.
    * @return new syslog parser Rfc5424 compliant.
    */
  def rfc5424(bytes: ByteString, binding: Rfc5424.Binding): ByteStringParser = new Rfc5424Parser(bytes, binding)

}

/**
  * Parser helpers containing various shortcut for character matching.
  */
private trait ParserHelpers {
  this: ByteStringParser =>

  @inline def openQuote(): Boolean = ch('<')

  @inline def closeQuote(): Boolean = ch('>')

  @inline def openBracket(): Boolean = ch('[')

  @inline def closeBracket(): Boolean = ch(']')

  @inline def sp(): Boolean = ch(' ')

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
  * @param bytes   data to parse.
  * @param binding binding parser configuration.
  */
private class Rfc5424Parser(bytes: ByteString, binding: Rfc5424.Binding) extends ByteStringParser(bytes) with ParserHelpers {

  override def process(): Boolean =
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

  def version(): Boolean =
    times(1, CharMatchers.Digit19) && optional(times(1, 2, CharMatchers.Digit))

  def hostname(): Boolean =
    sp() && or(
      nilValue(),
      capture(binding.hostname) {
        times(1, 255, CharMatchers.PrintUsAscii)
      }
    )

  def appName(): Boolean =
    sp() && or(
      nilValue(),
      capture(binding.appName) {
        times(1, 48, CharMatchers.PrintUsAscii)
      }
    )

  def procId(): Boolean =
    sp() && or(
      nilValue(),
      capture(binding.procId) {
        times(1, 128, CharMatchers.PrintUsAscii)
      }
    )

  def msgId(): Boolean =
    sp() && or(
      nilValue(),
      capture(binding.msgId) {
        times(1, 32, CharMatchers.PrintUsAscii)
      }
    )

  def timestamp(): Boolean =
    sp() && or(
      nilValue(),
      capture(binding.timestamp) {
        fullDate() && ch('T') && fullTime()
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

  def timeHour(): Boolean = times(2, CharMatchers.Digit)

  def timeMinute(): Boolean = times(2, CharMatchers.Digit)

  def timeSecond(): Boolean = times(2, CharMatchers.Digit)

  def timeSecFrac(): Boolean = point() && times(1, 6, CharMatchers.Digit)

  def timeOffset(): Boolean = or(
    ch('Z'),
    timeNumOffset()
  )

  def timeNumOffset(): Boolean =
    or(ch('+'), ch('-')) && timeHour() && colon() && timeMinute()

  def structuredData(): Boolean = or(
    nilValue(),
    capture(binding.structData) {
      sdElement()
    }
  )

  def sdElement(): Boolean =
    openBracket() && sdName() && zeroOrMore(sp() && sdParam()) && closeBracket()

  def sdParam(): Boolean =
    sdName() && equal() && doubleQuote() && paramValue() && doubleQuote()

  def paramValue(): Boolean = zeroOrMore(SyslogParser.ParamValueMatcher)

  def sdName(): Boolean = times(1, 32, SyslogParser.SdNameMatcher)

  def msg(): Boolean =
    sp() && capture(binding.message) {
      zeroOrMore(CharMatchers.All)
    }

  private def capturePrival(rule: => Boolean): Boolean = {
    mark()
    val state = rule
    if (binding.facility.isDefined || binding.severity.isDefined) {
      val prival = partition().asDigit()

      // Read severity or facility
      if (binding.facility.isDefined) {
        builder.put(binding.facility.get.bind(prival >> 3))
      }
      if (binding.severity.isDefined) {
        builder.put(binding.severity.get.bind(prival & 7))
      }
    }
    state
  }

  // scalastyle:on

}
