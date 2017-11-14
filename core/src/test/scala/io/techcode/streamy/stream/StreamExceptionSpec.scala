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
package io.techcode.streamy.stream

import io.circe._
import io.techcode.streamy.util.JsonUtil._
import org.scalatest._

/**
  * Stream exception spec.
  */
class StreamExceptionSpec extends FlatSpec with Matchers {

  val generic = new StreamException("foobar")

  "StreamException" must "not have a stacktrace" in {
    generic.getStackTrace.length should equal(0)
  }

  it should "be convert to json" in {
    generic.toJson should equal(Json.fromJsonObject(JsonObject.singleton("message", "foobar")))
  }

  it should "be convert to json with state" in {
    new StreamException("foobar", Some(Json.obj("details" -> "test"))).toJson should equal(Json.obj(
      "message" -> "foobar",
      "state" -> Json.obj("details" -> "test")
    ))
  }

  it should "be convert to json with string state" in {
    new StreamException("foobar", Some("test")).toJson should equal(Json.obj(
      "message" -> "foobar",
      "state" -> "test"
    ))
  }

  it should "be convert to json with exception" in {
    new StreamException("foobar", ex = Some(new StreamException("test"))).toJson should equal(Json.obj(
      "message" -> "foobar", "exception" -> s"io.techcode.streamy.stream.StreamException: test${scala.util.Properties.lineSeparator}"
    ))
  }

  it should "be convert to json with exception and state" in {
    new StreamException("foobar", state = Some(Json.obj("details" -> "test")), ex = Some(new StreamException("test"))).toJson should equal(Json.obj(
      "message" -> "foobar",
      "state" -> Json.obj("details" -> "test"),
      "exception" -> s"io.techcode.streamy.stream.StreamException: test${scala.util.Properties.lineSeparator}"
    ))
  }

}