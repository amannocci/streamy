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
package io.techcode.streamy.syslog.util.printer

import java.net.InetAddress

import akka.util.ByteString
import io.techcode.streamy.syslog.component.transformer.SyslogTransformer.Framing.Framing
import io.techcode.streamy.syslog.component.transformer.SyslogTransformer._
import io.techcode.streamy.util.json._
import io.techcode.streamy.util.parser.Binder
import io.techcode.streamy.util.printer.JsonPrinter
import org.apache.commons.lang3.StringUtils

/**
  * Syslog printer companion.
  */
object SyslogPrinter {

  val Nil: String = "-"
  val NilByte: Byte = '-'.toByte
  val Space: Byte = ' '
  val Date: String = "1970-01-01T00:00:00.000Z"
  val DateStamp: String = "Jan 1 00:00:00.000"
  val ProcId: String = "1"
  val AppName: String = "streamy"
  val Facility: Int = 3
  val Severity: Int = 6
  val SemiColon: Byte = ':'
  val NewLine: Byte = '\n'
  val Inf: Byte = '<'
  val Sup: Byte = '>'
  val OpenBracket: Byte = '['
  val CloseBracket: Byte = ']'
  val Version: Byte = '1'

  // Default hostname
  val HostName: String = InetAddress.getLocalHost.getHostName

  /**
    * Create a syslog printer that transform incoming [[Json]] to [[ByteString]].
    * This printer is Rfc5424 compliant.
    *
    * @param pkt  data to print.
    * @param conf printer configuration.
    * @return new syslog printer Rfc5424 compliant.
    */
  def rfc5424(pkt: Json, conf: Rfc5424.Config): JsonPrinter = new Rfc5424Printer(pkt, conf)

  /**
    * Create a syslog printer that transform incoming [[Json]] to [[ByteString]].
    * This printer is Rfc3164 compliant.
    *
    * @param pkt  data to print.
    * @param conf printer configuration.
    * @return new syslog printer Rfc3164 compliant.
    */
  def rfc3164(pkt: Json, conf: Rfc3164.Config): JsonPrinter = new Rfc3164Printer(pkt, conf)

}

/**
  * Printer helpers containing various shortcut for printing.
  */
private abstract class PrinterHelpers(pkt: Json) extends JsonPrinter(pkt) {

  /**
    * Print data part to format syslog message.
    *
    * @param conf         name of the field.
    * @param defaultValue default value.
    */
  def computeVal(conf: Option[Binder], defaultValue: String): ByteString = {
    if (conf.isDefined) {
      val binder = conf.get
      binder.bind(pkt.evaluate(Root / binder.key).getOrElse(defaultValue))
    } else {
      ByteString.fromArrayUnsafe(defaultValue.getBytes)
    }
  }

  /**
    * Populate data part to format syslog message.
    *
    * @param facilityConf configuration for facility.
    * @param severityConf configuration for severity.
    */
  def computePrival(facilityConf: Option[Binder], severityConf: Option[Binder]): ByteString = {
    var prival = 0
    if (severityConf.isDefined) {
      val binder = severityConf.get
      prival = pkt.evaluate(Root / binder.key).flatMap(_.asInt).getOrElse(SyslogPrinter.Facility)
    } else {
      prival = SyslogPrinter.Severity
    }
    if (facilityConf.isDefined) {
      val binder = facilityConf.get
      prival += pkt.evaluate(Root / binder.key).flatMap(_.asInt).getOrElse(SyslogPrinter.Facility) << 3
    } else {
      prival += SyslogPrinter.Facility << 3
    }
    ByteString.fromArrayUnsafe(prival.toString.getBytes)
  }

  /**
    * Prepare framing record.
    *
    * @param framing framing configuration.
    * @param size    size of the record.
    */
  def framing(framing: Framing, size: Int)(record: => Unit): Boolean = {
    // Compute optimal size
    if (framing == Framing.Delimiter) {
      builder.sizeHint(size + 1)
    } else {
      val count = ByteString.fromArrayUnsafe(size.toString.getBytes)
      builder.sizeHint(size + count.length + 1)
      builder ++= count
      builder.putByte(SyslogPrinter.Space)
    }

    // Print record
    record

    // Handle end of framing
    if (framing == Framing.Delimiter) {
      builder.putByte(SyslogPrinter.NewLine)
    }
    true
  }

}

/**
  * Syslog printer that transform incoming [[Json]] to [[ByteString]].
  * This printer is Rfc3164 compliant.
  *
  * @param pkt  data to print.
  * @param conf printer configuration.
  */
private class Rfc3164Printer(pkt: Json, conf: Rfc3164.Config) extends PrinterHelpers(pkt) {

  // Fast binding access
  private val binding: Rfc3164.Binding = conf.binding

  // Compute prival
  private val prival: ByteString = computePrival(binding.facility, binding.severity)

  // Compute timestamp
  private val timestamp: ByteString = computeVal(binding.timestamp, SyslogPrinter.DateStamp)

  // Compute hostname
  private val hostname: ByteString = computeVal(binding.hostname, SyslogPrinter.HostName)

  // Compute appName
  private val appName: ByteString = computeVal(binding.appName, SyslogPrinter.AppName)

  // Compute proc id
  private val procId: ByteString = computeVal(binding.procId, SyslogPrinter.ProcId)

  // Compute message
  private val message: Option[ByteString] = binding.message.map { binder =>
    binder.bind(pkt.evaluate(Root / binder.key).getOrElse(JsString(StringUtils.EMPTY)))
  }

  // Size of the record
  private val size: Int = {
    6 + prival.length +
      timestamp.length +
      hostname.length +
      appName.length +
      procId.length +
      message.map(_.length + 2).getOrElse(0)
  }

  override def process(): Boolean =
    framing(conf.framing, size) {
      // Add prival
      builder.putByte(SyslogPrinter.Inf)
      builder ++= prival
      builder.putByte(SyslogPrinter.Sup)

      // Add timestamp
      builder ++= timestamp
      builder.putByte(SyslogPrinter.Space)

      // Add hostname
      builder ++= hostname
      builder.putByte(SyslogPrinter.Space)

      // Add app name
      builder ++= appName

      // Add proc id
      builder.putByte(SyslogPrinter.OpenBracket)
      builder ++= procId
      builder.putByte(SyslogPrinter.CloseBracket)

      // Add message
      if (message.isDefined) {
        builder.putByte(SyslogPrinter.SemiColon)
        builder.putByte(SyslogPrinter.Space)
        builder ++= message.get
      }
    }

}

/**
  * Syslog printer that transform incoming [[Json]] to [[ByteString]].
  * This printer is Rfc5424 compliant.
  *
  * @param pkt  data to print.
  * @param conf printer configuration.
  */
private class Rfc5424Printer(pkt: Json, conf: Rfc5424.Config) extends PrinterHelpers(pkt) {

  // Fast binding access
  private val binding: Rfc5424.Binding = conf.binding

  // Compute prival
  private val prival: ByteString = computePrival(binding.facility, binding.severity)

  // Compute timestamp
  private val timestamp: ByteString = computeVal(binding.timestamp, SyslogPrinter.Date)

  // Compute hostname
  private val hostname: ByteString = computeVal(binding.hostname, SyslogPrinter.Nil)

  // Compute appName
  private val appName: ByteString = computeVal(binding.appName, SyslogPrinter.Nil)

  // Compute proc id
  private val procId: ByteString = computeVal(binding.procId, SyslogPrinter.Nil)

  // Compute msg id
  private val msgId: ByteString = computeVal(binding.msgId, SyslogPrinter.Nil)

  // Compute message
  private val message: Option[ByteString] = binding.message.map { binder =>
    binder.bind(pkt.evaluate(Root / binder.key).getOrElse(JsString(StringUtils.EMPTY)))
  }

  // Size of the record
  private val size: Int = {
    10 + prival.length +
      timestamp.length +
      hostname.length +
      appName.length +
      procId.length +
      msgId.length +
      message.map(_.length + 1).getOrElse(0)
  }

  override def process(): Boolean =
    framing(conf.framing, size) {
      // Add prival
      builder.putByte(SyslogPrinter.Inf)
      builder ++= prival
      builder.putByte(SyslogPrinter.Sup)

      // Add version
      builder.putByte(SyslogPrinter.Version)
      builder.putByte(SyslogPrinter.Space)

      // Add timestamp
      builder ++= timestamp
      builder.putByte(SyslogPrinter.Space)

      // Add hostname
      builder ++= hostname
      builder.putByte(SyslogPrinter.Space)

      // Add app name
      builder ++= appName
      builder.putByte(SyslogPrinter.Space)

      // Add proc id
      builder ++= procId
      builder.putByte(SyslogPrinter.Space)

      // Add message id
      builder ++= msgId
      builder.putByte(SyslogPrinter.Space)

      // Skip structured data
      builder.putByte(SyslogPrinter.NilByte)

      // Add message
      if (message.isDefined) {
        builder.putByte(SyslogPrinter.Space)
        builder ++= message.get
      }
    }

}
