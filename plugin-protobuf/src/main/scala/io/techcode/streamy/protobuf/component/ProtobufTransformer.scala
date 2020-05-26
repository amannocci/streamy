/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2018-2020
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
package io.techcode.streamy.protobuf.component

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Framing}
import akka.util.ByteString
import com.google.protobuf.MessageLite
import io.techcode.streamy.event.StreamEvent

/**
  * Protobuf transformer companion.
  */
object ProtobufTransformer {

  /**
    * Create a protobuf flow that transform incoming [[ByteString]] to [[StreamEvent]].
    *
    * @param conf flow configuration.
    * @return new protobuf flow.
    */
  def parser[T <: MessageLite](conf: Parser.Config[T]): Flow[ByteString, StreamEvent, NotUsed] =
    Framing.simpleFramingProtocolDecoder(conf.maxSize)
      .map(raw => conf.proto.getParserForType.parseFrom(raw.asByteBuffer).asInstanceOf[T])
      .via(Flow.fromFunction(conf.decoder))

  /**
    * Create a protobuf flow that transform incoming [[StreamEvent]] to [[ByteString]].
    *
    * @param conf flow configuration.
    * @return new protobuf flow.
    */
  def printer[T <: MessageLite](conf: Printer.Config[T]): Flow[StreamEvent, ByteString, NotUsed] =
    Flow.fromFunction(conf.encoder)
      .map(obj => ByteString.fromArrayUnsafe(obj.toByteArray))
      .via(Framing.simpleFramingProtocolEncoder(conf.maxSize))

  // Parser related stuff
  object Parser {

    // Configuration
    case class Config[T <: MessageLite](
      maxSize: Int = Int.MaxValue - 4,
      proto: MessageLite,
      decoder: T => StreamEvent
    )

  }

  // Printer related stuff
  object Printer {

    // Configuration
    case class Config[T <: MessageLite](
      maxSize: Int = Int.MaxValue - 4,
      proto: MessageLite,
      encoder: StreamEvent => T
    )

  }

}
