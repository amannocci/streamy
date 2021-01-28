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
package io.techcode.streamy.graphite.component

import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.TestSink
import akka.util.ByteString
import io.techcode.streamy.TestSystem
import io.techcode.streamy.event.StreamEvent
import io.techcode.streamy.graphite.component.GraphiteSourceSpec.Output
import io.techcode.streamy.graphite.component.GraphiteTransformer.Binding
import io.techcode.streamy.tcp.component.TcpFlow.Client
import io.techcode.streamy.tcp.component.{TcpFlow, TcpSink}
import io.techcode.streamy.util.json.Json

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * Graphite source spec.
  */
class GraphiteSourceSpec extends TestSystem {

  import system.dispatcher

  "Graphite source" should {
    "emit stream event correctly from multiples connections" in {
      val runnable = GraphiteSource.server(GraphiteSourceSpec.Source.Simple)
        .toMat(TestSink.probe[StreamEvent])(Keep.both)
        .run()

      runnable._1.onComplete {
        case Success(_) =>
          Source.single(GraphiteSourceSpec.Input)
            .runWith(TcpSink.client(GraphiteSourceSpec.Sink.Simple))
          Source.single(GraphiteSourceSpec.Input)
            .runWith(TcpSink.client(GraphiteSourceSpec.Sink.Simple))
        case Failure(ex) => ex.printStackTrace()
      }

      runnable._2.requestNext(5 seconds) should equal(Output)
      runnable._2.requestNext(5 seconds) should equal(Output)
    }
  }

}

object GraphiteSourceSpec {

  val Input: ByteString = ByteString("gatling.basicsimulation.allRequests.ok.count 2 1518123202\n")

  val Output: StreamEvent = StreamEvent(Json.obj(
    "timestamp" -> 1518123202L,
    "path" -> "gatling.basicsimulation.allRequests.ok.count",
    "value" -> 2F
  ))

  object Source {

    val Simple: GraphiteSource.Config = GraphiteSource.Config(
      host = "localhost",
      port = 8080,
      binding = Binding(
        path = Some("path"),
        value = Some("value"),
        timestamp = Some("timestamp")
      )
    )

  }

  object Sink {

    val Simple: Client.Config = TcpFlow.Client.Config(
      host = "localhost",
      port = 8080
    )

  }

}
