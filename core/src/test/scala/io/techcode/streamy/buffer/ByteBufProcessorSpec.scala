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

import org.scalatest._

/**
  * ByteBuf processor spec.
  */
class ByteBufProcessorSpec extends WordSpecLike with Matchers {

  "A ByteBuf processor" must {
    "be able to detect a nul byte" in {
      ByteBufProcessor.FindNul.process('\0') should be(false)
    }

    "be able to detect a non nul byte" in {
      ByteBufProcessor.FindNonNul.process('e') should be(false)
    }

    "be able to detect a cr byte" in {
      ByteBufProcessor.FindCR.process('\r') should be(false)
    }

    "be able to detect a non cr byte" in {
      ByteBufProcessor.FindNonCR.process('e') should be(false)
    }

    "be able to detect a lf byte" in {
      ByteBufProcessor.FindLf.process('\n') should be(false)
    }

    "be able to detect a non lf byte" in {
      ByteBufProcessor.FindNonLf.process('e') should be(false)
    }

    "be able to detect a space byte" in {
      ByteBufProcessor.FindSpace.process(' ') should be(false)
    }

    "be able to detect an open bracket byte" in {
      ByteBufProcessor.FindOpenBracket.process('[') should be(false)
    }

    "be able to detect an close bracket byte" in {
      ByteBufProcessor.FindCloseBracket.process(']') should be(false)
    }

    "be able to detect an open quote byte" in {
      ByteBufProcessor.FindOpenQuote.process('<') should be(false)
    }

    "be able to detect an close quote byte" in {
      ByteBufProcessor.FindCloseQuote.process('>') should be(false)
    }

    "be able to detect a semi colon" in {
      ByteBufProcessor.FindSemiColon.process(':') should be(false)
    }
  }

}