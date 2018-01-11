/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017-2018
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
package io.techcode.streamy.fingerprint.component.transformer

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.stream.scaladsl.Source
import akka.stream.testkit.scaladsl.TestSink
import akka.testkit.{ImplicitSender, TestKit}
import io.techcode.streamy.fingerprint.component.transformer.FingerprintTransformer.Config
import io.techcode.streamy.util.json._
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
  * Fingerprint transformer spec.
  */
class FingerprintTransformerSpec extends TestKit(ActorSystem("FingerprintTransformerSpec"))
  with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

  implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(system))

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "Fingerprint transformer" should {
    "be used in a flow" in {
      Source.single(Json.parse("""{"message":"test"}""").getOrElse(JsNull))
        .via(FingerprintTransformer.transformer(Config(source = Root / "message", hashing = "md5")))
        .runWith(TestSink.probe[Json])
        .requestNext() should equal(Json.parse("""{"message":"098f6bcd4621d373cade4e832627b4f6"}""").getOrElse(JsNull))
    }

    "transform correctly a packet inplace with md5 hashing" in {
      val input = Json.parse("""{"message":"test"}""").getOrElse(JsNull)
      val component = new FingerprintTransformer(Config(source = Root / "message", hashing = "md5"))
      component.apply(input) should equal(Json.parse("""{"message":"098f6bcd4621d373cade4e832627b4f6"}""").getOrElse(JsNull))
    }

    "transform correctly a packet inplace with sha1 hashing" in {
      val input = Json.parse("""{"message":"test"}""").getOrElse(JsNull)
      val component = new FingerprintTransformer(Config(source = Root / "message", hashing = "sha1"))
      component.apply(input) should equal(Json.parse("""{"message":"a94a8fe5ccb19ba61c4c0873d391e987982fbbd3"}""").getOrElse(JsNull))
    }

    "transform correctly a packet inplace with sha256 hashing" in {
      val input = Json.parse("""{"message":"test"}""").getOrElse(JsNull)
      val component = new FingerprintTransformer(Config(source = Root / "message", hashing = "sha256"))
      component.apply(input) should equal(Json.parse("""{"message":"9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08"}""").getOrElse(JsNull))
    }

    "transform correctly a packet inplace with sha384 hashing" in {
      val input = Json.parse("""{"message":"test"}""").getOrElse(JsNull)
      val component = new FingerprintTransformer(Config(source = Root / "message", hashing = "sha384"))
      component.apply(input) should equal(Json.parse("""{"message":"768412320f7b0aa5812fce428dc4706b3cae50e02a64caa16a782249bfe8efc4b7ef1ccb126255d196047dfedf17a0a9"}""").getOrElse(JsNull))
    }

    "transform correctly a packet inplace with sha512 hashing" in {
      val input = Json.parse("""{"message":"test"}""").getOrElse(JsNull)
      val component = new FingerprintTransformer(Config(source = Root / "message", hashing = "sha512"))
      component.apply(input) should equal(Json.parse("""{"message":"ee26b0dd4af7e749aa1a8ee3c10ae9923f618980772e473f8819a5d4940e0db27ac185f8a0e1d5f84f88bc887fd67b143732c304cc5fa9ad8e6f57f50028a8ff"}""").getOrElse(JsNull))
    }

    "transform correctly a packet inplace with alder32 hashing" in {
      val input = Json.parse("""{"message":"test"}""").getOrElse(JsNull)
      val component = new FingerprintTransformer(Config(source = Root / "message", hashing = "alder32"))
      component.apply(input) should equal(Json.parse("""{"message":"c1015d04"}""").getOrElse(JsNull))
    }

    "transform correctly a packet inplace with crc32 hashing" in {
      val input = Json.parse("""{"message":"test"}""").getOrElse(JsNull)
      val component = new FingerprintTransformer(Config(source = Root / "message", hashing = "crc32"))
      component.apply(input) should equal(Json.parse("""{"message":"0c7e7fd8"}""").getOrElse(JsNull))
    }

    "transform correctly a packet inplace with crc32c hashing" in {
      val input = Json.parse("""{"message":"test"}""").getOrElse(JsNull)
      val component = new FingerprintTransformer(Config(source = Root / "message", hashing = "crc32c"))
      component.apply(input) should equal(Json.parse("""{"message":"c072a086"}""").getOrElse(JsNull))
    }

    "transform correctly a packet inplace with murmur3_32 hashing" in {
      val input = Json.parse("""{"message":"test"}""").getOrElse(JsNull)
      val component = new FingerprintTransformer(Config(source = Root / "message", hashing = "murmur3_32"))
      component.apply(input) should equal(Json.parse("""{"message":"13d26bba"}""").getOrElse(JsNull))
    }

    "transform correctly a packet inplace with murmur3_128 hashing" in {
      val input = Json.parse("""{"message":"test"}""").getOrElse(JsNull)
      val component = new FingerprintTransformer(Config(source = Root / "message", hashing = "murmur3_128"))
      component.apply(input) should equal(Json.parse("""{"message":"9de1bd74cc287dac824dbdf93182129a"}""").getOrElse(JsNull))
    }

    "transform correctly a packet inplace with sipHash24 hashing" in {
      val input = Json.parse("""{"message":"test"}""").getOrElse(JsNull)
      val component = new FingerprintTransformer(Config(source = Root / "message", hashing = "sipHash24"))
      component.apply(input) should equal(Json.parse("""{"message":"9a72565c525e7626"}""").getOrElse(JsNull))
    }

    "transform correctly a packet inplace with farmHashFingerprint64 hashing" in {
      val input = Json.parse("""{"message":"test"}""").getOrElse(JsNull)
      val component = new FingerprintTransformer(Config(source = Root / "message", hashing = "farmHashFingerprint64"))
      component.apply(input) should equal(Json.parse("""{"message":"b2b585aa3d381777"}""").getOrElse(JsNull))
    }
  }

}
