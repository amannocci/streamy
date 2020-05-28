/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017-2019
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

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.testkit.{ImplicitSender, TestKit, TestKitBase}
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec, WordSpecLike}

/**
  * Streamy spec.
  */
class StreamySpec extends WordSpecLike with Matchers {

  "Streamy" should {
    "start even if there is no plugin" in {
      Streamy.main(Array("--dry-run"))
    }
  }

}

/**
  * Helper for system test.
  */
trait StreamyTestSystem extends WordSpec with Matchers with BeforeAndAfterAll with TestKitBase with ImplicitSender {

  implicit lazy val system: ActorSystem = {
    def systemConfig = ConfigFactory.parseString(s"akka.stream.materializer.auto-fusing=true")
      .withFallback(config)
      .withFallback(ConfigFactory.load())

    ActorSystem(getClass.getSimpleName, systemConfig)
  }

  protected implicit val materializer: ActorMaterializer = ActorMaterializer()

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
    super.afterAll()
  }

  protected def config: Config = ConfigFactory.empty()

}