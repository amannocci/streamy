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

import akka.stream.scaladsl.Source
import akka.util.ByteString
import io.techcode.streamy.TestSystem
import io.techcode.streamy.tcp.component.TcpFlow.Client
import io.techcode.streamy.tcp.component.TcpSource.Server
import io.techcode.streamy.tcp.event.TcpEvent

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.{Failure, Success}
import scala.sys.process._

/**
  * Tcp source spec.
  */
class TcpSourceSpec extends TestSystem {

  import system.dispatcher

  "Tcp source" when {
    "tls is disabled" should {
      "receive event correctly" in {
        system.eventStream.subscribe(testActor, classOf[TcpEvent.Server.ConnectionCreated])
        system.eventStream.subscribe(testActor, classOf[TcpEvent.Server.ConnectionClosed])
        TcpSource.server(TcpSourceSpec.Source.Simple).run().onComplete {
          case Success(_) =>
            Source.single(TcpSourceSpec.Input)
              .runWith(TcpSink.client(TcpSourceSpec.Flow.Simple))
          case Failure(ex) => ex.printStackTrace()
        }
        expectMsgClass(1 minute, classOf[TcpEvent.Server.ConnectionCreated])
        expectMsgClass(1 minute, classOf[TcpEvent.Server.ConnectionClosed])
      }
    }

    "tls is enabled" should {
      "receive event correctly" in {
        system.eventStream.subscribe(testActor, classOf[TcpEvent.Server.ConnectionCreated])
        system.eventStream.subscribe(testActor, classOf[TcpEvent.Server.ConnectionClosed])
        TcpSource.server(TcpSourceSpec.Source.Secure).run().onComplete {
          case Success(_) =>
            Source.single(TcpSourceSpec.Input)
              .runWith(TcpSink.client(TcpSourceSpec.Flow.Secure))
          case Failure(ex) => ex.printStackTrace()
        }
        expectMsgClass(1 minute, classOf[TcpEvent.Server.ConnectionCreated])
        expectMsgClass(1 minute, classOf[TcpEvent.Server.ConnectionClosed])
      }
    }
  }

}

object TcpSourceSpec {

  val Input: ByteString = ByteString("Hello world !")

  object Source {

    val Simple: Server.Config = TcpSource.Server.Config(
      akka.stream.scaladsl.Flow[ByteString],
      host = "localhost",
      port = 4998
    )

    val Secure: Server.Config = TcpSource.Server.Config(
      akka.stream.scaladsl.Flow[ByteString],
      host = "localhost",
      port = 4999,
      secured = true
    )
  }

  object Flow {

    val Simple: Client.Config = TcpFlow.Client.Config(
      host = "localhost",
      port = 4998
    )

    val Secure: Client.Config = TcpFlow.Client.Config(
      host = "localhost",
      port = 4999,
      secured = true
    )

  }

}
