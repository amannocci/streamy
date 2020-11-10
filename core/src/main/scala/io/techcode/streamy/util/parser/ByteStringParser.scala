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
package io.techcode.streamy.util.parser

import akka.util.ByteString

import scala.language.implicitConversions

/**
  * Represent a [[ByteString]] parser that provide an efficient way to parse [[ByteString]].
  */
trait ByteStringParser[Out] extends Parser[ByteString, Out] {

  override def parse(raw: ByteString): Either[ParseException, Out] = {
    _length = raw.length
    super.parse(raw)
  }

  @inline def current(): Char = (data(_cursor) & 0xFF).toChar

  final def slice(): ByteString = data.slice(_mark, _cursor)

  /**
    * Returns byte at current cursor position without bounds check.
    * Advances cursor by 1.
    *
    * @return current byte.
    */
  def readByte(): Byte = {
    val b = data(_cursor)
    skip()
    b
  }

  /**
    * Returns byte at current cursor position.
    * If the input is less than 1 byte left, a padding is applied.
    * Advances cursor by 1.
    *
    * @return current byte.
    */
  def readBytePadded(): Byte = remainingSize match {
    case 0 => -1
    case _ => readByte()
  }

  /**
    * Returns the next two bytes as an unsigned 16-bit value,
    * with the first becoming the more-significant byte (i.e. big endian/network byte order),
    * if possible without any range checks.
    * Advances cursor by 2.
    */
  def readDoubleByte(): Char = {
    val c = _cursor
    skip(2)
    ((data(c) << 8) | data(c + 1) & 0xFF).toChar
  }

  /**
    * Returns the next two bytes as an unsigned 16-bit value,
    * with the first becoming the more-significant byte (i.e. big endian/network byte order).
    * If the input has less than 2 bytes left, a padding is applied.
    */
  def readDoubleBytePadded(): Char = remainingSize match {
    case 0 => '\uFFFF'
    case 1 => ((readByte() << 8) | 0xFF).toChar
    case _ => readDoubleByte()
  }

  /**
    * Returns the next four bytes as an [[Int]],
    * with the first becoming the most-significant byte (i.e. big endian/network byte order),
    * if possible without any range checks.
    * Advances the cursor by 4.
    */
  def readQuadByte(): Int = {
    val c = _cursor
    skip(4)
    data(c) << 24 |
      (data(c + 1) & 0xFF) << 16 |
      (data(c + 2) & 0xFF) << 8 |
      data(c + 3) & 0xFF
  }

  /**
    * Returns the next four bytes as an [[Int]],
    * with the first becoming the most-significant byte (i.e. big endian/network byte order).
    * If the input has less than 4 bytes left, a padding is applied and its result returned.
    */
  def readQuadBytePadded(): Int = remainingSize match {
    case 0 => 0xFFFFFFFF
    case 1 => (readByte() << 24) | 0xFFFFFF
    case 2 => (readDoubleByte() << 16) | 0xFFFF
    case 3 => (readDoubleByte() << 16) | ((readByte() & 0xFF) << 8) | 0xFF
    case _ => readQuadByte()
  }

  /**
    * Returns the eight eight bytes as a [[Long]],
    * with the first becoming the most-significant byte (i.e. big endian/network byte order),
    * if possible without any range checks.
    * Advances the cursor by 8.
    */
  def readOctaByte(): Long = {
    val c = _cursor
    skip(8)
    data(c).toLong << 56 |
      (data(c + 1) & 0xFFL) << 48 |
      (data(c + 2) & 0xFFL) << 40 |
      (data(c + 3) & 0xFFL) << 32 |
      (data(c + 4) & 0xFFL) << 24 |
      (data(c + 5) & 0xFFL) << 16 |
      (data(c + 6) & 0xFFL) << 8 |
      data(c + 7) & 0xFFL
  }

  /**
    * Returns the next eight bytes as a [[Long]],
    * with the first becoming the most-significant byte (i.e. big endian/network byte order).
    * If the input has less than 8 bytes left, a padding is applied and its result returned.
    */
  def readOctaBytePadded(): Long = remainingSize match {
    case 0 => 0xFFFFFFFFFFFFFFFFL
    case 1 => (readByte().toLong << 56) | 0xFFFFFFFFFFFFFFL
    case 2 => (readDoubleByte().toLong << 48) | 0xFFFFFFFFFFFFL
    case 3 => (readDoubleByte().toLong << 48) | ((readByte() & 0XFFL) << 40) | 0xFFFFFFFFFFL
    case 4 => (readQuadByte().toLong << 32) | 0xFFFFFFFFL
    case 5 => (readQuadByte().toLong << 32) | ((readByte() & 0xFFL) << 24) | 0xFFFFFFL
    case 6 => (readQuadByte().toLong << 32) | ((readDoubleByte() & 0xFFFFL) << 16) | 0xFFFFL
    case 7 => (readQuadByte().toLong << 32) | ((readDoubleByte() & 0xFFFFL) << 16) | ((readByte() & 0xFFL) << 8) | 0xFFL
    case _ => readOctaByte()
  }

}
