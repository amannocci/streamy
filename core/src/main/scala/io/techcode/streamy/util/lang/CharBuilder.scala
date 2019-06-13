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

import java.util

/**
  * CharBuilder is a specialized way to build Strings.
  *
  * It wraps a (growable) array of characters, and can accept
  * additional String or Char data to be added to its buffer.
  */
final class CharBuilder {
  @inline val InitialSize = 32

  private var buf = new Array[Char](InitialSize)
  private var capacity = InitialSize
  private var len = 0

  def reset(): CharBuilder = {
    len = 0
    this
  }

  def length(): Int = len

  override def toString: String = new String(buf, 0, len)

  private def resizeIfNecessary(goal: Int): Unit = {
    if (goal > capacity) {
      var cap = capacity
      while (goal > cap && cap > 0) cap *= 2
      if (cap > capacity) {
        val newBuf = new Array[Char](cap)
        System.arraycopy(buf, 0, newBuf, 0, capacity)
        buf = newBuf
        capacity = cap
      } else if (cap < capacity) {
        sys.error("Maximum string size exceeded")
      }
    }
  }

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

  def append(ch: Char): CharBuilder = {
    val tlen = len + 1
    resizeIfNecessary(tlen)
    buf(len) = ch
    len = tlen
    this
  }

  def append(value: Any): CharBuilder = append(value.toString)

  def append(builder: CharBuilder): CharBuilder = {
    val tlen = len + builder.len
    resizeIfNecessary(tlen)
    System.arraycopy(builder.buf, 0, buf, len, builder.len)
    len = tlen
    this
  }

}
