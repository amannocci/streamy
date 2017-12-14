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
package io.techcode.streamy.util.parser

import com.google.common.base.CharMatcher

/**
  * Contains all commons [[CharMatcher]].
  */
object CharMatchers {

  // false
  val Empty: CharMatcher = CharMatcher.none()

  // true
  val All: CharMatcher = CharMatcher.any()

  // [a-z]
  val LowerAlpha: CharMatcher = CharMatcher.inRange('a', 'z')

  // [A-Z]
  val UpperAlpha: CharMatcher = CharMatcher.inRange('A', 'Z')

  // [a-zA-Z]
  val Alpha: CharMatcher = LowerAlpha.or(UpperAlpha)

  // [0-9]
  val Digit: CharMatcher = CharMatcher.inRange('0', '9')

  // [1-9]
  val Digit19: CharMatcher = CharMatcher.inRange('1', '9')

  // [a-zA-Z0-9]
  val AlphaNum: CharMatcher = Alpha.or(Digit)

  // [a-f]
  val LowerHexLetter: CharMatcher = CharMatcher.inRange('a', 'f')

  // [A-F]
  val UpperHexLetter: CharMatcher = CharMatcher.inRange('A', 'F')

  // [a-fA-F]
  val HexLetter: CharMatcher = LowerHexLetter.or(UpperHexLetter)

  // [0-9a-fA-F]
  val HexDigit: CharMatcher = Digit.or(HexLetter)

  // [!-~]
  val PrintUsAscii: CharMatcher = CharMatcher.inRange('!', '~')

}
