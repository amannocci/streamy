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
  def getBuffer: ByteString = buf

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
    * Returns true if a byte can be read, otherwise false.
    */
  def isReadable: Boolean = readableBytes() > 0

  /**
    * Returns true if and only if this buffer contains equal to or more than the specified number of elements.
    */
  def isReadable(size: Int): Boolean = size < readableBytes()

  /**
    * Returns the readerIndex of this buffer.
    */
  def readerIndex: Int = _readerIndex

  /**
    * Transfers this buffer's data to a newly created buffer starting at the current
    * readerIndex and increases the readerIndex by the number of the transferred bytes.
    * The returned buffer's readerIndex is 0.
    *
    * @param processor processor.
    * @return the newly created buffer which contains the transferred bytes.
    */
  def readBytes(processor: ByteBufProcessor): ByteBuf = {
    val index = _readerIndex
    var value: Byte = 0
    do {
      value = readByte
    } while (processor.process(value))
    ByteBuf(buf.slice(index, _readerIndex - 1).compact)
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
    * Gets a string at the current readerIndex and increases the readerIndex by n in this buffer.
    * n depends of the ByteBufProcessor.
    *
    * @param processor byte buf processor.
    * @return decoded string from bytebuf.
    */
  def readString(processor: ByteBufProcessor): String =
    // Compact method is invoke implicitly
    readBytes(processor).buf.utf8String

  /**
    * Gets an int at the current readerIndex and increases the readerIndex by n in this buffer.
    * n depends of the ByteBufProcessor.
    *
    * @param processor byte buf processor.
    * @return decoded integer from bytebuf.
    */
  def readDigit(processor: ByteBufProcessor): Int =
    // Compact method is invoke implicitly
    readString(processor).toInt

  /**
    * Returns a slice of this buffer's readable bytes.
    */
  def slice(): ByteString = buf.slice(_readerIndex, buf.length)

  /**
    * Returns the number of readable bytes which is equal to (length - readerIndex)
    *
    * @return the number of readable bytes.
    */
  def readableBytes(): Int = buf.length - _readerIndex

  /**
    * Gets a byte at the current readerIndex and increases the readerIndex by 1 in this buffer.
    *
    * @throws IndexOutOfBoundsException if readableBytes is less than 1.
    */
  def readByte: Byte = readOrGetByte(updateIndex = true)

  /**
    * Gets a byte at the current readerIndex in this buffer.
    *
    * @throws IndexOutOfBoundsException if readableBytes is less than 1.
    */
  def getByte: Byte = readOrGetByte(updateIndex = false)

  /**
    * Skip a byte at the current readerIndex and increases the readerIndex by 1 in this buffer.
    */
  def skipByte(): Unit = _readerIndex += 1

  /**
    * Gets an int at the current readerIndex and increases the readerIndex by 4 in this buffer.
    *
    * @throws IndexOutOfBoundsException if readableBytes is less than 4.
    */
  def readInt(): Int = readOrGetInt(updateIndex = true)


  /**
    * Gets an int at the current readerIndex in this buffer.
    *
    * @throws IndexOutOfBoundsException if readableBytes is less than 4.
    */
  def getInt: Int = readOrGetInt(updateIndex = false)

  /**
    * Skip an int at the current readerIndex and increases the readerIndex by 1 in this buffer.
    */
  def skipInt(): Unit = _readerIndex += 4

  override def toString: String =
    // Compact is called implicitly
    slice().utf8String

  @inline private def readOrGetInt(updateIndex: Boolean): Int = {
    if (readableBytes() < 4) {
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
    if (readableBytes() < 1) {
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

/**
  * ByteBuf object companion.
  */
object ByteBuf {

  import scala.language.implicitConversions

  /**
    * Create a new bytebuf based on given bytestring.
    *
    * @param buf bytestring involved.
    * @return newly created bytebuf.
    */
  def apply(buf: ByteString): ByteBuf = new ByteBuf(buf)

  /**
    * Implicit conversion from bytestring to bytebuf.
    *
    * @param x bytestring to wrap.
    * @return bytebuf.
    */
  implicit def byteString2byteBuf(x: ByteString): ByteBuf = ByteBuf(x)

}
