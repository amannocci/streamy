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

import java.util.UUID
import akka.http.scaladsl.Http
import com.dimafeng.testcontainers.scalatest.TestContainerForAll
import com.dimafeng.testcontainers.{ElasticsearchContainer, ForAllTestContainer}
import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.{ElasticClient, ElasticProperties}
import io.techcode.streamy.TestSystem
import org.testcontainers.utility.DockerImageName

/**
  * Helper for elasticsearch spec.
  */
trait ElasticsearchSpec extends TestSystem with TestContainerForAll {

  override val containerDef: ElasticsearchContainer.Def = ElasticsearchContainer.Def(
    dockerImageName = {
      if (System.getProperty("os.arch") == "amd64") {
        DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:7.17.9-amd64")
      } else {
        DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:7.17.9-arm64")
      }
    }
  )

  def createClient(container: Containers): ElasticClient = {
    val props = ElasticProperties(s"http://${container.containerIpAddress}:${container.mappedPort(9200)}")
    ElasticClient(JavaClient(props))
  }

  def randomIndex(): String = {
    UUID.randomUUID().toString
  }

  override def afterAll(): Unit = {
    Http().shutdownAllConnectionPools()
    super.afterAll()
  }

}
