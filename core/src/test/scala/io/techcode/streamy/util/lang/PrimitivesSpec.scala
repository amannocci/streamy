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

import com.google.common.math.{IntMath, LongMath}
import io.techcode.streamy.StreamyTestSystem

/**
  * Primitives spec.
  */
class PrimitivesSpec extends StreamyTestSystem {

  "Primitives" should {
    "return correct string size for int" in {
      // Positive cases
      var size = 1
      for (i <- 0 until String.valueOf(Int.MaxValue).length) {
        Primitives.stringSize(1 * IntMath.pow(10, i)) should equal(size)
        size += 1
      }

      // Negative cases
      size = 2
      for (i <- 0 until String.valueOf(Int.MaxValue).length) {
        Primitives.stringSize(-1 * IntMath.pow(10, i)) should equal(size)
        size += 1
      }

      Primitives.stringSize(Int.MinValue) should equal(Int.MinValue.toString.length)
      Primitives.stringSize(Int.MaxValue) should equal(Int.MaxValue.toString.length)
    }

    "return correct string size for long" in {
      var size = 1
      for (i <- 0 until String.valueOf(Long.MaxValue).length) {
        Primitives.stringSize(1 * LongMath.pow(10, i)) should equal(size)
        size += 1
      }

      // Negative cases
      size = 2
      for (i <- 0 until String.valueOf(Long.MaxValue).length) {
        Primitives.stringSize(-1 * LongMath.pow(10, i)) should equal(size)
        size += 1
      }

      Primitives.stringSize(Long.MinValue) should equal(Long.MinValue.toString.length)
      Primitives.stringSize(Long.MaxValue) should equal(Long.MaxValue.toString.length)
    }
  }

}
