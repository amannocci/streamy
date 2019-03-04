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
package io.techcode.streamy.fingerprint.component

import io.techcode.streamy.util.json._
import org.openjdk.jmh.annotations.Benchmark

/**
  * Fingerprint transformer bench.
  *
  * Benchmark                                 Mode  Cnt        Score       Error  Units
  * FingerprintTransformerBench.alder32      thrpt   20  4686576.959 ± 13602.988  ops/s
  * FingerprintTransformerBench.crc32        thrpt   20  5485118.562 ± 10160.267  ops/s
  * FingerprintTransformerBench.crc32c       thrpt   20  5866767.950 ± 14128.656  ops/s
  * FingerprintTransformerBench.farmHash     thrpt   20  5412289.991 ± 17282.797  ops/s
  * FingerprintTransformerBench.md5          thrpt   20  2548109.185 ±  3993.488  ops/s
  * FingerprintTransformerBench.murmur3_128  thrpt   20  4076691.529 ± 13666.090  ops/s
  * FingerprintTransformerBench.murmur3_32   thrpt   20  7490568.120 ± 27839.105  ops/s
  * FingerprintTransformerBench.sha1         thrpt   20  2046655.977 ±  6422.633  ops/s
  * FingerprintTransformerBench.sha256       thrpt   20  1618330.819 ±  4246.589  ops/s
  * FingerprintTransformerBench.sha384       thrpt   20  1187285.739 ±  3253.281  ops/s
  * FingerprintTransformerBench.sha512       thrpt   20  1182532.462 ±  2985.362  ops/s
  * FingerprintTransformerBench.sipHash24    thrpt   20  4450924.230 ±  8829.009  ops/s
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

    val Simple: Json = Json.parse("""{"message":"test"}""").getOrElse(JsNull)

  }

  object Transformer {

    val Md5 = new FingerprintTransformer(FingerprintTransformer.Config(source = Root / "message", hashing = "md5"))

    val Sha1 = new FingerprintTransformer(FingerprintTransformer.Config(source = Root / "message", hashing = "sha1"))

    val Sha256 = new FingerprintTransformer(FingerprintTransformer.Config(source = Root / "message", hashing = "sha256"))

    val Sha384 = new FingerprintTransformer(FingerprintTransformer.Config(source = Root / "message", hashing = "sha384"))

    val Sha512 = new FingerprintTransformer(FingerprintTransformer.Config(source = Root / "message", hashing = "sha512"))

    val Alder32 = new FingerprintTransformer(FingerprintTransformer.Config(source = Root / "message", hashing = "alder32"))

    val Crc32 = new FingerprintTransformer(FingerprintTransformer.Config(source = Root / "message", hashing = "crc32"))

    val Crc32c = new FingerprintTransformer(FingerprintTransformer.Config(source = Root / "message", hashing = "crc32c"))

    val Murmur3_32 = new FingerprintTransformer(FingerprintTransformer.Config(source = Root / "message", hashing = "murmur3_32"))

    val Murmur3_128 = new FingerprintTransformer(FingerprintTransformer.Config(source = Root / "message", hashing = "murmur3_128"))

    val SipHash24 = new FingerprintTransformer(FingerprintTransformer.Config(source = Root / "message", hashing = "sipHash24"))

    val FarmHash = new FingerprintTransformer(FingerprintTransformer.Config(source = Root / "message", hashing = "farmHashFingerprint64"))

  }

}
