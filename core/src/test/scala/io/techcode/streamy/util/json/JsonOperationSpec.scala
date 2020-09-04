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

import org.scalatest._

/**
  * JsonPointer spec.
  */
class JsonOperationSpec extends WordSpecLike with Matchers {

  "Json add operation" should {
    "add correctly a field in json object" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Add(Root / "foobar", "test"))
      result should equal(Json.obj(
        "test" -> "test",
        "foobar" -> "test"
      ))
      input should not equal result
    }

    "add correctly a field in json object using minus sign" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Add(Root / "-", "foobar"))
      result should equal(Json.obj(
        "test" -> "test",
        "-" -> "foobar"
      ))
    }

    "add correctly a field in deep json object" in {
      val input = Json.obj("test" -> Json.arr(Json.obj("test" -> "test")))
      val result = input.patch(Add(Root / "test" / 0 / "foobar", "test"))
      result should equal(Json.obj(
        "test" -> Json.arr(Json.obj(
          "test" -> "test",
          "foobar" -> "test"
        ))
      ))
      input should not equal result
    }

    "add correctly a value in json array" in {
      val input = Json.arr("test", "test")
      val result = input.patch(Add(Root / 1, "test"))
      result should equal(Json.arr("test", "test", "test"))
      input should not equal result
    }

    "add correctly a value in deep json array" in {
      val input = Json.arr("test", Json.obj("test" -> Json.arr("test", "foobar")))
      val result = input.patch(Add(Root / 1 / "test" / 1, "test"))
      result should equal(Json.arr(
        "test",
        Json.obj("test" -> Json.arr(
          "test",
          "test",
          "foobar"
        ))
      ))
      input should not equal result
    }

    "add correctly a value in json array at the end" in {
      val input = Json.arr("test", "test")
      val result = input.patch(Add(Root / "-", "foobar"))
      result should equal(Json.arr("test", "test", "foobar"))
    }

    "fail to add a value in json array if we point a json object" in {
      val input = Json.arr("test", "test")
      val result = input.patch(Add(Root / "fail", "test"))
      result should equal(JsUndefined)
    }

    "fail to add a value in json array if we fail to evaluate path on json object" in {
      val input = Json.obj("test" -> Json.obj("missing" -> Json.arr("test", "test")))
      val result = input.patch(Add(Root / "test" / "fail" / 0, "test"))
      result should equal(JsUndefined)
    }

    "fail to add a value in json object if we point a json array" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Add(Root / 0, "test"))
      result should equal(JsUndefined)
    }

    "fail to add a value in json object if we fail to evaluate path on json array" in {
      val input = Json.obj("test" -> Json.arr(Json.obj("test" -> "test")))
      val result = input.patch(Add(Root / "test" / 1 / "test", "test"))
      result should equal(JsUndefined)
    }

    "fail to add a value in json array out of bounds" in {
      val input = Json.arr("test", "test")
      val result = input.patch(Add(Root / 5, "foobar"))
      result should equal(JsUndefined)
    }

    "switch correctly to root value" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Add(Root, Json.obj("test" -> "foobar")))
      result should equal(Json.obj("test" -> "foobar"))
    }
  }

  "Json test operation" should {
    "return JsUndefined on failure" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Test(Root / "test", JsInt.fromLiteral(0)))
      result should equal(JsUndefined)
    }

    "return current value on success" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Test(Root / "test", JsString.fromLiteral("test")))
      result should equal(input)
    }

    "return JsUndefined on deep failure" in {
      val input = Json.obj("test" -> Json.obj("test" -> "failure"))
      val result = input.patch(Test(Root / "test" / "test", JsInt.fromLiteral(0)))
      result should equal(JsUndefined)
    }

    "return JsUndefined on missing" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Test(Root / "failure", JsInt.fromLiteral(0)))
      result should equal(JsUndefined)
    }
  }

  "Json replace operation" should {
    "replace correctly a field in json object" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Replace(Root / "test", "foobar"))
      result should equal(Json.obj(
        "test" -> "foobar"
      ))
      input should not equal result
    }

    "replace correctly a field in json object using minus sign" in {
      val input = Json.obj("-" -> "test")
      val result = input.patch(Replace(Root / "-", "foobar"))
      result should equal(Json.obj(
        "-" -> "foobar"
      ))
      input should not equal result
    }

    "replace correctly a field in deep json object" in {
      val input = Json.obj("test" -> Json.arr(Json.obj("test" -> "test")))
      val result = input.patch(Replace(Root / "test" / 0 / "test", "foobar"))
      result should equal(Json.obj(
        "test" -> Json.arr(Json.obj(
          "test" -> "foobar"
        ))
      ))
      input should not equal result
    }

    "replace correctly a value in json array" in {
      val input = Json.arr("test", "test")
      val result = input.patch(Replace(Root / 1, "foobar"))
      result should equal(Json.arr("test", "foobar"))
      input should not equal result
    }

    "replace correctly a value in json array at the end" in {
      val input = Json.arr("test", "test")
      val result = input.patch(Replace(Root / "-", "foobar"))
      result should equal(Json.arr("test", "foobar"))
      input should not equal result
    }

    "replace correctly a value in deep json array" in {
      val input = Json.arr("test", Json.obj("test" -> Json.arr("test", "test")))
      val result = input.patch(Replace(Root / 1 / "test" / 1, "foobar"))
      result should equal(Json.arr(
        "test",
        Json.obj("test" -> Json.arr(
          "test",
          "foobar",
        ))
      ))
      input should not equal result
    }

    "fail to replace a value in json array if we point a json object" in {
      val input = Json.arr("test", "test")
      val result = input.patch(Replace(Root / "fail", "test"))
      result should equal(JsUndefined)
    }

    "fail to replace a value in json array if we fail to evaluate path on json object" in {
      val input = Json.obj("test" -> Json.obj("missing" -> Json.arr("test", "test")))
      val result = input.patch(Replace(Root / "test" / "fail" / 0, "test"))
      result should equal(JsUndefined)
    }

    "fail to replace a value in json object if we point a json array" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Replace(Root / 0, "test"))
      result should equal(JsUndefined)
    }

    "fail to replace a value in json object if we fail to evaluate path on json array" in {
      val input = Json.obj("test" -> Json.arr(Json.obj("test" -> "test")))
      val result = input.patch(Replace(Root / "test" / 1 / "test", "test"))
      result should equal(JsUndefined)
    }

    "fail to replace a non existing field in json object" in {
      val input = Json.obj("missing" -> "test")
      val result = input.patch(Replace(Root / "test", "foobar"))
      result should equal(JsUndefined)
    }

    "fail to replace a non existing value in json array" in {
      val input = Json.arr("missing", "test")
      val result = input.patch(Replace(Root / 2, "foobar"))
      result should equal(JsUndefined)
    }

    "switch correctly to root value" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Replace(Root, Json.obj("test" -> "foobar")))
      result should equal(Json.obj("test" -> "foobar"))
    }
  }

  "Json remove operation" should {
    "remove correctly a field in json object" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Remove(Root / "test"))
      result should equal(Json.obj())
      input should not equal result
    }

    "remove correctly a field in json object using minus sign" in {
      val input = Json.obj("-" -> "test")
      val result = input.patch(Remove(Root / "-"))
      result should equal(Json.obj())
      input should not equal result
    }

    "remove correctly a field in deep json object" in {
      val input = Json.obj("test" -> Json.arr(Json.obj("test" -> "test")))
      val result = input.patch(Remove(Root / "test" / 0 / "test"))
      result should equal(Json.obj(
        "test" -> Json.arr(Json.obj())
      ))
      input should not equal result
    }

    "remove correctly a value in json array" in {
      val input = Json.arr("test", "test")
      val result = input.patch(Remove(Root / 1))
      result should equal(Json.arr("test"))
      input should not equal result
    }

    "remove correctly a value in json array at the end" in {
      val input = Json.arr("test", "test")
      val result = input.patch(Remove(Root / "-"))
      result should equal(Json.arr("test"))
      input should not equal result
    }

    "remove correctly a value in deep json array" in {
      val input = Json.arr("test", Json.obj("test" -> Json.arr("test", "test")))
      val result = input.patch(Remove(Root / 1 / "test" / 1))
      result should equal(Json.arr(
        "test",
        Json.obj("test" -> Json.arr("test"))
      ))
      input should not equal result
    }

    "remove correctly a json value at root" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Remove(Root))
      result should equal(input)
    }

    "fail to remove a value in json array if we point a json object" in {
      val input = Json.arr("test", "test")
      val result = input.patch(Remove(Root / "fail"))
      result should equal(JsUndefined)
    }

    "fail to remove a value in json array if we fail to evaluate path on json object" in {
      val input = Json.obj("test" -> Json.obj("missing" -> Json.arr("test", "test")))
      val result = input.patch(Remove(Root / "test" / "fail" / 0))
      result should equal(JsUndefined)
    }

    "fail to remove a value in json object if we point a json array" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Remove(Root / 0))
      result should equal(JsUndefined)
    }

    "fail to remove a value in json object if we fail to evaluate path on json array" in {
      val input = Json.obj("test" -> Json.arr(Json.obj("test" -> "test")))
      val result = input.patch(Remove(Root / "test" / 1 / "test"))
      result should equal(JsUndefined)
    }

    "fail to remove a field in json object" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Remove(Root / "fail"))
      result should equal(JsUndefined)
    }

    "fail to remove a field in json object using minus sign" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Remove(Root / "-"))
      result should equal(JsUndefined)
    }

    "fail to remove a field in json array" in {
      val input = Json.arr("test", "test")
      val result = input.patch(Remove(Root / 2))
      result should equal(JsUndefined)
    }

    "fail to remove a field in json array at the end" in {
      val input = Json.arr()
      val result = input.patch(Remove(Root / "-"))
      result should equal(JsUndefined)
    }

    "fail to remove silently a field in json object" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Remove(Root / "fail", mustExist = false))
      result should equal(Json.obj("test" -> "test"))
    }

    "fail to remove silently a field in json object using minus" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Remove(Root / "-", mustExist = false))
      result should equal(Json.obj("test" -> "test"))
    }

    "fail to remove silently a field in deep json object" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Remove(Root / "test" / "test" / "fail", mustExist = false))
      result should equal(Json.obj("test" -> "test"))
    }

    "fail to remove silently a value in json array" in {
      val input = Json.arr("test", "test")
      val result = input.patch(Remove(Root / 2, mustExist = false))
      result should equal(Json.arr("test", "test"))
    }

    "fail to remove silently a value in json array at the end" in {
      val input = Json.arr()
      val result = input.patch(Remove(Root / "-", mustExist = false))
      result should equal(Json.arr())
    }

    "fail to remove silently a value in deep json array" in {
      val input = Json.arr("test", "test")
      val result = input.patch(Remove(Root / 0 / 1 / 2, mustExist = false))
      result should equal(Json.arr("test", "test"))
    }
  }

  "Json move operation" should {
    "move correctly a field in json object" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Move(Root / "test", Root / "foobar"))
      result should equal(Json.obj("foobar" -> "test"))
      input should not equal result
    }

    "move correctly a field in json object using minus sign" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Move(Root / "test", Root / "-"))
      result should equal(Json.obj("-" -> "test"))
      input should not equal result
    }

    "move correctly a field in deep json object" in {
      val input = Json.obj("test" -> Json.arr(Json.obj("test" -> "test")))
      val result = input.patch(Move(Root / "test" / 0 / "test", Root / "foobar"))
      result should equal(Json.obj(
        "test" -> Json.arr(Json.obj()),
        "foobar" -> "test"
      ))
      input should not equal result
    }

    "move correctly a value in json array" in {
      val input = Json.arr("test01", "test02")
      val result = input.patch(Move(Root / 1, Root / 0))
      result should equal(Json.arr("test02", "test01"))
      input should not equal result
    }

    "move correctly a value in json array at the end" in {
      val input = Json.arr("test01", "test02")
      val result = input.patch(Move(Root / "-", Root / 0))
      result should equal(Json.arr("test02", "test01"))
      input should not equal result
    }

    "move correctly a value in deep json array" in {
      val input = Json.arr("test", Json.obj("test" -> Json.arr("test", "foobar")))
      val result = input.patch(Move(Root / 1 / "test" / 1, Root / 0))
      result should equal(Json.arr(
        "foobar",
        "test",
        Json.obj("test" -> Json.arr("test"))
      ))
      input should not equal result
    }

    "fail to move a value in json array if we point a json object" in {
      val input = Json.arr("test", "test")
      val result = input.patch(Move(Root / "fail", Root))
      result should equal(JsUndefined)
    }

    "fail to move a value in json array if we fail to evaluate path on json object" in {
      val input = Json.obj("test" -> Json.obj("missing" -> Json.arr("test", "test")))
      val result = input.patch(Move(Root / "test" / "fail" / 0, Root))
      result should equal(JsUndefined)
    }

    "fail to move a value in json object if we point a json array" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Move(Root / 0, Root))
      result should equal(JsUndefined)
    }

    "fail to move a value in json object if we fail to evaluate path on json array" in {
      val input = Json.obj("test" -> Json.arr(Json.obj("test" -> "test")))
      val result = input.patch(Move(Root / "test" / 1 / "test", Root))
      result should equal(JsUndefined)
    }

    "fail to move a non existing field in json object" in {
      val input = Json.obj("missing" -> "test")
      val result = input.patch(Move(Root / "test", Root))
      result should equal(JsUndefined)
    }

    "fail to replace a non existing value in json array" in {
      val input = Json.arr("missing", "test")
      val result = input.patch(Move(Root / 2, Root))
      result should equal(JsUndefined)
    }
  }

  "Json copy operation" should {
    "copy correctly a field in json object" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Copy(Root / "test", Root / "foobar"))
      result should equal(Json.obj(
        "foobar" -> "test",
        "test" -> "test"
      ))
      input should not equal result
    }

    "copy correctly a field in json object using minus sign" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Copy(Root / "test", Root / "-"))
      result should equal(Json.obj(
        "-" -> "test",
        "test" -> "test"
      ))
      input should not equal result
    }

    "copy correctly a field in deep json object" in {
      val input = Json.obj("test" -> Json.arr(Json.obj("test" -> "test")))
      val result = input.patch(Copy(Root / "test" / 0 / "test", Root / "foobar"))
      result should equal(Json.obj(
        "test" -> Json.arr(Json.obj("test" -> "test")),
        "foobar" -> "test"
      ))
      input should not equal result
    }

    "copy correctly a value in json array" in {
      val input = Json.arr("test01", "test02")
      val result = input.patch(Copy(Root / 1, Root / 0))
      result should equal(Json.arr("test02", "test01", "test02"))
      input should not equal result
    }

    "copy correctly a value in json array at the end" in {
      val input = Json.arr("test01", "test02")
      val result = input.patch(Copy(Root / 1, Root / "-"))
      result should equal(Json.arr("test01", "test02", "test02"))
      input should not equal result
    }

    "copy correctly a value in deep json array" in {
      val input = Json.arr("test", Json.obj("test" -> Json.arr("test", "foobar")))
      val result = input.patch(Copy(Root / 1 / "test" / 1, Root / 0))
      result should equal(Json.arr(
        "foobar",
        "test",
        Json.obj("test" -> Json.arr("test", "foobar"))
      ))
      input should not equal result
    }

    "fail to copy a value in json array if we point a json object" in {
      val input = Json.arr("test", "test")
      val result = input.patch(Copy(Root / "fail", Root))
      result should equal(JsUndefined)
    }

    "fail to copy a value in json array if we fail to evaluate path on json object" in {
      val input = Json.obj("test" -> Json.obj("missing" -> Json.arr("test", "test")))
      val result = input.patch(Copy(Root / "test" / "fail" / 0, Root))
      result should equal(JsUndefined)
    }

    "fail to copy a value in json object if we point a json array" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Copy(Root / 0, Root))
      result should equal(JsUndefined)
    }

    "fail to copy a value in json object if we fail to evaluate path on json array" in {
      val input = Json.obj("test" -> Json.arr(Json.obj("test" -> "test")))
      val result = input.patch(Copy(Root / "test" / 1 / "test", Root))
      result should equal(JsUndefined)
    }

    "fail to copy a non existing field in json object" in {
      val input = Json.obj("missing" -> "test")
      val result = input.patch(Copy(Root / "test", Root))
      result should equal(JsUndefined)
    }

    "fail to copy a non existing value in json array" in {
      val input = Json.arr("missing", "test")
      val result = input.patch(Copy(Root / 2, Root))
      result should equal(JsUndefined)
    }
  }

}