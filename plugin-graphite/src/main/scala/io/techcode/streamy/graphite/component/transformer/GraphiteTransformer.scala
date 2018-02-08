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
package io.techcode.streamy.graphite.component.transformer

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Framing => StreamFraming}
import akka.util.ByteString
import io.techcode.streamy.component.SourceTransformer
import io.techcode.streamy.graphite.util.parser.GraphiteParser
import io.techcode.streamy.util.json.Json
import io.techcode.streamy.util.parser.{Binder, ByteStringParser}

/**
  * Graphite transformer companion.
  */
object GraphiteTransformer {

  /**
    * Create a graphite flow that transform incoming [[ByteString]] to [[Json]].
    * This parser is compliant with Graphite protocol.
    *
    * @param conf flow configuration.
    * @return new graphite flow compliant with Graphite protocol.
    */
  def parser(conf: Config): Flow[ByteString, Json, NotUsed] = {
    StreamFraming.delimiter(ByteString("\n"), conf.maxSize, allowTruncation = true)
      .via(Flow.fromFunction(new SourceTransformer {
        override def newParser(pkt: ByteString): ByteStringParser = GraphiteParser.parser(pkt, conf)
      }))
  }

  // Fields binding
  case class Binding(
    path: Option[Binder] = None,
    value: Option[Binder] = None,
    timestamp: Option[Binder] = None
  )

  // Configuration
  case class Config(
    maxSize: Int = Int.MaxValue,
    binding: Binding = Binding()
  )

}
