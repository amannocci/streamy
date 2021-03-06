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
package io.techcode.streamy.component

import akka.stream.ActorAttributes.supervisionStrategy
import akka.stream.Supervision
import akka.stream.scaladsl.Source
import akka.stream.testkit.scaladsl.TestSink
import akka.util.ByteString
import io.techcode.streamy.StreamyTestSystem
import io.techcode.streamy.event.StreamEvent
import io.techcode.streamy.util.StreamException
import io.techcode.streamy.util.json._
import io.techcode.streamy.util.printer.{ByteStringPrinter, PrintException}

/**
  * Sink transformer spec.
  */
class SinkTransformerSpec extends StreamyTestSystem {

  "Sink transformer" should {
    "handle correctly a json value" in {
      val sink = new SinkTransformer {
        def factory(): ByteStringPrinter[Json] = () => ByteString.empty
      }

      Source.single(StreamEvent(Json.obj("foo" -> "bar")))
        .via(sink)
        .runWith(TestSink.probe[ByteString])
        .requestNext() should equal(ByteString.empty)
    }

    "handle correctly a json value with skipped error" in {
      val sink = new SinkTransformer {
        def factory(): ByteStringPrinter[Json] = () => throw new PrintException("Error")
      }

      Source.single(StreamEvent(Json.obj("foo" -> "bar")))
        .via(sink)
        .addAttributes(supervisionStrategy(_ => Supervision.Resume))
        .runWith(TestSink.probe[ByteString])
        .request(1)
        .expectComplete()
    }

    "handle correctly a json value with error" in {
      val sink = new SinkTransformer {
        def factory(): ByteStringPrinter[Json] = () => throw new PrintException("Error")
      }

      Source.single(StreamEvent(Json.obj("foo" -> "bar")))
        .via(sink)
        .addAttributes(supervisionStrategy(_ => Supervision.Stop))
        .runWith(TestSink.probe[ByteString])
        .request(1)
        .expectError() should equal(new StreamException(StreamEvent(Json.obj("foo" -> "bar")), new PrintException("Error")))
    }
  }

}

