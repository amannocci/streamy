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
package io.techcode.streamy.util.printer

import java.lang.{StringBuilder => JStringBuilder}

import akka.util.{ByteString, ByteStringBuilder}
import io.techcode.streamy.util.json.Json

/**
  * Represent a [[ByteString]] printer that provide an efficient way to print [[Json]].
  */
trait ByteStringPrinter extends Printer[ByteString]

/**
  * Represent a direct [[ByteString]] printer that provide an efficient way to print [[Json]].
  */
trait DirectByteStringPrinter extends ByteStringPrinter {

  // Used to build bytestring directly
  protected var builder: ByteStringBuilder = ByteString.newBuilder

  override def cleanup(): Unit = builder = ByteString.newBuilder

}

/**
  * Represent a derived [[ByteString]] printer based on [[JStringBuilder]] that provide an efficient way to print [[Json]].
  */
trait DerivedByteStringPrinter extends ByteStringPrinter {

  // Used to build bytestring indirectly
  protected var builder: JStringBuilder = new JStringBuilder(256)

  override def cleanup(): Unit = builder.setLength(0)

}
