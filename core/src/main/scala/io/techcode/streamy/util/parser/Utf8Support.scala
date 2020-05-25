package io.techcode.streamy.util.parser

import java.nio.{ByteBuffer, CharBuffer}

import io.techcode.streamy.util.parser.ByteStringParser.UTF8

import scala.annotation.tailrec

trait Utf8Support[Out] extends ByteStringParser[Out] {

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

  // Decode mode
  private var reader: () => Char = asciiReader

  def utf8(inner: => Boolean): Boolean = {
    reader = utf8Reader
    try {
      inner
    } finally {
      reader = asciiReader
    }
  }

  override def current(): Char = reader()

  override def advance(): Boolean = {
    if (charBuffer.position() == 0) _cursor += 1
    true
  }

  override def cleanup(): Unit = {
    super.cleanup()
    byteBuffer.clear()
    charBuffer.clear()
  }

}
