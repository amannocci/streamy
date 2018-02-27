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

/**
  * Syslog printer companion.
  */
object SyslogPrinter {

  val Nil: ByteString = ByteString('-')
  val Space: ByteString = ByteString(' ')
  val Date: ByteString = ByteString("1970-01-01T00:00:00.000Z")
  val DateStamp: ByteString = ByteString("Jan 1 00:00:00.000")
  val ProcId: ByteString = ByteString("1")
  val AppName: ByteString = ByteString("streamy")
  val Facility: Int = 3
  val Severity: Int = 6
  val SemiColon: ByteString = ByteString(':')
  val NewLine: ByteString = ByteString('\n')
  val Inf: ByteString = ByteString('<')
  val Sup: ByteString = ByteString('>')
  val OpenBracket: ByteString = ByteString('[')
  val CloseBracket: ByteString = ByteString(']')
  val Version: ByteString = ByteString('1')

  // Default hostname
  val HostName: ByteString = ByteString(InetAddress.getLocalHost.getHostName)

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
  def computeVal(conf: Option[Binder], defaultValue: ByteString): ByteString = {
    if (conf.isDefined) {
      val binder = conf.get
      pkt.evaluate(Root / binder.key).map(binder.bind).getOrElse(defaultValue)
    } else {
      defaultValue
    }
  }

  /**
    * Populate data part to format syslog message.
    *
    * @param facilityConf configuration for facility.
    * @param severityConf configuration for severity.
    */
  def computePrival(facilityConf: Option[Binder], severityConf: Option[Binder]): ByteString = {
    var prival: Int = 0
    if (severityConf.isDefined) {
      val binder = severityConf.get
      prival = pkt.evaluate(Root / binder.key).asInt.getOrElse(SyslogPrinter.Severity)
    } else {
      prival = SyslogPrinter.Severity
    }
    if (facilityConf.isDefined) {
      val binder = facilityConf.get
      prival += pkt.evaluate(Root / binder.key).asInt.getOrElse(SyslogPrinter.Facility) << 3
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
    // Handle start of framing
    if (framing == Framing.Count) {
      val count = ByteString(size.toString)
      builder ++= count
      builder ++= SyslogPrinter.Space
    }

    // Print record
    record

    // Handle end of framing
    if (framing == Framing.Delimiter) {
      builder ++= SyslogPrinter.NewLine
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
  private val message: ByteString = binding.message.flatMap { binder =>
    pkt.evaluate(Root / binder.key).map(binder.bind)
  }.getOrElse(ByteString.empty)

  // Size of the record
  private val size: Int = {
    6 + prival.length +
      timestamp.length +
      hostname.length +
      appName.length +
      procId.length +
      (if (message.nonEmpty) message.length + 2 else 0)
  }

  override def process(): Boolean =
    framing(conf.framing, size) {
      // Add prival
      builder ++= SyslogPrinter.Inf
      builder ++= prival
      builder ++= SyslogPrinter.Sup

      // Add timestamp
      builder ++= timestamp
      builder ++= SyslogPrinter.Space

      // Add hostname
      builder ++= hostname
      builder ++= SyslogPrinter.Space

      // Add app name
      builder ++= appName

      // Add proc id
      builder ++= SyslogPrinter.OpenBracket
      builder ++= procId
      builder ++= SyslogPrinter.CloseBracket

      // Add message
      if (message.nonEmpty) {
        builder ++= SyslogPrinter.SemiColon
        builder ++= SyslogPrinter.Space
        builder ++= message
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
  private val message: ByteString = binding.message.flatMap { binder =>
    pkt.evaluate(Root / binder.key).map(binder.bind)
  }.getOrElse(ByteString.empty)

  // Size of the record
  private val size: Int = {
    10 + prival.length +
      timestamp.length +
      hostname.length +
      appName.length +
      procId.length +
      msgId.length +
      (if (message.nonEmpty) message.length + 1 else 0)
  }

  override def process(): Boolean =
    framing(conf.framing, size) {
      // Add prival
      builder ++= SyslogPrinter.Inf
      builder ++= prival
      builder ++= SyslogPrinter.Sup

      // Add version
      builder ++= SyslogPrinter.Version
      builder ++= SyslogPrinter.Space

      // Add timestamp
      builder ++= timestamp
      builder ++= SyslogPrinter.Space

      // Add hostname
      builder ++= hostname
      builder ++= SyslogPrinter.Space

      // Add app name
      builder ++= appName
      builder ++= SyslogPrinter.Space

      // Add proc id
      builder ++= procId
      builder ++= SyslogPrinter.Space

      // Add message id
      builder ++= msgId
      builder ++= SyslogPrinter.Space

      // Skip structured data
      builder ++= SyslogPrinter.Nil

      // Add message
      if (message.nonEmpty) {
        builder ++= SyslogPrinter.Space
        builder ++= message
      }
    }

}
