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

import java.nio.charset.{Charset, StandardCharsets}

import akka.util.ByteString

/**
  * Represent a byte string partition by wrapping a bytestring.
  *
  * @param bytes bytestring to wrap.
  * @param start start index position.
  * @param end   end index position.
  */
class ByteStringPartition private[parser](private val bytes: ByteString, private val start: Int, private val end: Int) {

  /**
    * Returns an UTF-8 [[String]] representation of this [[ByteStringPartition]].
    *
    * @return an utf-8 string representation.
    */
  def asString(): String = asString(StandardCharsets.UTF_8)

  /**
    * Returns a [[String]] representation based on given [[Charset]] of this [[ByteStringPartition]].
    *
    * @param charset charset to use to decode.
    * @return a string representation.
    */
  def asString(charset: Charset): String = asBytes().decodeString(charset)

  /**
    * Returns a [[ByteString]] representation of this [[ByteStringPartition]].
    *
    * @return a bytestring representation.
    */
  def asBytes(): ByteString = bytes.slice(start, end)

  /**
    * Returns an [[Int]] representation of this [[ByteStringPartition]].
    *
    * @return an int representation.
    */
  def asDigit(): Int = asString(StandardCharsets.US_ASCII).toInt

}
