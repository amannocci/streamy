/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2021
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
package io.techcode.streamy.syslog.component

import akka.Done
import akka.actor.ActorSystem
import akka.stream.scaladsl.{Keep, Sink}
import io.techcode.streamy.event.StreamEvent
import io.techcode.streamy.tcp.component.{TcpFlow, TcpSink}
import pureconfig._
import pureconfig.generic.semiauto._

import scala.concurrent.Future
import scala.concurrent.duration.{Duration, FiniteDuration}

/**
  * Syslog sink companion.
  */
object SyslogSink {

  // Configuration readers
  implicit val reconnectConfigReader: ConfigReader[SyslogSink.ReconnectConfig] =
    deriveReader[SyslogSink.ReconnectConfig]

  // Reconnect component configuration
  case class ReconnectConfig(
    minBackoff: FiniteDuration,
    maxBackoff: FiniteDuration,
    randomFactor: Double
  ) {
    def toTcpReconnectConfig: TcpFlow.Client.ReconnectConfig = TcpFlow.Client.ReconnectConfig(
      minBackoff, maxBackoff, randomFactor
    )
  }

  /**
    * Create a syslog sink that receive [[StreamEvent]].
    * This client is compliant with Syslog rfc5424 protocol.
    *
    * @param config sink configuration.
    * @return new syslog sink compliant with Syslog rfc5424 protocol.
    */
  def client(config: Rfc5424.Config)(implicit system: ActorSystem): Sink[StreamEvent, Future[Done]] = {
    SyslogTransformer.printer(config.toTransformConfig)
      .toMat(TcpSink.client(config.toTcpConfig))(Keep.right)
  }

  /**
    * Create a syslog sink that receive [[StreamEvent]].
    * This client is compliant with Syslog rfc3164 protocol.
    *
    * @param config sink configuration.
    * @return new syslog sink compliant with Syslog rfc3164 protocol.
    */
  def client(config: Rfc3164.Config)(implicit system: ActorSystem): Sink[StreamEvent, Future[Done]] = {
    SyslogTransformer.printer(config.toTransformConfig)
      .toMat(TcpSink.client(config.toTcpConfig))(Keep.right)
  }


  // Rfc5424 related stuff
  object Rfc5424 {

    // Configuration readers
    implicit val configReader: ConfigReader[Config] = deriveReader[Config]

    // Configuration
    case class Config(
      host: String,
      port: Int,
      secured: Boolean = false,
      idleTimeout: Duration = Duration.Inf,
      connectTimeout: Duration = Duration.Inf,
      reconnect: Option[ReconnectConfig] = None,
      framing: SyslogTransformer.Framing.Framing = SyslogTransformer.Rfc5424.DefaultFraming,
      binding: SyslogTransformer.Rfc5424.Binding = SyslogTransformer.Rfc5424.DefaultBinding
    ) {

      def toTransformConfig: SyslogTransformer.Rfc5424.Config = SyslogTransformer.Rfc5424.Config(
        framing = framing,
        binding = binding
      )

      def toTcpConfig: TcpFlow.Client.Config = TcpFlow.Client.Config(
        host,
        port,
        secured,
        idleTimeout,
        reconnect = reconnect.map(_.toTcpReconnectConfig)
      )
    }

  }

  // Rfc3164 related stuff
  object Rfc3164 {

    // Configuration readers
    implicit val configReader: ConfigReader[Config] = deriveReader[Config]

    // Configuration
    case class Config(
      host: String,
      port: Int,
      secured: Boolean = false,
      idleTimeout: Duration = Duration.Inf,
      connectTimeout: Duration = Duration.Inf,
      reconnect: Option[ReconnectConfig] = None,
      framing: SyslogTransformer.Framing.Framing = SyslogTransformer.Rfc3164.DefaultFraming,
      binding: SyslogTransformer.Rfc3164.Binding = SyslogTransformer.Rfc3164.DefaultBinding
    ) {

      def toTransformConfig: SyslogTransformer.Rfc3164.Config = SyslogTransformer.Rfc3164.Config(
        framing = framing,
        binding = binding
      )

      def toTcpConfig: TcpFlow.Client.Config = TcpFlow.Client.Config(
        host,
        port,
        secured,
        idleTimeout,
        reconnect = reconnect.map(_.toTcpReconnectConfig)
      )
    }

  }

}
