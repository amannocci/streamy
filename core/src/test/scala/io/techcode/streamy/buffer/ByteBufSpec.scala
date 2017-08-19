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
import com.google.common.primitives.Ints
import org.scalatest._

/**
  * ByteBuf spec.
  */
class ByteBufSpec extends FlatSpec with Matchers {

  "A ByteBuf" must "be able to wrap a bytestring" in {
    new ByteBuf(ByteString("foobar"))
  }

  it must "return current bytestring" in {
    val input = ByteString("foobar")
    new ByteBuf(input).getBuffer should equal(input)
  }

  it should "accept new bytestring" in {
    val oldBuf = ByteString("old")
    val newBuf = ByteString("new")
    val byteBuf = new ByteBuf(oldBuf)
    byteBuf.readByte
    byteBuf.readerIndex should equal(1)
    byteBuf.setBuffer(newBuf)
    byteBuf.getBuffer should equal(newBuf)
    byteBuf.readerIndex should equal(0)
  }

  it should "update reader index on read operations" in {
    val input = ByteString("foobar")
    val byteBuf = new ByteBuf(input)
    byteBuf.readByte
    byteBuf.readerIndex should equal(1)
  }

  it should "read bytes based on bytebuf processor" in {
    val input = ByteString("foo\nbar")
    val byteBuf = new ByteBuf(input)
    val result = byteBuf.readBytes(ByteBufProcessor.FindLf)
    result should equal(ByteString("foo"))
  }

  it should "skip bytes based on bytebuf processor" in {
    val input = ByteString("foo\nbar")
    val byteBuf = new ByteBuf(input)
    byteBuf.skipBytes(ByteBufProcessor.FindLf)
    byteBuf.slice() should equal(ByteString("bar"))
  }

  it should "return a correct slice based on readerIndex" in {
    val input = ByteString("foobar")
    val byteBuf = new ByteBuf(input)
    byteBuf.readByte
    byteBuf.slice() should equal(ByteString("oobar"))
  }

  it should "read a byte correctly" in {
    val input = ByteString("foobar")
    val byteBuf = new ByteBuf(input)
    byteBuf.readByte should equal('f')
    byteBuf.readerIndex should equal(1)
  }

  it should "get a byte correctly" in {
    val input = ByteString("foobar")
    val byteBuf = new ByteBuf(input)
    byteBuf.getByte should equal('f')
    byteBuf.readerIndex should equal(0)
  }

  it should "read an int correctly" in {
    val input = ByteString(Ints.toByteArray(1))
    val byteBuf = new ByteBuf(input)
    byteBuf.readInt should equal(1)
    byteBuf.readerIndex should equal(4)
  }

  it should "get an int correctly" in {
    val input = ByteString(Ints.toByteArray(13))
    val byteBuf = new ByteBuf(input)
    byteBuf.getInt should equal(13)
    byteBuf.readerIndex should equal(0)
  }

}