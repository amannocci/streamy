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
package io.techcode.streamy.metric

import akka.actor.Kill
import com.typesafe.config.{Config, ConfigFactory}
import io.techcode.streamy.metric.event.MetricEvent
import io.techcode.streamy.plugin.TestPlugin

class MetricPluginSpec extends TestPlugin {

  "Plugin" can {
    "be started with jvm enable" in {
      system.eventStream.subscribe(testActor, classOf[MetricEvent.Jvm])
      create(classOf[MetricPlugin], MetricPluginSpec.JvmEnableConf)
      expectMsgClass(classOf[MetricEvent.Jvm])
    }

    "be started with jvm enable and embedded" in {
      system.eventStream.subscribe(testActor, classOf[MetricEvent.Jvm])
      create(classOf[MetricPlugin], MetricPluginSpec.JvmEnableEmbeddedConf)
      expectMsgClass(classOf[MetricEvent.Jvm])
    }

    "be started with jvm disable" in {
      create(classOf[MetricPlugin], MetricPluginSpec.JvmDisableConf)
    }

    "be stopped" in {
      create(classOf[MetricPlugin], MetricPluginSpec.JvmEnableConf) ! Kill
    }
  }

}

private object MetricPluginSpec {

  val JvmEnableEmbeddedConf: Config = ConfigFactory.parseString(
    """
      |jvm {
      |  initial-delay = 1ms
      |  interval = 1s
      |  embedded = true
      |}
    """.stripMargin)

  val JvmEnableConf: Config = ConfigFactory.parseString(
    """
      |jvm {
      |  initial-delay = 1ms
      |  interval = 1s
      |  embedded = false
      |}
    """.stripMargin)

  val JvmDisableConf: Config = ConfigFactory.parseString(
    """
      |jvm {}
    """.stripMargin)

}
