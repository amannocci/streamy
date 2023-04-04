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
package io.techcode.streamy.util.json

import io.techcode.streamy.util.json.JsonBench.Sample
import org.openjdk.jmh.annotations.Benchmark

/**
  * Json bench.
  *
  * Benchmark                     Mode  Cnt          Score         Error  Units
  * JsonBench.jsObjectDeepMerge  thrpt   20    4619275.263 ±   13331.739  ops/s
  * JsonBench.jsObjectMerge      thrpt   20   18448338.686 ±   74711.403  ops/s
  * JsonBench.jsObjectPut        thrpt   20   31021091.147 ±   80648.889  ops/s
  * JsonBench.jsObjectRemove     thrpt   20   35558980.545 ±   58338.391  ops/s
  * JsonBench.sizeOfBoolean      thrpt   20  600008514.993 ± 2056584.910  ops/s
  * JsonBench.sizeOfDouble       thrpt   20     921418.428 ±    3084.545  ops/s
  * JsonBench.sizeOfFloat        thrpt   20    2834863.921 ±    7170.611  ops/s
  * JsonBench.sizeOfInt          thrpt   20  127173678.158 ± 2710535.287  ops/s
  * JsonBench.sizeOfLong         thrpt   20  125545400.334 ± 2400845.844  ops/s
  * JsonBench.sizeOfNull         thrpt   20  600796620.964 ± 1474248.936  ops/s
  * JsonBench.sizeOfNumber       thrpt   20   10799327.105 ±   22829.902  ops/s
  * JsonBench.sizeOfString       thrpt   20  637878878.510 ± 2603274.692  ops/s
  */
class JsonBench {

  @Benchmark def sizeOfNull(): Int = JsNull.sizeHint

  @Benchmark def sizeOfBoolean(): Int = JsTrue.sizeHint

  @Benchmark def sizeOfInt(): Int = JsInt(Int.MaxValue).sizeHint()

  @Benchmark def sizeOfLong(): Int = JsLong(Long.MaxValue).sizeHint()

  @Benchmark def sizeOfFloat(): Int = JsFloat(Float.MaxValue).sizeHint()

  @Benchmark def sizeOfDouble(): Int = JsDouble(Double.MaxValue).sizeHint()

  @Benchmark def sizeOfNumber(): Int = JsBigDecimal(BigDecimal("1e20")).sizeHint()

  @Benchmark def sizeOfString(): Int = JsString("1e20").sizeHint()

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

  @Benchmark def jsFlatten(): JsObject = JsonBench.Sample.JsonObjToFlatten.flatten()

  @Benchmark def jsEvaluate(): MaybeJson = JsonBench.Sample.JsonObj.evaluate(JsonBench.Pointer.Access)

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

    val JsonObjToFlatten: JsObject = Json.obj(
      "test01" -> Json.obj(
        "test01" -> "test",
        "test02" -> "test"
      ),
      "test02" -> Json.obj(
        "test01" -> "test",
        "test02" -> "test"
      ),
      "test03" -> Json.obj(
        "test01" -> "test",
        "test02" -> "test"
      )
    )

  }

  object Pointer {
    val Access: JsonPointer = Root / "string"
  }

}
