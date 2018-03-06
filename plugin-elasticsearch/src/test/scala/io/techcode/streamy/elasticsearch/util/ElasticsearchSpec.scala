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
package io.techcode.streamy.elasticsearch.util

import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.softwaremill.sttp.SttpBackend
import com.softwaremill.sttp.akkahttp.AkkaHttpBackend
import io.techcode.streamy.TestSystem
import pl.allegro.tech.embeddedelasticsearch.{EmbeddedElastic, PopularProperties}

import scala.concurrent.{ExecutionContextExecutor, Future}

/**
  * Helper for elasticsearch spec.
  */
class ElasticsearchSpec extends TestSystem {

  implicit lazy val httpClient: SttpBackend[Future, Source[ByteString, Any]] = AkkaHttpBackend.usingActorSystem(system)
  implicit lazy val executionContext: ExecutionContextExecutor = system.dispatcher

  lazy val elastic5_0: EmbeddedElastic = {
    EmbeddedElastic.builder()
      .withElasticVersion("5.0.0")
      .withEsJavaOpts("-Xms128m -Xmx256m")
      .withSetting(PopularProperties.HTTP_PORT, 8080)
      .withSetting(PopularProperties.CLUSTER_NAME, "embedded")
      .build()
      .start()
  }

  override def beforeAll(): Unit = {
    elastic5_0.getHttpPort
  }

  override def afterAll: Unit = {
    super.afterAll
    elastic5_0.stop()
  }

}
