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
package io.techcode.streamy.component

import akka.stream.ActorAttributes.SupervisionStrategy
import akka.stream._
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.util.ByteString
import io.techcode.streamy.event.StreamEvent
import io.techcode.streamy.util.json.Json
import io.techcode.streamy.util.printer.ByteStringPrinter

import scala.language.postfixOps
import scala.util.control.NonFatal

/**
  * Sink transformer abstract implementation that provide
  * a convenient way to process a convertion from [[StreamEvent]] to [[ByteString]].
  */
abstract class SinkTransformer extends GraphStage[FlowShape[StreamEvent, ByteString]] {

  // Inlet
  val in: Inlet[StreamEvent] = Inlet[StreamEvent]("sinkTransformer.in")

  // Outlet
  val out: Outlet[ByteString] = Outlet[ByteString]("sinkTransformer.out")

  override val shape: FlowShape[StreamEvent, ByteString] = FlowShape(in, out)

  /**
    * Factory to create a bytestring printer.
    *
    * @return a bytestring parser.
    */
  def factory(): ByteStringPrinter[Json]

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) with InHandler with OutHandler {

    // Set handler
    setHandlers(in, out, this)

    // Printer for this logic
    private val printer = factory()

    private def decider = inheritedAttributes.mandatoryAttribute[SupervisionStrategy].decider

    override def onPush(): Unit = {
      val evt = grab(in)
      try {
        printer.print(evt.payload) match {
          case Right(result) => push(out, result)
          case Left(ex) => evt.discard(ex)
        }
      } catch {
        case NonFatal(ex) => decider(ex) match {
          case Supervision.Stop => failStage(ex)
          case _ => pull(in)
        }
      }
    }

    override def onPull(): Unit = pull(in)

  }

}
