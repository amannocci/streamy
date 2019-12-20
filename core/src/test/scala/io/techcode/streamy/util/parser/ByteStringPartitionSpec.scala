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

import java.nio.charset.StandardCharsets

import akka.util.ByteString
import org.scalatest._

/**
  * ByteString partition spec.
  */
class ByteStringPartitionSpec extends WordSpecLike with Matchers {

  "ByteStringPartition" should {
    "be convert to utf-8 string" in {
      val result = new ByteStringPartition(ByteString("foobar"), 0, 3)
      result.asString() should equal("foo")
    }

    "be convert to iso 8859 string" in {
      val result = new ByteStringPartition(ByteString("foobar"), 0, 3)
      result.asString(StandardCharsets.ISO_8859_1) should equal("foo")
    }

    "be convert to digit" in {
      val result = new ByteStringPartition(ByteString("123456"), 0, 3)
      result.asDigit() should equal(123)
    }

    "be convert to byte string" in {
      val result = new ByteStringPartition(ByteString("123456"), 0, 3)
      result.asBytes() should equal(ByteString("123"))
    }
  }

}
