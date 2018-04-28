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
package io.techcode.streamy.date.component

import io.techcode.streamy.component.TestTransformer
import io.techcode.streamy.component.Transformer.ErrorBehaviour
import io.techcode.streamy.util.json._

/**
  * Date transformer spec.
  */
class DateTransformerSpec extends TestTransformer {

  "Date transformer" should {
    "be able to convert iso8601 to iso8601 date format" in {
      except(
        JsonTransformerSpec.Transformer.Idempotent,
        JsonTransformerSpec.Input.Iso8601,
        JsonTransformerSpec.Output.Iso8601
      )
    }

    "be able to convert iso8601 to custom date format" in {
      except(
        JsonTransformerSpec.Transformer.FromIsoToCustom,
        JsonTransformerSpec.Input.Iso8601,
        JsonTransformerSpec.Output.Custom
      )
    }

    "be able to convert custom to iso8601 date format" in {
      except(
        JsonTransformerSpec.Transformer.FromCustomToIso,
        JsonTransformerSpec.Input.Custom,
        JsonTransformerSpec.Output.Iso8601
      )
    }

    "throw an exception for non string input" in {
      exceptError(
        JsonTransformerSpec.Transformer.Validation,
        JsonTransformerSpec.Input.NotString
      )
    }
  }

}

object JsonTransformerSpec {

  object Input {

    val Iso8601: JsObject = Json.obj("date" -> "2000-10-11T14:32:52Z")

    val Custom: JsObject = Json.obj("date" -> "Wed Oct 11 14:32:52 2000")

    val NotString: JsObject = Json.obj("date" -> 1)

  }

  object Transformer {

    val Idempotent = DateTransformer(DateTransformer.Config(
      source = Root / "date",
      inputPattern = DateTransformer.Iso8601,
      outputPattern = DateTransformer.Iso8601
    ))

    val FromIsoToCustom = DateTransformer(DateTransformer.Config(
      source = Root / "date",
      inputPattern = DateTransformer.Iso8601,
      outputPattern = "EEE MMM dd HH:mm:ss yyyy"
    ))

    val FromCustomToIso = DateTransformer(DateTransformer.Config(
      source = Root / "date",
      inputPattern = "EEE MMM dd HH:mm:ss yyyy",
      outputPattern = DateTransformer.Iso8601
    ))

    val Validation = DateTransformer(DateTransformer.Config(
      source = Root / "date",
      onError = ErrorBehaviour.Discard,
      inputPattern = "EEE MMM dd HH:mm:ss yyyy",
      outputPattern = DateTransformer.Iso8601
    ))

  }

  object Output {

    val Iso8601: JsObject = Json.obj("date" -> "2000-10-11T14:32:52Z")

    val Custom: JsObject = Json.obj("date" -> "Wed Oct 11 14:32:52 2000")

  }

}
