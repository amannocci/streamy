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
    * @return current bytestring.
    */
  def getBuffer: ByteString = {
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
    readOrGetByte(updateIndex = true)
  }

  /**
    * Gets a byte at the current readerIndex in this buffer.
    *
    * @throws IndexOutOfBoundsException if readableBytes is less than 1.
    */
  def getByte: Byte = {
    readOrGetByte(updateIndex = false)
  }

  /**
    * Skip a byte at the current readerIndex and increases the readerIndex by 1 in this buffer.
    */
  def skipByte(): Unit = {
    _readerIndex += 1
  }

  /**
    * Gets an int at the current readerIndex and increases the readerIndex by 4 in this buffer.
    *
    * @throws IndexOutOfBoundsException if readableBytes is less than 4.
    */
  def readInt(): Int = {
    readOrGetInt(updateIndex = true)
  }

  /**
    * Gets an int at the current readerIndex in this buffer.
    *
    * @throws IndexOutOfBoundsException if readableBytes is less than 4.
    */
  def getInt: Int = {
    readOrGetInt(updateIndex = false)
  }

  /**
    * Skip a atn int the current readerIndex and increases the readerIndex by 1 in this buffer.
    */
  def skipInt(): Unit = {
    _readerIndex += 4
  }

  @inline private def readOrGetInt(updateIndex: Boolean): Int = {
    if (_readerIndex + 4 > buf.length) {
      throw new IndexOutOfBoundsException()
    } else {
      var value = buf(_readerIndex) << 24
      value |= (buf(_readerIndex + 1) & 255) << 16
      value |= (buf(_readerIndex + 2) & 255) << 8
      value |= buf(_readerIndex + 3) & 255
      if (updateIndex) {
        _readerIndex += 4
      }
      value
    }
  }

  @inline private def readOrGetByte(updateIndex: Boolean): Byte = {
    if (_readerIndex + 1 > buf.length) {
      throw new IndexOutOfBoundsException()
    } else {
      val value = buf(_readerIndex)
      if (updateIndex) {
        _readerIndex += 1
      }
      value
    }
  }

}
