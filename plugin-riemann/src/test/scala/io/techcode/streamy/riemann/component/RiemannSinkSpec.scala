/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2018-2021
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
package io.techcode.streamy.riemann.component

import akka.stream.scaladsl.{Keep, Source}
import akka.stream.testkit.scaladsl.TestSink
import io.techcode.streamy.TestSystem
import io.techcode.streamy.event.StreamEvent
import io.techcode.streamy.util.json._

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}

/**
  * Riemann sink spec.
  */
class RiemannSinkSpec extends TestSystem {

  import system.dispatcher

  "Riemann sink" should {
    "send stream event correctly to server" in {
      val runnable = RiemannSource.server(RiemannSinkSpec.Source.Simple)
        .toMat(TestSink.probe[StreamEvent])(Keep.both)
        .run()

      runnable._1.onComplete {
        case Success(_) =>
          Source.single(RiemannSinkSpec.Event)
            .runWith(RiemannSink.client(RiemannSinkSpec.Sink.Simple))
          Source.single(RiemannSinkSpec.Event)
            .runWith(RiemannSink.client(RiemannSinkSpec.Sink.Simple))
        case Failure(ex) => ex.printStackTrace()
      }

      runnable._2.requestNext(5 seconds) should equal(RiemannSinkSpec.Event)
    }
  }

}


object RiemannSinkSpec {

  val Event: StreamEvent = StreamEvent(Json.obj("ok" -> true))

  object Source {

    val Simple: RiemannSource.Config = RiemannSource.Config(
      host = "localhost",
      port = 8080
    )

  }

  object Sink {

    val Simple: RiemannSink.Config = RiemannSink.Config(
      host = "localhost",
      port = 8080
    )

  }

}



