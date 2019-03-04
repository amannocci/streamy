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
package io.techcode.streamy.date.component

import java.time.format.DateTimeFormatter

import io.techcode.streamy.util.json._
import org.openjdk.jmh.annotations.Benchmark

/**
  * Date transformer bench.
  *
  * Benchmark                              Mode  Cnt        Score      Error  Units
  * DateTransformerBench.fromCustomToIso  thrpt   20  1130483.695 ± 4606.799  ops/s
  * DateTransformerBench.fromIsoToCustom  thrpt   20  1198713.552 ± 3952.319  ops/s
  * DateTransformerBench.idempotent       thrpt   20  1359561.435 ± 2540.553  ops/s
  */
class DateTransformerBench {

  @Benchmark def idempotent(): Json = DateTransformerBench.Transformer.Idempotent(DateTransformerBench.Sample.Iso8601)

  @Benchmark def fromIsoToCustom(): Json = DateTransformerBench.Transformer.FromIsoToCustom(DateTransformerBench.Sample.Iso8601)

  @Benchmark def fromCustomToIso(): Json = DateTransformerBench.Transformer.FromCustomToIso(DateTransformerBench.Sample.Custom)

}

object DateTransformerBench {

  object Sample {

    val Iso8601: Json = Json.parse("""{"date":"2018-10-01T15:10:30Z"}""").getOrElse(JsNull)

    val Custom: Json = Json.parse("""{"date":"Wed Oct 11 14:32:52 2000"}""").getOrElse(JsNull)

  }

  object Transformer {

    val Idempotent: DateTransformer = new DateTransformer(DateTransformer.Config(
      source = Root / "date",
      inputFormatter = DateTransformer.Iso8601,
      outputFormatter = DateTransformer.Iso8601
    ))

    val FromIsoToCustom: DateTransformer = new DateTransformer(DateTransformer.Config(
      source = Root / "date",
      inputFormatter = DateTransformer.Iso8601,
      outputFormatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss yyyy")
    ))

    val FromCustomToIso: DateTransformer = new DateTransformer(DateTransformer.Config(
      source = Root / "date",
      inputFormatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss yyyy"),
      outputFormatter = DateTransformer.Iso8601
    ))

  }

}
