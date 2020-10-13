/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017-2020
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
package io.techcode.streamy

import java.time.Duration

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestKit, TestKitBase}
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

/**
  * Streamy spec.
  */
class StreamySpec extends AnyWordSpecLike with Matchers {

  "Streamy" should {
    "start even if there is no plugin" in {
      Streamy.main(Array("--dry-run"))
    }

    "use correctly configuration" in {
      val conf = ConfigFactory.load().resolve()
      conf.getStringList("akka.loggers").get(0) should equal("akka.event.slf4j.Slf4jLogger")
      conf.getString("akka.logging-filter") should equal("akka.event.slf4j.Slf4jLoggingFilter")
      conf.getDuration("akka.logger-startup-timeout") should equal(Duration.ofSeconds(30))
      conf.getInt("akka.actor.default-dispatcher.fork-join-executor.parallelism-min") should equal(2)
      conf.getInt("akka.actor.default-dispatcher.fork-join-executor.parallelism-max") should equal(2)
      conf.getInt("akka.stream.materializer.max-input-buffer-size") should equal(16)
      conf.getConfig("streamy.plugin").isEmpty should equal(true)
    }
  }

}

/**
  * Helper for system test.
  */
trait StreamyTestSystem extends AnyWordSpecLike with Matchers with BeforeAndAfterAll with TestKitBase with ImplicitSender {

  implicit lazy val system: ActorSystem = {
    def systemConfig = ConfigFactory.parseString(s"akka.stream.materializer.auto-fusing=true")
      .withFallback(config)
      .withFallback(ConfigFactory.load())

    ActorSystem(getClass.getSimpleName, systemConfig)
  }

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system, verifySystemShutdown = true)
    super.afterAll()
  }

  protected def config: Config = ConfigFactory.empty()

}