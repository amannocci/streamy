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
package io.techcode.streamy.fingerprint.component

import akka.NotUsed
import io.techcode.streamy.component.TestTransformer
import io.techcode.streamy.event.StreamEvent
import io.techcode.streamy.fingerprint.component.FingerprintTransformer.Config
import io.techcode.streamy.util.json._

/**
  * Fingerprint transformer spec.
  */
class FingerprintTransformerSpec extends TestTransformer {

  "Fingerprint transformer" should {
    "be used in a flow" in {
      except(
        FingerprintTransformer(Config(source = Root / "message", hashing = "md5")),
        StreamEvent(Json.parseStringUnsafe("""{"message":"test"}""")),
        StreamEvent(Json.parseStringUnsafe("""{"message":"098f6bcd4621d373cade4e832627b4f6"}"""))
      )
    }

    "transform correctly a packet inplace with md5 hashing" in {
      except(
        FingerprintTransformer(Config(source = Root / "message", hashing = "md5")),
        StreamEvent(Json.parseStringUnsafe("""{"message":"test"}""")),
        StreamEvent(Json.parseStringUnsafe("""{"message":"098f6bcd4621d373cade4e832627b4f6"}"""))
      )
    }

    "transform correctly a packet inplace with sha1 hashing" in {
      except(
        FingerprintTransformer(Config(source = Root / "message", hashing = "sha1")),
        StreamEvent(Json.parseStringUnsafe("""{"message":"test"}""")),
        StreamEvent(Json.parseStringUnsafe("""{"message":"a94a8fe5ccb19ba61c4c0873d391e987982fbbd3"}"""))
      )
    }

    "transform correctly a packet inplace with sha256 hashing" in {
      except(
        FingerprintTransformer(Config(source = Root / "message", hashing = "sha256")),
        StreamEvent(Json.parseStringUnsafe("""{"message":"test"}""")),
        StreamEvent(Json.parseStringUnsafe("""{"message":"9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08"}"""))
      )
    }

    "transform correctly a packet inplace with sha384 hashing" in {
      except(
        FingerprintTransformer(Config(source = Root / "message", hashing = "sha384")),
        StreamEvent(Json.parseStringUnsafe("""{"message":"test"}""")),
        StreamEvent(Json.parseStringUnsafe("""{"message":"768412320f7b0aa5812fce428dc4706b3cae50e02a64caa16a782249bfe8efc4b7ef1ccb126255d196047dfedf17a0a9"}"""))
      )
    }

    "transform correctly a packet inplace with sha512 hashing" in {
      except(
        FingerprintTransformer(Config(source = Root / "message", hashing = "sha512")),
        StreamEvent(Json.parseStringUnsafe("""{"message":"test"}""")),
        StreamEvent(Json.parseStringUnsafe("""{"message":"ee26b0dd4af7e749aa1a8ee3c10ae9923f618980772e473f8819a5d4940e0db27ac185f8a0e1d5f84f88bc887fd67b143732c304cc5fa9ad8e6f57f50028a8ff"}"""))
      )
    }

    "transform correctly a packet inplace with alder32 hashing" in {
      except(
        FingerprintTransformer(Config(source = Root / "message", hashing = "alder32")),
        StreamEvent(Json.parseStringUnsafe("""{"message":"test"}""")),
        StreamEvent(Json.parseStringUnsafe("""{"message":"c1015d04"}"""))
      )
    }

    "transform correctly a packet inplace with crc32 hashing" in {
      except(
        FingerprintTransformer(Config(source = Root / "message", hashing = "crc32")),
        StreamEvent(Json.parseStringUnsafe("""{"message":"test"}""")),
        StreamEvent(Json.parseStringUnsafe("""{"message":"0c7e7fd8"}"""))
      )
    }

    "transform correctly a packet inplace with crc32c hashing" in {
      except(
        FingerprintTransformer(Config(source = Root / "message", hashing = "crc32c")),
        StreamEvent(Json.parseStringUnsafe("""{"message":"test"}""")),
        StreamEvent(Json.parseStringUnsafe("""{"message":"c072a086"}"""))
      )
    }

    "transform correctly a packet inplace with murmur3_32 hashing" in {
      except(
        FingerprintTransformer(Config(source = Root / "message", hashing = "murmur3_32")),
        StreamEvent(Json.parseStringUnsafe("""{"message":"test"}""")),
        StreamEvent(Json.parseStringUnsafe("""{"message":"13d26bba"}"""))
      )
    }

    "transform correctly a packet inplace with murmur3_128 hashing" in {
      except(
        FingerprintTransformer(Config(source = Root / "message", hashing = "murmur3_128")),
        StreamEvent(Json.parseStringUnsafe("""{"message":"test"}""")),
        StreamEvent(Json.parseStringUnsafe("""{"message":"9de1bd74cc287dac824dbdf93182129a"}"""))
      )
    }

    "transform correctly a packet inplace with sipHash24 hashing" in {
      except(
        FingerprintTransformer(Config(source = Root / "message", hashing = "sipHash24")),
        StreamEvent(Json.parseStringUnsafe("""{"message":"test"}""")),
        StreamEvent(Json.parseStringUnsafe("""{"message":"9a72565c525e7626"}"""))
      )
    }

    "transform correctly a packet inplace with farmHashFingerprint64 hashing" in {
      except(
        FingerprintTransformer(Config(source = Root / "message", hashing = "farmHashFingerprint64")),
        StreamEvent(Json.parseStringUnsafe("""{"message":"test"}""")),
        StreamEvent(Json.parseStringUnsafe("""{"message":"b2b585aa3d381777"}"""))
      )
    }

    "handle correctly unexpected input" in {
      except(
        FingerprintTransformer(Config(source = Root / "message", hashing = "farmHashFingerprint64")),
        StreamEvent(Json.parseStringUnsafe("""{"message":10}""")),
        StreamEvent(Json.parseStringUnsafe("""{"message":10}"""))
      )
    }
  }

}
