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

import akka.NotUsed
import akka.stream.scaladsl.Sink
import akka.stream.testkit.scaladsl.TestSink
import io.techcode.streamy.elasticsearch.component.ElasticsearchSource.HostConfig
import io.techcode.streamy.elasticsearch.util.ElasticsearchSpec
import io.techcode.streamy.event.StreamEvent
import io.techcode.streamy.util.StreamException
import io.techcode.streamy.util.json._
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.support.WriteRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.common.xcontent.XContentType

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Elasticsearch source spec.
  */
class ElasticsearchSourceSpec extends ElasticsearchSpec {

  "Elasticsearch source" should {
    "retrieve data from paginate source" in {
      // Prepare for test
      val index = randomIndex()
      restClient.index(new IndexRequest(index)
        .source("""{"foo": "bar"}""", XContentType.JSON)
        .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE), RequestOptions.DEFAULT
      )
      restClient.index(new IndexRequest(index)
        .source("""{"foo": "bar"}""", XContentType.JSON)
        .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE), RequestOptions.DEFAULT
      )

      // Stream to test
      val stream = ElasticsearchSource.paginate(ElasticsearchSource.Config(
        Seq(HostConfig(
          scheme = "http",
          host = elasticHost,
          port = elasticPort
        )),
        index,
        Json.parseStringUnsafe("""{"query":{"match_all":{}}}"""),
        bulk = 1
      )
      ).runWith(TestSink.probe[StreamEvent[NotUsed]])

      // Check
      stream.requestNext(5 seconds).payload.evaluate(Root / "_source").get[JsObject] should equal(Json.obj("foo" -> "bar"))
      stream.requestNext(5 seconds).payload.evaluate(Root / "_source").get[JsObject] should equal(Json.obj("foo" -> "bar"))
      stream.expectComplete()
    }

    "retrieve data from single source" in {
      // Prepare for test
      val index = randomIndex()
      restClient.index(new IndexRequest(index)
        .source("""{"foo": "bar"}""", XContentType.JSON)
        .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE), RequestOptions.DEFAULT
      )

      // Result of query
      val result = ElasticsearchSource.single(ElasticsearchSource.Config(
        Seq(HostConfig(
          scheme = "http",
          host = elasticHost,
          port = elasticPort
        )),
        index,
        Json.parseStringUnsafe("""{"query":{"match_all":{}}}""")
      )
      ).runWith(Sink.head[StreamEvent[NotUsed]])

      // Check
      whenReady(result, timeout(30 seconds), interval(100 millis)) { x =>
        x should not equal Json.obj()
      }
    }

    "fail on index not found from single source" in {
      // Prepare for test
      val index = randomIndex()

      // Result of query
      val result = ElasticsearchSource.single(ElasticsearchSource.Config(
        Seq(HostConfig(
          scheme = "http",
          host = elasticHost,
          port = elasticPort
        )),
        index,
        Json.parseStringUnsafe("""{"query":{"match_all":{}}}""")
      )
      ).runWith(Sink.ignore)

      assert(result.failed.futureValue(timeout(30 seconds), interval(100 millis)).isInstanceOf[StreamException[NotUsed]])
    }

  }

}
