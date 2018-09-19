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
package io.techcode.streamy.util.parser

import akka.util.ByteString
import com.google.common.base.CharMatcher
import io.techcode.streamy.util.Binder
import io.techcode.streamy.util.json.{JsObjectBuilder, Json}

import scala.util.control.NoStackTrace

trait Parser[In] {

  // Safe cursor position
  protected var _mark: Int = 0

  // Current cursor position
  protected var _cursor: Int = 0

  // Data consumed to retrieve a char
  protected var _consumed: Byte = 0

  // Local access
  protected var data: In = null.asInstanceOf[In]

  // Used to build json object directly
  protected var builder: JsObjectBuilder = Json.objectBuilder()

  /**
    * Attempt to parse input [[ByteString]].
    *
    * @return [[In]] object result of parsing.
    */
  final def parse(raw: In): Either[ParseException, Json] =
    try {
      data = raw
      Right(run())
    } catch {
      case ex: ParseException => Left(ex)
    } finally {
      cleanup()
    }

  /**
    * Process parsing based on [[data]] and current context.
    *
    * @return parsing result.
    */
  def run(): Json = {
    if (root()) {
      builder.result()
    } else {
      throw ParseException(s"Unexpected input at index ${_cursor}")
    }
  }

  /**
    * Cleanup parser context for next usage.
    */
  def cleanup(): Unit = {
    _mark = 0
    _cursor = 0
    _consumed = 0
    data = null.asInstanceOf[In]
    builder = Json.objectBuilder()
  }

  /**
    * This method must be override and considered as root rule parsing.
    *
    * @return true if parsing succeeded, otherwise false.
    */
  def root(): Boolean

  /**
    * Returns character at current cursor position without bounds check.
    *
    * @return current character.
    */
  def current(): Char

  /**
    * Returns the length of the input data to parse.
    *
    * @return length of the input data.
    */
  def length: Int

  /**
    * The index of the next (yet unmatched) character.
    *
    * @return index of the next character.
    */
  final def cursor(): Int = _cursor

  /**
    * Mark current cursor position.
    */
  final def mark(): Unit = _mark = _cursor

  /**
    * Unmark current cursor position to latest mark position.
    */
  final def unmark(): Unit = _cursor = _mark

  /**
    * Advance cursor by n where n is consumed data.
    */
  final def advance(): Unit = _cursor += _consumed

  /**
    * Except to match end of input.
    *
    * @return true if parsing succeeded, otherwise false.
    */
  final def eoi(): Boolean = _cursor == length

  /**
    * Runs the inner rule and capture a [[String]] if field is defined.
    *
    * @param field field to populate if success.
    * @param inner inner rule.
    * @return true if parsing succeeded, otherwise false.
    */
  def capture(field: Binder, optional: Boolean = false)(inner: => Boolean): Boolean

  /**
    * Except to match a single character.
    *
    * @param ch character excepted.
    * @return true if parsing succeeded, otherwise false.
    */
  final def ch(ch: Char): Boolean = {
    if (_cursor < length && ch == current()) {
      advance()
      true
    } else {
      false
    }
  }

  /**
    * Except to match a sequence of characters.
    *
    * @param str characters sequence.
    * @return true if parsing succeeded, otherwise false.
    */
  final def str(str: String): Boolean = {
    if (_cursor + str.length <= length) {
      var i = 0
      var state = true
      while (state && i < str.length) {
        if (str(i) == current()) {
          advance()
          i += 1
        } else {
          state = false
        }
      }
      state
    } else {
      false
    }
  }

  /**
    * Runs the matcher until it fails or count is exceeded.
    *
    * @param count   target count.
    * @param matcher character matcher.
    * @return true if parsing succeeded, otherwise false.
    */
  final def times(count: Int, matcher: CharMatcher): Boolean = {
    require(count > 0)
    if (_cursor + count <= length) {
      var c = 0
      while (c < count && matcher.matches(current())) {
        advance()
        c += 1
      }
      count == c
    } else {
      false
    }
  }

  /**
    * Runs the inner rule until it fails or count is exceeded.
    *
    * @param count target count.
    * @param rule  inner rule.
    * @return true if parsing succeeded, otherwise false.
    */
  final def times(count: Int)(rule: => Boolean): Boolean = {
    require(count > 0)
    var c = 0
    var state = true
    while (_cursor < length && c < count && state) {
      if (rule) {
        c += 1
      } else {
        state = false
      }
    }
    count == c
  }

  /**
    * Runs the matcher until it fails or end range is exceeded.
    *
    * @param start   start of the range.
    * @param end     end of the range.
    * @param matcher character matcher.
    * @return true if parsing succeeded, otherwise false.
    */
  final def times(start: Int, end: Int, matcher: CharMatcher): Boolean = {
    require(start > 0)
    if (_cursor + end < length) {
      var c = 0
      while (matcher.matches(current())) {
        advance()
        c += 1
      }
      c >= start
    } else if (_cursor + start < length) {
      val bound = length - _cursor
      var c = 0
      while (c < bound && matcher.matches(current())) {
        advance()
        c += 1
      }
      c >= start
    } else {
      false
    }
  }

  /**
    * Set current cursor position to the end of [[ByteString]]
    *
    * @return always true.
    */
  final def any(): Boolean = {
    _cursor = length
    true
  }

  /**
    * Runs the matcher until it fails, always succeeds.
    *
    * @param matcher character matcher.
    * @return true if parsing succeeded, otherwise false.
    */
  final def zeroOrMore(matcher: CharMatcher): Boolean = {
    while (_cursor < length && matcher.matches(current())) advance()
    true
  }

  /**
    * Runs the inner rule until it fails, always succeeds.
    *
    * @param rule inner rule.
    * @return true if parsing succeeded, otherwise false.
    */
  final def zeroOrMore(rule: => Boolean): Boolean = {
    while (_cursor < length && rule) ()
    true
  }

  /**
    * Runs the matcher until it fails, succeeds if the matcher succeeded at least once.
    *
    * @param matcher character matcher.
    * @return true if parsing succeeded, otherwise false.
    */
  final def oneOrMore(matcher: CharMatcher): Boolean = {
    val start = _cursor
    while (_cursor < length && matcher.matches(current())) advance()
    start != _cursor
  }

  /**
    * Runs the inner rule until it fails, succeeds if the matcher succeeded at least once.
    *
    * @param rule inner rule.
    * @return true if parsing succeeded, otherwise false.
    */
  final def oneOrMore(rule: => Boolean): Boolean = {
    var c = 0
    var state = true
    while (_cursor < length && state) {
      if (rule) {
        c += 1
      } else {
        state = false
      }
    }
    c > 0
  }

  /**
    * Runs the inner rule, succeeds even if the inner rule is failed.
    *
    * @param rule inner rule.
    * @return true if parsing succeeded, otherwise false.
    */
  final def optional(rule: => Boolean): Boolean = {
    val start = _cursor
    if (!rule) {
      _cursor = start
    }
    true
  }

  /**
    * Runs the first inner rule and the second inner rule if the first failed.
    * Failed when the first and second inner rule are failed.
    *
    * @param x first inner rule.
    * @param y second inner rule.
    * @return true if parsing succeeded, otherwise false.
    */
  final def or(x: => Boolean, y: => Boolean): Boolean = {
    val start = _cursor
    if (x) {
      true
    } else {
      _cursor = start
      y
    }
  }

  /**
    * Runs a sub parser and return
    *
    * @param parser sub parser to run.
    * @param action action to run on sub parser.
    * @tparam T sub type parser.
    * @return true if sub parser succeeded, otherwise false.
    */
  final def subParser[T <: Parser[In]](parser: T, action: T => Boolean): Boolean = {
    parser.data = data
    if (action(parser)) {
      _cursor = parser._cursor
      builder.putAll(parser.builder)
      true
    } else {
      false
    }
  }

}

case class ParseException(msg: String) extends RuntimeException(msg) with NoStackTrace
