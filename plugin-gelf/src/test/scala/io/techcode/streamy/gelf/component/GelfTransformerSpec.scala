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
package io.techcode.streamy.gelf.component

import akka.NotUsed
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import io.techcode.streamy.component.TestTransformer
import io.techcode.streamy.event.StreamEvent
import io.techcode.streamy.util.json.Json

/**
  * Graphite transformer spec.
  */
class GelfTransformerSpec extends TestTransformer {

  "Gelf transformer" should {
    "parser correctly gelf message" in {
      except(
        GelfTransformerSpec.Transformer.Parsing,
        GelfTransformerSpec.Input.Parser,
        GelfTransformerSpec.Output.Parser
      )
    }

    "fail on bad input" in {
      exceptError(
        GelfTransformerSpec.Transformer.Parsing,
        GelfTransformerSpec.Input.Bad,
      )
    }
  }

}

object GelfTransformerSpec {

  object Input {

    val Parser: ByteString = ByteString("""{"foo":"bar"}""") ++ ByteString("\u0000")

    val Bad: ByteString = ByteString("""{"foo":bar}""") ++ ByteString("\u0000")

  }

  object Transformer {
    val Parsing: Flow[ByteString, StreamEvent, NotUsed] = GelfTransformer.parser()
  }

  object Output {

    val Parser: StreamEvent = StreamEvent(Json.obj(
      "foo" -> "bar"
    ))

  }

}

