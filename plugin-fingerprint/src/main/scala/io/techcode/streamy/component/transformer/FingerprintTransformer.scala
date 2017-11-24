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
package io.techcode.streamy.component.transformer

import java.nio.charset.StandardCharsets

import akka.NotUsed
import akka.stream.scaladsl.Flow
import com.google.common.hash.{HashFunction, Hashing}
import io.techcode.streamy.component.SimpleTransformer
import io.techcode.streamy.component.SimpleTransformer.SuccessBehaviour
import io.techcode.streamy.component.SimpleTransformer.SuccessBehaviour.SuccessBehaviour
import io.techcode.streamy.component.Transformer.ErrorBehaviour
import io.techcode.streamy.component.Transformer.ErrorBehaviour.ErrorBehaviour
import io.techcode.streamy.component.transformer.FingerprintTransformer.Config
import io.techcode.streamy.util.json._

/**
  * Fingerprint transformer implementation.
  */
private[transformer] class FingerprintTransformer(config: Config) extends SimpleTransformer(config) {

  // Choose right transform function
  private val hashFunc: (String => String) = FingerprintTransformer.Hashings(config.hashing)
    .hashString(_, StandardCharsets.UTF_8).toString

  override def transform(value: Json): Option[Json] =
    value.asString.map(hashFunc(_))

}

/**
  * Fingerprint transformer companion.
  */
object FingerprintTransformer {

  // All supported hashing
  val Hashings: Map[String, HashFunction] = Map.newBuilder
    .+=("md5" -> Hashing.md5())
    .+=("sha1" -> Hashing.sha1())
    .+=("sha256" -> Hashing.sha256())
    .+=("sha384" -> Hashing.sha384())
    .+=("sha512" -> Hashing.sha512())
    .+=("alder32" -> Hashing.adler32())
    .+=("crc32" -> Hashing.crc32())
    .+=("crc32c" -> Hashing.crc32c())
    .+=("murmur3_32" -> Hashing.murmur3_32())
    .+=("murmur3_128" -> Hashing.murmur3_128())
    .+=("sipHash24" -> Hashing.sipHash24())
    .+=("farmHashFingerprint64" -> Hashing.farmHashFingerprint64())
    .result()

  // Component configuration
  case class Config(
    override val source: JsonPointer,
    override val target: Option[JsonPointer] = None,
    override val onSuccess: SuccessBehaviour = SuccessBehaviour.Skip,
    override val onError: ErrorBehaviour = ErrorBehaviour.Skip,
    hashing: String
  ) extends SimpleTransformer.Config(source, target, onSuccess, onError)

  /**
    * Create a fingerprint transformer flow that transform incoming [[Json]] objects.
    *
    * @param conf flow configuration.
    * @return new fingerprint flow.
    */
  def transformer(conf: Config): Flow[Json, Json, NotUsed] =
    Flow.fromFunction(new FingerprintTransformer(conf))

}
