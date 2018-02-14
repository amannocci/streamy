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
package io.techcode.streamy.syslog.util.parser

import java.nio.charset.StandardCharsets

import akka.NotUsed
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Framing.FramingException
import akka.stream.stage.{GraphStage, GraphStageLogic, InHandler, OutHandler}
import akka.stream.{Attributes, FlowShape, Inlet, Outlet}
import akka.util.ByteString
import io.techcode.streamy.util.StreamException

import scala.util.{Failure, Success, Try}

// Provides a Syslog framing flow that can separate records from an incoming Syslog-formatted [[ByteString]] stream.
object SyslogFraming {

  /**
    * Returns a flow that parses an incoming Syslog stream and emits the identified records.
    *
    * The incoming stream is expected to be a concatenation of records of the format:
    * [record length] [record data]
    *
    * The flow will emit each record's data as a byte string.
    *
    * @param maxRecordLength The maximum record length allowed.
    *                        If a record is indicated to be longer, this Flow will fail the stream.
    */
  def scanner(maxRecordLength: Int = Int.MaxValue): Flow[ByteString, ByteString, NotUsed] =
    Flow[ByteString].via(new SyslogFramingStage(maxRecordLength)).named("syslogFraming")

  /**
    * Syslog framing stage.
    *
    * @param maxRecordLength max record length.
    */
  private class SyslogFramingStage(maxRecordLength: Int) extends GraphStage[FlowShape[ByteString, ByteString]] {

    // The maximum length of the record prefix indicating its size.
    private val maxRecordPrefixLength = maxRecordLength.toString.length

    // Shape stuff
    val in: Inlet[ByteString] = Inlet[ByteString]("SyslogFramingStage.in")
    val out: Outlet[ByteString] = Outlet[ByteString]("SyslogFramingStage.out")
    override val shape: FlowShape[ByteString, ByteString] = FlowShape(in, out)

    override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new SyslogFramingLogic

    /**
      * Syslog framing logic.
      */
    private class SyslogFramingLogic extends GraphStageLogic(shape) with InHandler with OutHandler {

      // Delimiter between header and data
      private val Space = ' '.toByte

      // Buffer
      private var buffer = ByteString.empty

      // The byte length of the next record, if known
      private var recordLength: Option[Int] = None

      // Last index
      private var lastIndex: Int = 0

      // Register handlers
      setHandlers(in, out, this)

      override def onPush(): Unit = {
        buffer ++= grab(in)
        doParse()
      }

      override def onPull(): Unit = doParse()

      override def onUpstreamFinish(): Unit =
        if (buffer.isEmpty) {
          completeStage()
        } else if (isAvailable(out)) {
          doParse()
        }

      private def tryPull(): Unit =
        if (isClosed(in)) {
          failStage(new StreamException("Stream finished but there was a truncated final record in the buffer."))
        } else {
          pull(in)
        }

      /**
        * Try to parse record size.
        */
      private def tryParse(): Unit = {
        val idx = buffer.indexOf(Space, from = lastIndex)
        if (idx < 0) {
          if (buffer.size > maxRecordPrefixLength) {
            failStage(new StreamException(s"Record size prefix is longer than $maxRecordPrefixLength bytes."))
          } else if (isClosed(in) && buffer.isEmpty) {
            completeStage()
          } else {
            lastIndex = buffer.size
            tryPull()
          }
        } else {
          val (recordSize, buf) = buffer.splitAt(idx)
          buffer = buf.drop(1).compact

          Try(recordSize.decodeString(StandardCharsets.US_ASCII).toInt) match {
            case Success(length) =>
              if (length > maxRecordLength) {
                failStage(new StreamException(s"Record of size $length bytes exceeds maximum of $maxRecordLength bytes."))
              } else if (length < 0) {
                failStage(new FramingException(s"Record size prefix $length is negative."))
              } else {
                recordLength = Some(length)
                doParse()
              }
            case Failure(ex) => failStage(ex)
          }
        }
      }

      /**
        * Parse current record based on buffer.
        */
      private def doParse(): Unit = {
        if (recordLength.isDefined) {
          val length = recordLength.get

          // We have enough bytes
          if (buffer.size >= length) {
            // Split current record from next one
            val (record, buf) = buffer.splitAt(length)
            buffer = buf.compact
            recordLength = None
            lastIndex = 0

            // Push to next step
            push(out, record.compact)
          } else {
            // We need more data
            tryPull()
          }
        } else {
          // Attempt to parse record size
          tryParse()
        }
      }

    }

  }

}
