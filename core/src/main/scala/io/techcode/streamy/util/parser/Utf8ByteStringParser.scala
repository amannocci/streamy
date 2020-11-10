/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2018-2020
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
import java.nio.{ByteBuffer, CharBuffer}

import io.techcode.streamy.util.parser.Utf8ByteStringParser.UTF8

import scala.annotation.tailrec

/**
  * Represent a [[akka.util.ByteString]] parser that provide an efficient way to parse [[akka.util.ByteString]] in utf8.
  */
trait Utf8ByteStringParser[Out] extends ByteStringParser[Out] {

  // Buffers
  private val byteBuffer = ByteBuffer.allocate(4)
  private val charBuffer = CharBuffer.allocate(2)

  // UTF-8 Decoder
  private val decoder = UTF8.newDecoder()

  // Ascii reader
  private val asciiReader: () => Char = () => (data(cursor) & 0xFF).toChar

  // Utf-8 reader
  private val utf8Reader: () => Char = () => {
    mark()

    @tailrec def decode(byte: Byte, remainingBytes: Int): Char = {
      byteBuffer.put(byte)
      if (remainingBytes > 0) {
        skip()
        if (hasRemaining) {
          decode(data(cursor), remainingBytes - 1)
        } else {
          fail()
        }
      } else {
        byteBuffer.flip()
        val coderResult = decoder.decode(byteBuffer, charBuffer, false)
        charBuffer.flip()
        val result = if (coderResult.isUnderflow & charBuffer.hasRemaining) {
          charBuffer.get()
        } else {
          fail()
        }
        byteBuffer.clear()
        if (!charBuffer.hasRemaining) charBuffer.clear()
        result
      }
    }

    if (charBuffer.position() > 0) {
      val result = charBuffer.get()
      charBuffer.clear()
      result
    } else {
      val byte = data(cursor)
      if (byte >= 0) {
        byte.toChar // 7-Bit ASCII
      } else if ((byte & 0xE0) == 0xC0) {
        decode(byte, 1) // 2-byte UTF-8 sequence
      } else if ((byte & 0xF0) == 0xE0) {
        decode(byte, 2) // 3-byte UTF-8 sequence
      } else if ((byte & 0xF8) == 0xF0) {
        decode(byte, 3) // 4-byte UTF-8 sequence
      } else {
        fail()
      }
    }
  }

  // Decode mode
  private var reader: () => Char = asciiReader

  // Return error char and reset mark
  protected def fail(): Char = {
    unmark()
    Utf8ByteStringParser.ErrorChar
  }

  def utf8(inner: => Boolean): Boolean = {
    reader = utf8Reader
    try {
      inner
    } finally {
      reader = asciiReader
    }
  }

  override def current(): Char = reader()

  override def skip(numElems: Int): Boolean = {
    if (charBuffer.position() == 0) _cursor += numElems
    true
  }

  override def cleanup(): Unit = {
    super.cleanup()
    byteBuffer.clear()
    charBuffer.clear()
  }

}

/**
  * Companion utf8 byte string parser.
  */
object Utf8ByteStringParser {
  val UTF8: Charset = StandardCharsets.UTF_8
  val ErrorChar = '\uFFFD' // compile-time constant, universal UTF-8 replacement character '�'
  val EOI = '\uFFFF'
}
