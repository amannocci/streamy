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
package io.techcode.streamy.fingerprint.component.transformer

import io.techcode.streamy.fingerprint.component.transformer.FingerprintTransformer.Config
import io.techcode.streamy.fingerprint.component.transformer.FingerprintTransformerBench._
import io.techcode.streamy.util.json._
import org.openjdk.jmh.annotations.Benchmark

/**
  * Fingerprint transformer bench.
  *
  * Benchmark                                                Mode  Cnt        Score       Error  Units
  * FingerprintTransformerBench.benchAlder32                thrpt   20  3182163,688 ±  3644,464  ops/s
  * FingerprintTransformerBench.benchCrc32                  thrpt   20  3684606,141 ± 13613,941  ops/s
  * FingerprintTransformerBench.benchCrc32c                 thrpt   20  3019386,407 ±  5976,112  ops/s
  * FingerprintTransformerBench.benchFarmHashFingerprint64  thrpt   20  3762133,759 ±  5891,104  ops/s
  * FingerprintTransformerBench.benchMd5                    thrpt   20  1731620,350 ±  3215,174  ops/s
  * FingerprintTransformerBench.benchMurmur3_128            thrpt   20  2727832,043 ±  3514,204  ops/s
  * FingerprintTransformerBench.benchMurmur3_32             thrpt   20  5339783,961 ± 12122,176  ops/s
  * FingerprintTransformerBench.benchSha1                   thrpt   20  1407266,374 ±  3087,984  ops/s
  * FingerprintTransformerBench.benchSha256                 thrpt   20  1033798,398 ±  1131,969  ops/s
  * FingerprintTransformerBench.benchSha384                 thrpt   20   863588,079 ±   936,946  ops/s
  * FingerprintTransformerBench.benchSha512                 thrpt   20   808811,133 ±  3746,056  ops/s
  * FingerprintTransformerBench.benchSipHash24              thrpt   20  3129688,670 ±  3663,746  ops/s
  */
class FingerprintTransformerBench {

  @Benchmark def benchMd5(): Json = Md5Fingerprint(Simple)

  @Benchmark def benchSha1(): Json = Sha1Fingerprint(Simple)

  @Benchmark def benchSha256(): Json = Sha256Fingerprint(Simple)

  @Benchmark def benchSha384(): Json = Sha384Fingerprint(Simple)

  @Benchmark def benchSha512(): Json = Sha512Fingerprint(Simple)

  @Benchmark def benchAlder32(): Json = Alder32Fingerprint(Simple)

  @Benchmark def benchCrc32(): Json = Crc32Fingerprint(Simple)

  @Benchmark def benchCrc32c(): Json = Crc32cFingerprint(Simple)

  @Benchmark def benchMurmur3_32(): Json = Murmur3_32Fingerprint(Simple)

  @Benchmark def benchMurmur3_128(): Json = Murmur3_128Fingerprint(Simple)

  @Benchmark def benchSipHash24(): Json = SipHash24Fingerprint(Simple)

  @Benchmark def benchFarmHashFingerprint64(): Json = FarmHashFingerprint(Simple)

}

object FingerprintTransformerBench {

  val Simple: Json = Json.parse("""{"message":"test"}""").getOrElse(JsNull)

  val Md5Fingerprint = new FingerprintTransformer(Config(source = Root / "message", hashing = "md5"))

  val Sha1Fingerprint = new FingerprintTransformer(Config(source = Root / "message", hashing = "sha1"))

  val Sha256Fingerprint = new FingerprintTransformer(Config(source = Root / "message", hashing = "sha256"))

  val Sha384Fingerprint = new FingerprintTransformer(Config(source = Root / "message", hashing = "sha384"))

  val Sha512Fingerprint = new FingerprintTransformer(Config(source = Root / "message", hashing = "sha512"))

  val Alder32Fingerprint = new FingerprintTransformer(Config(source = Root / "message", hashing = "alder32"))

  val Crc32Fingerprint = new FingerprintTransformer(Config(source = Root / "message", hashing = "crc32"))

  val Crc32cFingerprint = new FingerprintTransformer(Config(source = Root / "message", hashing = "crc32c"))

  val Murmur3_32Fingerprint = new FingerprintTransformer(Config(source = Root / "message", hashing = "murmur3_32"))

  val Murmur3_128Fingerprint = new FingerprintTransformer(Config(source = Root / "message", hashing = "murmur3_128"))

  val SipHash24Fingerprint = new FingerprintTransformer(Config(source = Root / "message", hashing = "sipHash24"))

  val FarmHashFingerprint = new FingerprintTransformer(Config(source = Root / "message", hashing = "farmHashFingerprint64"))

}
