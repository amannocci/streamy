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
package io.techcode.streamy.component.transform

import io.circe._
import io.circe.parser._
import gnieh.diffson.Pointer._
import io.techcode.streamy.component.transform.FingerprintTransformer.Config
import org.openjdk.jmh.annotations.Benchmark

/**
  * Fingerprint transform bench.
  */
class FingerprintTransformBench {

  @Benchmark def benchMd5(): Unit = {
    new FingerprintTransformer(Config(source = root / "message", hashing = "md5")).apply(FingerprintTransformBench.Simple)
  }

  @Benchmark def benchSha1(): Unit = {
    new FingerprintTransformer(Config(source = root / "message", hashing = "sha1")).apply(FingerprintTransformBench.Simple)
  }

  @Benchmark def benchSha256(): Unit = {
    new FingerprintTransformer(Config(source = root / "message", hashing = "sha256")).apply(FingerprintTransformBench.Simple)
  }

  @Benchmark def benchSha384(): Unit = {
    new FingerprintTransformer(Config(source = root / "message", hashing = "sha384")).apply(FingerprintTransformBench.Simple)
  }

  @Benchmark def benchSha512(): Unit = {
    new FingerprintTransformer(Config(source = root / "message", hashing = "sha512")).apply(FingerprintTransformBench.Simple)
  }

  @Benchmark def benchAlder32(): Unit = {
    new FingerprintTransformer(Config(source = root / "message", hashing = "alder32")).apply(FingerprintTransformBench.Simple)
  }

  @Benchmark def benchCrc32(): Unit = {
    new FingerprintTransformer(Config(source = root / "message", hashing = "crc32")).apply(FingerprintTransformBench.Simple)
  }

  @Benchmark def benchCrc32c(): Unit = {
    new FingerprintTransformer(Config(source = root / "message", hashing = "crc32c")).apply(FingerprintTransformBench.Simple)
  }

  @Benchmark def benchMurmur3_32(): Unit = {
    new FingerprintTransformer(Config(source = root / "message", hashing = "murmur3_32")).apply(FingerprintTransformBench.Simple)
  }

  @Benchmark def benchMurmur3_128(): Unit = {
    new FingerprintTransformer(Config(source = root / "message", hashing = "murmur3_128")).apply(FingerprintTransformBench.Simple)
  }

  @Benchmark def benchSipHash24(): Unit = {
    new FingerprintTransformer(Config(source = root / "message", hashing = "sipHash24")).apply(FingerprintTransformBench.Simple)
  }

  @Benchmark def benchFarmHashFingerprint64(): Unit = {
    new FingerprintTransformer(Config(source = root / "message", hashing = "farmHashFingerprint64")).apply(FingerprintTransformBench.Simple)
  }

}

object FingerprintTransformBench {
  val Simple: Json = parse("""{"message":"test"}""").getOrElse(Json.Null)
}
