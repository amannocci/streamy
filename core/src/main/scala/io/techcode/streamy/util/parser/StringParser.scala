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

import io.techcode.streamy.util.Binder

import scala.language.implicitConversions

/**
  * Represent a [[String]] parser that provide an efficient way to parse [[String]].
  */
abstract class StringParser extends Parser[String] {

  final def length: Int = {
    if (_length == -1) {
      _length = data.length
    }
    _length
  }

  final def current(): Char = data.charAt(_cursor)

  final def capture(field: Binder, optional: Boolean = false)(inner: => Boolean): Boolean = {
    mark()
    var state = inner
    if (state && field.isDefined) {
      val binding = field.bind(builder, data.slice(_mark, _cursor))
      if (!binding) {
        state = optional
      }
    }
    state
  }

}
