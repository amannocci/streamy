/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2018
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
package io.techcode.streamy.util.json

import akka.util.ByteString
import io.bullet.borer.Dom.Element
import io.bullet.borer.compat.akka._
import io.techcode.streamy.util.parser.{ByteStringParser, ParseException, StringParser}
import org.openjdk.jmh.annotations.Benchmark

import scala.io.Source

/**
  * Json parser bench.
  */
class JsonParserBench {

  @Benchmark def parseBorerByteString(): Element =
    io.bullet.borer.Json.decode(JsonParserBench.Sample.ByteStringImpl).to[Element].value

  @Benchmark def parseByteString(): Either[ParseException, Json] =
    JsonParserBench.ByteStringJsonParser.parse(JsonParserBench.Sample.ByteStringImpl)

  @Benchmark def parseBytes(): Either[ParseException, Json] =
    JsonParserBench.ByteStringJsonParser.parse(ByteString.fromArrayUnsafe(JsonParserBench.Sample.BytesImpl))

  @Benchmark def parseString(): Either[ParseException, Json] =
    JsonParserBench.StringJsonParser.parse(JsonParserBench.Sample.StringImpl)

}

object JsonParserBench {

  val ByteStringJsonParser: ByteStringParser[Json] = JsonParser.byteStringParser()

  val StringJsonParser: StringParser[Json] = JsonParser.stringParser()

  object Sample {

    val StringImpl: String = Source.fromResource("twitter_api_response.json").mkString

    val ByteStringImpl: ByteString = ByteString(StringImpl).compact

    val BytesImpl: Array[Byte] = ByteStringImpl.toArray[Byte]

  }

}