/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017-2019
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

import akka.util.ByteString
import io.techcode.streamy.util.parser.ByteStringParser.UTF8

import scala.annotation.tailrec
import scala.language.implicitConversions

/**
  * Represent a [[ByteString]] parser that provide an efficient way to parse [[ByteString]].
  */
trait ByteStringParser[Out] extends Parser[ByteString, Out] {

  // Buffers
  private val byteBuffer = ByteBuffer.allocate(4)
  private val charBuffer = CharBuffer.allocate(2)

  // UTF-8 Decoder
  private val decoder = UTF8.newDecoder()

  // Ascii reader
  private val asciiReader: () => Char = () => (data(_cursor) & 0xFF).toChar

  // Utf-8 reader
  private val utf8Reader: () => Char = () => {
    mark()

    @tailrec def decode(byte: Byte, remainingBytes: Int): Char = {
      byteBuffer.put(byte)
      if (remainingBytes > 0) {
        advance()
        if (_cursor < data.length) {
          decode(data(_cursor), remainingBytes - 1)
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
      val byte = data(_cursor)
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

  // Return error char and reset mark
  private def fail(): Char = {
    unmark()
    ByteStringParser.ErrorChar
  }

  // Decode mode
  private var reader: () => Char = asciiReader

  override def parse(raw: ByteString): Either[ParseException, Out] = {
    _length = raw.length
    super.parse(raw)
  }

  @inline final def current(): Char = reader()

  def utf8(inner: => Boolean): Boolean = {
    reader = utf8Reader
    try {
      inner
    } finally {
      reader = asciiReader
    }
  }

  /**
    * Advance cursor by n where n is consumed data.
    */
  override final def advance(): Boolean = {
    if (charBuffer.position() == 0) _cursor += 1
    true
  }

  final def slice(): ByteString = data.slice(_mark, _cursor)

  override def cleanup(): Unit = {
    super.cleanup()
    byteBuffer.clear()
    charBuffer.clear()
  }

}

/**
  * Companion byte string parser.
  */
object ByteStringParser {
  val UTF8: Charset = StandardCharsets.UTF_8
  val ErrorChar = '\uFFFD' // compile-time constant, universal UTF-8 replacement character '�'
  val EOI = '\uFFFF'
}
