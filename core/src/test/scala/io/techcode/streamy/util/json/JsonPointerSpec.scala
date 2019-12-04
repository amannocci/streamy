/*
 * The MIT License (MIT)
 * <p>
 * Copyright (C) 2017-2019
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
class JsPointerSpec extends WordSpecLike with Matchers {

  "JsonPointer" should {
    "return same json value for root pointer" in {
      val input = Json.obj("test" -> "foobar")
      input.evaluate(Root) should equal(input)
    }

    "return a value if possible when evaluate on json object" in {
      val input = Json.obj("test" -> "foobar")
      input.evaluate(Root / "test") should equal(JsString("foobar"))
    }

    "return a value if possible when evaluate on deep json object" in {
      val input = Json.obj("test" -> Json.arr(Json.obj("test" -> "foobar")))
      input.evaluate(Root / "test" / 0 / "test") should equal(JsString("foobar"))
    }

    "return a none when evaluate on json object is failed" in {
      val input = Json.obj("test" -> "foobar")
      input.evaluate(Root / "failed") should equal(JsUndefined)
    }

    "return a none when evaluate on json object and excepting a json array" in {
      val input = Json.obj("test" -> "foobar")
      input.evaluate(Root / 0) should equal(JsUndefined)
    }

    "return a value if possible when evaluate on json array" in {
      val input = Json.arr("test", "foobar")
      input.evaluate(Root / 0) should equal(JsString("test"))
      input.evaluate(Root / 1) should equal(JsString("foobar"))
    }

    "return a value if possible when evaluate on deep json array" in {
      val input = Json.obj("test" -> Json.obj("test" -> Json.arr("foobar")))
      input.evaluate(Root / "test" / "test" / 0) should equal(JsString("foobar"))
    }

    "return a none when evaluate on json array is failed" in {
      val input = Json.arr()
      input.evaluate(Root / 0) should equal(JsUndefined)
    }

    "return a none when evaluate on json array and excepting a json object" in {
      val input = Json.arr("test", "foobar")
      input.evaluate(Root / "failed") should equal(JsUndefined)
    }

    "return a string representation" in {
      (Root / "failed").toString should equal("/failed")
    }

    "equal to the same root json pointer" in {
      Root should equal(Root)
    }

    "equal to the same json pointer" in {
      Root / "foobar" should equal(Root / "foobar")
    }

    "not equal to the same json pointer" in {
      Root / "foobar" should not equal("foobar")
    }

    "be iterable" in {
      (Root / "foobar" / 0).toSeq should equal(Seq(Left("foobar"), Right(0)))
    }
  }

  "JsonObjectAccessor" should {
    "be equal to the same accessor" in {
      JsObjectAccessor("foobar") should equal(JsObjectAccessor("foobar"))
    }

    "be not equal to a different accessor" in {
      JsObjectAccessor("foobar") should not equal JsObjectAccessor("foo")
    }

    "be not equal to an array accessor" in {
      JsObjectAccessor("foobar") should not equal JsArrayAccessor(0)
    }
  }

  "JsonArrayAccessor" should {
    "be equal to the same accessor" in {
      JsArrayAccessor(0) should equal(JsArrayAccessor(0))
    }

    "be not equal to a different accessor" in {
      JsArrayAccessor(0) should not equal JsArrayAccessor(1)
    }

    "be not equal to an array accessor" in {
      JsArrayAccessor(0) should not equal JsObjectAccessor("foobar")
    }
  }

  "JsonPointer parser" should {
    "parse correctly a root pointer" in {
      val parser = new JsonPointerParser()
      parser.parse("/") should equal(Right(Root))
    }

    "parse correctly a simple key pointer" in {
      val parser = new JsonPointerParser()
      parser.parse("/key") should equal(Right(Root / "key"))
    }

    "parse correctly a simple key pointer with escaped character" in {
      val parser = new JsonPointerParser()
      parser.parse("/key~0~1") should equal(Right(Root / "key~/"))
    }

    "parse correctly a simple indice pointer with character" in {
      val parser = new JsonPointerParser()
      parser.parse("/0") should equal(Right(Root / 0))
    }

    "parse correctly a simple indice pointer with escaped character" in {
      val parser = new JsonPointerParser()
      parser.parse("/0/~0~1") should equal(Right(Root / 0 / "~/"))
    }

    "parse correctly a complex pointer" in {
      val parser = new JsonPointerParser()
      parser.parse("/0/key/1") should equal(Right(Root / 0 / "key" / 1))
    }

    "parse correctly a complex pointer with escaped character" in {
      val parser = new JsonPointerParser()
      parser.parse("/0/key/1/~0~1") should equal(Right(Root / 0 / "key" / 1 / "~/"))
    }

    "parse correctly a multiples pointers" in {
      val parser = new JsonPointerParser()
      parser.parse("/key") should equal(Right(Root / "key"))
      parser.parse("/foobar") should equal(Right(Root / "foobar"))
    }

    "fail correctly for an invalid json pointer" in {
      val parser = new JsonPointerParser()
      parser.parse("key").isLeft should equal(true)
    }
  }

}