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

import io.techcode.streamy.util.StreamException
import io.techcode.streamy.util.json.{JsNull, JsUndefined, Json, MaybeJson}
import org.scalatest.{Matchers, WordSpecLike}

/**
  * Stream event spec.
  */
class StreamEventSpec extends WordSpecLike with Matchers {

  // Test attr key
  private val attrKey = AttributeKey[MaybeJson]("test")

  "Stream event" can {
    "be created from payload and attributes map" in {
      val value = StreamEvent.from(Json.obj(), Map(attrKey -> JsUndefined))
      value.payload should equal(Json.obj())
      value.attribute[MaybeJson](attrKey) should equal(Some(JsUndefined))
    }

    "be access" in {
      val value = StreamEvent(Json.obj()).mutate(attrKey, JsUndefined)
      value.payload should equal(Json.obj())
      value.attribute[MaybeJson](attrKey) should equal(Some(JsUndefined))
      value.attributes().get(attrKey) should equal(Some(JsUndefined))
    }

    "be mutate with new payload" in {
      val input = StreamEvent(Json.obj()).mutate(Json.obj("test" -> "foobar"))
      val output = StreamEvent(Json.obj("test" -> "foobar"))
      input should equal(output)
    }

    "be mutate with new attr" in {
      val input = StreamEvent(Json.obj()).mutate(attrKey, JsUndefined)
      val output = StreamEventImpl(Json.obj(), Map(attrKey -> JsUndefined))
      input should equal(output)
    }

    "be mutate by removing attr" in {
      val input = StreamEvent(Json.obj()).mutate(attrKey)
      val output = StreamEvent(Json.obj())
      input should equal(output)
    }

    "be mutate with new attributes map" in {
      val map: Map[AttributeKey[_], _] = Map(attrKey -> JsUndefined)
      val input = StreamEvent(Json.obj()).mutate(map)
      val output = StreamEventImpl(Json.obj(), Map(attrKey -> JsUndefined))
      input should equal(output)
    }

    "be discard by message" in {
      assertThrows[StreamException] {
        StreamEvent(Json.obj()).discard("foobar")
      }
    }

    "be discard by exception" in {
      assertThrows[StreamException] {
        StreamEvent(Json.obj()).discard(new IllegalArgumentException)
      }
    }
  }

}
