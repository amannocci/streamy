/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017-2018
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
package io.techcode.streamy.component

import akka.util.ByteString
import io.techcode.streamy.util.StreamException
import io.techcode.streamy.util.json._
import io.techcode.streamy.util.printer.DerivedByteStringPrinter
import org.scalatest._

/**
  * Sink transformer spec.
  */
class SinkTransformerSpec extends WordSpec with Matchers {

  class Impl(succeeded: Boolean) extends SinkTransformer {
    def newPrinter(pkt: Json): DerivedByteStringPrinter = new DerivedByteStringPrinter(pkt) {
      override def process(): Boolean = succeeded
    }
  }

  "Sink transformer" should {
    "print correctly a json value when success" in {
      val sink = new Impl(true)
      sink(Json.obj("foo" -> "bar")) should equal(ByteString.empty)
    }

    "print correctly a json value when failed" in {
      val sink = new Impl(false)
      assertThrows[StreamException] {
        sink(Json.obj("foo" -> "bar"))
      }
    }
  }

}

