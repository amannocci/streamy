/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017-2019
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

import io.techcode.streamy.event.StreamEvent
import io.techcode.streamy.util.json._
import org.openjdk.jmh.annotations.Benchmark

/**
  * Date transformer bench.
  */
class DateTransformerBench {

  @Benchmark def idempotent(): MaybeJson = DateTransformerBench.Transformer.Idempotent(DateTransformerBench.Sample.Iso8601)

  @Benchmark def fromIsoToCustom(): MaybeJson = DateTransformerBench.Transformer.FromIsoToCustom(DateTransformerBench.Sample.Iso8601)

  @Benchmark def fromCustomToIso(): MaybeJson = DateTransformerBench.Transformer.FromCustomToIso(DateTransformerBench.Sample.Custom)

}

object DateTransformerBench {

  object Sample {

    val Iso8601: StreamEvent = StreamEvent(Json.parseStringUnsafe("""{"date":"2018-10-01T15:10:30Z"}"""))

    val Custom: StreamEvent = StreamEvent(Json.parseStringUnsafe("""{"date":"Wed Oct 11 14:32:52 2000"}"""))

  }

  object Transformer {

    val Idempotent: DateTransformerLogic = new DateTransformerLogic(DateTransformer.Config(
      source = Root / "date",
      inputFormatter = DateTransformer.Iso8601,
      outputFormatter = DateTransformer.Iso8601
    ))

    val FromIsoToCustom: DateTransformerLogic = new DateTransformerLogic(DateTransformer.Config(
      source = Root / "date",
      inputFormatter = DateTransformer.Iso8601,
      outputFormatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss yyyy")
    ))

    val FromCustomToIso: DateTransformerLogic = new DateTransformerLogic(DateTransformer.Config(
      source = Root / "date",
      inputFormatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss yyyy"),
      outputFormatter = DateTransformer.Iso8601
    ))

  }

}
