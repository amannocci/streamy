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
package io.techcode.streamy.graphite.util.parser

import akka.util.ByteString
import com.google.common.base.CharMatcher
import io.techcode.streamy.graphite.component.GraphiteTransformer
import io.techcode.streamy.util.Binder
import io.techcode.streamy.util.json._
import io.techcode.streamy.util.parser.{ByteStringParser, ParseException}

/**
  * Graphite parser companion.
  */
object GraphiteParser {

  // Delimiter value matcher
  private[parser] val DelimiterMatcher: CharMatcher = CharMatcher.noneOf(" ").precomputed()

  /**
    * Create a graphite parser that transform incoming [[ByteString]] to [[Json]].
    * This parser is compliant with Graphite protocol.
    *
    * @param config parser configuration.
    * @return new graphite parser compliant with Graphite protocol.
    */
  def parser(config: GraphiteTransformer.Config): ByteStringParser[Json] = new GraphiteParser(config)

}

/**
  * Parser helpers containing various shortcut for character matching.
  */
private trait ParserHelpers {
  this: ByteStringParser[Json] =>

  @inline def sp(): Boolean = ch(' ')

}

/**
  * Graphite parser that transform incoming [[ByteString]] to [[Json]].
  * This parser is compliant with Graphite protocol.
  *
  * @param config parser configuration.
  */
private class GraphiteParser(config: GraphiteTransformer.Config) extends ByteStringParser[Json] with ParserHelpers {

  private val binding = config.binding

  private implicit var builder: JsObjectBuilder = Json.objectBuilder()

  def run(): Json = {
    if (root()) {
      builder.result()
    } else {
      throw new ParseException(s"Unexpected input at index ${_cursor}")
    }
  }

  override def root(): Boolean =
    path() &&
      sp() &&
      value() &&
      sp() &&
      timestamp() &&
      eoi()

  def path(): Boolean = parseUntilDelimiter(binding.path)

  def value(): Boolean = parseUntilDelimiter(binding.value)

  def timestamp(): Boolean = parseUntilDelimiter(binding.timestamp)

  @inline private def parseUntilDelimiter(field: Binder): Boolean =
    capture(oneOrMore(GraphiteParser.DelimiterMatcher)) {
      field(_)
    }

  override def cleanup(): Unit = {
    super.cleanup()
    builder = Json.objectBuilder()
  }

}
