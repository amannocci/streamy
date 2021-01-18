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
package io.techcode.streamy.component.flow

import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Source
import akka.stream.testkit.scaladsl.TestSink
import io.techcode.streamy.StreamyTestSystem
import io.techcode.streamy.component.ComponentRegistry
import io.techcode.streamy.event.StreamEvent
import io.techcode.streamy.util.json._

/**
  * Buffer flow spec.
  */
class BufferFlowSpec extends StreamyTestSystem {

  "Buffer flow" should {
    "be get from component registry" in {
      val componentRegistry = ComponentRegistry(system)
      componentRegistry.getFlow("buffer").isDefined should equal(true)
    }

    "be able to buffer with back-pressure" in {
      val stream = Source.single(StreamEvent(Json.obj("message" -> "foo")))
        .via(BufferFlow(BufferFlow.Config(maxSize = 1, OverflowStrategy.backpressure)))
        .runWith(TestSink.probe[StreamEvent])

      stream.requestNext() should equal(StreamEvent(Json.obj("message" -> "foo")))
      stream.expectComplete()
    }

    "be able to buffer with drop tail" in {
      val stream = Source(Seq(
        StreamEvent(Json.obj("message" -> "foo")),
        StreamEvent(Json.obj("message" -> "bar"))
      )).via(BufferFlow(BufferFlow.Config(maxSize = 1, OverflowStrategy.dropTail)))
        .runWith(TestSink.probe[StreamEvent])

      stream.requestNext() should equal(StreamEvent(Json.obj("message" -> "bar")))
      stream.expectComplete()
    }

    "be able to buffer with drop new" in {
      val stream = Source(Seq(
        StreamEvent(Json.obj("message" -> "foo")),
        StreamEvent(Json.obj("message" -> "bar"))
      )).via(BufferFlow(BufferFlow.Config(maxSize = 1, OverflowStrategy.dropNew)))
        .runWith(TestSink.probe[StreamEvent])

      stream.requestNext() should equal(StreamEvent(Json.obj("message" -> "foo")))
      stream.expectComplete()
    }

    "be able to buffer with drop head" in {
      val stream = Source(Seq(
        StreamEvent(Json.obj("message" -> "foo")),
        StreamEvent(Json.obj("message" -> "bar"))
      )).via(BufferFlow(BufferFlow.Config(maxSize = 1, OverflowStrategy.dropHead)))
        .runWith(TestSink.probe[StreamEvent])

      stream.requestNext() should equal(StreamEvent(Json.obj("message" -> "bar")))
      stream.expectComplete()
    }

    "be able to buffer with drop buffer" in {
      val stream = Source(Seq(
        StreamEvent(Json.obj("message" -> "foo")),
        StreamEvent(Json.obj("message" -> "bar"))
      )).via(BufferFlow(BufferFlow.Config(maxSize = 1, OverflowStrategy.dropBuffer)))
        .runWith(TestSink.probe[StreamEvent])

      stream.requestNext() should equal(StreamEvent(Json.obj("message" -> "bar")))
      stream.expectComplete()
    }
  }

}


