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

import akka.stream.ActorAttributes.supervisionStrategy
import akka.stream.Supervision
import akka.stream.scaladsl.Source
import akka.stream.testkit.scaladsl.TestSink
import com.typesafe.config.ConfigFactory
import io.techcode.streamy.StreamyTestSystem
import io.techcode.streamy.component.FlowTransformer.SuccessBehaviour
import io.techcode.streamy.component.FlowTransformer.SuccessBehaviour.SuccessBehaviour
import io.techcode.streamy.component.Transformer.ErrorBehaviour
import io.techcode.streamy.component.Transformer.ErrorBehaviour.ErrorBehaviour
import io.techcode.streamy.event.StreamEvent
import io.techcode.streamy.util.json._
import io.techcode.streamy.config._
import pureconfig._
import pureconfig.generic.auto._
import pureconfig.error.ConfigReaderException

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

  "Flow transformer" should {
    "be built from configuration" in {
      ConfigSource.fromConfig(ConfigFactory.parseString(
        """{
          |"source": "/",
          |"on-success": "skip",
          |"on-error": "skip"
          |}""".stripMargin)).loadOrThrow[ImplConfig]

      ConfigSource.fromConfig(ConfigFactory.parseString(
        """{
          |"source": "/",
          |"on-success": "remove",
          |"on-error": "discard"
          |}""".stripMargin)).loadOrThrow[ImplConfig]

      ConfigSource.fromConfig(ConfigFactory.parseString(
        """{
          |"source": "/",
          |"on-error": "discard-and-report"
          |}""".stripMargin)).loadOrThrow[ImplConfig]

      assertThrows[ConfigReaderException[_]] {
        ConfigSource.fromConfig(ConfigFactory.parseString(
          """{
            |"source": "/",
            |"on-success": "unknown"
            |}""".stripMargin)).loadOrThrow[ImplConfig]
      }

      assertThrows[ConfigReaderException[_]] {
        ConfigSource.fromConfig(ConfigFactory.parseString(
          """{
            |"source": "/",
            |"on-error": "unknown"
            |}""".stripMargin)).loadOrThrow[ImplConfig]
      }
    }

    "transform correctly an event inplace" in {
      val transformer = new FlowTransformer {
        def factory(): FlowTransformerLogic = new Impl(ImplConfig(Root / "message"))
      }

      val stream = Source.single(StreamEvent(Json.obj("message" -> "foo")))
        .via(transformer)
        .addAttributes(supervisionStrategy(_ => Supervision.Resume))
        .runWith(TestSink.probe[StreamEvent])

      stream.requestNext() should equal(StreamEvent(Json.obj("message" -> "foobar")))
      stream.expectComplete()
    }

    "transform correctly an event with a specific target" in {
      val transformer = new FlowTransformer {
        def factory(): FlowTransformerLogic = new Impl(ImplConfig(Root / "message", Some(Root / "target")))
      }

      val stream = Source.single(StreamEvent(Json.obj("message" -> "foo")))
        .via(transformer)
        .addAttributes(supervisionStrategy(_ => Supervision.Resume))
        .runWith(TestSink.probe[StreamEvent])

      stream.requestNext() should equal(StreamEvent(Json.obj(
        "message" -> "foo",
        "target" -> "foobar"
      )))
      stream.expectComplete()
    }

    "transform correctly an event with a specific target and remove source" in {
      val transformer = new FlowTransformer {
        def factory(): FlowTransformerLogic = new Impl(ImplConfig(Root / "message", Some(Root / "target"), onSuccess = SuccessBehaviour.Remove))
      }

      val stream = Source.single(StreamEvent(Json.obj("message" -> "foo")))
        .via(transformer)
        .addAttributes(supervisionStrategy(_ => Supervision.Resume))
        .runWith(TestSink.probe[StreamEvent])

      stream.requestNext() should equal(StreamEvent(Json.obj("target" -> "foobar")))
      stream.expectComplete()
    }

    "transform correctly an event with a root target" in {
      val transformer = new FlowTransformer {
        def factory(): FlowTransformerLogic = new Impl(ImplConfig(Root / "message", Some(Root)))
      }

      val stream = Source.single(StreamEvent(Json.obj("message" -> Json.obj("message" -> "foo"))))
        .via(transformer)
        .addAttributes(supervisionStrategy(_ => Supervision.Resume))
        .runWith(TestSink.probe[StreamEvent])

      stream.requestNext() should equal(StreamEvent(Json.obj("message" -> "foo")))
      stream.expectComplete()
    }

    "transform correctly an event with a root target and remove source" in {
      val transformer = new FlowTransformer {
        def factory(): FlowTransformerLogic = new Impl(ImplConfig(Root / "message", Some(Root), SuccessBehaviour.Remove))
      }

      val stream = Source.single(StreamEvent(Json.obj("message" -> Json.obj("test" -> "foo"))))
        .via(transformer)
        .addAttributes(supervisionStrategy(_ => Supervision.Resume))
        .runWith(TestSink.probe[StreamEvent])

      stream.requestNext() should equal(StreamEvent(Json.obj("test" -> "foo")))
      stream.expectComplete()
    }

    "transform correctly an event inplace with parent transform" in {
      val transformer = new FlowTransformer {
        def factory(): FlowTransformerLogic = new ImplParent(ImplConfig(Root / "message"))
      }

      val stream = Source.single(StreamEvent(Json.obj("message" -> "foo")))
        .via(transformer)
        .addAttributes(supervisionStrategy(_ => Supervision.Resume))
        .runWith(TestSink.probe[StreamEvent])

      stream.requestNext() should equal(StreamEvent(Json.obj("message" -> "foobar")))
      stream.expectComplete()
    }

    "skip correctly an event with a wrong source field" in {
      val transformer = new FlowTransformer {
        def factory(): FlowTransformerLogic = new Impl(ImplConfig(Root / "message"))
      }

      val stream = Source.single(StreamEvent(Json.obj("message" -> 1)))
        .via(transformer)
        .addAttributes(supervisionStrategy(_ => Supervision.Resume))
        .runWith(TestSink.probe[StreamEvent])

      stream.requestNext() should equal(StreamEvent(Json.obj("message" -> 1)))
      stream.expectComplete()
    }

    "skip correctly an event with a wrong source field and parent transform" in {
      val transformer = new FlowTransformer {
        def factory(): FlowTransformerLogic = new ImplParent(ImplConfig(Root / "message"))
      }

      val stream = Source.single(StreamEvent(Json.obj("message" -> 1)))
        .via(transformer)
        .addAttributes(supervisionStrategy(_ => Supervision.Resume))
        .runWith(TestSink.probe[StreamEvent])

      stream.requestNext() should equal(StreamEvent(Json.obj("message" -> 1)))
      stream.expectComplete()
    }

    "skip correctly an event with a wrong source field with a specific target" in {
      val transformer = new FlowTransformer {
        def factory(): FlowTransformerLogic = new Impl(ImplConfig(Root / "message", Some(Root / "target")))
      }

      val stream = Source.single(StreamEvent(Json.obj("message" -> 1)))
        .via(transformer)
        .addAttributes(supervisionStrategy(_ => Supervision.Resume))
        .runWith(TestSink.probe[StreamEvent])

      stream.requestNext() should equal(StreamEvent(Json.obj("message" -> 1)))
      stream.expectComplete()
    }

    "skip correctly an event with a wrong source field with a specific target and remove source" in {
      val transformer = new FlowTransformer {
        def factory(): FlowTransformerLogic = new Impl(ImplConfig(Root / "message", Some(Root / "target"), onSuccess = SuccessBehaviour.Remove))
      }

      val stream = Source.single(StreamEvent(Json.obj("message" -> 1)))
        .via(transformer)
        .addAttributes(supervisionStrategy(_ => Supervision.Resume))
        .runWith(TestSink.probe[StreamEvent])

      stream.requestNext() should equal(StreamEvent(Json.obj("message" -> 1)))
      stream.expectComplete()
    }

    "skip correctly an event by throwing an error based on exception" in {
      val transformer = new FlowTransformer {
        def factory(): FlowTransformerLogic =
          new ImplWithCatchException(ImplConfig(Root / "message"))
      }

      val stream = Source.single(StreamEvent(Json.obj("message" -> 1)))
        .via(transformer)
        .addAttributes(supervisionStrategy(_ => Supervision.Resume))
        .runWith(TestSink.probe[StreamEvent])

      stream.requestNext() should equal(StreamEvent(Json.obj("message" -> 1)))
      stream.expectComplete()
    }

    "skip correctly an event by throwing an error based on msg" in {
      val transformer = new FlowTransformer {
        def factory(): FlowTransformerLogic =
          new ImplWithError(ImplConfig(Root / "message"))
      }

      val stream = Source.single(StreamEvent(Json.obj("message" -> 1)))
        .via(transformer)
        .addAttributes(supervisionStrategy(_ => Supervision.Resume))
        .runWith(TestSink.probe[StreamEvent])

      stream.requestNext() should equal(StreamEvent(Json.obj("message" -> 1)))
      stream.expectComplete()
    }

    "discard correctly an event by throwing an error based on exception" in {
      val transformer = new FlowTransformer {
        def factory(): FlowTransformerLogic = new ImplWithCatchException(ImplConfig(Root / "message", onError = ErrorBehaviour.Discard))
      }

      val stream = Source(Seq(
        StreamEvent(Json.obj("message" -> 1)),
        StreamEvent(Json.obj("message" -> "foo"))
      )).via(transformer)
        .addAttributes(supervisionStrategy(_ => Supervision.Resume))
        .runWith(TestSink.probe[StreamEvent])

      stream.requestNext() should equal(StreamEvent(Json.obj("message" -> "foobar")))
      stream.expectComplete()
    }

    "discard correctly an event by throwing an error and report based on exception" in {
      val transformer = new FlowTransformer {
        def factory(): FlowTransformerLogic = new ImplWithCatchException(ImplConfig(Root / "message", onError = ErrorBehaviour.DiscardAndReport))
      }

      val stream = Source(Seq(
        StreamEvent(Json.obj("message" -> 1)),
        StreamEvent(Json.obj("message" -> "foo"))
      )).via(transformer)
        .addAttributes(supervisionStrategy(_ => Supervision.Resume))
        .runWith(TestSink.probe[StreamEvent])

      stream.requestNext() should equal(StreamEvent(Json.obj("message" -> "foobar")))
      stream.expectComplete()
    }

    "discard correctly an event by throwing an error based on msg" in {
      val transformer = new FlowTransformer {
        def factory(): FlowTransformerLogic = new ImplWithError(ImplConfig(Root / "message", onError = ErrorBehaviour.Discard))
      }

      val stream = Source(Seq(
        StreamEvent(Json.obj("message" -> 1)),
        StreamEvent(Json.obj("message" -> "foo"))
      )).via(transformer)
        .addAttributes(supervisionStrategy(_ => Supervision.Resume))
        .runWith(TestSink.probe[StreamEvent])

      stream.requestNext() should equal(StreamEvent(Json.obj("message" -> "foobar")))
      stream.expectComplete()
    }

    "discard correctly an event by throwing an error and report based on msg" in {
      val transformer = new FlowTransformer {
        def factory(): FlowTransformerLogic = new ImplWithError(ImplConfig(Root / "message", onError = ErrorBehaviour.DiscardAndReport))
      }

      val stream = Source(Seq(
        StreamEvent(Json.obj("message" -> 1)),
        StreamEvent(Json.obj("message" -> "foo"))
      )).via(transformer)
        .addAttributes(supervisionStrategy(_ => Supervision.Resume))
        .runWith(TestSink.probe[StreamEvent])

      stream.requestNext() should equal(StreamEvent(Json.obj("message" -> "foobar")))
      stream.expectComplete()
    }

    "discard correctly an event by throwing an error based on uncatched exception" in {
      val transformer = new FlowTransformer {
        def factory(): FlowTransformerLogic =
          new ImplWithException(ImplConfig(Root / "message"))
      }

      val stream = Source(Seq(
        StreamEvent(Json.obj("message" -> 1)),
        StreamEvent(Json.obj("message" -> "foo"))
      )).via(transformer)
        .addAttributes(supervisionStrategy(_ => Supervision.Resume))
        .runWith(TestSink.probe[StreamEvent])

      stream.requestNext() should equal(StreamEvent(Json.obj("message" -> "foobar")))
      stream.expectComplete()
    }

    "discard an event and fail correctly by throwing an error based on uncatched exception" in {
      val transformer = new FlowTransformer {
        def factory(): FlowTransformerLogic =
          new ImplWithException(ImplConfig(Root / "message"))
      }

      Source.single(StreamEvent(Json.obj("message" -> 1)))
        .via(transformer)
        .addAttributes(supervisionStrategy(_ => Supervision.Stop))
        .runWith(TestSink.probe[StreamEvent])
        .request(1)
        .expectError()
    }
  }

}

