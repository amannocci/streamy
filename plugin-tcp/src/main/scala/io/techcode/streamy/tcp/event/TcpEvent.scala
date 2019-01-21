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
package io.techcode.streamy.tcp.event

import java.net.InetSocketAddress

import akka.actor.DeadLetterSuppression
import io.techcode.streamy.tcp.component.TcpFlow

/**
  * Tcp events.
  */
object TcpEvent {

  // Marker interface for tcp events
  sealed trait All extends DeadLetterSuppression

  object Server {

    /**
      * This event is fired when a tcp server connection is created.
      *
      * @param localAddress  Local IP Socket address.
      * @param remoteAddress Remote IP Socket address.
      */
    case class ConnectionCreated(localAddress: InetSocketAddress, remoteAddress: InetSocketAddress) extends All

    /**
      * This event is fired when a tcp server connection is closed.
      *
      * @param localAddress  Local IP Socket address.
      * @param remoteAddress Remote IP Socket address.
      */
    case class ConnectionClosed(localAddress: InetSocketAddress, remoteAddress: InetSocketAddress) extends All

  }

  object Client {

    /**
      * This event is fired when a tcp client connection is created.
      *
      * @param config configuration of the tcp connection created.
      */
    case class ConnectionCreated(config: TcpFlow.Client.Config) extends All

    /**
      * This event is fired when a tcp client connection is closed.
      *
      * @param config configuration of the tcp connection closed.
      */
    case class ConnectionClosed(config: TcpFlow.Client.Config) extends All

  }

}
