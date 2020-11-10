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

import java.util.concurrent.TimeUnit

import akka.util.ByteString
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import io.bullet.borer.Dom.Element
import io.bullet.borer.compat.akka._
import io.circe
import io.circe.ParsingFailure
import io.techcode.streamy.util.parser.{ByteStringParser, ParseException, StringParser}
import io.circe.parser._
import io.techcode.streamy.util.json.JsonParserBench.JacksonMapper
import org.openjdk.jmh.annotations.{Benchmark, BenchmarkMode, Mode, OutputTimeUnit, Param, Scope, Setup, State}

import scala.io.Source

/**
  * Json parser bench.
  */
@State(Scope.Thread)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.SECONDS)
class JsonParserBench {

  // Data
  private var string: String = _
  private var bytes: Array[Byte] = _
  private var byteString: ByteString = _

  @Param(
    Array(
      /*"australia-abc.json",
      "bitcoin.json",*/
      "doj-blog.json"/*,
      "eu-lobby-country.json",
      "eu-lobby-financial.json",
      "eu-lobby-repr.json",
      "github-events.json",
      "github-gists.json",
      "json-generator.json",
      "meteorites.json",
      "movies.json",
      "reddit-scala.json",
      "rick-morty.json",
      "temp-anomaly.json",
      "thai-cinemas.json",
      "turkish.json",
      "twitter_api_compact_response.json",
      "twitter_api_response.json"*/))
  var fileName: String = _

  @Setup
  def setup(): Unit = {
    string = Source.fromResource(fileName).mkString
    byteString = ByteString(string).compact
    bytes = byteString.toArray[Byte]
  }

  @Benchmark def borerByteString(): Element =
    io.bullet.borer.Json.decode(byteString).to[Element].value

  @Benchmark def borerBytes(): Element =
    io.bullet.borer.Json.decode(bytes).to[Element].value

  @Benchmark def streamyByteString(): Either[ParseException, Json] =
    JsonParserBench.StreamyByteStringJsonParser.parse(byteString)

  @Benchmark def streamyBytes(): Either[ParseException, Json] =
    JsonParserBench.StreamyByteStringJsonParser.parse(ByteString.fromArrayUnsafe(bytes))

  @Benchmark def streamyString(): Either[ParseException, Json] =
    JsonParserBench.StreamyStringJsonParser.parse(string)

  @Benchmark def circeString(): Either[ParsingFailure, circe.Json] =
    io.circe.parser.parse(string)

  @Benchmark def jacksonBytes(): JsonNode = JacksonMapper.readTree(bytes)

  @Benchmark def jacksonString(): JsonNode = JacksonMapper.readTree(string)


}

object JsonParserBench {

  val StreamyByteStringJsonParser: ByteStringParser[Json] = JsonParser.byteStringParser()

  val StreamyStringJsonParser: StringParser[Json] = JsonParser.stringParser()

  val JacksonMapper: ObjectMapper = new ObjectMapper()

}