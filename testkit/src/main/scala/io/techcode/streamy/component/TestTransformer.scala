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
package io.techcode.streamy.component

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Source}
import akka.stream.testkit.scaladsl.TestSink
import io.techcode.streamy.TestSystem
import org.scalatest.Assertion

/**
  * Helper for source transformer test.
  */
trait TestTransformer extends TestSystem {

  /**
    * Except an element using given transformer.
    *
    * @param transformer transformer to use.
    * @param input       input to process with transformer.
    * @param output      output expected after transformation.
    * @tparam In  input type.
    * @tparam Out output type.
    */
  def except[In, Out](transformer: Flow[In, Out, NotUsed], input: In, output: Out): Assertion = {
    Source.single(input)
      .via(transformer)
      .runWith(TestSink.probe[Out])
      .requestNext() should equal(output)
  }

  /**
    * Except an element using given transformer.
    *
    * @param transformer transformer to use.
    * @param input       multiple input to process with transformer.
    * @param output      output expected after transformation.
    * @tparam In  input type.
    * @tparam Out output type.
    */
  def except[In, Out](transformer: Flow[In, Out, NotUsed], input: Iterator[In], output: Out): Assertion = {
    Source.fromIterator[In](() => input)
      .via(transformer)
      .runWith(TestSink.probe[Out])
      .requestNext() should equal(output)
  }

  /**
    * Except an error using given transformer with given input.
    *
    * @param transformer transformer to use.
    * @param input       input to process with transformer.
    * @tparam In  input type.
    * @tparam Out output type.
    */
  def exceptError[In, Out](transformer: Flow[In, Out, NotUsed], input: In): Throwable = {
    Source.single(input)
      .via(transformer)
      .runWith(TestSink.probe[Out])
      .request(1)
      .expectError()
  }

}
