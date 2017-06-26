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
package io.techcode.streamy.buffer

import java.nio.ByteBuffer

import akka.util.ByteString

/**
  * ByteString buf for easy read and write.
  */
class ByteBuf(private var buf: ByteString) {

  // Current reader index
  private var _readerIndex: Int = 0

  /**
    * Gets a current bytestring.
    *
    * @param buf current bytestring.
    * @return current bytestring.
    */
  def getBuffer(buf: ByteString): ByteString = {
    buf
  }

  /**
    * Sets a current bytestring.
    *
    * @param buf current bytestring.
    */
  def setBuffer(buf: ByteString): Unit = {
    _readerIndex = 0
    this.buf = buf
  }

  /**
    * Returns the readerIndex of this buffer.
    */
  def readerIndex: Int = _readerIndex

  /**
    * Increases the current readerIndex based on processor.
    *
    * @param processor processor.
    */
  def readBytes(processor: ByteBufProcessor): ByteString = {
    val index = _readerIndex
    var value: Byte = 0
    do {
      value = readByte
    } while (processor.process(value))
    buf.slice(index, _readerIndex - 1)
  }

  /**
    * Increases the current readerIndex based on processor.
    *
    * @param processor processor.
    */
  def skipBytes(processor: ByteBufProcessor): Unit = {
    var value: Byte = 0
    do {
      value = readByte
    } while (processor.process(value))
  }

  /**
    * Returns a slice of this buffer's readable bytes.
    */
  def slice(): ByteString = {
    buf.slice(_readerIndex, buf.length)
  }

  /**
    * Gets a byte at the current readerIndex and increases the readerIndex by 1 in this buffer.
    *
    * @throws IndexOutOfBoundsException if readableBytes is less than 1.
    */
  def readByte: Byte = {
    _getByte(increment = true)
  }

  /**
    * Gets a byte at the current readerIndex in this buffer.
    *
    * @throws IndexOutOfBoundsException if readableBytes is less than 1.
    */
  def getByte: Byte = {
    _getByte(increment = false)
  }

  /**
    * Gets an int at the current readerIndex and increases the readerIndex by 4 in this buffer.
    *
    * @throws IndexOutOfBoundsException if readableBytes is less than 4.
    */
  def readInt(): Int = {
    _getInt(increment = true)
  }

  /**
    * Gets an int at the current readerIndex in this buffer.
    *
    * @throws IndexOutOfBoundsException if readableBytes is less than 4.
    */
  def getInt: Int = {
    _getInt(increment = false)
  }

  @inline private def _getInt(increment: Boolean): Int = {
    if (_readerIndex + 4 >= buf.length) {
      throw new IndexOutOfBoundsException()
    } else {
      val value = ByteBuffer.wrap(Array[Byte](
        buf(_readerIndex),
        buf(_readerIndex + 1),
        buf(_readerIndex + 2),
        buf(_readerIndex + 3)
      )).getInt()
      if (increment) {
        _readerIndex += 4
      }
      value
    }
  }

  @inline private def _getByte(increment: Boolean): Byte = {
    if (_readerIndex + 1 >= buf.length) {
      throw new IndexOutOfBoundsException()
    } else {
      val value = buf(_readerIndex)
      if (increment) {
        _readerIndex += 1
      }
      value
    }
  }

}
