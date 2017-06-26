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

/**
  * Provides a mechanism to iterate over a collection of bytes.
  */
trait ByteBufProcessor {

  /**
    * Returns true if the processor wants to continue the loop hand handle the next byte in the buffer.
    *
    * @param value byte value.
    * @return true if the processor wants to continue the loop hand handle the next byte in the buffer, otherwise false.
    */
  def process(value: Byte): Boolean

}

/**
  * Processors.
  */
object ByteBufProcessor {

  /**
    * Aborts on a NUL ('0x00').
    */
  val FindNul = new ByteBufProcessor {
    override def process(value: Byte): Boolean = value != 0
  }

  /**
    * Aborts on a non NUL ('0x00').
    */
  val FindNonNul = new ByteBufProcessor {
    override def process(value: Byte): Boolean = value == 0
  }

  /**
    * Aborts on a CR ('\r').
    */
  val FindCR = new ByteBufProcessor {
    override def process(value: Byte): Boolean = value != '\r'
  }

  /**
    * Aborts on a non CR ('\r').
    */
  val FindNonCR = new ByteBufProcessor {
    override def process(value: Byte): Boolean = value == '\r'
  }

  /**
    * Aborts on a Lf ('\n').
    */
  val FindLf = new ByteBufProcessor {
    override def process(value: Byte): Boolean = value != '\n'
  }

  /**
    * Aborts on a non Lf ('\n').
    */
  val FindNonLf = new ByteBufProcessor {
    override def process(value: Byte): Boolean = value == '\n'
  }

  /**
    * Aborts on a Space (' ').
    */
  val FindSpace = new ByteBufProcessor {
    override def process(value: Byte): Boolean = value != ' '
  }

}
