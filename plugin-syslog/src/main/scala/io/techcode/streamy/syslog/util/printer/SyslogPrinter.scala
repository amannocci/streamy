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
package io.techcode.streamy.syslog.util.printer

import java.net.InetAddress

import akka.util.ByteString
import io.techcode.streamy.syslog.component.SyslogTransformer.Framing.Framing
import io.techcode.streamy.syslog.component.SyslogTransformer._
import io.techcode.streamy.util.json._
import io.techcode.streamy.util.lang.CharBuilder
import io.techcode.streamy.util.printer.{ByteStringPrinter, DerivedByteStringPrinter}
import io.techcode.streamy.util.{Binder, SomeBinder}

/**
  * Syslog printer companion.
  */
object SyslogPrinter {

  val Nil: String = "-"
  val Space: Char = ' '
  val Empty: String = ""
  val Date: String = "1970-01-01T00:00:00.000Z"
  val DateStamp: String = "Jan 1 00:00:00.000"
  val ProcId: String = "1"
  val AppName: String = "streamy"
  val Facility: Int = 3
  val Severity: Int = 6
  val SemiColon: Char = ':'
  val NewLine: Char = '\n'
  val Inf: Char = '<'
  val Sup: Char = '>'
  val OpenBracket: Char = '['
  val CloseBracket: Char = ']'
  val Version: Char = '1'

  // Default hostname
  val HostName: String = InetAddress.getLocalHost.getHostName

  /**
    * Create a syslog printer that transform incoming [[Json]] to [[ByteString]].
    * This printer is Rfc5424 compliant.
    *
    * @param conf printer configuration.
    * @return new syslog printer Rfc5424 compliant.
    */
  def rfc5424(conf: Rfc5424.Config): ByteStringPrinter[Json] = new Rfc5424Printer(conf)

  /**
    * Create a syslog printer that transform incoming [[Json]] to [[ByteString]].
    * This printer is Rfc3164 compliant.
    *
    * @param conf printer configuration.
    * @return new syslog printer Rfc3164 compliant.
    */
  def rfc3164(conf: Rfc3164.Config): ByteStringPrinter[Json] = new Rfc3164Printer(conf)

}

/**
  * Printer helpers containing various shortcut for printing.
  */
private abstract class PrinterHelpers extends DerivedByteStringPrinter[Json] {

  /**
    * Print data part to format syslog message.
    *
    * @param conf         name of the field.
    * @param defaultValue default value.
    */
  def computeVal(conf: Binder, defaultValue: String): Unit =
    computeValHook(conf, defaultValue)((): Unit)

  /**
    * Print data part to format syslog message.
    *
    * @param conf         name of the field.
    * @param defaultValue default value.
    * @param hook         hook to trigger if a value is process.
    */
  def computeValHook(conf: Binder, defaultValue: String)(hook: => Unit = (): Unit): Unit = {
    if (conf.isDefined) {
      conf.applyString(data, hook)
    } else {
      if (defaultValue.nonEmpty) {
        hook
        builder.append(defaultValue)
      }
    }
  }

  /**
    * Populate data part to format syslog message.
    *
    * @param facilityConf configuration for facility.
    * @param severityConf configuration for severity.
    */
  def computePrival(facilityConf: Binder, severityConf: Binder): Unit = {
    var prival: Int = 0
    severityConf match {
      case binder: SomeBinder =>
        prival = data.evaluate(Root / binder.key).getOrElse[Int](SyslogPrinter.Severity)
      case _ =>
        prival = SyslogPrinter.Severity
    }
    facilityConf match {
      case binder: SomeBinder =>
        prival += data.evaluate(Root / binder.key).getOrElse[Int](SyslogPrinter.Facility) << 3
      case _ =>
        prival += SyslogPrinter.Facility << 3
    }
    builder.append(prival)
  }

  /**
    * Prepare framing record.
    *
    * @param framing framing configuration.
    */
  def framing(framing: Framing)(record: => Unit): Unit = {
    // Print record
    record

    // Handle start of framing
    if (framing == Framing.Count) {
      val count = new CharBuilder
      count.append(builder.length())
      count.append(SyslogPrinter.Space)
      count.append(builder)
      builder = count
    }

    // Handle end of framing
    if (framing == Framing.Delimiter) {
      builder.append(SyslogPrinter.NewLine)
    }
  }

}

/**
  * Syslog printer that transform incoming [[Json]] to [[ByteString]].
  * This printer is Rfc3164 compliant.
  *
  * @param conf printer configuration.
  */
private class Rfc3164Printer(conf: Rfc3164.Config) extends PrinterHelpers {

  // Fast binding access
  private val binding: Rfc3164.Binding = conf.binding

  override def run(): ByteString = {
    framing(conf.framing) {
      // Add prival
      builder.append(SyslogPrinter.Inf)
      computePrival(binding.facility, binding.severity)
      builder.append(SyslogPrinter.Sup)

      // Add timestamp
      computeVal(binding.timestamp, SyslogPrinter.DateStamp)
      builder.append(SyslogPrinter.Space)

      // Add hostname
      computeVal(binding.hostname, SyslogPrinter.HostName)
      builder.append(SyslogPrinter.Space)

      // Add app name
      computeVal(binding.appName, SyslogPrinter.AppName)

      // Add proc id
      builder.append(SyslogPrinter.OpenBracket)
      computeVal(binding.procId, SyslogPrinter.ProcId)
      builder.append(SyslogPrinter.CloseBracket)

      // Add message
      computeValHook(binding.message, SyslogPrinter.Empty) {
        builder.append(SyslogPrinter.SemiColon)
        builder.append(SyslogPrinter.Space)
      }
    }
    ByteString(builder.toString)
  }

}

/**
  * Syslog printer that transform incoming [[Json]] to [[ByteString]].
  * This printer is Rfc5424 compliant.
  *
  * @param conf printer configuration.
  */
private class Rfc5424Printer(conf: Rfc5424.Config) extends PrinterHelpers {

  // Fast binding access
  private val binding: Rfc5424.Binding = conf.binding

  override def run(): ByteString = {
    framing(conf.framing) {
      // Add prival
      builder.append(SyslogPrinter.Inf)
      computePrival(binding.facility, binding.severity)
      builder.append(SyslogPrinter.Sup)

      // Add version
      builder.append(SyslogPrinter.Version)
      builder.append(SyslogPrinter.Space)

      // Add timestamp
      computeVal(binding.timestamp, SyslogPrinter.Date)
      builder.append(SyslogPrinter.Space)

      // Add hostname
      computeVal(binding.hostname, SyslogPrinter.Nil)
      builder.append(SyslogPrinter.Space)

      // Add app name
      computeVal(binding.appName, SyslogPrinter.Nil)
      builder.append(SyslogPrinter.Space)

      // Add proc id
      computeVal(binding.procId, SyslogPrinter.Nil)
      builder.append(SyslogPrinter.Space)

      // Add message id
      computeVal(binding.msgId, SyslogPrinter.Nil)
      builder.append(SyslogPrinter.Space)

      // Skip structured data
      builder.append(SyslogPrinter.Nil)

      // Add message
      computeValHook(binding.message, SyslogPrinter.Empty) {
        builder.append(SyslogPrinter.Space)
      }
    }
    ByteString(builder.toString)
  }

}
