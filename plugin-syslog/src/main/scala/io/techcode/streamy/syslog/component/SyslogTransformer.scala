/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017-2018
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
import io.techcode.streamy.syslog.component.SyslogTransformer.Framing.Framing
import io.techcode.streamy.syslog.util.parser.{SyslogFraming, SyslogParser}
import io.techcode.streamy.syslog.util.printer.SyslogPrinter
import io.techcode.streamy.util.json.Json
import io.techcode.streamy.util.{Binder, NoneBinder}

/**
  * Syslog transformer companion.
  */
object SyslogTransformer {

  // Constants
  private val NewLineDelimiter = ByteString("\n")

  /**
    * Create a syslog flow that transform incoming [[ByteString]] to [[Json]].
    * This flow is Rfc5424 compliant.
    *
    * @param conf flow configuration.
    * @return new syslog flow Rfc5424 compliant.
    */
  def parser(conf: Rfc5424.Config): Flow[ByteString, Json, NotUsed] = {
    val framing: Flow[ByteString, ByteString, NotUsed] = {
      if (conf.framing == Framing.Delimiter) {
        StreamFraming.delimiter(NewLineDelimiter, conf.maxSize, allowTruncation = true)
      } else {
        SyslogFraming.scanner(conf.maxSize)
      }
    }

    framing.via(Flow.fromGraph(SourceTransformer(() => SyslogParser.rfc5424(conf))))
  }

  /**
    * Create a syslog flow that transform incoming [[ByteString]] to [[Json]].
    * This flow is Rfc3164 compliant.
    *
    * @param conf flow configuration.
    * @return new syslog flow Rfc3164 compliant.
    */
  def parser(conf: Rfc3164.Config): Flow[ByteString, Json, NotUsed] = {
    val framing: Flow[ByteString, ByteString, NotUsed] = {
      if (conf.framing == Framing.Delimiter) {
        StreamFraming.delimiter(NewLineDelimiter, conf.maxSize, allowTruncation = true)
      } else {
        SyslogFraming.scanner(conf.maxSize)
      }
    }

    framing.via(Flow.fromGraph(SourceTransformer(() => SyslogParser.rfc3164(conf))))
  }

  /**
    * Create a syslog flow that transform incoming [[Json]] to [[ByteString]].
    * This flow is Rfc5424 compliant.
    *
    * @param conf flow configuration.
    * @return new syslog flow Rfc5424 compliant.
    */
  def printer(conf: Rfc5424.Config): Flow[Json, ByteString, NotUsed] =
    Flow.fromGraph(SinkTransformer(() => SyslogPrinter.rfc5424(conf)))

  /**
    * Create a syslog flow that transform incoming [[Json]] to [[ByteString]].
    * This flow is Rfc3164 compliant.
    *
    * @param conf flow configuration.
    * @return new syslog flow Rfc3164 compliant.
    */
  def printer(conf: Rfc3164.Config): Flow[Json, ByteString, NotUsed] =
    Flow.fromGraph(SinkTransformer(() => SyslogPrinter.rfc3164(conf)))

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
      facility: Binder = NoneBinder,
      severity: Binder = NoneBinder,
      timestamp: Binder = NoneBinder,
      hostname: Binder = NoneBinder,
      appName: Binder = NoneBinder,
      procId: Binder = NoneBinder,
      msgId: Binder = NoneBinder,
      structData: Binder = NoneBinder,
      message: Binder = NoneBinder
    )

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
      facility: Binder = NoneBinder,
      severity: Binder = NoneBinder,
      timestamp: Binder = NoneBinder,
      hostname: Binder = NoneBinder,
      appName: Binder = NoneBinder,
      procId: Binder = NoneBinder,
      message: Binder = NoneBinder
    )

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
