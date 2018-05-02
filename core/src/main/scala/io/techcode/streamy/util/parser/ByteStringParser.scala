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
import com.google.common.base.CharMatcher
import io.techcode.streamy.util.Binder
import io.techcode.streamy.util.json._

import scala.language.implicitConversions

/**
  * Represent a [[ByteString]] parser that provide an efficient way to parse [[ByteString]].
  *
  * @param bytes input to parse.
  */
abstract class ByteStringParser(val bytes: ByteString) {

  // Safe cursor position
  private var _mark: Int = 0

  // Current cursor position
  private var _cursor: Int = 0

  // Used to build json object directly
  lazy protected val builder: JsObjectBuilder = Json.objectBuilder()

  /**
    * Attempt to parse input [[ByteString]].
    *
    * @return [[Json]] object result of parsing or [[None]].
    */
  final def parse(): Option[JsObject] = {
    if (process()) {
      Some(builder.result())
    } else {
      None
    }
  }

  /**
    * Compute error message report.
    *
    * @return error message report.
    */
  def error(): String = {
    if (bytes.isEmpty) {
      "Unexpected empty input"
    } else {
      s"Unexpected '${current()}' at index ${_cursor}"
    }
  }

  /**
    * This method must be override and considered as root rule parsing.
    *
    * @return true if parsing succeeded, otherwise false.
    */
  def process(): Boolean

  /**
    * Returns character at current cursor position without bounds check.
    *
    * @return current character.
    */
  final def current(): Char = (bytes(_cursor) & 0xFF).toChar

  /**
    * Returns a [[ByteStringPartition]] based on start and end index.
    *
    * @return a byte string partition.
    */
  final def partition(): ByteStringPartition = new ByteStringPartition(bytes, _mark, _cursor)

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
    * Except to match end of input.
    *
    * @return true if parsing succeeded, otherwise false.
    */
  final def eoi(): Boolean = _cursor == bytes.length

  /**
    * Runs the inner rule and capture a [[String]] if field is defined.
    *
    * @param field field to populate if success.
    * @param inner inner rule.
    * @return true if parsing succeeded, otherwise false.
    */
  final def capture(field: Option[Binder], optional: Boolean = false)(inner: => Boolean): Boolean = {
    mark()
    var state = inner
    if (state && field.isDefined) {
      val binding = field.get.bind(builder, bytes.slice(_mark, _cursor))
      if (!binding) {
        state = optional
      }
    }
    state
  }

  /**
    * Except to match a single character.
    *
    * @param ch character excepted.
    * @return true if parsing succeeded, otherwise false.
    */
  final def ch(ch: Char): Boolean = {
    if (_cursor < bytes.length && ch == current()) {
      _cursor += 1
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
    var i = 0
    var state = true
    while (state && i < str.length) {
      if (_cursor < bytes.length && str(i) == current()) {
        _cursor += 1
        i += 1
      } else {
        state = false
      }
    }
    state
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
    var c = 0
    while (_cursor < bytes.length && matcher.matches(current()) && c < count) {
      _cursor += 1
      c += 1
    }
    count == c
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
    while (_cursor < bytes.length && c < count && state) {
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
    var c = 0
    while (_cursor < bytes.length && matcher.matches(current()) && c < end) {
      _cursor += 1
      c += 1
    }
    c >= start
  }

  /**
    * Set current cursor position to the end of [[ByteString]]
    *
    * @return always true.
    */
  final def any(): Boolean = {
    _cursor = bytes.length
    true
  }

  /**
    * Runs the matcher until it fails, always succeeds.
    *
    * @param matcher character matcher.
    * @return true if parsing succeeded, otherwise false.
    */
  final def zeroOrMore(matcher: CharMatcher): Boolean = {
    while (_cursor < bytes.length && matcher.matches(current())) _cursor += 1
    true
  }

  /**
    * Runs the inner rule until it fails, always succeeds.
    *
    * @param rule inner rule.
    * @return true if parsing succeeded, otherwise false.
    */
  final def zeroOrMore(rule: => Boolean): Boolean = {
    while (_cursor < bytes.length && rule) ()
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
    while (_cursor < bytes.length && matcher.matches(current())) _cursor += 1
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
    while (_cursor < bytes.length && state) {
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
  final def subParser[T <: ByteStringParser](parser: T, action: T => Boolean): Boolean = {
    if (action(parser)) {
      _cursor = parser._cursor
      builder.putAll(parser.builder)
      true
    } else {
      false
    }
  }

}
