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

import akka.actor.ActorSystem
import akka.io.Inet.SocketOption
import akka.stream.Materializer
import akka.stream.scaladsl.Tcp.{IncomingConnection, ServerBinding}
import akka.stream.scaladsl.{Flow, Sink, Source, Tcp}
import akka.util.ByteString
import io.techcode.streamy.tcp.event.TcpEvent
import io.techcode.streamy.tcp.util.TlsContext

import scala.collection.immutable
import scala.concurrent.Future
import scala.concurrent.duration.Duration

/**
  * Tcp source companion.
  */
object TcpSource {

  // Server related stuff
  object Server {

    // Default values
    val DefaultBacklog: Int = 100

    case class Config(
      handler: Flow[ByteString, ByteString, _],
      host: String,
      port: Int,
      secured: Boolean = false,
      idleTimeout: Duration = Duration.Inf,
      backlog: Int = DefaultBacklog,
      options: immutable.Traversable[SocketOption] = Nil
    )

  }

  /**
    * Create a new tcp server source.
    *
    * @param config flow configuration.
    */
  def server(config: Server.Config)(implicit system: ActorSystem, materializer: Materializer): Future[ServerBinding] = {
    {
      if (config.secured) {
        tlsServer(config)
      } else {
        plainServer(config)
      }
    }.to(Sink.foreach { conn: IncomingConnection ⇒
      system.eventStream.publish(TcpEvent.Server.ConnectionCreated(conn.localAddress, conn.remoteAddress))
      conn.flow.alsoTo(Sink.onComplete { _ =>
        system.eventStream.publish(TcpEvent.Server.ConnectionClosed(conn.localAddress, conn.remoteAddress))
      }).join(Flow.lazyInitAsync { () =>
        Future.successful(config.handler)
      }).run()
    }).run()
  }

  /**
    * Create a tcp plain service.
    *
    * @param config source configuration.
    * @return tcp plain server.
    */
  private def plainServer(config: Server.Config)(implicit system: ActorSystem): Source[IncomingConnection, Future[ServerBinding]] =
    Tcp().bind(
      config.host,
      config.port,
      config.backlog,
      config.options,
      halfClose = false,
      config.idleTimeout
    )

  /**
    * Create a tcp tls service.
    *
    * @param config source configuration.
    * @return tcp tls server.
    */
  private def tlsServer(config: Server.Config)(implicit system: ActorSystem): Source[IncomingConnection, Future[ServerBinding]] = {
    val ctx = TlsContext.create()

    // Tls connection
    Tcp().bindTls(
      config.host,
      config.port,
      ctx.sslContext,
      ctx.negotiateNewSession,
      config.backlog,
      config.options,
      config.idleTimeout
    )
  }


}
