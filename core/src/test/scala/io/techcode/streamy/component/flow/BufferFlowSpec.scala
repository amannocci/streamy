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
import com.typesafe.config.ConfigFactory
import io.techcode.streamy.StreamyTestSystem
import io.techcode.streamy.component.ComponentRegistry
import io.techcode.streamy.event.StreamEvent
import io.techcode.streamy.util.json._
import pureconfig.error.ConfigReaderException

/**
  * Buffer flow spec.
  */
class BufferFlowSpec extends StreamyTestSystem {

  private val componentRegistry = ComponentRegistry(system)

  "Buffer flow" should {
    "be get from component registry" in {
      componentRegistry.getFlow("buffer").isDefined should equal(true)
    }

    "be able to buffer with back-pressure" in {
      val conf = ConfigFactory.parseString("""{"max-size":1, "overflow-strategy": "back-pressure"}""")
      val stream = Source.single(StreamEvent(Json.obj("message" -> "foo")))
        .via(componentRegistry.getFlow("buffer").get(conf))
        .runWith(TestSink.probe[StreamEvent])

      stream.requestNext() should equal(StreamEvent(Json.obj("message" -> "foo")))
      stream.expectComplete()
    }

    "be able to buffer with drop tail" in {
      val conf = ConfigFactory.parseString("""{"max-size":1, "overflow-strategy": "drop-tail"}""")
      val stream = Source(Seq(
        StreamEvent(Json.obj("message" -> "foo")),
        StreamEvent(Json.obj("message" -> "bar"))
      )).via(componentRegistry.getFlow("buffer").get(conf))
        .runWith(TestSink.probe[StreamEvent])

      stream.requestNext() should equal(StreamEvent(Json.obj("message" -> "bar")))
      stream.expectComplete()
    }

    "be able to buffer with drop new" in {
      val conf = ConfigFactory.parseString("""{"max-size":1, "overflow-strategy": "drop-new"}""")
      val stream = Source(Seq(
        StreamEvent(Json.obj("message" -> "foo")),
        StreamEvent(Json.obj("message" -> "bar"))
      )).via(componentRegistry.getFlow("buffer").get(conf))
        .runWith(TestSink.probe[StreamEvent])

      stream.requestNext() should equal(StreamEvent(Json.obj("message" -> "foo")))
      stream.expectComplete()
    }

    "be able to buffer with drop head" in {
      val conf = ConfigFactory.parseString("""{"max-size":1, "overflow-strategy": "drop-head"}""")
      val stream = Source(Seq(
        StreamEvent(Json.obj("message" -> "foo")),
        StreamEvent(Json.obj("message" -> "bar"))
      )).via(componentRegistry.getFlow("buffer").get(conf))
        .runWith(TestSink.probe[StreamEvent])

      stream.requestNext() should equal(StreamEvent(Json.obj("message" -> "bar")))
      stream.expectComplete()
    }

    "be able to buffer with drop buffer" in {
      val conf = ConfigFactory.parseString("""{"max-size":1, "overflow-strategy": "drop-buffer"}""")
      val stream = Source(Seq(
        StreamEvent(Json.obj("message" -> "foo")),
        StreamEvent(Json.obj("message" -> "bar"))
      )).via(componentRegistry.getFlow("buffer").get(conf))
        .runWith(TestSink.probe[StreamEvent])

      stream.requestNext() should equal(StreamEvent(Json.obj("message" -> "bar")))
      stream.expectComplete()
    }

    "throw error with wrong overflow strategy" in {
      val conf = ConfigFactory.parseString("""{"max-size":1, "overflow-strategy": "unknown"}""")
      intercept[ConfigReaderException[_]] {
        componentRegistry.getFlow("buffer").get(conf)
      }.getMessage() should include("Overflow strategy must be one of")
    }
  }

}


