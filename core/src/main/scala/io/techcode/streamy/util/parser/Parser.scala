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
package io.techcode.streamy.util.parser

import com.google.common.base.CharMatcher

import scala.collection.mutable
import scala.util.control.NoStackTrace

// scalastyle:off number.of.methods
trait Parser[In, Out] {

  // Safe cursor position
  protected var _mark: Int = 0

  // Current cursor position
  protected[util] var _cursor: Int = 0

  // Local access
  protected var data: In = null.asInstanceOf[In]

  // Stack
  protected val stack: mutable.Stack[Any] = mutable.Stack[Any]()

  // Cache length of data
  protected[util] var _length: Int = -1

  /**
    * Attempt to parse input [[In]].
    *
    * @return [[In]] object result of parsing.
    */
  def parse(raw: In): Either[ParseException, Out] =
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
  def run(): Out

  /**
    * Cleanup parser context for next usage.
    */
  def cleanup(): Unit = {
    _mark = 0
    _cursor = 0
    _length = -1
    data = null.asInstanceOf[In]
    if (stack.nonEmpty) { // Avoid uneeded new array allocation
      stack.clear()
    }
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
  @inline def current(): Char

  /**
    * Returns the length of the input data to parse.
    *
    * @return length of the input data.
    */
  @inline final def length: Int = _length

  /**
    * The index of the next (yet unmatched) character.
    *
    * @return index of the next character.
    */
  final def cursor: Int = _cursor

  /**
    * Mark current cursor position.
    */
  final def mark(): Unit = _mark = _cursor

  /**
    * Unmark current cursor position to latest mark position.
    */
  final def unmark(): Unit = _cursor = _mark

  /**
    * Returns remaining number of data elements.
    *
    * @return remaining number of data elements.
    */
  @inline def hasRemaining: Boolean = _cursor < _length

  /**
    * Returns number of remaining data elements.
    *
    * @return number of remaining data elements.
    */
  @inline def remainingSize: Int = _length - _cursor

  /**
    * Create a slice of data based on mark and cursor.
    *
    * @return slice of data.
    */
  def slice(): In

  /**
    * Skip one element.
    */
  @inline final def skip(): Boolean = skip(1)

  /**
    * Skip n elements.
    *
    * @param numElems number of elements to skip.
    */
  @inline def skip(numElems: Int): Boolean = {
    _cursor += numElems
    true
  }

  /**
    * Unskip one element.
    */
  @inline final def unskip(): Boolean = unskip(1)

  /**
    * Unskip n elements.
    *
    * @param numElems number of elements to unskip.
    */
  @inline def unskip(numElems: Int): Boolean = {
    require(numElems >= 0, "Number of elements to unskip must be superior or equal to zero")
    _cursor -= numElems
    _cursor = Math.max(0, _cursor)
    true
  }

  /**
    * Except to match end of input.
    *
    * @return true if parsing succeeded, otherwise false.
    */
  final def eoi(): Boolean = _cursor == _length

  /**
    * Runs the inner rule and capture a [[In]] if rule is a success.
    *
    * @param inner inner rule.
    * @param field field to populate if success.
    * @return true if parsing succeeded, otherwise false.
    */
  def capture(inner: => Boolean)(field: In => Boolean): Boolean = {
    mark()
    inner && field(slice())
  }

  /**
    * Runs the inner rule and capture a [[In]] if bind is defined and rule is a success.
    *
    * @param bind   optional bind.
    * @param inner  inner rule.
    * @param fields fields to populate if success.
    * @return true if parsing succeeded, otherwise false.
    */
  def capture(bind: Option[String], inner: => Boolean)(fields: (String, In) => Boolean): Boolean = {
    mark()
    inner && bind.forall(k => fields(k, slice()))
  }

  /**
    * Runs the inner rule and capture a [[In]] if rule is a success optionally.
    *
    * @param inner inner rule.
    * @param field field to populate if success.
    * @return true if parsing succeeded, otherwise false.
    */
  def captureOptional(inner: => Boolean)(field: In => Boolean): Boolean = {
    capture(inner)(field)
    true
  }

  /**
    * Runs the inner rule and capture a [[In]] if bind is defined and rule is a success optionally.
    *
    * @param bind   optional bind.
    * @param inner  inner rule.
    * @param fields field to populate if success.
    * @return true if parsing succeeded, otherwise false.
    */
  def captureOptional(bind: Option[String], inner: => Boolean)(fields: (String, In) => Boolean): Boolean = {
    capture(bind, inner)(fields)
    true
  }

  /**
    * Except to match a single character.
    *
    * @param ch character excepted.
    * @return true if parsing succeeded, otherwise false.
    */
  final def ch(ch: Char): Boolean = hasRemaining && ch == current() && skip()

  /**
    * Except to match a sequence of characters.
    *
    * @param str characters sequence.
    * @return true if parsing succeeded, otherwise false.
    */
  final def str(str: String): Boolean = str.length <= remainingSize && {
    var i = 0
    var state = true
    while (state && i < str.length) {
      state = str.charAt(i) == current()
      skip()
      i += 1
    }
    state
  }

  /**
    * Runs the inner rule until it fails or end range is exceeded.
    *
    * @param start start of the range include.
    * @param end   end of the range include.
    * @param rule  inner rule.
    * @return true if parsing succeeded, otherwise false.
    */
  final def times(start: Int, end: Int)(rule: => Boolean): Boolean = {
    require(start > 0 && start <= end)
    var c = 0
    while (c < end && rule) {
      c += 1
    }
    c >= start && c <= end
  }

  /**
    * Runs the matcher until it fails or end range is exceeded.
    *
    * @param start   start of the range include.
    * @param end     end of the range include.
    * @param matcher character matcher.
    * @return true if parsing succeeded, otherwise false.
    */
  final def times(start: Int, end: Int, matcher: CharMatcher): Boolean = start <= remainingSize &&
    times(start, Math.min(_cursor + end, remainingSize)) {
      matcher.matches(current()) && skip()
    }

  /**
    * Runs the inner rule until it fails or count is exceeded.
    *
    * @param count target count.
    * @param rule  inner rule.
    * @return true if parsing succeeded, otherwise false.
    */
  final def times(count: Int)(rule: => Boolean): Boolean = times(count, count)(rule)

  /**
    * Runs the matcher until it fails or count is exceeded.
    *
    * @param count   target count.
    * @param matcher character matcher.
    * @return true if parsing succeeded, otherwise false.
    */
  final def times(count: Int, matcher: CharMatcher): Boolean = count <= remainingSize &&
    times(count) {
      matcher.matches(current()) && skip()
    }

  /**
    * Runs the inner rule until it fails, always succeeds.
    *
    * @param rule inner rule.
    * @return true if parsing succeeded, otherwise false.
    */
  final def zeroOrMore(rule: => Boolean): Boolean = {
    while (rule) ()
    true
  }

  /**
    * Runs the matcher until it fails, always succeeds.
    *
    * @param matcher character matcher.
    * @return true if parsing succeeded, otherwise false.
    */
  final def zeroOrMore(matcher: CharMatcher): Boolean = zeroOrMore {
    hasRemaining && matcher.matches(current()) && skip()
  }

  /**
    * Runs the inner rule until it fails, succeeds if the matcher succeeded at least once.
    *
    * @param rule inner rule.
    * @return true if parsing succeeded, otherwise false.
    */
  final def oneOrMore(rule: => Boolean): Boolean = rule && zeroOrMore(rule)

  /**
    * Runs the matcher until it fails, succeeds if the matcher succeeded at least once.
    *
    * @param matcher character matcher.
    * @return true if parsing succeeded, otherwise false.
    */
  final def oneOrMore(matcher: CharMatcher): Boolean = oneOrMore {
    hasRemaining && matcher.matches(current()) && skip()
  }

  /**
    * Runs the inner rule, succeeds even if the inner rule is failed.
    *
    * @param rule inner rule.
    * @return true if parsing succeeded, otherwise false.
    */
  final def optional(rule: => Boolean): Boolean = {
    val start = _cursor
    rule || {
      _cursor = start
      true
    }
  }

  /**
    * Set current cursor position to the end of [[In]]
    *
    * @return always true.
    */
  final def any(): Boolean = {
    _cursor = length
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
    x || {
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
  final def subParser[T <: Parser[In, Out]](parser: T)(action: T => Boolean): Boolean = {
    parser._cursor = _cursor
    parser._length = _length
    parser.data = data
    action(parser) && merge(parser)
  }

  /**
    * Merge result of another parser.
    *
    * @param parser parser to result to merge.
    * @tparam T sub type parser.
    */
  def merge[T <: Parser[In, Out]](parser: T): Boolean = {
    _cursor = parser._cursor
    stack ++= parser.stack
    true
  }

}

// scalastyle:on number.of.methods

/**
  * Parse exception.
  *
  * @param msg reason of parse exception.
  */
class ParseException(msg: => String) extends RuntimeException(msg) with NoStackTrace {

  def canEqual(a: Any): Boolean = a.isInstanceOf[ParseException]

  override def equals(that: Any): Boolean =
    that match {
      case that: ParseException => that.canEqual(this) && this.hashCode == that.hashCode
      case _ => false
    }

  override def hashCode: Int = 31 + msg.hashCode

}
