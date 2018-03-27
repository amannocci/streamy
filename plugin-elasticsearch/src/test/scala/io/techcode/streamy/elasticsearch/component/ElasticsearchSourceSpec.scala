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

import akka.stream.scaladsl.Sink
import akka.stream.testkit.scaladsl.TestSink
import akka.util.ByteString
import io.techcode.streamy.elasticsearch.util.ElasticsearchSpec
import io.techcode.streamy.util.StreamException
import io.techcode.streamy.util.json._
import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.support.WriteRequest
import org.elasticsearch.common.xcontent.XContentType
import org.scalatest.time.{Seconds, Span}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Elasticsearch source spec.
  */
class ElasticsearchSourceSpec extends ElasticsearchSpec {

  override def beforeAll(): Unit = {
    restClient.index(new IndexRequest("testing", "test")
      .source("""{"foo": "bar"}""", XContentType.JSON)
      .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE))
    restClient.index(new IndexRequest("testing", "test")
      .source("""{"foo": "bar"}""", XContentType.JSON)
      .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE))
  }

  "Elasticsearch source" should {
    "retrieve data from paginate source" in {
      val stream = ElasticsearchSource.paginate(ElasticsearchSource.Config(
        Seq(s"http://$elasticHost:9200"),
        "testing",
        "test",
        Json.parse("""{"query":{"match_all":{}}}""").getOrElse(JsNull),
        bulk = 1
      )).runWith(TestSink.probe[Json])

      stream.requestNext().evaluate(Root / "_source").get should equal(Json.obj("foo" -> "bar"))
      stream.requestNext().evaluate(Root / "_source").get should equal(Json.obj("foo" -> "bar"))
      stream.expectComplete()
    }

    "retrieve data from single source" in {
      val result = ElasticsearchSource.single(ElasticsearchSource.Config(
        Seq(s"http://$elasticHost:9200"),
        "testing",
        "test",
        Json.parse("""{"query":{"match_all":{}}}""").getOrElse(JsNull)
      )).runWith(Sink.reduce[ByteString]((x, y) => x ++ y))

      whenReady(result, timeout(30 seconds), interval(100 millis)) { x =>
        x should not equal ByteString.empty
      }
    }

    "fail on wrong request from single source" in {
      val result = ElasticsearchSource.single(ElasticsearchSource.Config(
        Seq(s"http://$elasticHost:9200"),
        "testing",
        "test",
        Json.parse("""{"query":{"match_all"}""").getOrElse(JsNull)
      )).runWith(Sink.reduce[ByteString]((x, y) => x ++ y))

      assert(result.failed.futureValue(timeout(30 seconds), interval(100 millis)).isInstanceOf[StreamException])
    }

    "fail on index not found from single source" in {
      val result = ElasticsearchSource.single(ElasticsearchSource.Config(
        Seq(s"http://$elasticHost:9200"),
        "notFound",
        "test",
        Json.parse("""{"query":{"match_all":{}}}""").getOrElse(JsNull)
      )).runWith(Sink.reduce[ByteString]((x, y) => x ++ y))

      assert(result.failed.futureValue(timeout(30 seconds), interval(100 millis)).isInstanceOf[StreamException])
    }

  }

}
