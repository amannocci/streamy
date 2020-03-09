/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2020
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
import akka.stream.{FlowShape, Graph}
import akka.stream.scaladsl.{Broadcast, Flow, GraphDSL, Keep, ZipWith}

object PassThroughFlow {

  def apply[In, Out](processingFlow: Flow[In, Out, NotUsed]): Graph[FlowShape[In, (Out, In)], NotUsed] =
    apply[In, Out, (Out, In)](processingFlow, Keep.both)

  def apply[In, Out, Result](processingFlow: Flow[In, Out, NotUsed], output: (Out, In) => Result): Graph[FlowShape[In, Result], NotUsed] =
    Flow.fromGraph(GraphDSL.create() { implicit builder => {
      import GraphDSL.Implicits._

      val broadcast = builder.add(Broadcast[In](2))
      val zip = builder.add(ZipWith[Out, In, Result]((left, right) => output(left, right)))

      // format: off
      broadcast.out(0) ~> processingFlow ~> zip.in0
      broadcast.out(1) ~> zip.in1
      // format: on

      FlowShape(broadcast.in, zip.out)
    }
    }
    )
}