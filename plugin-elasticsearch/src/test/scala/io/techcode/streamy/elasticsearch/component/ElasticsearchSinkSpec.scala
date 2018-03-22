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
import io.techcode.streamy.elasticsearch.util.ElasticsearchSpec
import io.techcode.streamy.util.json._

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Elasticsearch source spec.
  */
class ElasticsearchSinkSpec extends ElasticsearchSpec {

  "Elasticsearch sink" should {
    "send data" in {
      val result = Source.single(Json.obj("foo" -> "bar"))
        .runWith(ElasticsearchSink(ElasticsearchFlow.Config(
          Seq(s"http://$elasticHost:9200"),
          "testing",
          "test",
          "index",
          bulk = 1
        )))
      Await.result(result, 30 seconds) should equal(Done)
    }

    "send data using multiple workers" in {
      val result = Source.single(Json.obj("foo" -> "bar"))
        .runWith(ElasticsearchSink(ElasticsearchFlow.Config(
          Seq(s"http://$elasticHost:9200"),
          "testing",
          "test",
          "index",
          bulk = 1,
          worker = 2
        )))
      Await.result(result, 30 seconds) should equal(Done)
    }
  }

}
