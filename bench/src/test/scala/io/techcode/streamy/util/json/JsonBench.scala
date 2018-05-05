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
  * Benchmark                  Mode  Cnt          Score         Error  Units
  * JsonBench.sizeOfBoolean   thrpt   20  383786564,399 ± 5719602,963  ops/s
  * JsonBench.sizeOfDouble    thrpt   20     405973,902 ±    5508,228  ops/s
  * JsonBench.sizeOfFloat     thrpt   20     637717,711 ±    9582,861  ops/s
  * JsonBench.sizeOfInt       thrpt   20   80227505,913 ±  544809,624  ops/s
  * JsonBench.sizeOfLong      thrpt   20   80049668,639 ± 1418872,361  ops/s
  * JsonBench.sizeOfNull      thrpt   20  397903186,125 ± 4661371,194  ops/s
  * JsonBench.sizeOfNumber    thrpt   20     801240,215 ±   15385,757  ops/s
  * JsonBench.sizeOfString    thrpt   20  405808076,145 ± 6456433,208  ops/s
  * JsonBench.jsObjectMerge   thrpt   20   10453731.824 ±   27275.448  ops/s
  * JsonBench.jsObjectPut     thrpt   20   31257556.224 ±   91964.570  ops/s
  * JsonBench.jsObjectRemove  thrpt   20   34733958.871 ±   59629.057  ops/s
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

  }

}
