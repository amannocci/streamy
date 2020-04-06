/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2020
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
package io.techcode.streamy.event

import akka.NotUsed
import io.techcode.streamy.util.StreamException
import io.techcode.streamy.util.json.{JsNull, JsUndefined, Json, MaybeJson}
import org.scalatest.{Matchers, WordSpecLike}

/**
  * Stream event spec.
  */
class StreamEventSpec extends WordSpecLike with Matchers {

  "Stream event" can {
    "be mutate with only new payload" in {
      val input = StreamEvent.from(Json.obj()).mutate(Json.obj("test" -> "foobar"))
      val output = StreamEvent.from(Json.obj("test" -> "foobar"))
      input should equal(output)
    }

    "be mutate with only new context" in {
      val input = StreamEvent(Json.obj(), JsNull).mutate[MaybeJson](JsUndefined)
      val output = StreamEvent(Json.obj(), JsUndefined)
      input should equal(output)
    }

    "be mutate" in {
      val input = StreamEvent(Json.obj(), JsNull).mutate[MaybeJson](Json.arr(), JsUndefined)
      val output = StreamEvent(Json.arr(), JsUndefined)
      input should equal(output)
    }

    "be discard by message" in {
      assertThrows[StreamException[NotUsed]] {
        StreamEvent.from(Json.obj()).discard("foobar")
      }
    }

    "be discard by exception" in {
      assertThrows[StreamException[NotUsed]] {
        StreamEvent.from(Json.obj()).discard(new IllegalArgumentException)
      }
    }
  }

}
