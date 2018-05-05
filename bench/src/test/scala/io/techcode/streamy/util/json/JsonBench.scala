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
package io.techcode.streamy.util.json

import io.techcode.streamy.util.json.JsonBench.Sample
import org.openjdk.jmh.annotations.Benchmark

/**
  * Json bench.
  *
  * Benchmark                     Mode  Cnt          Score          Error  Units
  * JsonBench.jsObjectDeepMerge  thrpt   20    4661650.592 ±    17629.346  ops/s
  * JsonBench.jsObjectMerge      thrpt   20   10637587.953 ±    83426.949  ops/s
  * JsonBench.jsObjectPut        thrpt   20   32169277.167 ±   185800.184  ops/s
  * JsonBench.jsObjectRemove     thrpt   20   34739219.428 ±   225275.530  ops/s
  * JsonBench.sizeOfBoolean      thrpt   20  593776699.742 ± 15838080.011  ops/s
  * JsonBench.sizeOfDouble       thrpt   20     907602.836 ±     4266.168  ops/s
  * JsonBench.sizeOfFloat        thrpt   20    2829499.576 ±    14212.255  ops/s
  * JsonBench.sizeOfInt          thrpt   20  126880646.503 ±  2641026.566  ops/s
  * JsonBench.sizeOfLong         thrpt   20  125732192.726 ±   167151.973  ops/s
  * JsonBench.sizeOfNull         thrpt   20  596134223.045 ±  1579742.453  ops/s
  * JsonBench.sizeOfNumber       thrpt   20   10678000.593 ±    25752.126  ops/s
  * JsonBench.sizeOfString       thrpt   20  632913582.587 ±  1715777.853  ops/s
  */
class JsonBench {

  @Benchmark def sizeOfNull(): Int = JsNull.sizeHint

  @Benchmark def sizeOfBoolean(): Int = JsTrue.sizeHint

  @Benchmark def sizeOfInt(): Int = JsInt(Int.MaxValue).sizeHint

  @Benchmark def sizeOfLong(): Int = JsLong(Long.MaxValue).sizeHint

  @Benchmark def sizeOfFloat(): Int = JsFloat(Float.MaxValue).sizeHint()

  @Benchmark def sizeOfDouble(): Int = JsDouble(Double.MaxValue).sizeHint()

  @Benchmark def sizeOfNumber(): Int = JsBigDecimal(BigDecimal("1e20")).sizeHint()

  @Benchmark def sizeOfString(): Int = JsString("1e20").sizeHint

  @Benchmark def jsObjectMerge(): JsObject = {
    Sample.JsonObj.merge(Sample.JsonObj)
  }

  @Benchmark def jsObjectDeepMerge(): JsObject = {
    Sample.DeepJsonObj.deepMerge(Sample.DeepJsonObj)
  }

  @Benchmark def jsObjectPut(): JsObject = {
    Sample.JsonObj.put("foobar" -> "test")
  }

  @Benchmark def jsObjectRemove(): JsObject = {
    Sample.JsonObj.remove("double")
  }

}

object JsonBench {

  object Sample {

    val JsonObj: JsObject = Json.obj(
      "int" -> Int.MaxValue,
      "long" -> Long.MaxValue,
      "float" -> Float.MaxValue,
      "double" -> Double.MaxValue,
      "string" -> "string"
    )

    val DeepJsonObj: JsObject = Json.obj(
      "int" -> Int.MaxValue,
      "long" -> Long.MaxValue,
      "string" -> "string",
      "obj" -> JsonObj
    )

  }

}
