/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2020
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
package io.techcode.streamy.pipeline

import akka.stream.scaladsl.{Flow, Sink, Source}
import com.typesafe.config.ConfigFactory
import io.techcode.streamy.StreamyTestSystem
import io.techcode.streamy.component.ComponentRegistry
import io.techcode.streamy.event.StreamEvent

import scala.language.postfixOps

/**
  * Pipeline spec.
  */
class PipelineSpec extends StreamyTestSystem {

  // Component registry
  private val componentRegistry = ComponentRegistry(system)
  componentRegistry.registerSource("single", conf => {
    Source.single(StreamEvent("test"))
  })
  componentRegistry.registerFlow("transform", conf => {
    Flow[StreamEvent].map(_.mutate("foobar"))
  })
  componentRegistry.registerSink("blackhole", conf => Sink.ignore)

  "Pipeline" can {
    "have a source and sink" in {
      new Pipeline(system, "test", Pipeline.Config(
        sources = ConfigFactory.parseString(
          """source {
            |  type = "single"
            |}
            |""".stripMargin),
        flows = ConfigFactory.empty(),
        sinks = ConfigFactory.parseString(
          """sink {
            |  type = "blackhole"
            |  inputs = ["source"]
            |}
            |""".stripMargin)
      )).onStart()
    }

    "have a source, flow and sink" in {
      new Pipeline(system, "test", Pipeline.Config(
        sources = ConfigFactory.parseString(
          """source {
            |  type = "single"
            |}
            |""".stripMargin),
        flows = ConfigFactory.parseString(
          """flow {
            |  inputs = ["source"]
            |  type = "transform"
            |}
            |""".stripMargin),
        sinks = ConfigFactory.parseString(
          """sink {
            |  type = "blackhole"
            |  inputs = ["flow"]
            |}
            |""".stripMargin)
      )).onStart()
    }

    "have a multiples source, flow and sink" in {
      new Pipeline(system, "test", Pipeline.Config(
        sources = ConfigFactory.parseString(
          """source-1 {
            |  type = "single"
            |}
            |source-2 {
            |  type = "single"
            |}
            |""".stripMargin),
        flows = ConfigFactory.parseString(
          """flow {
            |  inputs = ["source-1", "source-2"]
            |  type = "transform"
            |}
            |""".stripMargin),
        sinks = ConfigFactory.parseString(
          """sink {
            |  type = "blackhole"
            |  inputs = ["flow"]
            |}
            |""".stripMargin)
      )).onStart()
    }

    "have a source, multiples flow and sink" in {
      new Pipeline(system, "test", Pipeline.Config(
        sources = ConfigFactory.parseString(
          """source {
            |  type = "single"
            |}
            |""".stripMargin),
        flows = ConfigFactory.parseString(
          """flow-1 {
            |  inputs = ["source"]
            |  type = "transform"
            |}
            |flow-2 {
            |  inputs = ["flow-1"]
            |  type = "transform"
            |}
            |""".stripMargin),
        sinks = ConfigFactory.parseString(
          """sink {
            |  type = "blackhole"
            |  inputs = ["flow-2"]
            |}
            |""".stripMargin)
      )).onStart()
    }

    "have a source, flow and multiples sink" in {
      new Pipeline(system, "test", Pipeline.Config(
        sources = ConfigFactory.parseString(
          """source {
            |  type = "single"
            |}
            |""".stripMargin),
        flows = ConfigFactory.parseString(
          """flow {
            |  inputs = ["source"]
            |  type = "transform"
            |}
            |""".stripMargin),
        sinks = ConfigFactory.parseString(
          """sink-1 {
            |  type = "blackhole"
            |  inputs = ["flow"]
            |}
            |sink-2 {
            |  type = "blackhole"
            |  inputs = ["flow"]
            |}
            |""".stripMargin)
      )).onStart()
    }

    "have a multiples source, multiples flow and sink" in {
      new Pipeline(system, "test", Pipeline.Config(
        sources = ConfigFactory.parseString(
          """source {
            |  type = "single"
            |}
            |""".stripMargin),
        flows = ConfigFactory.parseString(
          """flow-1 {
            |  inputs = ["source"]
            |  type = "transform"
            |}
            |flow-2 {
            |  inputs = ["source"]
            |  type = "transform"
            |}
            |""".stripMargin),
        sinks = ConfigFactory.parseString(
          """sink {
            |  type = "blackhole"
            |  inputs = ["flow-1", "flow-2"]
            |}
            |""".stripMargin)
      )).onStart()
    }

    "have a multiples source and one sink" in {
      new Pipeline(system, "test", Pipeline.Config(
        sources = ConfigFactory.parseString(
          """source-1 {
            |  type = "single"
            |}
            |source-2 {
            |  type = "single"
            |}
            |""".stripMargin),
        flows = ConfigFactory.empty(),
        sinks = ConfigFactory.parseString(
          """sink {
            |  type = "blackhole"
            |  inputs = ["source-1", "source-2"]
            |}
            |""".stripMargin)
      )).onStart()
    }

    "have a multiples source and multiples sink" in {
      new Pipeline(system, "test", Pipeline.Config(
        sources = ConfigFactory.parseString(
          """source-1 {
            |  type = "single"
            |}
            |source-2 {
            |  type = "single"
            |}
            |""".stripMargin),
        flows = ConfigFactory.empty(),
        sinks = ConfigFactory.parseString(
          """sink-1 {
            |  type = "blackhole"
            |  inputs = ["source-1", "source-2"]
            |}
            |sink-2 {
            |  type = "blackhole"
            |  inputs = ["source-1", "source-2"]
            |}
            |""".stripMargin)
      )).onStart()
    }

    "have a multiples source, multiples flow and multiples sink" in {
      new Pipeline(system, "test", Pipeline.Config(
        sources = ConfigFactory.parseString(
          """source-1 {
            |  type = "single"
            |}
            |source-2 {
            |  type = "single"
            |}
            |""".stripMargin),
        flows = ConfigFactory.parseString(
          """flow-1 {
            |  inputs = ["source-1", "source-2"]
            |  type = "transform"
            |}
            |flow-2 {
            |  inputs = ["flow-1"]
            |  type = "transform"
            |}
            |""".stripMargin),
        sinks = ConfigFactory.parseString(
          """sink-1 {
            |  type = "blackhole"
            |  inputs = ["flow-2"]
            |}
            |sink-2 {
            |  type = "blackhole"
            |  inputs = ["flow-2"]
            |}
            |""".stripMargin)
      )).onStart()
    }

    "have a invalid source type" in {
      assertThrows[PipelineException] {
        new Pipeline(system, "test", Pipeline.Config(
          sources = ConfigFactory.parseString(
            """source {
              |  type = "unknown"
              |}
              |""".stripMargin),
          flows = ConfigFactory.empty(),
          sinks = ConfigFactory.parseString(
            """sink {
              |  type = "blackhole"
              |  inputs = ["source"]
              |}
              |""".stripMargin)
        )).onStart()
      }
    }

    "have a invalid flow type" in {
      assertThrows[PipelineException] {
        new Pipeline(system, "test", Pipeline.Config(
          sources = ConfigFactory.parseString(
            """source {
              |  type = "single"
              |}
              |""".stripMargin),
          flows = ConfigFactory.parseString(
            """flow {
              |  inputs = ["source"]
              |  type = "unknown"
              |}
              |""".stripMargin),
          sinks = ConfigFactory.parseString(
            """sink {
              |  type = "blackhole"
              |  inputs = ["source"]
              |}
              |""".stripMargin)
        )).onStart()
      }
    }

    "have a invalid sink type" in {
      assertThrows[PipelineException] {
        new Pipeline(system, "test", Pipeline.Config(
          sources = ConfigFactory.parseString(
            """source {
              |  type = "single"
              |}
              |""".stripMargin),
          flows = ConfigFactory.empty(),
          sinks = ConfigFactory.parseString(
            """sink {
              |  type = "unknown"
              |  inputs = ["source"]
              |}
              |""".stripMargin)
        )).onStart()
      }
    }
  }

}
