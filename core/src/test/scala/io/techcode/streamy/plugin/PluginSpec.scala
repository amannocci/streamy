/*
 * The MIT License (MIT)
 * <p>
 * Copyright (C) 2017-2019
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
package io.techcode.streamy.plugin

import akka.actor.{ActorRef, Kill, Props}
import com.typesafe.config.{Config, ConfigFactory}
import io.techcode.streamy.StreamyTestSystem
import org.scalatest.mockito.MockitoSugar
import pureconfig._
import pureconfig.generic.auto._

import scala.reflect.io.Path

/**
  * Plugin spec.
  */
class PluginSpec extends StreamyTestSystem with MockitoSugar {

  "Plugin" can {
    "be started" in {
      create()
    }

    "be stopped" in {
      create() ! Kill
    }

    "not receive message by default" in {
      create() ! "test"
    }

    "can use it's data folder" in {
      create()
    }
  }

  private def create(): ActorRef = {
    val conf: Config = ConfigFactory.empty()
    val description: PluginDescription = loadConfigOrThrow[PluginDescription](ConfigFactory.parseString("""{"name":"test","version":"0.1.0"}"""))
    val typed: Class[_] = classOf[Impl]
    system.actorOf(Props(
      typed,
      PluginData(
        description,
        conf,
        Path(".").toDirectory
      )
    ))
  }

}

class Impl(
  override val data: PluginData
) extends Plugin(data) {

  override def onStart(): Unit = {
    log.info("start")
    dataFolder
  }

  override def onStop(): Unit = log.info("stop")

}