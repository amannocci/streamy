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
class JsonOperationSpec extends WordSpecLike with Matchers {

  "Json bulk operation" should {
    "bulk correctly a sequence of operations on json object" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Bulk(Root, Seq(
        Add(Root / "foobar", "test"),
        Replace(Root / "foobar", "foobar"),
        Copy(Root / "foobar", Root / "barfoo"),
        Remove(Root / "test"),
        Test(Root / "foobar", "foobar")
      )))
      result should equal(Some(Json.obj(
        "foobar" -> "foobar",
        "barfoo" -> "foobar"
      )))
      Some(input) should not equal result
    }

    "bulk correctly a sequence of operations on json array" in {
      val input = Json.arr("test")
      val result = input.patch(Bulk(Root, Seq(
        Add(Root / -1, "test"),
        Replace(Root / 1, "foobar"),
        Copy(Root / 1, Root / 0),
        Remove(Root / 0),
        Test(Root / 0, "test")
      )))
      result should equal(Some(Json.arr("test", "foobar")))
      Some(input) should not equal result
    }

    "bulk correctly a sequence of operations on deep json object" in {
      val input = Json.obj("test" -> Json.obj("test" -> "test"))
      val result = input.patch(Bulk(Root / "test", Seq(
        Add(Root / "foobar", "test"),
        Replace(Root / "foobar", "foobar"),
        Copy(Root / "foobar", Root / "barfoo"),
        Remove(Root / "test"),
        Test(Root / "foobar", "foobar")
      )))
      result should equal(Some(Json.obj("test" -> Json.obj(
        "foobar" -> "foobar",
        "barfoo" -> "foobar"
      ))))
      Some(input) should not equal result
    }

    "bulk correctly a sequence of operations on deep json array" in {
      val input = Json.arr(Json.arr("test"))
      val result = input.patch(Bulk(Root / 0, Seq(
        Add(Root / -1, "test"),
        Replace(Root / 1, "foobar"),
        Copy(Root / 1, Root / 0),
        Remove(Root / 0),
        Test(Root / 0, "test")
      )))
      result should equal(Some(Json.arr(Json.arr("test", "foobar"))))
      Some(input) should not equal result
    }

    "fail to bulk operation if one operation fail" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Bulk(Root / "test", Seq(Test(Root / "fail", "test"))))
      result should equal(None)
    }

    "fail to bulk operation if we fail to evaluate path" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Bulk(Root / "fail", Seq(Test(Root / "fail", "test"))))
      result should equal(None)
    }
  }

  "Json add operation" should {
    "add correctly a field in json object" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Add(Root / "foobar", "test"))
      result should equal(Some(Json.obj(
        "test" -> "test",
        "foobar" -> "test"
      )))
      Some(input) should not equal result
    }

    "add correctly a field in deep json object" in {
      val input = Json.obj("test" -> Json.arr(Json.obj("test" -> "test")))
      val result = input.patch(Add(Root / "test" / 0 / "foobar", "test"))
      result should equal(Some(Json.obj(
        "test" -> Json.arr(Json.obj(
          "test" -> "test",
          "foobar" -> "test"
        ))
      )))
      Some(input) should not equal result
    }

    "add correctly a value in json array" in {
      val input = Json.arr("test", "test")
      val result = input.patch(Add(Root / 1, "test"))
      result should equal(Some(Json.arr("test", "test", "test")))
      Some(input) should not equal result
    }

    "add correctly a value in deep json array" in {
      val input = Json.arr("test", Json.obj("test" -> Json.arr("test", "foobar")))
      val result = input.patch(Add(Root / 1 / "test" / 1, "test"))
      result should equal(Some(Json.arr(
        "test",
        Json.obj("test" -> Json.arr(
          "test",
          "test",
          "foobar"
        ))
      )))
      Some(input) should not equal result
    }

    "add correctly a value in json array at the end" in {
      val input = Json.arr("test", "test")
      val result = input.patch(Add(Root / -1, "foobar"))
      result should equal(Some(Json.arr("test", "test", "foobar")))
    }

    "fail to add a value in json array if we point a json object" in {
      val input = Json.arr("test", "test")
      val result = input.patch(Add(Root / "fail", "test"))
      result should equal(None)
    }

    "fail to add a value in json array if we fail to evaluate path on json object" in {
      val input = Json.obj("test" -> Json.obj("missing" -> Json.arr("test", "test")))
      val result = input.patch(Add(Root / "test" / "fail" / 0, "test"))
      result should equal(None)
    }

    "fail to add a value in json object if we point a json array" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Add(Root / 0, "test"))
      result should equal(None)
    }

    "fail to add a value in json object if we fail to evaluate path on json array" in {
      val input = Json.obj("test" -> Json.arr(Json.obj("test" -> "test")))
      val result = input.patch(Add(Root / "test" / 1 / "test", "test"))
      result should equal(None)
    }

    "fail to add a value in json array out of bounds" in {
      val input = Json.arr("test", "test")
      val result = input.patch(Add(Root / 5, "foobar"))
      result should equal(None)
    }

    "switch correctly to root value" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Add(Root, Json.obj("test" -> "foobar")))
      result should equal(Some(Json.obj("test" -> "foobar")))
    }
  }

  "Json test operation" should {
    "return none on failure" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Test(Root / "test", JsInt(0)))
      result should equal(None)
    }

    "return current value on success" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Test(Root / "test", JsString("test")))
      result should equal(Some(input))
    }

    "return none on deep failure" in {
      val input = Json.obj("test" -> Json.obj("test" -> "failure"))
      val result = input.patch(Test(Root / "test" / "test", JsInt(0)))
      result should equal(None)
    }

    "return none on missing" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Test(Root / "failure", JsInt(0)))
      result should equal(None)
    }
  }

  "Json replace operation" should {
    "replace correctly a field in json object" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Replace(Root / "test", "foobar"))
      result should equal(Some(Json.obj(
        "test" -> "foobar"
      )))
      Some(input) should not equal result
    }

    "replace correctly a field in deep json object" in {
      val input = Json.obj("test" -> Json.arr(Json.obj("test" -> "test")))
      val result = input.patch(Replace(Root / "test" / 0 / "test", "foobar"))
      result should equal(Some(Json.obj(
        "test" -> Json.arr(Json.obj(
          "test" -> "foobar"
        ))
      )))
      Some(input) should not equal result
    }

    "replace correctly a value in json array" in {
      val input = Json.arr("test", "test")
      val result = input.patch(Replace(Root / 1, "foobar"))
      result should equal(Some(Json.arr("test", "foobar")))
      Some(input) should not equal result
    }

    "replace correctly a value in deep json array" in {
      val input = Json.arr("test", Json.obj("test" -> Json.arr("test", "test")))
      val result = input.patch(Replace(Root / 1 / "test" / 1, "foobar"))
      result should equal(Some(Json.arr(
        "test",
        Json.obj("test" -> Json.arr(
          "test",
          "foobar",
        ))
      )))
      Some(input) should not equal result
    }

    "fail to replace a value in json array if we point a json object" in {
      val input = Json.arr("test", "test")
      val result = input.patch(Replace(Root / "fail", "test"))
      result should equal(None)
    }

    "fail to replace a value in json array if we fail to evaluate path on json object" in {
      val input = Json.obj("test" -> Json.obj("missing" -> Json.arr("test", "test")))
      val result = input.patch(Replace(Root / "test" / "fail" / 0, "test"))
      result should equal(None)
    }

    "fail to replace a value in json object if we point a json array" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Replace(Root / 0, "test"))
      result should equal(None)
    }

    "fail to replace a value in json object if we fail to evaluate path on json array" in {
      val input = Json.obj("test" -> Json.arr(Json.obj("test" -> "test")))
      val result = input.patch(Replace(Root / "test" / 1 / "test", "test"))
      result should equal(None)
    }

    "fail to replace a non existing field in json object" in {
      val input = Json.obj("missing" -> "test")
      val result = input.patch(Replace(Root / "test", "foobar"))
      result should equal(None)
    }

    "fail to replace a non existing value in json array" in {
      val input = Json.arr("missing", "test")
      val result = input.patch(Replace(Root / 2, "foobar"))
      result should equal(None)
    }

    "switch correctly to root value" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Replace(Root, Json.obj("test" -> "foobar")))
      result should equal(Some(Json.obj("test" -> "foobar")))
    }
  }

  "Json remove operation" should {
    "remove correctly a field in json object" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Remove(Root / "test"))
      result should equal(Some(Json.obj()))
      Some(input) should not equal result
    }

    "remove correctly a field in deep json object" in {
      val input = Json.obj("test" -> Json.arr(Json.obj("test" -> "test")))
      val result = input.patch(Remove(Root / "test" / 0 / "test"))
      result should equal(Some(Json.obj(
        "test" -> Json.arr(Json.obj())
      )))
      Some(input) should not equal result
    }

    "remove correctly a value in json array" in {
      val input = Json.arr("test", "test")
      val result = input.patch(Remove(Root / 1))
      result should equal(Some(Json.arr("test")))
      Some(input) should not equal result
    }

    "remove correctly a value in deep json array" in {
      val input = Json.arr("test", Json.obj("test" -> Json.arr("test", "test")))
      val result = input.patch(Remove(Root / 1 / "test" / 1))
      result should equal(Some(Json.arr(
        "test",
        Json.obj("test" -> Json.arr("test"))
      )))
      Some(input) should not equal result
    }

    "remove correctly a json value at root" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Remove(Root))
      result should equal(Some(input))
    }

    "fail to remove a value in json array if we point a json object" in {
      val input = Json.arr("test", "test")
      val result = input.patch(Remove(Root / "fail"))
      result should equal(None)
    }

    "fail to remove a value in json array if we fail to evaluate path on json object" in {
      val input = Json.obj("test" -> Json.obj("missing" -> Json.arr("test", "test")))
      val result = input.patch(Remove(Root / "test" / "fail" / 0))
      result should equal(None)
    }

    "fail to remove a value in json object if we point a json array" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Remove(Root / 0))
      result should equal(None)
    }

    "fail to remove a value in json object if we fail to evaluate path on json array" in {
      val input = Json.obj("test" -> Json.arr(Json.obj("test" -> "test")))
      val result = input.patch(Remove(Root / "test" / 1 / "test"))
      result should equal(None)
    }

    "fail to remove a field in json object" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Remove(Root / "fail"))
      result should equal(None)
    }

    "fail to remove a field in json array" in {
      val input = Json.arr("test", "test")
      val result = input.patch(Remove(Root / 2))
      result should equal(None)
    }

    "fail to remove silently a field in json object" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Remove(Root / "fail", mustExist = false))
      result should equal(Some(Json.obj("test" -> "test")))
    }

    "fail to remove silently a field in deep json object" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Remove(Root / "test" / "test" / "fail", mustExist = false))
      result should equal(Some(Json.obj("test" -> "test")))
    }

    "fail to remove silently a value in json array" in {
      val input = Json.arr("test", "test")
      val result = input.patch(Remove(Root / 2, mustExist = false))
      result should equal(Some(Json.arr("test", "test")))
    }

    "fail to remove silently a value in deep json array" in {
      val input = Json.arr("test", "test")
      val result = input.patch(Remove(Root / 0 / 1 / 2, mustExist = false))
      result should equal(Some(Json.arr("test", "test")))
    }
  }

  "Json move operation" should {
    "move correctly a field in json object" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Move(Root / "test", Root / "foobar"))
      result should equal(Some(Json.obj("foobar" -> "test")))
      Some(input) should not equal result
    }

    "move correctly a field in deep json object" in {
      val input = Json.obj("test" -> Json.arr(Json.obj("test" -> "test")))
      val result = input.patch(Move(Root / "test" / 0 / "test", Root / "foobar"))
      result should equal(Some(Json.obj(
        "test" -> Json.arr(Json.obj()),
        "foobar" -> "test"
      )))
      Some(input) should not equal result
    }

    "move correctly a value in json array" in {
      val input = Json.arr("test01", "test02")
      val result = input.patch(Move(Root / 1, Root / 0))
      result should equal(Some(Json.arr("test02", "test01")))
      Some(input) should not equal result
    }

    "move correctly a value in deep json array" in {
      val input = Json.arr("test", Json.obj("test" -> Json.arr("test", "foobar")))
      val result = input.patch(Move(Root / 1 / "test" / 1, Root / 0))
      result should equal(Some(Json.arr(
        "foobar",
        "test",
        Json.obj("test" -> Json.arr("test"))
      )))
      Some(input) should not equal result
    }

    "fail to move a value in json array if we point a json object" in {
      val input = Json.arr("test", "test")
      val result = input.patch(Move(Root / "fail", Root))
      result should equal(None)
    }

    "fail to move a value in json array if we fail to evaluate path on json object" in {
      val input = Json.obj("test" -> Json.obj("missing" -> Json.arr("test", "test")))
      val result = input.patch(Move(Root / "test" / "fail" / 0, Root))
      result should equal(None)
    }

    "fail to move a value in json object if we point a json array" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Move(Root / 0, Root))
      result should equal(None)
    }

    "fail to move a value in json object if we fail to evaluate path on json array" in {
      val input = Json.obj("test" -> Json.arr(Json.obj("test" -> "test")))
      val result = input.patch(Move(Root / "test" / 1 / "test", Root))
      result should equal(None)
    }

    "fail to move a non existing field in json object" in {
      val input = Json.obj("missing" -> "test")
      val result = input.patch(Move(Root / "test", Root))
      result should equal(None)
    }

    "fail to replace a non existing value in json array" in {
      val input = Json.arr("missing", "test")
      val result = input.patch(Move(Root / 2, Root))
      result should equal(None)
    }
  }

  "Json copy operation" should {
    "copy correctly a field in json object" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Copy(Root / "test", Root / "foobar"))
      result should equal(Some(Json.obj(
        "foobar" -> "test",
        "test" -> "test"
      )))
      Some(input) should not equal result
    }

    "copy correctly a field in deep json object" in {
      val input = Json.obj("test" -> Json.arr(Json.obj("test" -> "test")))
      val result = input.patch(Copy(Root / "test" / 0 / "test", Root / "foobar"))
      result should equal(Some(Json.obj(
        "test" -> Json.arr(Json.obj("test" -> "test")),
        "foobar" -> "test"
      )))
      Some(input) should not equal result
    }

    "copy correctly a value in json array" in {
      val input = Json.arr("test01", "test02")
      val result = input.patch(Copy(Root / 1, Root / 0))
      result should equal(Some(Json.arr("test02", "test01", "test02")))
      Some(input) should not equal result
    }

    "copy correctly a value in deep json array" in {
      val input = Json.arr("test", Json.obj("test" -> Json.arr("test", "foobar")))
      val result = input.patch(Copy(Root / 1 / "test" / 1, Root / 0))
      result should equal(Some(Json.arr(
        "foobar",
        "test",
        Json.obj("test" -> Json.arr("test", "foobar"))
      )))
      Some(input) should not equal result
    }

    "fail to copy a value in json array if we point a json object" in {
      val input = Json.arr("test", "test")
      val result = input.patch(Copy(Root / "fail", Root))
      result should equal(None)
    }

    "fail to copy a value in json array if we fail to evaluate path on json object" in {
      val input = Json.obj("test" -> Json.obj("missing" -> Json.arr("test", "test")))
      val result = input.patch(Copy(Root / "test" / "fail" / 0, Root))
      result should equal(None)
    }

    "fail to copy a value in json object if we point a json array" in {
      val input = Json.obj("test" -> "test")
      val result = input.patch(Copy(Root / 0, Root))
      result should equal(None)
    }

    "fail to copy a value in json object if we fail to evaluate path on json array" in {
      val input = Json.obj("test" -> Json.arr(Json.obj("test" -> "test")))
      val result = input.patch(Copy(Root / "test" / 1 / "test", Root))
      result should equal(None)
    }

    "fail to copy a non existing field in json object" in {
      val input = Json.obj("missing" -> "test")
      val result = input.patch(Copy(Root / "test", Root))
      result should equal(None)
    }

    "fail to copy a non existing value in json array" in {
      val input = Json.arr("missing", "test")
      val result = input.patch(Copy(Root / 2, Root))
      result should equal(None)
    }
  }

}