/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017-2018
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

import io.techcode.streamy.util.json._
import org.openjdk.jmh.annotations.Benchmark

/**
  * Json transformer bench.
  *
  * Benchmark                                         Mode  Cnt        Score      Error  Units
  * JsonTransformerBench.benchSimpleFailure          thrpt   20  4892720,580 ± 6160,451  ops/s
  * JsonTransformerBench.benchSimpleSource           thrpt   20   642601,605 ± 1092,502  ops/s
  * JsonTransformerBench.benchSimpleSourceAndTarget  thrpt   20   559402,909 ± 1008,676  ops/s
  */
class JsonTransformerBench {

  @Benchmark def benchSimpleSource(): Json =
    JsonTransformerBench.Source(Json.obj("message" -> """{"test":"test"}"""))

  @Benchmark def benchSimpleSourceAndTarget(): Json =
    JsonTransformerBench.SourceAndTarget(Json.obj("message" -> """{"test":"test"}"""))

  @Benchmark def benchSimpleFailure(): Json =
    JsonTransformerBench.Failure(Json.obj("message" -> "test"))

}

/**
  * Json transformer bench companion.
  */
private object JsonTransformerBench {

  val Source = new DeserializerTransformer(JsonTransformer.Config(source = Root / "message"))

  val SourceAndTarget = new DeserializerTransformer(JsonTransformer.Config(source = Root / "message", target = Some(Root / "target")))

  val Failure = new DeserializerTransformer(JsonTransformer.Config(source = Root / "message"))

}
