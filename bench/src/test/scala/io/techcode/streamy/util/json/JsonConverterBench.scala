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

import java.io.ByteArrayInputStream

import akka.util.ByteString
import org.openjdk.jmh.annotations.Benchmark

/**
  * Json converter bench.
  *
  * Benchmark                             Mode  Cnt        Score      Error  Units
  * JsonConverterBench.parseByteString   thrpt   20   353623.560 ±  761.032  ops/s
  * JsonConverterBench.parseBytes        thrpt   20   359455.272 ± 2065.001  ops/s
  * JsonConverterBench.parseInputStream  thrpt   20   313175.914 ±  963.117  ops/s
  * JsonConverterBench.parseString       thrpt   20   337981.761 ±  832.755  ops/s
  * JsonConverterBench.stringify         thrpt   20   439173.350 ± 4634.660  ops/s
  */
class JsonConverterBench {

  @Benchmark def parseByteString(): Json = JsonConverter.parse(JsonConverterBench.Sample.ByteStringImpl).getOrElse(JsNull)

  @Benchmark def parseBytes(): Json = JsonConverter.parse(JsonConverterBench.Sample.BytesImpl).getOrElse(JsNull)

  @Benchmark def parseString(): Json = JsonConverter.parse(JsonConverterBench.Sample.StringImpl).getOrElse(JsNull)

  @Benchmark def parseInputStream(): Json = JsonConverter.parse(new ByteArrayInputStream(JsonConverterBench.Sample.BytesImpl)).getOrElse(JsNull)

  @Benchmark def print(): String = JsonConverter.print(JsonConverterBench.Sample.JsonObj)

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