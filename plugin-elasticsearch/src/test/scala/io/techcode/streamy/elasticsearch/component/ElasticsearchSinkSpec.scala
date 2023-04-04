/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2018
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
package io.techcode.streamy.elasticsearch.component

import akka.Done
import akka.stream.scaladsl.Source
import io.techcode.streamy.elasticsearch.component.ElasticsearchFlow.HostConfig
import io.techcode.streamy.elasticsearch.event.ElasticsearchEvent
import io.techcode.streamy.elasticsearch.util.ElasticsearchSpec
import io.techcode.streamy.event.StreamEvent
import io.techcode.streamy.util.json._

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Elasticsearch source spec.
  */
class ElasticsearchSinkSpec extends ElasticsearchSpec {

  "Elasticsearch sink" should {
    "send data" in withContainers { container =>
      val result = Source.single(StreamEvent(Json.obj("foo" -> "bar")))
        .runWith(ElasticsearchSink(ElasticsearchFlow.Config(
          Seq(HostConfig(
            scheme = "http",
            host = container.containerIpAddress,
            port = container.mappedPort(9200)
          )),
          randomIndex(),
          "index",
          bulk = 1
        )))

      whenReady(result, timeout(30 seconds), interval(100 millis)) { x =>
        x should equal(Done)
      }
    }

    "send data with document parsing bypass" in withContainers { container =>
      val result = Source.single(StreamEvent(Json.printByteStringUnsafe(Json.obj("foo" -> "bar"))))
        .runWith(ElasticsearchSink(ElasticsearchFlow.Config(
          Seq(HostConfig(
            scheme = "http",
            host = container.containerIpAddress,
            port = container.mappedPort(9200)
          )),
          randomIndex(),
          "index",
          bulk = 1,
          bypassDocumentParsing = true
        )))

      whenReady(result, timeout(30 seconds), interval(100 millis)) { x =>
        x should equal(Done)
      }
    }

    "send data in bulk" in withContainers { container =>
      val result = Source.fromIterator[StreamEvent](() => Seq(StreamEvent(Json.obj("foo" -> "bar")), StreamEvent(Json.obj("foo" -> "bar"))).iterator)
        .runWith(ElasticsearchSink(ElasticsearchFlow.Config(
          Seq(HostConfig(
            scheme = "http",
            host = container.containerIpAddress,
            port = container.mappedPort(9200)
          )),
          randomIndex(),
          "index",
          bulk = 1
        )))

      whenReady(result, timeout(30 seconds), interval(100 millis)) { x =>
        x should equal(Done)
      }
    }

    "try to send data to unresponsive elasticsearch" in withContainers { container =>
      system.eventStream.subscribe(testActor, classOf[ElasticsearchEvent.Failure])

      Source.fromIterator[StreamEvent](() => Seq(StreamEvent(Json.obj("foo" -> "bar")), StreamEvent(Json.obj("foo" -> "bar"))).iterator)
        .runWith(ElasticsearchSink(ElasticsearchFlow.Config(
          Seq(HostConfig(
            scheme = "http",
            host = container.containerIpAddress,
            port = container.mappedPort(9200) + 1
          )),
          randomIndex(),
          "index",
          bulk = 1
        )))

      expectMsgClass(classOf[ElasticsearchEvent.Failure])
    }
  }

}
