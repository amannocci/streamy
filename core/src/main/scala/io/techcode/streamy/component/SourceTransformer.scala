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
import io.techcode.streamy.event.Event
import io.techcode.streamy.util.StreamException
import io.techcode.streamy.util.json._
import io.techcode.streamy.util.parser.ByteStringParser

import scala.language.postfixOps
import scala.util.control.NonFatal

/**
  * Source transformer abstract implementation that provide
  * a convenient way to process a conversion from [[ByteString]] to [[Event]].
  */
final case class SourceTransformer[T](factory: () => ByteStringParser[Json]) extends GraphStage[FlowShape[ByteString, Event[T]]] {

  val in: Inlet[ByteString] = Inlet[ByteString]("sourceTransformer.in")

  val out: Outlet[Event[T]] = Outlet[Event[T]]("sourceTransformer.out")

  override val shape: FlowShape[ByteString, Event[T]] = FlowShape(in, out)

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) with InHandler with OutHandler {

    // Set handler
    setHandlers(in, out, this)

    private val parser = factory()

    private def decider = inheritedAttributes.mandatoryAttribute[SupervisionStrategy].decider

    override def onPush(): Unit = {
      try {
        val data = grab(in)
        parser.parse(data) match {
          case Right(result) => push(out, Event[T](result))
          case Left(ex) => throw new StreamException(ex.getMessage, data, Map.empty)
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
