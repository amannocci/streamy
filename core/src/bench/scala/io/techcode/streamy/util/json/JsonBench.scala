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
package io.techcode.streamy.util.json

import org.openjdk.jmh.annotations.Benchmark

/**
  * Json bench.
  *
  * Benchmark                 Mode  Cnt          Score         Error  Units
  * JsonBench.sizeOfBoolean  thrpt   20  383786564,399 ± 5719602,963  ops/s
  * JsonBench.sizeOfDouble   thrpt   20     405973,902 ±    5508,228  ops/s
  * JsonBench.sizeOfFloat    thrpt   20     637717,711 ±    9582,861  ops/s
  * JsonBench.sizeOfInt      thrpt   20   80227505,913 ±  544809,624  ops/s
  * JsonBench.sizeOfLong     thrpt   20   80049668,639 ± 1418872,361  ops/s
  * JsonBench.sizeOfNull     thrpt   20  397903186,125 ± 4661371,194  ops/s
  * JsonBench.sizeOfNumber   thrpt   20     801240,215 ±   15385,757  ops/s
  * JsonBench.sizeOfString   thrpt   20  405808076,145 ± 6456433,208  ops/s
  */
class JsonBench {

  @Benchmark def sizeOfNull(): Int = JsNull.size

  @Benchmark def sizeOfBoolean(): Int = JsTrue.size

  @Benchmark def sizeOfInt(): Int = JsInt(Int.MaxValue).size

  @Benchmark def sizeOfLong(): Int = JsLong(Long.MaxValue).size

  @Benchmark def sizeOfFloat(): Int = JsFloat(Float.MaxValue).size()

  @Benchmark def sizeOfDouble(): Int = JsDouble(Double.MaxValue).size()

  @Benchmark def sizeOfNumber(): Int = JsBigDecimal(BigDecimal("1e20")).size()

  @Benchmark def sizeOfString(): Int = JsString("1e20").size

}