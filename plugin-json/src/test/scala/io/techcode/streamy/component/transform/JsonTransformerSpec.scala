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
package io.techcode.streamy.component.transform

import io.circe._
import io.techcode.streamy.component.transform.JsonTransformer.{Config, Mode}
import io.techcode.streamy.util.json._
import org.scalatest.{FlatSpec, Matchers}

/**
  * Json transform spec.
  */
class JsonTransformerSpec extends FlatSpec with Matchers {

  "Json transformer" must "deserialize correctly a packet inplace" in {
    val input = Json.obj("message" -> """{"message":"foobar"}""")
    val component = new JsonTransformer(Config(root / "message"))
    component.apply(input) should equal(Json.obj("message" -> Json.obj("message" -> "foobar")))
  }

  it must "deserialize correctly a packet with a root target" in {
    val input = Json.obj("message" -> """{"test":"foobar"}""")
    val component = new JsonTransformer(Config(root / "message", Some(root)))
    component.apply(input) should equal(Json.obj(
      "message" -> """{"test":"foobar"}""",
      "test" -> "foobar"
    ))
  }

  it must "deserialize correctly a packet with a root target equal to an existing field" in {
    val input = Json.obj("message" -> """{"message":"foobar"}""")
    val component = new JsonTransformer(Config(root / "message", Some(root)))
    component.apply(input) should equal(Json.obj("message" -> "foobar"))
  }

  it must "fast skip correctly a packet with a wrong source field" in {
    val input = Json.obj("message" -> "foobar")
    val component = new JsonTransformer(Config(root / "message"))
    component.apply(input) should equal(Json.obj("message" -> "foobar"))
  }

  it must "skip correctly a packet with a wrong source field" in {
    val input = Json.obj("message" -> "{foobar}")
    val component = new JsonTransformer(Config(root / "message"))
    component.apply(input) should equal(Json.obj("message" -> "{foobar}"))
  }

  it must "serialize correctly a packet inplace" in {
    val input = Json.obj("message" -> Json.obj("message" -> "foobar"))
    val component = new JsonTransformer(Config(root / "message", mode = Mode.Serialize))
    component.apply(input) should equal(Json.obj("message" -> """{"message":"foobar"}"""))
  }

  it must "serialize correctly a packet with a root source" in {
    val input = Json.obj("test" -> "foobar")
    val component = new JsonTransformer(Config(root, Some(root / "message"), mode = Mode.Serialize))
    component.apply(input) should equal(Json.obj(
      "message" -> """{"test":"foobar"}""",
      "test" -> "foobar"
    ))
  }

  it must "serialize correctly a packet with a root target equal to an existing field" in {
    val input = Json.obj("test" -> "foobar")
    val component = new JsonTransformer(Config(root, Some(root / "test"), mode = Mode.Serialize))
    component.apply(input) should equal(Json.obj(
      "test" -> """{"test":"foobar"}"""
    ))
  }

}
