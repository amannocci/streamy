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
package io.techcode.streamy.util.json

import akka.util.ByteString
import org.openjdk.jmh.annotations.Benchmark

/**
  * Json converter bench.
  *
  * Benchmark                             Mode  Cnt       Score      Error  Units
  * JsonConverterBench.parseByteString   thrpt   20  339328.830 ± 1576.074  ops/s
  * JsonConverterBench.parseBytes        thrpt   20  342529.538 ± 3368.933  ops/s
  * JsonConverterBench.parseString       thrpt   20  339976.360 ± 1783.041  ops/s
  * JsonConverterBench.print             thrpt   20  568632.750 ± 3483.906  ops/s
  */
class JsonConverterBench {

  @Benchmark def parseByteString(): Json = JsonConverter.parse(JsonConverterBench.Sample.ByteStringImpl).getOrElse(JsNull)

  @Benchmark def parseBytes(): Json = JsonConverter.parse(JsonConverterBench.Sample.BytesImpl).getOrElse(JsNull)

  @Benchmark def parseString(): Json = JsonConverter.parse(JsonConverterBench.Sample.StringImpl).getOrElse(JsNull)

  @Benchmark def print(): String = Json.print(JsonConverterBench.Sample.JsonObj).getOrElse(JsNull)

}

object JsonConverterBench {

  object Sample {

    val JsonObj: JsObject = Json.obj(
      "int" -> Int.MaxValue,
      "long" -> Long.MaxValue,
      "float" -> Float.MaxValue,
      "double" -> Double.MaxValue,
      "string" -> "string"
    )

    val StringImpl: String = Json.obj(
      "int" -> Int.MaxValue,
      "long" -> Long.MaxValue,
      "float" -> Float.MaxValue,
      "double" -> Double.MaxValue,
      "string" -> "string"
    ).toString

    val ByteStringImpl: ByteString = ByteString(StringImpl)

    val BytesImpl: Array[Byte] = ByteStringImpl.toArray[Byte]

  }

}