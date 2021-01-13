/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2021
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

package io.techcode.streamy.elasticsearch

import com.typesafe.config.ConfigFactory
import io.techcode.streamy.component.ComponentRegistry
import io.techcode.streamy.plugin.TestPlugin
import org.scalatest.concurrent.Eventually.eventually

/**
  * Elasticsearch plugin spec.
  */
class ElasticsearchPluginSpec extends TestPlugin {

  "Elasticsearch plugin" should {
    "register a materializable elasticsearch single source component" in {
      create(classOf[ElasticsearchPlugin], ConfigFactory.empty())
      eventually { ComponentRegistry(system).getSource("elasticsearch-single").isDefined should equal(true) }
      val component = ComponentRegistry(system).getSource("elasticsearch-single").get
      component(ConfigFactory.parseString("""{"hosts":[{"scheme":"http", "host":"localhost", "port":9200}], "query":"{}", "index-name":"foobar"}"""))
    }

    "register a materializable elasticsearch paginate source component" in {
      create(classOf[ElasticsearchPlugin], ConfigFactory.empty())
      eventually { ComponentRegistry(system).getSource("elasticsearch-paginate").isDefined should equal(true) }
      val component = ComponentRegistry(system).getSource("elasticsearch-paginate").get
      component(ConfigFactory.parseString("""{"hosts":[{"scheme":"http", "host":"localhost", "port":9200}], "query":"{}", "index-name":"foobar"}"""))
    }

    "register a materializable elasticsearch flow component" in {
      create(classOf[ElasticsearchPlugin], ConfigFactory.empty())
      eventually { ComponentRegistry(system).getFlow("elasticsearch").isDefined should equal(true) }
      val component = ComponentRegistry(system).getFlow("elasticsearch").get
      component(ConfigFactory.parseString("""{"hosts":[{"scheme":"http", "host":"localhost", "port":9200}], "action": "index", "index-name":"foobar"}"""))
    }

    "register a materializable elasticsearch sink component" in {
      create(classOf[ElasticsearchPlugin], ConfigFactory.empty())
      eventually { ComponentRegistry(system).getSink("elasticsearch").isDefined should equal(true) }
      val component = ComponentRegistry(system).getSink("elasticsearch").get
      component(ConfigFactory.parseString("""{"hosts":[{"scheme":"http", "host":"localhost", "port":9200}], "action": "index", "index-name":"foobar"}"""))
    }
  }

}
