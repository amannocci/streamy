/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017
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

import akka.actor.{ActorSystem, Kill, Props}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Materializer}
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest._
import org.scalatest.mockito.MockitoSugar

import scala.reflect.io.Path

/**
  * Plugin spec.
  */
class PluginSpec extends FlatSpec with Matchers with MockitoSugar {

  // Name of application
  val ApplicationName = "streamy"

  // Actor system
  implicit val system: ActorSystem = ActorSystem(ApplicationName)

  implicit val materializer: Materializer = ActorMaterializer(ActorMaterializerSettings(system))

  "Plugin" can "be started and stopped" in {
    val conf: Config = ConfigFactory.empty()
    val description: PluginDescription = PluginDescription.create(Path(".").toURL, ConfigFactory.parseString("""{"name":"test","version":"0.1.0"}"""))
    val typed: Class[_] = classOf[Impl]
    system.actorOf(Props(
      typed,
      system,
      materializer,
      description,
      conf
    ))
  }

}

class Impl(
  val system: ActorSystem,
  val materializer: Materializer,
  override val description: PluginDescription,
  override val conf: Config
) extends Plugin(system, materializer, description, conf) {

  override def onStart(): Unit = log.info("start")

  override def onStop(): Unit = log.info("stop")

}