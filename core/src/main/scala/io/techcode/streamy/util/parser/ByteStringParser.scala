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

import akka.util.ByteString

import scala.language.implicitConversions

/**
  * Represent a [[ByteString]] parser that provide an efficient way to parse [[ByteString]].
  */
trait ByteStringParser[Out] extends Parser[ByteString, Out] {

  // Return error char and reset mark
  protected def fail(): Char = {
    unmark()
    ByteStringParser.ErrorChar
  }

  override def parse(raw: ByteString): Either[ParseException, Out] = {
    _length = raw.length
    super.parse(raw)
  }

  @inline def current(): Char = (data(_cursor) & 0xFF).toChar

  /**
    * Advance cursor by n where n is consumed data.
    */
  override def advance(): Boolean = {
    _cursor += 1
    true
  }

  final def slice(): ByteString = data.slice(_mark, _cursor)

}

/**
  * Companion byte string parser.
  */
object ByteStringParser {
  val UTF8: Charset = StandardCharsets.UTF_8
  val ErrorChar = '\uFFFD' // compile-time constant, universal UTF-8 replacement character '�'
  val EOI = '\uFFFF'
}
