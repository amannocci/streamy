/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017
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
package io.techcode.streamy.json.component.transformer

import io.techcode.streamy.json.component.transformer.JsonTransformer.Config
import io.techcode.streamy.util.json._
import org.openjdk.jmh.annotations.Benchmark

/**
  * Json transformer bench.
  *
  * Benchmark                                         Mode  Cnt        Score       Error  Units
  * JsonTransformerBench.benchSimpleFailure          thrpt   20  3858517,923 ± 10333,010  ops/s
  * JsonTransformerBench.benchSimpleSource           thrpt   20   620497,203 ±  1062,658  ops/s
  * JsonTransformerBench.benchSimpleSourceAndTarget  thrpt   20   538585,604 ±   722,987  ops/s
  */
class JsonTransformerBench {

  @Benchmark def benchSimpleSource(): Json = {
    new JsonTransformer(Config(source = Root / "message"))
      .apply(Json.obj("message" -> """{"test":"test"}"""))
  }

  @Benchmark def benchSimpleSourceAndTarget(): Json = {
    new JsonTransformer(Config(source = Root / "message", target = Some(Root / "target")))
      .apply(Json.obj("message" -> """{"test":"test"}"""))
  }

  @Benchmark def benchSimpleFailure(): Json = {
    new JsonTransformer(Config(source = Root / "message"))
      .apply(Json.obj("message" -> "test"))
  }

}
