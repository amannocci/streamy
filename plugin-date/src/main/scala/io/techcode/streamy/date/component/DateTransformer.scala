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
package io.techcode.streamy.date.component

import java.time.format.DateTimeFormatter
import akka.NotUsed
import akka.stream.scaladsl.Flow
import io.techcode.streamy.component.FlowTransformer.SuccessBehaviour
import io.techcode.streamy.component.FlowTransformer.SuccessBehaviour.SuccessBehaviour
import io.techcode.streamy.component.Transformer.ErrorBehaviour
import io.techcode.streamy.component.Transformer.ErrorBehaviour.ErrorBehaviour
import io.techcode.streamy.component.{FlowTransformer, FlowTransformerLogic}
import io.techcode.streamy.event.StreamEvent
import io.techcode.streamy.util.json._
import io.techcode.streamy.config._
import pureconfig._
import pureconfig.generic.semiauto._


/**
  * Date transformer implementation.
  */
private[component] class DateTransformerLogic(config: DateTransformer.Config) extends FlowTransformerLogic(config) {

  override val errorMsg: String = DateTransformer.genericErrorMsg

  override def transform(value: Json): MaybeJson = value
    .map[String](x => config.outputFormatter.format(config.inputFormatter.parse(x)))

}

/**
  * Date transformer companion.
  */
object DateTransformer {
  // Iso 8601
  val Iso8601: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX")

  // Configuration readers
  implicit val dateTimeFormatterReader: ConfigReader[DateTimeFormatter] = ConfigReader.fromString[DateTimeFormatter](
      ConvertHelpers.catchReadError(value => DateTimeFormatter.ofPattern(value))
    )

  implicit val configReader: ConfigReader[Config] = deriveReader[Config]

  // Generic error message if transform can't happen
  private[component] val genericErrorMsg = "Can't transform date"

  /**
    * Create a date transformer flow that transform incoming [[StreamEvent]] objects.
    *
    * @param conf flow configuration.
    * @return new date flow.
    */
  def apply(conf: Config): Flow[StreamEvent, StreamEvent, NotUsed] =
    Flow.fromGraph(new FlowTransformer {
      def factory(): FlowTransformerLogic = new DateTransformerLogic(conf)
    })

  // Component configuration
  case class Config(
    override val source: JsonPointer,
    override val target: Option[JsonPointer] = None,
    override val onSuccess: SuccessBehaviour = SuccessBehaviour.Skip,
    override val onError: ErrorBehaviour = ErrorBehaviour.Skip,
    inputFormatter: DateTimeFormatter,
    outputFormatter: DateTimeFormatter
  ) extends FlowTransformer.Config(source, target, onSuccess, onError)

}
