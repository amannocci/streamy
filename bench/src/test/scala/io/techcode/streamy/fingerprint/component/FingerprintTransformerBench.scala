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
import io.techcode.streamy.util.json._
import org.openjdk.jmh.annotations.Benchmark

/**
  * Fingerprint transformer bench.
  */
class FingerprintTransformerBench {

  @Benchmark def md5(): Json = FingerprintTransformerBench.Transformer.Md5(FingerprintTransformerBench.Sample.Simple)

  @Benchmark def sha1(): Json = FingerprintTransformerBench.Transformer.Sha1(FingerprintTransformerBench.Sample.Simple)

  @Benchmark def sha256(): Json = FingerprintTransformerBench.Transformer.Sha256(FingerprintTransformerBench.Sample.Simple)

  @Benchmark def sha384(): Json = FingerprintTransformerBench.Transformer.Sha384(FingerprintTransformerBench.Sample.Simple)

  @Benchmark def sha512(): Json = FingerprintTransformerBench.Transformer.Sha512(FingerprintTransformerBench.Sample.Simple)

  @Benchmark def alder32(): Json = FingerprintTransformerBench.Transformer.Alder32(FingerprintTransformerBench.Sample.Simple)

  @Benchmark def crc32(): Json = FingerprintTransformerBench.Transformer.Crc32(FingerprintTransformerBench.Sample.Simple)

  @Benchmark def crc32c(): Json = FingerprintTransformerBench.Transformer.Crc32c(FingerprintTransformerBench.Sample.Simple)

  @Benchmark def murmur3_32(): Json = FingerprintTransformerBench.Transformer.Murmur3_32(FingerprintTransformerBench.Sample.Simple)

  @Benchmark def murmur3_128(): Json = FingerprintTransformerBench.Transformer.Murmur3_128(FingerprintTransformerBench.Sample.Simple)

  @Benchmark def sipHash24(): Json = FingerprintTransformerBench.Transformer.SipHash24(FingerprintTransformerBench.Sample.Simple)

  @Benchmark def farmHash(): Json = FingerprintTransformerBench.Transformer.FarmHash(FingerprintTransformerBench.Sample.Simple)

}

object FingerprintTransformerBench {

  object Sample {

    val Simple: Json = Json.parseStringUnsafe("""{"message":"test"}""")

  }

  object Transformer {

    val Md5 = new FingerprintTransformerLogic[NotUsed](FingerprintTransformer.Config(source = Root / "message", hashing = "md5"))

    val Sha1 = new FingerprintTransformerLogic[NotUsed](FingerprintTransformer.Config(source = Root / "message", hashing = "sha1"))

    val Sha256 = new FingerprintTransformerLogic[NotUsed](FingerprintTransformer.Config(source = Root / "message", hashing = "sha256"))

    val Sha384 = new FingerprintTransformerLogic[NotUsed](FingerprintTransformer.Config(source = Root / "message", hashing = "sha384"))

    val Sha512 = new FingerprintTransformerLogic[NotUsed](FingerprintTransformer.Config(source = Root / "message", hashing = "sha512"))

    val Alder32 = new FingerprintTransformerLogic[NotUsed](FingerprintTransformer.Config(source = Root / "message", hashing = "alder32"))

    val Crc32 = new FingerprintTransformerLogic[NotUsed](FingerprintTransformer.Config(source = Root / "message", hashing = "crc32"))

    val Crc32c = new FingerprintTransformerLogic[NotUsed](FingerprintTransformer.Config(source = Root / "message", hashing = "crc32c"))

    val Murmur3_32 = new FingerprintTransformerLogic[NotUsed](FingerprintTransformer.Config(source = Root / "message", hashing = "murmur3_32"))

    val Murmur3_128 = new FingerprintTransformerLogic[NotUsed](FingerprintTransformer.Config(source = Root / "message", hashing = "murmur3_128"))

    val SipHash24 = new FingerprintTransformerLogic[NotUsed](FingerprintTransformer.Config(source = Root / "message", hashing = "sipHash24"))

    val FarmHash = new FingerprintTransformerLogic[NotUsed](FingerprintTransformer.Config(source = Root / "message", hashing = "farmHashFingerprint64"))

  }

}
