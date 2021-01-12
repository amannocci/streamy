/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2020
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
package io.techcode.streamy.gelf.component

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Framing}
import akka.util.ByteString
import io.techcode.streamy.component.SourceTransformer
import io.techcode.streamy.event.StreamEvent
import io.techcode.streamy.util.json.{Json, JsonParser}
import io.techcode.streamy.util.parser.ByteStringParser

/**
  * Gelf transformer companion.
  */
object GelfTransformer {

  // Default configuration
  val DefaultConfig: Config = Config()

  // Null byte delimiter
  private val NullByteDelimiter: ByteString = ByteString("\u0000")

  /**
    * Create a gelf flow that transform incoming [[ByteString]] to [[StreamEvent]].
    * This parser is compliant with Gelf protocol.
    *
    * @param conf flow configuration.
    * @return new gelf flow compliant with Gelf protocol.
    */
  def parser(conf: Config = DefaultConfig): Flow[ByteString, StreamEvent, NotUsed] =
    Framing.delimiter(NullByteDelimiter, conf.maxSize, allowTruncation = true)
      .via(Flow.fromGraph(new SourceTransformer {
        override def factory(): ByteStringParser[Json] = JsonParser.byteStringParser()
      }))

  // Configuration
  case class Config(
    maxSize: Int = Int.MaxValue
  )

}
