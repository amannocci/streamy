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
package io.techcode.streamy.util.lang

import akka.util.ByteString
import io.techcode.streamy.util.math.{RyuDouble, RyuFloat}

object CharBuilder {

  private val Digits: Array[Char] = Array(
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
    'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
    'u', 'v', 'w', 'x', 'y', 'z'
  )

  private val DigitTens: Array[Char] = Array(
    '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
    '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
    '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
    '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
    '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
    '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
    '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
    '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
    '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
    '9', '9', '9', '9', '9', '9', '9', '9', '9', '9'
  )

  private val DigitOnes: Array[Char] = Array(
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
  )

  private val IntMinValue: Array[Char] = Int.MinValue.toString.toCharArray

  private val LongMinValue: Array[Char] = Long.MinValue.toString.toCharArray

}

/**
  * CharBuilder is a specialized way to build Strings.
  *
  * It wraps a (growable) array of characters, and can accept
  * additional String or Char data to be added to its buffer.
  */
final class CharBuilder {

  // Initial size
  @inline val InitialSize = 32

  // Internal stuff
  private var buf = new Array[Char](InitialSize)
  private var capacity = InitialSize
  private var len = 0

  private def resizeIfNecessary(goal: Int): Unit = {
    if (goal > capacity) {
      var cap = capacity
      while (goal > cap && cap > 0) cap *= 2
      if (cap > capacity) {
        val newBuf = new Array[Char](cap)
        System.arraycopy(buf, 0, newBuf, 0, capacity)
        buf = newBuf
        capacity = cap
      } else if (cap < capacity) throw new OutOfMemoryError
    }
  }

  /**
    * Reset char builder.
    *
    * @return this object for chaining.
    */
  def reset(): CharBuilder = {
    len = 0
    this
  }

  /**
    * Returns the length (character count).
    *
    * @return the length of the sequence of characters.
    */
  def length(): Int = len

  /**
    * Appends a char sequence.
    *
    * @param seq sequence.
    * @return this object for chaining.
    */
  def append(seq: Array[Char]): CharBuilder = {
    val totalLen = len + seq.length
    resizeIfNecessary(totalLen)
    System.arraycopy(seq, 0, buf, len, seq.length)
    len = totalLen
    this
  }

  /**
    * Appends a sequence.
    *
    * @param seq sequence.
    * @return this object for chaining.
    */
  def append(seq: CharSequence): CharBuilder = {
    val totalLen = len + seq.length
    resizeIfNecessary(totalLen)
    var i = 0
    var j = len
    len = totalLen
    while (i < seq.length) {
      buf(j) = seq.charAt(i)
      i += 1
      j += 1
    }
    this
  }

  /**
    * Appends an int to builder.
    *
    * @param value int.
    * @return this object for chaining.
    */
  def append(value: Int): CharBuilder = {
    val totalLen = len + Primitives.stringSize(value)
    resizeIfNecessary(totalLen)

    if (Int.MinValue == value) {
      append(CharBuilder.IntMinValue)
      this
    } else {
      // Compute vars
      var rawValue = value
      var charPos = totalLen
      var sign: Char = 0
      var q = 0
      var r = 0

      // Handle negative sign
      if (rawValue < 0) {
        sign = '-'
        rawValue = -rawValue
      }

      // Generate two digits per iteration
      while (rawValue >= 65536) {
        q = rawValue / 100
        // really: r = rawValue - (q * 100);
        r = rawValue - ((q << 6) + (q << 5) + (q << 2))
        rawValue = q
        charPos -= 1
        buf(charPos) = CharBuilder.DigitOnes(r)
        charPos -= 1
        buf(charPos) = CharBuilder.DigitTens(r)
      }

      // Fall thru to fast mode for smaller numbers
      // assert(rawValue <= 65536, rawValue)
      do {
        q = (rawValue * 52429) >>> (16 + 3)
        // r = rawValue - (q * 10)
        r = rawValue - ((q << 3) + (q << 1))
        charPos -= 1
        buf(charPos) = CharBuilder.Digits(r)
        rawValue = q
      } while (rawValue != 0)

      // Handle sign
      if (sign != 0) {
        charPos -= 1
        buf(charPos) = sign
      }

      len = totalLen
      this
    }
  }

  /**
    * Appends an int to builder.
    *
    * @param value int.
    * @return this object for chaining.
    */
  def append(value: Long): CharBuilder = {
    val totalLen = len + Primitives.stringSize(value)
    resizeIfNecessary(totalLen)

    if (Long.MinValue == value) {
      append(CharBuilder.LongMinValue)
      this
    } else {
      // Compute vars
      var rawValue = value
      var charPos = totalLen
      var sign: Char = 0
      var q: Long = 0
      var r: Int = 0

      // Handle negative sign
      if (rawValue < 0) {
        sign = '-'
        rawValue = -rawValue
      }

      // Generate two digits per iteration
      while (rawValue >= Int.MaxValue) {
        q = rawValue / 100
        // really: r = rawValue - (q * 100);
        r = (rawValue - ((q << 6) + (q << 5) + (q << 2))).toInt
        rawValue = q
        charPos -= 1
        buf(charPos) = CharBuilder.DigitOnes(r)
        charPos -= 1
        buf(charPos) = CharBuilder.DigitTens(r)
      }

      // Get 2 digits/iteration using ints
      var q2 = 0
      var rawValue2 = rawValue.toInt
      while (rawValue2 >= 65536) {
        q2 = rawValue2 / 100
        // really: r = i2 - (q * 100)
        r = rawValue2 - ((q2 << 6) + (q2 << 5) + (q2 << 2))
        rawValue2 = q2
        charPos -= 1
        buf(charPos) = CharBuilder.DigitOnes(r)
        charPos -= 1
        buf(charPos) = CharBuilder.DigitTens(r)
      }

      // Fall thru to fast mode for smaller numbers
      // assert(rawValue2 <= 65536, rawValue2);
      do {
        q2 = (rawValue2 * 52429) >>> (16 + 3)
        // r = rawValue - (q2 * 10)
        r = rawValue2 - ((q2 << 3) + (q2 << 1))
        charPos -= 1
        buf(charPos) = CharBuilder.Digits(r)
        rawValue2 = q2
      } while (rawValue2 != 0)

      // Handle sign
      if (sign != 0) {
        charPos -= 1
        buf(charPos) = sign
      }

      len = totalLen
      this
    }
  }

  /**
    * Appends an float to builder.
    *
    * @param value float.
    * @return this object for chaining.
    */
  def append(value: Float): CharBuilder = {
    val totalLen = len + 15
    resizeIfNecessary(totalLen)
    len = len + RyuFloat.toString(value, buf, len)
    this
  }

  /**
    * Appends an double to builder.
    *
    * @param value double.
    * @return this object for chaining.
    */
  def append(value: Double): CharBuilder = {
    val totalLen = len + 24
    resizeIfNecessary(totalLen)
    len = len + RyuDouble.toString(value, buf, len)
    this
  }

  /**
    * Appends the string representation of the char.
    *
    * @param ch a character.
    * @return this object for chaining.
    */
  def append(ch: Char): CharBuilder = {
    val tlen = len + 1
    resizeIfNecessary(tlen)
    buf(len) = ch
    len = tlen
    this
  }

  /**
    * Appends the string representation of the value arguments.
    *
    * @param value any object.
    * @return this object for chaining.
    */
  def append(value: Any): CharBuilder = append(value.toString)

  /**
    * Appends the specified char builder to this sequence.
    *
    * @param builder char builder.
    * @return this object for chaining.
    */
  def append(builder: CharBuilder): CharBuilder = {
    val totalLen = len + builder.len
    resizeIfNecessary(totalLen)
    System.arraycopy(builder.buf, 0, buf, len, builder.len)
    len = totalLen
    this
  }

  override def toString: String = new String(buf, 0, len)

  /**
    * Returns a byte string representation of the object.
    * Very optimized due to HotSpotIntrinsicCandidate on method `StringUTF16.toBytes`.
    * It perform better than this.
    * ---------------------------------------------------------------------------------
    * val charBuf = CharBuffer.wrap(buf)
    * val size = (charBuf.remaining.toFloat * decoder.averageBytesPerChar).toInt
    * val byteBuf = new Array[Byte](size)
    * decoder.encode(charBuf, ByteBuffer.wrap(byteBuf), true)
    * ByteString.fromArrayUnsafe(byteBuf)
    * ---------------------------------------------------------------------------------
    *
    * @return a byte string representation of the object.
    */
  def toByteString: ByteString = ByteString(toString)

}
