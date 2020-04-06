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
package io.techcode.streamy.component

import akka.NotUsed
import akka.stream.scaladsl.Source
import akka.stream.testkit.scaladsl.TestSink
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import io.techcode.streamy.StreamyTestSystem
import io.techcode.streamy.component.FlowTransformer.SuccessBehaviour
import io.techcode.streamy.component.FlowTransformer.SuccessBehaviour.SuccessBehaviour
import io.techcode.streamy.component.Transformer.ErrorBehaviour
import io.techcode.streamy.component.Transformer.ErrorBehaviour.ErrorBehaviour
import io.techcode.streamy.event.StreamEvent
import io.techcode.streamy.util.json._

/**
  * Flow transformer spec.
  */
class FlowTransformerSpec extends StreamyTestSystem {

  class Impl(config: ImplConfig) extends FlowTransformerLogic(config) {
    override def transform(value: Json): MaybeJson = value match {
      case x: JsString => s"${x.value}bar"
      case x: JsObject => x
      case _ => JsUndefined
    }
  }

  class ImplParent(config: ImplConfig) extends FlowTransformerLogic(config) {
    override def transform(value: Json, payload: Json): MaybeJson = value match {
      case x: JsString => s"${x.value}bar"
      case _ => transform(value)
    }
  }

  class ImplWithCatchException(config: ImplConfig) extends FlowTransformerLogic(config) {
    override def transform(value: Json): MaybeJson = value
      .map[String](x => s"${x}bar")
      .orElse(error(new IllegalArgumentException))
  }

  class ImplWithException(config: ImplConfig) extends FlowTransformerLogic(config) {
    override def transform(value: Json): MaybeJson = value
      .map[String](x => s"${x}bar")
      .orElse(throw new IllegalArgumentException)
  }

  class ImplWithError(config: ImplConfig) extends FlowTransformerLogic(config) {
    override def transform(value: Json): MaybeJson = value
      .map[String](x => s"${x}bar")
      .orElse(error("Error"))
  }

  // Component configuration
  case class ImplConfig(
    override val source: JsonPointer,
    override val target: Option[JsonPointer] = None,
    override val onSuccess: SuccessBehaviour = SuccessBehaviour.Skip,
    override val onError: ErrorBehaviour = ErrorBehaviour.Skip
  ) extends FlowTransformer.Config(source, target, onSuccess, onError)

  def mat(decider: Supervision.Decider): ActorMaterializer =
    ActorMaterializer(ActorMaterializerSettings(system).withSupervisionStrategy(decider))

  "Flow transformer" should {
    "transform correctly an event inplace" in {
      implicit val materializer: ActorMaterializer = mat(_ => Supervision.Resume)

      val transformer = new IdentifyFlowTransformer[NotUsed] {
        def factory(): FlowTransformerLogic = new Impl(ImplConfig(Root / "message"))
      }

      val stream = Source.single(StreamEvent.from(Json.obj("message" -> "foo")))
        .via(transformer)
        .runWith(TestSink.probe[StreamEvent[NotUsed]])

      stream.requestNext() should equal(StreamEvent.from(Json.obj("message" -> "foobar")))
      stream.expectComplete()
    }

    "transform correctly an event with a specific target" in {
      implicit val materializer: ActorMaterializer = mat(_ => Supervision.Resume)

      val transformer = new IdentifyFlowTransformer[NotUsed] {
        def factory(): FlowTransformerLogic = new Impl(ImplConfig(Root / "message", Some(Root / "target")))
      }

      val stream = Source.single(StreamEvent.from(Json.obj("message" -> "foo")))
        .via(transformer)
        .runWith(TestSink.probe[StreamEvent[NotUsed]])

      stream.requestNext() should equal(StreamEvent.from(Json.obj(
        "message" -> "foo",
        "target" -> "foobar"
      )))
      stream.expectComplete()
    }

    "transform correctly an event with a specific target and remove source" in {
      implicit val materializer: ActorMaterializer = mat(_ => Supervision.Resume)

      val transformer = new IdentifyFlowTransformer[NotUsed] {
        def factory(): FlowTransformerLogic = new Impl(ImplConfig(Root / "message", Some(Root / "target"), onSuccess = SuccessBehaviour.Remove))
      }

      val stream = Source.single(StreamEvent.from(Json.obj("message" -> "foo")))
        .via(transformer)
        .runWith(TestSink.probe[StreamEvent[NotUsed]])

      stream.requestNext() should equal(StreamEvent.from(Json.obj("target" -> "foobar")))
      stream.expectComplete()
    }

    "transform correctly an event with a root target" in {
      implicit val materializer: ActorMaterializer = mat(_ => Supervision.Resume)

      val transformer = new IdentifyFlowTransformer[NotUsed] {
        def factory(): FlowTransformerLogic = new Impl(ImplConfig(Root / "message", Some(Root)))
      }

      val stream = Source.single(StreamEvent.from(Json.obj("message" -> Json.obj("message" -> "foo"))))
        .via(transformer)
        .runWith(TestSink.probe[StreamEvent[NotUsed]])

      stream.requestNext() should equal(StreamEvent.from(Json.obj("message" -> "foo")))
      stream.expectComplete()
    }

    "transform correctly an event with a root target and remove source" in {
      implicit val materializer: ActorMaterializer = mat(_ => Supervision.Resume)

      val transformer = new IdentifyFlowTransformer[NotUsed] {
        def factory(): FlowTransformerLogic = new Impl(ImplConfig(Root / "message", Some(Root), SuccessBehaviour.Remove))
      }

      val stream = Source.single(StreamEvent.from(Json.obj("message" -> Json.obj("test" -> "foo"))))
        .via(transformer)
        .runWith(TestSink.probe[StreamEvent[NotUsed]])

      stream.requestNext() should equal(StreamEvent.from(Json.obj("test" -> "foo")))
      stream.expectComplete()
    }

    "transform correctly an event inplace with parent transform" in {
      implicit val materializer: ActorMaterializer = mat(_ => Supervision.Resume)

      val transformer = new IdentifyFlowTransformer[NotUsed] {
        def factory(): FlowTransformerLogic = new ImplParent(ImplConfig(Root / "message"))
      }

      val stream = Source.single(StreamEvent.from(Json.obj("message" -> "foo")))
        .via(transformer)
        .runWith(TestSink.probe[StreamEvent[NotUsed]])

      stream.requestNext() should equal(StreamEvent.from(Json.obj("message" -> "foobar")))
      stream.expectComplete()
    }

    "skip correctly an event with a wrong source field" in {
      implicit val materializer: ActorMaterializer = mat(_ => Supervision.Resume)

      val transformer = new IdentifyFlowTransformer[NotUsed] {
        def factory(): FlowTransformerLogic = new Impl(ImplConfig(Root / "message"))
      }

      val stream = Source.single(StreamEvent.from(Json.obj("message" -> 1)))
        .via(transformer)
        .runWith(TestSink.probe[StreamEvent[NotUsed]])

      stream.requestNext() should equal(StreamEvent.from(Json.obj("message" -> 1)))
      stream.expectComplete()
    }

    "skip correctly an event with a wrong source field and parent transform" in {
      implicit val materializer: ActorMaterializer = mat(_ => Supervision.Resume)

      val transformer = new IdentifyFlowTransformer[NotUsed] {
        def factory(): FlowTransformerLogic = new ImplParent(ImplConfig(Root / "message"))
      }

      val stream = Source.single(StreamEvent.from(Json.obj("message" -> 1)))
        .via(transformer)
        .runWith(TestSink.probe[StreamEvent[NotUsed]])

      stream.requestNext() should equal(StreamEvent.from(Json.obj("message" -> 1)))
      stream.expectComplete()
    }

    "skip correctly an event with a wrong source field with a specific target" in {
      implicit val materializer: ActorMaterializer = mat(_ => Supervision.Resume)

      val transformer = new IdentifyFlowTransformer[NotUsed] {
        def factory(): FlowTransformerLogic = new Impl(ImplConfig(Root / "message", Some(Root / "target")))
      }

      val stream = Source.single(StreamEvent.from(Json.obj("message" -> 1)))
        .via(transformer)
        .runWith(TestSink.probe[StreamEvent[NotUsed]])

      stream.requestNext() should equal(StreamEvent.from(Json.obj("message" -> 1)))
      stream.expectComplete()
    }

    "skip correctly an event with a wrong source field with a specific target and remove source" in {
      implicit val materializer: ActorMaterializer = mat(_ => Supervision.Resume)

      val transformer = new IdentifyFlowTransformer[NotUsed] {
        def factory(): FlowTransformerLogic = new Impl(ImplConfig(Root / "message", Some(Root / "target"), onSuccess = SuccessBehaviour.Remove))
      }

      val stream = Source.single(StreamEvent.from(Json.obj("message" -> 1)))
        .via(transformer)
        .runWith(TestSink.probe[StreamEvent[NotUsed]])

      stream.requestNext() should equal(StreamEvent.from(Json.obj("message" -> 1)))
      stream.expectComplete()
    }

    "skip correctly an event by throwing an error based on exception" in {
      implicit val materializer: ActorMaterializer = mat(_ => Supervision.Resume)

      val transformer = new IdentifyFlowTransformer[NotUsed] {
        def factory(): FlowTransformerLogic =
          new ImplWithCatchException(ImplConfig(Root / "message"))
      }

      val stream = Source.single(StreamEvent.from(Json.obj("message" -> 1)))
        .via(transformer)
        .runWith(TestSink.probe[StreamEvent[NotUsed]])

      stream.requestNext() should equal(StreamEvent.from(Json.obj("message" -> 1)))
      stream.expectComplete()
    }

    "skip correctly an event by throwing an error based on msg" in {
      implicit val materializer: ActorMaterializer = mat(_ => Supervision.Resume)

      val transformer = new IdentifyFlowTransformer[NotUsed] {
        def factory(): FlowTransformerLogic =
          new ImplWithError(ImplConfig(Root / "message"))
      }

      val stream = Source.single(StreamEvent.from(Json.obj("message" -> 1)))
        .via(transformer)
        .runWith(TestSink.probe[StreamEvent[NotUsed]])

      stream.requestNext() should equal(StreamEvent.from(Json.obj("message" -> 1)))
      stream.expectComplete()
    }

    "discard correctly an event by throwing an error based on exception" in {
      implicit val materializer: ActorMaterializer = mat(_ => Supervision.Resume)

      val transformer = new IdentifyFlowTransformer[NotUsed] {
        def factory(): FlowTransformerLogic = new ImplWithCatchException(ImplConfig(Root / "message", onError = ErrorBehaviour.Discard))
      }

      val stream = Source(Seq(
        StreamEvent.from(Json.obj("message" -> 1)),
        StreamEvent.from(Json.obj("message" -> "foo"))
      )).via(transformer)
        .runWith(TestSink.probe[StreamEvent[NotUsed]])

      stream.requestNext() should equal(StreamEvent.from(Json.obj("message" -> "foobar")))
      stream.expectComplete()
    }

    "discard correctly an event by throwing an error and report based on exception" in {
      implicit val materializer: ActorMaterializer = mat(_ => Supervision.Resume)

      val transformer = new IdentifyFlowTransformer[NotUsed] {
        def factory(): FlowTransformerLogic = new ImplWithCatchException(ImplConfig(Root / "message", onError = ErrorBehaviour.DiscardAndReport))
      }

      val stream = Source(Seq(
        StreamEvent.from(Json.obj("message" -> 1)),
        StreamEvent.from(Json.obj("message" -> "foo"))
      )).via(transformer)
        .runWith(TestSink.probe[StreamEvent[NotUsed]])

      stream.requestNext() should equal(StreamEvent.from(Json.obj("message" -> "foobar")))
      stream.expectComplete()
    }

    "discard correctly an event by throwing an error based on msg" in {
      implicit val materializer: ActorMaterializer = mat(_ => Supervision.Resume)

      val transformer = new IdentifyFlowTransformer[NotUsed] {
        def factory(): FlowTransformerLogic = new ImplWithError(ImplConfig(Root / "message", onError = ErrorBehaviour.Discard))
      }

      val stream = Source(Seq(
        StreamEvent.from(Json.obj("message" -> 1)),
        StreamEvent.from(Json.obj("message" -> "foo"))
      )).via(transformer)
        .runWith(TestSink.probe[StreamEvent[NotUsed]])

      stream.requestNext() should equal(StreamEvent.from(Json.obj("message" -> "foobar")))
      stream.expectComplete()
    }

    "discard correctly an event by throwing an error and report based on msg" in {
      implicit val materializer: ActorMaterializer = mat(_ => Supervision.Resume)

      val transformer = new IdentifyFlowTransformer[NotUsed] {
        def factory(): FlowTransformerLogic = new ImplWithError(ImplConfig(Root / "message", onError = ErrorBehaviour.DiscardAndReport))
      }

      val stream = Source(Seq(
        StreamEvent.from(Json.obj("message" -> 1)),
        StreamEvent.from(Json.obj("message" -> "foo"))
      )).via(transformer)
        .runWith(TestSink.probe[StreamEvent[NotUsed]])

      stream.requestNext() should equal(StreamEvent.from(Json.obj("message" -> "foobar")))
      stream.expectComplete()
    }

    "discard correctly an event by throwing an error based on uncatched exception" in {
      implicit val materializer: ActorMaterializer = mat(_ => Supervision.Resume)

      val transformer = new IdentifyFlowTransformer[NotUsed] {
        def factory(): FlowTransformerLogic =
          new ImplWithException(ImplConfig(Root / "message"))
      }

      val stream = Source(Seq(
        StreamEvent.from(Json.obj("message" -> 1)),
        StreamEvent.from(Json.obj("message" -> "foo"))
      )).via(transformer)
        .runWith(TestSink.probe[StreamEvent[NotUsed]])

      stream.requestNext() should equal(StreamEvent.from(Json.obj("message" -> "foobar")))
      stream.expectComplete()
    }

    "discard an event and fail correctly by throwing an error based on uncatched exception" in {
      implicit val materializer: ActorMaterializer = mat(_ => Supervision.Stop)

      val transformer = new IdentifyFlowTransformer[NotUsed] {
        def factory(): FlowTransformerLogic =
          new ImplWithException(ImplConfig(Root / "message"))
      }

      Source.single(StreamEvent.from(Json.obj("message" -> 1)))
        .via(transformer)
        .runWith(TestSink.probe[StreamEvent[NotUsed]])
        .request(1)
        .expectError()
    }
  }

}

