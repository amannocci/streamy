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

import io.techcode.streamy.component.transform.JsonTransform.{Behaviour, Config}
import io.techcode.streamy.stream.StreamException
import org.scalatest.{FlatSpec, Matchers}
import play.api.libs.json._

/**
  * Json transform spec.
  */
class JsonTransformSpec extends FlatSpec with Matchers {

  "Json transform " must "transform correctly a packet inplace" in {
    val input = Json.obj("message" -> """{"message":"foobar"}""")
    val component = new JsonTransform(Config(__ \ "message"))
    component.apply(input) should equal(Json.obj("message" -> Json.obj("message" -> "foobar")))
  }

  it must "transform correctly a packet with a root target" in {
    val input = Json.obj("message" -> """{"test":"foobar"}""")
    val component = new JsonTransform(Config(__ \ "message", Some(__)))
    component.apply(input) should equal(Json.obj(
      "message" -> """{"test":"foobar"}""",
      "test" -> "foobar"
    ))
  }

  it must "transform correctly a packet with a root target equal to an existing field" in {
    val input = Json.obj("message" -> """{"message":"foobar"}""")
    val component = new JsonTransform(Config(__ \ "message", Some(__)))
    component.apply(input) should equal(Json.obj("message" -> "foobar"))
  }

  it must "transform correctly a packet with a specific target" in {
    val input = Json.obj("message" -> """{"test":"foobar"}""")
    val component = new JsonTransform(Config(__ \ "message", Some(__ \ "target")))
    component.apply(input) should equal(Json.obj(
      "message" -> """{"test":"foobar"}""",
      "target" -> Json.obj("test" -> "foobar")
    ))
  }

  it must "transform correctly a packet with a specific target and remove source" in {
    val input = Json.obj("message" -> """{"test":"foobar"}""")
    val component = new JsonTransform(Config(__ \ "message", Some(__ \ "target"), removeSource = true))
    component.apply(input) should equal(Json.obj("target" -> Json.obj("test" -> "foobar")))
  }

  it must "skip correctly a packet with a wrong source field" in {
    val input = Json.obj("message" -> "foobar")
    val component = new JsonTransform(Config(__ \ "message", onError = Behaviour.Skip))
    component.apply(input) should equal(Json.obj("message" -> "foobar"))
  }

  it must "skip correctly a packet with a wrong source field with a specific target" in {
    val input = Json.obj("message" -> "foobar")
    val component = new JsonTransform(Config(__ \ "message", Some(__ \ "target"), onError = Behaviour.Skip))
    component.apply(input) should equal(Json.obj("message" -> "foobar", "target" -> "foobar"))
  }

  it must "skip correctly a packet with a wrong source field with a specific target and remove source" in {
    val input = Json.obj("message" -> "foobar")
    val component = new JsonTransform(Config(__ \ "message", Some(__ \ "target"), removeSource = true, onError = Behaviour.Skip))
    component.apply(input) should equal(Json.obj("target" -> "foobar"))
  }

  it must "discard correctly a packet with a wrong source field by throwing an error" in {
    val input = Json.obj("message" -> "foobar")
    val component = new JsonTransform(Config(__ \ "message", onError = Behaviour.Discard))
    assertThrows[StreamException] {
      component.apply(input)
    }
  }

}
