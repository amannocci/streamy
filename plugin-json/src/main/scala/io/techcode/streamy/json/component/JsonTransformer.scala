/*
 * The MIT License (MIT)
 * <p>
 * Copyright (C) 2017-2019
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
package io.techcode.streamy.json.component

import akka.NotUsed
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import io.techcode.streamy.component.FlowTransformer
import io.techcode.streamy.component.FlowTransformer.SuccessBehaviour
import io.techcode.streamy.component.FlowTransformer.SuccessBehaviour.SuccessBehaviour
import io.techcode.streamy.component.Transformer.ErrorBehaviour
import io.techcode.streamy.component.Transformer.ErrorBehaviour.ErrorBehaviour
import io.techcode.streamy.json.component.JsonTransformer.Bind
import io.techcode.streamy.json.component.JsonTransformer.Bind.Bind
import io.techcode.streamy.json.component.JsonTransformer.Mode.Mode
import io.techcode.streamy.util.json._

/**
  * Json transformer companion.
  */
object JsonTransformer {

  // Component configuration
  case class Config(
    override val source: JsonPointer,
    override val target: Option[JsonPointer] = None,
    override val onSuccess: SuccessBehaviour = SuccessBehaviour.Skip,
    override val onError: ErrorBehaviour = ErrorBehaviour.Skip,
    mode: Mode = Mode.Deserialize,
    bind: Bind = Bind.String
  ) extends FlowTransformer.Config(source, target, onSuccess, onError)

  // Mode implementation
  object Mode extends Enumeration {
    type Mode = Value
    val Serialize, Deserialize = Value
  }

  // Bind implementation
  object Bind extends Enumeration {
    type Bind = Value
    val Bytes, String = Value
  }

  /**
    * Create a json transformer flow that transform incoming [[Json]] objects.
    *
    * @param conf flow configuration.
    * @return new json flow.
    */
  def apply(conf: Config): Flow[Json, Json, NotUsed] = {
    conf.mode match {
      case Mode.Serialize => Flow.fromFunction(new SerializerTransformer(conf))
      case Mode.Deserialize => Flow.fromFunction(new DeserializerTransformer(conf))
    }
  }

}

/**
  * Json serializer transformer implementation.
  *
  * @param config json transformer configuration.
  */
private class SerializerTransformer(config: JsonTransformer.Config) extends FlowTransformer(config) {

  @inline override def transform(value: Json): MaybeJson = {
    config.bind match {
      case Bind.String => value.toString
      case Bind.Bytes => ByteString(value.toString)
    }
  }

}

/**
  * Json deserializer transformer implementation.
  *
  * @param config json transformer configuration.
  */
private class DeserializerTransformer(config: JsonTransformer.Config) extends FlowTransformer(config) {

  private val byteStringJsonParser = JsonParser.byteStringParser()
  private val stringJsonParser = JsonParser.stringParser()

  @inline override def transform(value: Json): MaybeJson = {
    // Try to avoid parsing of wrong json
    config.bind match {
      case Bind.String => value.map[String](x => handle(value, stringJsonParser.parse(x)))
      case Bind.Bytes => value.map[ByteString](x => handle(value, byteStringJsonParser.parse(x)))
    }
  }

  private def handle(data: Json, result: Either[Throwable, Json]): Json = result match {
    case Right(succ) => succ
    case Left(ex) => onError(state = data, ex = Some(ex))
  }

}
