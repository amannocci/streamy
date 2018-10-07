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
package io.techcode.streamy.riemann.component

import akka.NotUsed
import akka.stream.scaladsl.Flow
import io.techcode.streamy.component.TestTransformer
import io.techcode.streamy.util.json._

import scala.language.postfixOps

/**
  * Riemann transformer spec.
  */
class RiemannTransformerSpec extends TestTransformer {

  "Riemann transformer" should {
    "print and parse data correctly" in {
      except[Json, Json](
        RiemannTransformerSpec.Transformer,
        RiemannTransformerSpec.Input,
        RiemannTransformerSpec.Output
      )
    }
  }

}

object RiemannTransformerSpec {

  val Input: Json = Json.obj(
    "ok" -> true,
    "error" -> "test",
    "events" -> Json.arr(
      Json.obj(
        "time" -> System.currentTimeMillis(),
        "state" -> "ok",
        "service" -> "test",
        "host" -> "example.com",
        "description" -> "metric",
        "tags" -> Json.arr("crit", "prod"),
        "ttl" -> 12.0F,
        "attributes" -> Json.obj(
          "foo" -> "bar",
          "number" -> 1.0
        ),
        "time_micros" -> System.currentTimeMillis(),
        "metric_sint64" -> 0L,
        "metric_d" -> 0D,
        "metric_f" -> 0F,
      )
    )
  )

  val Transformer: Flow[Json, Json, NotUsed] =
    RiemannTransformer.printer(RiemannTransformer.Printer.Config())
      .via(RiemannTransformer.parser(RiemannTransformer.Parser.Config()))

  val Output: Json = Json.obj(
    "ok" -> true,
    "error" -> "test",
    "events" -> Json.arr(
      Json.obj(
        "time" -> System.currentTimeMillis(),
        "state" -> "ok",
        "service" -> "test",
        "host" -> "example.com",
        "description" -> "metric",
        "tags" -> Json.arr("crit", "prod"),
        "ttl" -> 12.0F,
        "attributes" -> Json.obj(
          "foo" -> "bar",
          "number" -> "1.0"
        ),
        "time_micros" -> System.currentTimeMillis(),
        "metric_sint64" -> 0L,
        "metric_d" -> 0D,
        "metric_f" -> 0F,
      )
    )
  )

}
