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

import org.scalatest._

/**
  * JsonPointer spec.
  */
class JsonPointerSpec extends FlatSpec with Matchers {

  "JsonPointer" should "return same json value for root pointer" in {
    val input = Json.obj("test" -> "foobar")
    input.evaluate(Root) should equal(Some(input))
  }

  it should "return a value if possible when evaluate on json object" in {
    val input = Json.obj("test" -> "foobar")
    input.evaluate(Root / "test") should equal(Some(JsString("foobar")))
  }

  it should "return a value if possible when evaluate on deep json object" in {
    val input = Json.obj("test" -> Json.arr(Json.obj("test" -> "foobar")))
    input.evaluate(Root / "test" / 0 / "test") should equal(Some(JsString("foobar")))
  }

  it should "return a none when evaluate on json object is failed" in {
    val input = Json.obj("test" -> "foobar")
    input.evaluate(Root / "failed") should equal(None)
  }

  it should "return a none when evaluate on json object and excepting a json array" in {
    val input = Json.obj("test" -> "foobar")
    input.evaluate(Root / 0) should equal(None)
  }

  it should "return a value if possible when evaluate on json array" in {
    val input = Json.arr("test", "foobar")
    input.evaluate(Root / 0) should equal(Some(JsString("test")))
    input.evaluate(Root / 1) should equal(Some(JsString("foobar")))
  }

  it should "return a value if possible when evaluate on deep json array" in {
    val input = Json.obj("test" -> Json.obj("test" -> Json.arr("foobar")))
    input.evaluate(Root / "test" / "test" / 0) should equal(Some(JsString("foobar")))
  }

  it should "return a none when evaluate on json array is failed" in {
    val input = Json.arr()
    input.evaluate(Root / 0) should equal(None)
  }

  it should "return a none when evaluate on json array and excepting a json object" in {
    val input = Json.arr("test", "foobar")
    input.evaluate(Root / "failed") should equal(None)
  }

}