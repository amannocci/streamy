/*
 * The MIT License (MIT)
 * <p>
 * Copyright (C) 2017-2019
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
  val Empty: CharMatcher = CharMatcher.none().precomputed()

  // true
  val All: CharMatcher = CharMatcher.any().precomputed()

  // [a-z]
  val LowerAlpha: CharMatcher = CharMatcher.inRange('a', 'z').precomputed()

  // [A-Z]
  val UpperAlpha: CharMatcher = CharMatcher.inRange('A', 'Z').precomputed()

  // [a-zA-Z]
  val Alpha: CharMatcher = LowerAlpha.or(UpperAlpha).precomputed()

  // [0-9]
  val Digit: CharMatcher = CharMatcher.inRange('0', '9').precomputed()

  // [1-9]
  val Digit19: CharMatcher = CharMatcher.inRange('1', '9').precomputed()

  // [a-zA-Z0-9]
  val AlphaNum: CharMatcher = Alpha.or(Digit).precomputed()

  // [a-f]
  val LowerHexLetter: CharMatcher = CharMatcher.inRange('a', 'f').precomputed()

  // [A-F]
  val UpperHexLetter: CharMatcher = CharMatcher.inRange('A', 'F').precomputed()

  // [a-fA-F]
  val HexLetter: CharMatcher = LowerHexLetter.or(UpperHexLetter).precomputed()

  // [0-9a-fA-F]
  val HexDigit: CharMatcher = Digit.or(HexLetter).precomputed()

  // [!-~]
  val PrintUsAscii: CharMatcher = CharMatcher.inRange('!', '~').precomputed()

  // Rfc4648
  val Base64: CharMatcher = UpperAlpha.or(LowerAlpha).or(Digit).or(CharMatcher.anyOf("+/=")).precomputed()

}
