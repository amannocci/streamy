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
package io.techcode.streamy.graphite.component

import akka.NotUsed
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import io.techcode.streamy.component.TestTransformer
import io.techcode.streamy.util.json._
import io.techcode.streamy.util.parser.{FloatBinder, LongBinder, StringBinder}

/**
  * Graphite transformer spec.
  */
class GraphiteTransformerSpec extends TestTransformer {

  "Graphite transformer" should {
    "parser" should {
      "handle correctly graphite packet" in {
        except(
          GraphiteTransformerSpec.Transformer.Parsing,
          GraphiteTransformerSpec.Input.Parser,
          GraphiteTransformerSpec.Output.Parser
        )
      }
    }
  }

}

object GraphiteTransformerSpec {

  object Input {

    val Parser: ByteString = ByteString("gatling.basicsimulation.allRequests.ok.count 2 1518123202\n")

  }

  object Transformer {
    val Binding = GraphiteTransformer.Binding(
      path = Some(StringBinder("key")),
      value = Some(FloatBinder("value")),
      timestamp = Some(LongBinder("timestamp")),
    )

    val Parsing: Flow[ByteString, Json, NotUsed] = GraphiteTransformer.parser(GraphiteTransformer.Config(
      binding = Binding
    ))

  }

  object Output {

    val Parser: Json = Json.obj(
      "key" -> "gatling.basicsimulation.allRequests.ok.count",
      "value" -> 2F,
      "timestamp" -> 1518123202L
    )

  }


}