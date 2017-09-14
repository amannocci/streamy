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
package io.techcode.streamy.component

import io.techcode.streamy.component.SimpleTransformer.SuccessBehaviour
import io.techcode.streamy.component.SimpleTransformer.SuccessBehaviour.SuccessBehaviour
import io.techcode.streamy.component.Transformer.ErrorBehaviour
import io.techcode.streamy.component.Transformer.ErrorBehaviour.ErrorBehaviour
import io.techcode.streamy.stream.StreamException
import org.scalatest._
import play.api.libs.json.Reads._
import play.api.libs.json._

/**
  * Transform spec.
  */
class SimpleTransformerSpec extends FlatSpec with Matchers {

  class Impl(config: ImplConfig) extends SimpleTransformer(config) {
    override def parseInplace(path: JsPath): Reads[JsObject] = path.json.update(
      Reads.of[JsString].map(field => JsString(s"${field.value}bar"))
    )
  }

  // Component configuration
  case class ImplConfig(
    override val source: JsPath,
    override val target: Option[JsPath] = None,
    override val onSuccess: SuccessBehaviour = SuccessBehaviour.Skip,
    override val onError: ErrorBehaviour = ErrorBehaviour.Skip
  ) extends SimpleTransformer.Config(source, target, onSuccess, onError)

  "Impl transform" must "transform correctly a packet inplace" in {
    val input = Json.obj("message" -> "foo")
    val component = new Impl(ImplConfig(__ \ "message"))
    component.apply(input) should equal(Json.obj("message" -> "foobar"))
  }

  it must "transform correctly a packet with a specific target" in {
    val input = Json.obj("message" -> "foo")
    val component = new Impl(ImplConfig(__ \ "message", Some(__ \ "target")))
    component.apply(input) should equal(Json.obj(
      "message" -> "foo",
      "target" -> "foobar")
    )
  }

  it must "transform correctly a packet with a specific target and remove source" in {
    val input = Json.obj("message" -> "foo")
    val component = new Impl(ImplConfig(__ \ "message", Some(__ \ "target"), onSuccess = SuccessBehaviour.Remove))
    component.apply(input) should equal(Json.obj("target" -> "foobar"))
  }

  it must "skip correctly a packet with a wrong source field" in {
    val input = Json.obj("message" -> 1)
    val component = new Impl(ImplConfig(__ \ "message"))
    component.apply(input) should equal(Json.obj("message" -> 1))
  }

  it must "skip correctly a packet with a wrong source field with a specific target" in {
    val input = Json.obj("message" -> 1)
    val component = new Impl(ImplConfig(__ \ "message", Some(__ \ "target")))
    component.apply(input) should equal(Json.obj("message" -> 1))
  }

  it must "skip correctly a packet with a wrong source field with a specific target and remove source" in {
    val input = Json.obj("message" -> 1)
    val component = new Impl(ImplConfig(__ \ "message", Some(__ \ "target"), onSuccess = SuccessBehaviour.Remove))
    component.apply(input) should equal(Json.obj("message" -> 1))
  }

  it must "discard correctly a packet with a wrong source field by throwing an error" in {
    val input = Json.obj("message" -> 1)
    val component = new Impl(ImplConfig(__ \ "message", onError = ErrorBehaviour.Discard))
    assertThrows[StreamException] {
      component.apply(input)
    }
  }

  it must "discard correctly a packet with a wrong source field by throwing an error and report" in {
    val input = Json.obj("message" -> 1)
    val component = new Impl(ImplConfig(__ \ "message", onError = ErrorBehaviour.DiscardAndReport))
    assertThrows[StreamException] {
      component.apply(input)
    }
  }

}

