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

import gnieh.diffson.circe._
import io.circe._
import org.scalatest._

/**
  * JsonHelper spec.
  */
class JsonHelperSpec extends FlatSpec with Matchers {

  "JsonHelper" must "return true if a value exist" in {
    io.techcode.streamy.util.json.exist(Json.obj("test" -> "test"), root / "test") should equal(true)
  }

  it must "return false if a value doesn't exist" in {
    io.techcode.streamy.util.json.exist(Json.obj("test" -> "test"), root / "fail") should equal(false)
  }

  it must "return false if a value doesn't exist in deep object" in {
    io.techcode.streamy.util.json.exist(Json.obj("test" -> Json.obj("test" -> "test")), root / "fail" / "fail") should equal(false)
  }

  it must "evaluate correctly a path on a given json value" in {
    evaluate(Json.obj("test" -> "test"), root / "test") should equal(Json.fromString("test"))
  }

  it must "patch correctly a json value" in {
    patch(Json.obj("test" -> "test"), Add(root / "test", "changed")) should equal(Json.obj("test" -> "changed"))
  }

}
