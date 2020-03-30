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
import io.techcode.streamy.tcp.event.TcpEvent
import io.techcode.streamy.tcp.util.TcpSpec

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.sys.process._

/**
  * Tcp flow spec.
  */
class TcpFlowSpec extends TestSystem with TcpSpec {

  "Tcp flow" when {
    "tls is disabled" should {
      "send data correctly" in {
        val result = Source.single(TcpFlowSpec.Input)
          .via(TcpFlow.client(TcpFlowSpec.Flow.Simple.copy(port = ncatPlain.mappedPort(5000))))
          .runWith(Sink.ignore)
        whenReady(result, timeout(10 seconds), interval(500 millis)) { x =>
          x should equal(Done)
        }
      }

      "send data correctly with reconnect" in {
        val result = Source.single(TcpFlowSpec.Input)
          .via(TcpFlow.client(TcpFlowSpec.Flow.SimpleWithReconnect.copy(port = ncatPlain.mappedPort(5000))))
          .runWith(Sink.ignore)
        whenReady(result, timeout(10 seconds), interval(500 millis)) { x =>
          x should equal(Done)
        }
      }

      "send events correctly" in {
        system.eventStream.subscribe(testActor, classOf[TcpEvent.Client.ConnectionCreated])
        system.eventStream.subscribe(testActor, classOf[TcpEvent.Client.ConnectionClosed])
        Source.single(TcpFlowSpec.Input)
          .via(TcpFlow.client(TcpFlowSpec.Flow.Simple.copy(port = ncatPlain.mappedPort(5000))))
          .runWith(Sink.ignore)
        expectMsgClass(classOf[TcpEvent.Client.ConnectionCreated])
        expectMsgClass(classOf[TcpEvent.Client.ConnectionClosed])
      }

      "send events correctly with reconnect" in {
        system.eventStream.subscribe(testActor, classOf[TcpEvent.Client.ConnectionCreated])
        system.eventStream.subscribe(testActor, classOf[TcpEvent.Client.ConnectionClosed])
        Source.single(TcpFlowSpec.Input)
          .via(TcpFlow.client(TcpFlowSpec.Flow.SimpleWithReconnect.copy(port = ncatPlain.mappedPort(5000))))
          .runWith(Sink.ignore)
        expectMsgClass(classOf[TcpEvent.Client.ConnectionCreated])
        expectMsgClass(classOf[TcpEvent.Client.ConnectionClosed])
      }
    }

    "tls is enabled" should {
      "send data correctly" in {
        val result = Source.single(TcpFlowSpec.Input)
          .via(TcpFlow.client(TcpFlowSpec.Flow.Secure.copy(port = ncatTls.mappedPort(5000))))
          .runWith(Sink.ignore)
        whenReady(result, timeout(10 seconds), interval(500 millis)) { x =>
          x should equal(Done)
        }
      }

      "send data correctly with reconnect" in {
        val result = Source.single(TcpFlowSpec.Input)
          .via(TcpFlow.client(TcpFlowSpec.Flow.SecureWithReconnect.copy(port = ncatTls.mappedPort(5000))))
          .runWith(Sink.ignore)
        whenReady(result, timeout(10 seconds), interval(500 millis)) { x =>
          x should equal(Done)
        }
      }

      "send an events correctly" in {
        system.eventStream.subscribe(testActor, classOf[TcpEvent.Client.ConnectionCreated])
        system.eventStream.subscribe(testActor, classOf[TcpEvent.Client.ConnectionClosed])
        Source.single(TcpFlowSpec.Input)
          .via(TcpFlow.client(TcpFlowSpec.Flow.Secure.copy(port = ncatTls.mappedPort(5000))))
          .runWith(Sink.ignore)
        expectMsgClass(classOf[TcpEvent.Client.ConnectionCreated])
        expectMsgClass(classOf[TcpEvent.Client.ConnectionClosed])
      }

      "send an events correctly with reconnect" in {
        system.eventStream.subscribe(testActor, classOf[TcpEvent.Client.ConnectionCreated])
        system.eventStream.subscribe(testActor, classOf[TcpEvent.Client.ConnectionClosed])
        Source.single(TcpFlowSpec.Input)
          .via(TcpFlow.client(TcpFlowSpec.Flow.SecureWithReconnect.copy(port = ncatTls.mappedPort(5000))))
          .runWith(Sink.ignore)
        expectMsgClass(classOf[TcpEvent.Client.ConnectionCreated])
        expectMsgClass(classOf[TcpEvent.Client.ConnectionClosed])
      }
    }
  }

}

object TcpFlowSpec {

  val Input = ByteString("Hello world !")

  object Flow {
    val Simple = TcpFlow.Client.Config(
      host = "localhost",
      port = -1
    )

    val SimpleWithReconnect: TcpFlow.Client.Config = Simple.copy(
      reconnect = Some(TcpFlow.Client.ReconnectConfig(
        minBackoff = 1 seconds,
        maxBackoff = 1 second,
        randomFactor = 0.2D
      ))
    )

    val Secure = TcpFlow.Client.Config(
      host = "localhost",
      port = -1,
      secured = true
    )

    val SecureWithReconnect: TcpFlow.Client.Config = Secure.copy(
      port = -1,
      reconnect = Some(TcpFlow.Client.ReconnectConfig(
        minBackoff = 1 second,
        maxBackoff = 1 second,
        randomFactor = 0.2D
      ))
    )
  }
}
