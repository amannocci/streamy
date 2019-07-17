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
package io.techcode.streamy.component

import akka.stream.scaladsl.Source
import akka.stream.testkit.scaladsl.TestSink
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import io.techcode.streamy.StreamyTestSystem
import io.techcode.streamy.component.FlowTransformer.SuccessBehaviour
import io.techcode.streamy.component.FlowTransformer.SuccessBehaviour.SuccessBehaviour
import io.techcode.streamy.component.Transformer.ErrorBehaviour
import io.techcode.streamy.component.Transformer.ErrorBehaviour.ErrorBehaviour
import io.techcode.streamy.util.StreamException
import io.techcode.streamy.util.json._

/**
  * Flow transformer spec.
  */
class FlowTransformerSpec extends StreamyTestSystem {

  class Impl(config: ImplConfig) extends FlowTransformerLogic(config) {
    override def transform(value: Json): MaybeJson = value match {
      case x: JsString => s"${x.value}bar"
      case _ => JsUndefined
    }
  }

  class ImplParent(config: ImplConfig) extends FlowTransformerLogic(config) {
    override def transform(value: Json, pkt: Json): MaybeJson = value match {
      case x: JsString => s"${x.value}bar"
      case _ => JsUndefined
    }
  }

  // Component configuration
  case class ImplConfig(
    override val source: JsonPointer,
    override val target: Option[JsonPointer] = None,
    override val onSuccess: SuccessBehaviour = SuccessBehaviour.Skip,
    override val onError: ErrorBehaviour = ErrorBehaviour.Skip
  ) extends FlowTransformer.Config(source, target, onSuccess, onError)

  "Flow transformer" should {
    "transform correctly a packet inplace" in {
      val decider: Supervision.Decider = _ => Supervision.Resume

      implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(system).withSupervisionStrategy(decider))

      val transformer = FlowTransformer(() => new Impl(ImplConfig(Root / "message")))

      Source.single(Json.obj("message" -> "foo"))
        .via(transformer)
        .runWith(TestSink.probe[Json])
        .requestNext() should equal(Json.obj("message" -> "foobar"))
    }

    "transform correctly a packet with a specific target" in {
      val decider: Supervision.Decider = _ => Supervision.Resume

      implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(system).withSupervisionStrategy(decider))

      val transformer = FlowTransformer(() => new Impl(ImplConfig(Root / "message", Some(Root / "target"))))

      Source.single(Json.obj("message" -> "foo"))
        .via(transformer)
        .runWith(TestSink.probe[Json])
        .requestNext() should equal(Json.obj(
        "message" -> "foo",
        "target" -> "foobar"
      ))
    }

    "transform correctly a packet with a specific target and remove source" in {
      val input = Json.obj("message" -> "foo")
      val component = new Impl(ImplConfig(Root / "message", Some(Root / "target"), onSuccess = SuccessBehaviour.Remove))
      component.apply(input) should equal(Json.obj("target" -> "foobar"))
    }

    "transform correctly a packet inplace with parent transform" in {
      val input = Json.obj("message" -> "foo")
      val component = new ImplParent(ImplConfig(Root / "message"))
      component.transform(JsNull) should equal(JsUndefined)
      component.apply(input) should equal(Json.obj("message" -> "foobar"))
    }

    "skip correctly a packet with a wrong source field" in {
      val input = Json.obj("message" -> 1)
      val component = new Impl(ImplConfig(Root / "message"))
      component.apply(input) should equal(Json.obj("message" -> 1))
    }

    "skip correctly a packet with a wrong source field with a specific target" in {
      val input = Json.obj("message" -> 1)
      val component = new Impl(ImplConfig(Root / "message", Some(Root / "target")))
      component.apply(input) should equal(Json.obj("message" -> 1))
    }

    "skip correctly a packet with a wrong source field with a specific target and remove source" in {
      val input = Json.obj("message" -> 1)
      val component = new Impl(ImplConfig(Root / "message", Some(Root / "target"), onSuccess = SuccessBehaviour.Remove))
      component.apply(input) should equal(Json.obj("message" -> 1))
    }

    "discard correctly a packet with a wrong source field by throwing an error" in {
      val input = Json.obj("message" -> 1)
      val component = new Impl(ImplConfig(Root / "message", onError = ErrorBehaviour.Discard))
      assertThrows[StreamException] {
        component.apply(input)
      }
    }

    "discard correctly a packet with a wrong source field by throwing an error and report" in {
      val input = Json.obj("message" -> 1)
      val component = new Impl(ImplConfig(Root / "message", onError = ErrorBehaviour.DiscardAndReport))
      assertThrows[StreamException] {
        component.apply(input)
      }
    }
  }

}

