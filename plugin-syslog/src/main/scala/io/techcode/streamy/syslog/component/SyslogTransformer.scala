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
package io.techcode.streamy.syslog.component

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Framing => StreamFraming}
import akka.util.ByteString
import io.techcode.streamy.component.{SinkTransformer, SourceTransformer}
import io.techcode.streamy.event.StreamEvent
import io.techcode.streamy.syslog.component.SyslogTransformer.Framing.Framing
import io.techcode.streamy.syslog.util.parser.{SyslogFraming, SyslogParser}
import io.techcode.streamy.syslog.util.printer.SyslogPrinter
import io.techcode.streamy.util.json._
import io.techcode.streamy.util.parser.ByteStringParser
import io.techcode.streamy.util.printer.ByteStringPrinter

/**
  * Syslog transformer companion.
  */
object SyslogTransformer {

  // Constants
  private val NewLineDelimiter = ByteString("\n")

  /**
    * Create a syslog flow that transform incoming [[ByteString]] to [[StreamEvent]].
    * This flow is Rfc5424 compliant.
    *
    * @param conf flow configuration.
    * @return new syslog flow Rfc5424 compliant.
    */
  def parser(conf: Rfc5424.Config): Flow[ByteString, StreamEvent, NotUsed] = {
    val framing: Flow[ByteString, ByteString, NotUsed] = {
      if (conf.framing == Framing.Delimiter) {
        StreamFraming.delimiter(NewLineDelimiter, conf.maxSize, allowTruncation = true)
      } else {
        SyslogFraming.scanner(conf.maxSize)
      }
    }

    framing.via(Flow.fromGraph(new SourceTransformer {
      def factory(): ByteStringParser[Json] = SyslogParser.rfc5424(conf)
    }))
  }

  /**
    * Create a syslog flow that transform incoming [[ByteString]] to [[StreamEvent]].
    * This flow is Rfc3164 compliant.
    *
    * @param conf flow configuration.
    * @return new syslog flow Rfc3164 compliant.
    */
  def parser(conf: Rfc3164.Config): Flow[ByteString, StreamEvent, NotUsed] = {
    val framing: Flow[ByteString, ByteString, NotUsed] = {
      if (conf.framing == Framing.Delimiter) {
        StreamFraming.delimiter(NewLineDelimiter, conf.maxSize, allowTruncation = true)
      } else {
        SyslogFraming.scanner(conf.maxSize)
      }
    }

    framing.via(Flow.fromGraph(new SourceTransformer {
      def factory(): ByteStringParser[Json] = SyslogParser.rfc3164(conf)
    }))
  }

  /**
    * Create a syslog flow that transform incoming [[StreamEvent]] to [[ByteString]].
    * This flow is Rfc5424 compliant.
    *
    * @param conf flow configuration.
    * @return new syslog flow Rfc5424 compliant.
    */
  def printer[T](conf: Rfc5424.Config): Flow[StreamEvent, ByteString, NotUsed] =
    Flow.fromGraph(new SinkTransformer {
      def factory(): ByteStringPrinter[Json] = SyslogPrinter.rfc5424(conf)
    })

  /**
    * Create a syslog flow that transform incoming [[StreamEvent]] to [[ByteString]].
    * This flow is Rfc3164 compliant.
    *
    * @param conf flow configuration.
    * @return new syslog flow Rfc3164 compliant.
    */
  def printer[T](conf: Rfc3164.Config): Flow[StreamEvent, ByteString, NotUsed] =
    Flow.fromGraph(new SinkTransformer {
      def factory(): ByteStringPrinter[Json] = SyslogPrinter.rfc3164(conf)
    })

  // Common related stuff
  object Framing extends Enumeration {
    type Framing = Value
    val Delimiter, Count = Value
  }


  // Rfc 5424 related stuff
  object Rfc5424 {

    object Id {
      val Facility = "facility"
      val Severity = "severity"
      val Timestamp = "timestamp"
      val Hostname = "hostname"
      val AppName = "appName"
      val ProcId = "procId"
      val MsgId = "msgId"
      val StructData = "structDataId"
      val Message = "message"
    }

    case class Binding(
      facility: Option[String] = None,
      severity: Option[String] = None,
      timestamp: Option[String] = None,
      hostname: Option[String] = None,
      appName: Option[String] = None,
      procId: Option[String] = None,
      msgId: Option[String] = None,
      structData: Option[String] = None,
      message: Option[String] = None
    ) {
      val facilityPointer: Option[JsonPointer] = facility.map(Root / _)
      val severityPointer: Option[JsonPointer] = severity.map(Root / _)
      val timestampPointer: Option[JsonPointer] = timestamp.map(Root / _)
      val hostnamePointer: Option[JsonPointer] = hostname.map(Root / _)
      val appNamePointer: Option[JsonPointer] = appName.map(Root / _)
      val procIdPointer: Option[JsonPointer] = procId.map(Root / _)
      val msgIdPointer: Option[JsonPointer] = msgId.map(Root / _)
      val structDataPointer: Option[JsonPointer] = structData.map(Root / _)
      val messagePointer: Option[JsonPointer] = message.map(Root / _)
    }

    case class Config(
      mode: Mode = Rfc5424.Mode.Strict,
      maxSize: Int = Int.MaxValue,
      framing: Framing = Framing.Delimiter,
      binding: Binding = Binding()
    )

    sealed abstract class Mode(
      val hostname: Int,
      val appName: Int,
      val procId: Int,
      val msgId: Int
    )

    object Mode {

      // scalastyle:off magic.number
      // Strict mode
      case object Strict extends Mode(
        hostname = 255,
        appName = 48,
        procId = 128,
        msgId = 32
      )

      // Lenient mode
      case object Lenient extends Mode(
        hostname = 255,
        appName = 96,
        procId = 128,
        msgId = 64
      )

      // scalastyle:on magic.number

    }

  }

  // Rfc 3164 related stuff
  object Rfc3164 {

    object Id {
      val Facility = "facility"
      val Severity = "severity"
      val Timestamp = "timestamp"
      val Hostname = "hostname"
      val AppName = "appName"
      val ProcId = "procId"
      val MsgId = "msgId"
      val Message = "message"
    }

    case class Binding(
      facility: Option[String] = None,
      severity: Option[String] = None,
      timestamp: Option[String] = None,
      hostname: Option[String] = None,
      appName: Option[String] = None,
      procId: Option[String] = None,
      message: Option[String] = None
    ) {
      val facilityPointer: Option[JsonPointer] = facility.map(Root / _)
      val severityPointer: Option[JsonPointer] = severity.map(Root / _)
      val timestampPointer: Option[JsonPointer] = timestamp.map(Root / _)
      val hostnamePointer: Option[JsonPointer] = hostname.map(Root / _)
      val appNamePointer: Option[JsonPointer] = appName.map(Root / _)
      val procIdPointer: Option[JsonPointer] = procId.map(Root / _)
      val messagePointer: Option[JsonPointer] = message.map(Root / _)
    }

    sealed abstract class Mode(
      val hostname: Int,
      val appName: Int,
      val procId: Int
    )

    object Mode {

      // scalastyle:off magic.number
      // Strict mode
      case object Strict extends Mode(
        hostname = 255,
        appName = 48,
        procId = 128
      )

      // Lenient mode
      case object Lenient extends Mode(
        hostname = 255,
        appName = 96,
        procId = 128
      )

      // scalastyle:on magic.number

    }

    case class Config(
      mode: Mode = Rfc3164.Mode.Strict,
      maxSize: Int = Int.MaxValue,
      framing: Framing = Framing.Delimiter,
      binding: Binding = Binding()
    )

  }

}
