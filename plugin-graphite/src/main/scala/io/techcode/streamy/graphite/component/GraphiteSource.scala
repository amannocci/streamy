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
package io.techcode.streamy.graphite.component

import akka.actor.ActorSystem
import akka.stream.scaladsl.Tcp.ServerBinding
import akka.stream.scaladsl.{Keep, Source}
import io.techcode.streamy.event.StreamEvent
import io.techcode.streamy.graphite.component.GraphiteTransformer.{Binding, DefaultBinding}
import io.techcode.streamy.tcp.component.TcpSource
import io.techcode.streamy.tcp.component.TcpSource.Server.DefaultBacklog
import pureconfig._
import pureconfig.generic.semiauto._

import scala.concurrent.Future
import scala.concurrent.duration.Duration

/**
  * Graphite source companion.
  */
object GraphiteSource {

  // Default values
  private val DefaultMaxConnections = 1024

  // Configuration readers
  implicit val configReader: ConfigReader[GraphiteSource.Config] = deriveReader[GraphiteSource.Config]

  /**
    * Create a graphite source that emit [[StreamEvent]].
    * This server is compliant with Graphite protocol.
    *
    * @param config source configuration.
    * @return new graphite source compliant with Graphite protocol.
    */
  def server(config: Config)(implicit system: ActorSystem): Source[StreamEvent, Future[ServerBinding]] = {
    TcpSource.server(config.toTcpConfig)
      .map { conn =>
        Source.maybe
          .viaMat(conn)(Keep.right)
          .via(GraphiteTransformer.parser(config.toTransformConfig))
      }.flatMapMerge(config.maxConnections, identity)
  }


  // Configuration
  case class Config(
    host: String,
    port: Int,
    maxConnections: Int = DefaultMaxConnections,
    secured: Boolean = false,
    idleTimeout: Duration = Duration.Inf,
    backlog: Int = DefaultBacklog,
    maxSize: Int = Int.MaxValue,
    binding: Binding = DefaultBinding
  ) {

    def toTransformConfig: GraphiteTransformer.Config = GraphiteTransformer.Config(maxSize, binding)

    def toTcpConfig: TcpSource.Server.Config = TcpSource.Server.Config(
      host,
      port,
      secured,
      idleTimeout,
      backlog
    )
  }

}
