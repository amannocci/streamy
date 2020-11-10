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
package io.techcode.streamy.util.lang

import akka.util.ByteString
import io.techcode.streamy.StreamyTestSystem

/**
  * Char builder spec.
  */
class CharBuilderSpec extends StreamyTestSystem {

  "Char builder" should {
    "return correct length" in {
      val builder = new CharBuilder
      builder.length() should equal(0)
      builder.append(10)
      builder.length() should equal(2)
    }

    "be reset" in {
      val builder = new CharBuilder
      builder.append(10)
      builder.reset()
      builder.length() should equal(0)
    }

    "be able to grow based on size hint" in {
      val builder = new CharBuilder
      builder.sizeHint(2)
      builder.length() should equal(0)
    }

    "be able to drop last characters" in {
      val builder = new CharBuilder
      builder.append("0123456789").dropRight(5)
      builder.toString should equal("01234")
    }

    "be able to append sequence" in {
      val builder = new CharBuilder
      builder.append("foo").append("bar")
      builder.toString should equal("foobar")
    }

    "be able to append string slice" in {
      val builder = new CharBuilder
      builder.append("foobar", 0,3)
      builder.toString should equal("foo")
    }

    "be able to append bytestring" in {
      val builder = new CharBuilder
      builder.append(ByteString("foobar123-"))
      builder.toString should equal("foobar123-")
    }

    "be able to append char" in {
      val builder = new CharBuilder
      builder.append("foo").append("bar")
      builder.toString should equal("foobar")
    }

    "be able to append an int" in {
      val builder = new CharBuilder
      builder.append(-10).append(50).append(65536).append(Int.MinValue)
      builder.toString should equal("-105065536-2147483648")
    }

    "be able to append a long" in {
      val builder = new CharBuilder
      builder.append(-10L).append(9223372036854775800L).append(Long.MinValue)
      builder.toString should equal("-109223372036854775800-9223372036854775808")
    }

    "be able to append a float" in {
      val builder = new CharBuilder
      builder.append(Float.MaxValue)
      builder.toString should equal("3.4028235E38")
    }

    "be able to append a double" in {
      val builder = new CharBuilder
      builder.append(Double.MaxValue)
      builder.toString should equal("1.7976931348623157E308")
    }

    "be able to append any object" in {
      val builder = new CharBuilder
      builder.append(10)
      builder.toString should equal("10")
    }

    "be able to append char builder" in {
      val builder = new CharBuilder
      val other = new CharBuilder
      other.append("bar")
      builder.append("foo")
      builder.append(other)
      builder.toString should equal("foobar")
    }

    "be able to grow until error" in {
      val builder = new CharBuilder
      assertThrows[OutOfMemoryError] {
        var i = 0L
        val value = 'a' * 10000000
        while (i < Long.MaxValue) {
          builder.append(value)
          i += 10000000
        }
      }
    }
  }

}
