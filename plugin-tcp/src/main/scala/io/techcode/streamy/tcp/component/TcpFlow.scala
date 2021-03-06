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

import java.net.InetSocketAddress
import akka.actor.ActorSystem
import akka.io.Inet.SocketOption
import akka.stream.{IgnoreComplete, TLSClientAuth, TLSProtocol}
import akka.stream.scaladsl.{Flow, RestartFlow, Sink, Tcp}
import akka.util.ByteString
import io.techcode.streamy.tcp.event.TcpEvent
import io.techcode.streamy.tcp.util.TlsContext

import scala.concurrent.Future
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.Success

/**
  * Tcp flow companion.
  */
object TcpFlow {

  // Client related stuff
  object Client {

    case class Config(
      host: String,
      port: Int,
      secured: Boolean = false,
      idleTimeout: Duration = Duration.Inf,
      connectTimeout: Duration = Duration.Inf,
      reconnect: Option[ReconnectConfig] = None,
      options: Seq[SocketOption] = Nil
    )

    // Reconnect component configuration
    case class ReconnectConfig(
      minBackoff: FiniteDuration,
      maxBackoff: FiniteDuration,
      randomFactor: Double
    )

  }

  /**
    * Create a new tcp client flow.
    *
    * @param config flow configuration.
    */
  def client(config: Client.Config)(implicit system: ActorSystem): Flow[ByteString, ByteString, Any] = {
    // Connection configuration
    val connection: Flow[ByteString, ByteString, Any] = Flow.lazyFutureFlow[ByteString, ByteString, Any] { () =>
      // Lazily create a connection on first element
      system.eventStream.publish(TcpEvent.Client.ConnectionCreated(config))
      Future.successful(
        {
          if (config.secured) {
            tlsConnection(config)
          } else {
            plainConnection(config)
          }
        }.alsoTo(Sink.onComplete { _ =>
          system.eventStream.publish(TcpEvent.Client.ConnectionClosed(config))
        }))
    }


    // Wrap with reconnect if needed
    if (config.reconnect.isDefined) {
      val reconnectConfig = config.reconnect.get
      RestartFlow.withBackoff(
        minBackoff = reconnectConfig.minBackoff,
        maxBackoff = reconnectConfig.maxBackoff,
        randomFactor = reconnectConfig.randomFactor
      ) { () =>
        connection
      }
    } else {
      connection
    }
  }

  /**
    * Create a tcp plain connection.
    *
    * @param config flow configuration.
    * @return tcp plain connection.
    */
  private def plainConnection(config: Client.Config)(implicit system: ActorSystem): Flow[ByteString, ByteString, Any] =
    Tcp().outgoingConnection(
      InetSocketAddress.createUnresolved(config.host, config.port),
      connectTimeout = config.connectTimeout,
      idleTimeout = config.idleTimeout,
      options = config.options
    )

  /**
    * Create a tcp tls connection.
    *
    * @param config flow configuration.
    * @return tcp tls connection.
    */
  private def tlsConnection(config: Client.Config)(implicit system: ActorSystem): Flow[ByteString, ByteString, Any] = {
    val ctx = TlsContext.newSSLContext()

    // Tls connection
    Tcp().outgoingConnectionWithTls(
      InetSocketAddress.createUnresolved(config.host, config.port),
      createSSLEngine = () => {
        val engine = ctx.createSSLEngine()
        engine.setUseClientMode(true)
        engine
      },
      localAddress = None,
      connectTimeout = config.connectTimeout,
      idleTimeout = config.idleTimeout,
      options = config.options,
      verifySession = _ => Success(()),
      closing = IgnoreComplete
    )
  }

}
