/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017-2021
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
package io.techcode.streamy.component.flow

import akka.NotUsed
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.Flow
import io.techcode.streamy.event.StreamEvent
import pureconfig._
import pureconfig.error.FailureReason
import pureconfig.generic.semiauto._

object BufferFlow {

  // Component configuration
  case class Config(
    maxSize: Int,
    overflowStrategy: OverflowStrategy
  )

  // Configuration readers
  implicit val overflowStrategyReader: ConfigReader[OverflowStrategy] = ConfigReader.fromString[OverflowStrategy] {
    case "back-pressure" => Right(OverflowStrategy.backpressure)
    case "drop-buffer" => Right(OverflowStrategy.dropBuffer)
    case "drop-head" => Right(OverflowStrategy.dropHead)
    case "drop-new" => Right(OverflowStrategy.dropNew)
    case "drop-tail" => Right(OverflowStrategy.dropTail)
    case _ => Left(new FailureReason {
      val validStrategy = Seq("back-pressure", "drop-buffer", "drop-head", "drop-new", "drop-tail")

      override def description: String = s"Overflow strategy must be one of '${validStrategy.mkString(",")}'"
    })
  }

  implicit val configReader: ConfigReader[Config] = deriveReader[Config]

  /**
    * Create a buffer flow that buffer incoming [[StreamEvent]] objects.
    *
    * @param conf flow configuration.
    * @return new buffer flow.
    */
  def apply(conf: Config): Flow[StreamEvent, StreamEvent, NotUsed] = Flow[StreamEvent]
    .buffer(conf.maxSize, conf.overflowStrategy)

}
