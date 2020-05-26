/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2018-2019
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
package io.techcode.streamy.xymon.component

import akka.NotUsed
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import io.techcode.streamy.component.{SinkTransformer, SourceTransformer}
import io.techcode.streamy.event.StreamEvent
import io.techcode.streamy.util.json.Json
import io.techcode.streamy.util.parser.ByteStringParser
import io.techcode.streamy.util.printer.ByteStringPrinter
import io.techcode.streamy.util.{Binder, NoneBinder}
import io.techcode.streamy.xymon.util.parser.XymonParser
import io.techcode.streamy.xymon.util.printer.XymonPrinter

/**
  * Xymon transformer companion.
  */
object XymonTransformer {

  /**
    * Create a xymon flow that transform incoming [[ByteString]] to [[StreamEvent]].
    *
    * @param conf flow configuration.
    */
  def parser(conf: Parser.Config): Flow[ByteString, StreamEvent, NotUsed] =
    Flow.fromGraph(new SourceTransformer {
      def factory(): ByteStringParser[Json] = XymonParser.parser(conf)
    })

  /**
    * Create a xymon flow that transform incoming [[StreamEvent]] to [[ByteString]].
    *
    * @param conf flow configuration.
    */
  def printer[T](conf: Printer.Config): Flow[StreamEvent, ByteString, NotUsed] =
    Flow.fromGraph(new SinkTransformer {
      def factory(): ByteStringPrinter[Json] = XymonPrinter.printer(conf)
    })

  object Id {
    val Status = "status"
    val Lifetime = "lifetime"
    val Group = "group"
    val Host = "host"
    val Service = "service"
    val Color = "color"
    val Message = "message"
  }

  object Parser {

    case class Binding(
      lifetime: Binder = NoneBinder,
      group: Binder = NoneBinder,
      host: Binder = NoneBinder,
      service: Binder = NoneBinder,
      color: Binder = NoneBinder,
      message: Binder = NoneBinder
    )

    case class Config(
      binding: Binding = Binding()
    )

  }

  object Printer {

    case class Binding(
      lifetime: Binder = NoneBinder,
      group: Binder = NoneBinder,
      host: Binder = NoneBinder,
      service: Binder = NoneBinder,
      color: Binder = NoneBinder,
      message: Binder = NoneBinder
    )

    case class Config(
      binding: Binding = Binding()
    )

  }

}
