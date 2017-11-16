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

import io.circe._
import org.scalatest._

/**
  * JsonImplicit spec.
  */
class JsonImplicitSpec extends FlatSpec with Matchers {

  "JsonImplicit" must "provide a shortcut to convert json in string" in {
    jsonToString(Json.obj("test" -> Json.fromString("test"))) should equal("""{"test":"test"}""")
  }

  it must "provide a shortcut to convert string in json" in {
    stringToJson("""{"test":"test"}""") should equal(Json.fromString("""{"test":"test"}"""))
  }

  it must "provide a shortcut to convert float in json" in {
    floatToJson(2.0F) should equal(Json.fromFloatOrNull(2.0F))
  }

  it must "provide a shortcut to convert double in json" in {
    doubleToJson(2.0D) should equal(Json.fromDoubleOrNull(2.0D))
  }

  it must "provide a shortcut to convert int in json" in {
    intToJson(2) should equal(Json.fromInt(2))
  }

  it must "provide a shortcut to convert long in json" in {
    longToJson(2L) should equal(Json.fromLong(2L))
  }

  it must "provide a shortcut to convert boolean in json" in {
    booleanToJson(true) should equal(Json.fromBoolean(true))
  }

}
