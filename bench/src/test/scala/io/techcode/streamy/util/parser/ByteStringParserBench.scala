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
package io.techcode.streamy.util.parser

import akka.util.ByteString
import io.techcode.streamy.util.parser.ByteStringParserBench.{Str, Times}
import org.openjdk.jmh.annotations._

/**
  * Bytestring parser bench.
  */
class ByteStringParserBench {

  @Benchmark def str(): Either[ParseException, Boolean] = Str.parse(ByteStringParserBench.Lorem)

  @Benchmark def times(): Either[ParseException, Boolean] = Times.parse(ByteStringParserBench.Lorem)

}

private object ByteStringParserBench {

  val LoremStr: String = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."
  val Lorem: ByteString = ByteString(LoremStr)

  val Str: ByteStringParser[Boolean] = new ByteStringParser[Boolean]() {
    def run(): Boolean = root()

    def root(): Boolean = str(LoremStr)
  }

  val Times: ByteStringParser[Boolean] = new ByteStringParser[Boolean]() {
    def run(): Boolean = root()

    def root(): Boolean = times(LoremStr.length, CharMatchers.All)
  }

}
