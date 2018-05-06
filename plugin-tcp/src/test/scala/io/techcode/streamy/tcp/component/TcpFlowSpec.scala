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
package io.techcode.streamy.tcp.component

import akka.Done
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import io.techcode.streamy.TestSystem
import io.techcode.streamy.tcp.component.TcpFlow.ReconnectConfig
import io.techcode.streamy.tcp.event.{TcpConnectionCloseEvent, TcpConnectionCreateEvent}

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Tcp flow spec.
  */
class TcpFlowSpec extends TestSystem {

  "Tcp flow" should {
    "send data correctly" in {
      val result = Source.single(TcpFlowSpec.Input)
        .via(TcpFlow.client(TcpFlowSpec.Flow.Simple))
        .runWith(Sink.ignore)

      whenReady(result, timeout(30 seconds), interval(100 millis)) { x =>
        x should equal(Done)
      }
    }

    "send data correctly with reconnect" in {
      val result = Source.single(TcpFlowSpec.Input)
        .via(TcpFlow.client(TcpFlowSpec.Flow.SimpleWithReconnect))
        .runWith(Sink.ignore)

      whenReady(result, timeout(30 seconds), interval(100 millis)) { x =>
        x should equal(Done)
      }
    }

    "send event correctly" in {
      system.eventStream.subscribe(testActor, classOf[TcpConnectionCreateEvent])
      system.eventStream.subscribe(testActor, classOf[TcpConnectionCloseEvent])
      Source.single(TcpFlowSpec.Input)
        .via(TcpFlow.client(TcpFlowSpec.Flow.Simple))
        .runWith(Sink.ignore)

      expectMsgClass(classOf[TcpConnectionCreateEvent])
      expectMsgClass(classOf[TcpConnectionCloseEvent])
    }

    "send event correctly with reconnect" in {
      system.eventStream.subscribe(testActor, classOf[TcpConnectionCreateEvent])
      system.eventStream.subscribe(testActor, classOf[TcpConnectionCloseEvent])
      Source.single(TcpFlowSpec.Input)
        .via(TcpFlow.client(TcpFlowSpec.Flow.SimpleWithReconnect))
        .runWith(Sink.ignore)

      expectMsgClass(classOf[TcpConnectionCreateEvent])
      expectMsgClass(classOf[TcpConnectionCloseEvent])
    }

  }

}

object TcpFlowSpec {

  val Input = ByteString("Hello world !")

  object Flow {

    val Simple = TcpFlow.ClientConfig(
      host = "localhost",
      port = 500
    )

    val SimpleWithReconnect = TcpFlow.ClientConfig(
      host = "localhost",
      port = 500,
      reconnect = Some(ReconnectConfig(
        minBackoff = 1 seconds,
        maxBackoff = 1 second,
        randomFactor = 0.2D
      ))
    )

  }

}
