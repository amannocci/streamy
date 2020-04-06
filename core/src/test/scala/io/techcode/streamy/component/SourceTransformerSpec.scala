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

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Source}
import akka.stream.testkit.scaladsl.TestSink
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import akka.util.ByteString
import io.techcode.streamy.StreamyTestSystem
import io.techcode.streamy.event.StreamEvent
import io.techcode.streamy.util.StreamException
import io.techcode.streamy.util.json._
import io.techcode.streamy.util.parser.{ByteStringParser, ParseException}

/**
  * Source transformer spec.
  */
class SourceTransformerSpec extends StreamyTestSystem {

  "Source transformer" should {
    "handle correctly a bytestring" in {
      val source = Flow.fromGraph(new SourceTransformer[Json, NotUsed] {

        def factory(): ByteStringParser[Json] = new ByteStringParser[Json] {
          override def run(): Json = Json.obj()

          override def root(): Boolean = true
        }

        def pack(payload: Json): StreamEvent[NotUsed] = StreamEvent.from(payload)
      })
      Source.single(ByteString.empty)
        .via(source)
        .runWith(TestSink.probe[StreamEvent[NotUsed]])
        .requestNext() should equal(StreamEvent.from(Json.obj()))
    }

    "handle correctly a bytestring with skipped error" in {
      val decider: Supervision.Decider = _ => Supervision.Resume

      implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(system).withSupervisionStrategy(decider))

      val source = Flow.fromGraph(new SourceTransformer[Json, NotUsed] {
        def factory(): ByteStringParser[Json] = new ByteStringParser[Json] {
          override def run() = throw new ParseException("Error")

          override def root(): Boolean = false
        }

        def pack(payload: Json): StreamEvent[NotUsed] = StreamEvent.from(payload)
      })

      Source.single(ByteString.empty)
        .via(source)
        .runWith(TestSink.probe[StreamEvent[NotUsed]])
        .request(1)
        .expectComplete()
    }

    "handle correctly a bytestring with error" in {
      val decider: Supervision.Decider = _ => Supervision.Stop

      implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(system)
        .withSupervisionStrategy(decider))

      val source = Flow.fromGraph(new SourceTransformer[Json, NotUsed] {
        def factory(): ByteStringParser[Json] = new ByteStringParser[Json] {
          override def run() = throw new ParseException("Error")

          override def root(): Boolean = false
        }

        def pack(payload: Json): StreamEvent[NotUsed] = StreamEvent.from(payload)
      })

      Source.single(ByteString.empty)
        .via(source)
        .runWith(TestSink.probe[StreamEvent[NotUsed]])
        .request(1)
        .expectError() should equal(new StreamException(StreamEvent.from(ByteString.empty), new ParseException("Error")))
    }
  }

}
