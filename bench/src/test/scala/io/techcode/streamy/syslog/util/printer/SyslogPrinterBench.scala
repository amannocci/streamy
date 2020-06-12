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

import akka.util.ByteString
import io.techcode.streamy.syslog.component.SyslogTransformer
import io.techcode.streamy.syslog.component.SyslogTransformer.{Rfc3164, Rfc5424}
import io.techcode.streamy.util.json._
import io.techcode.streamy.util.printer.{ByteStringPrinter, PrintException}
import org.openjdk.jmh.annotations.Benchmark

/**
  * Syslog printer bench.
  */
class SyslogPrinterBench {

  @Benchmark def rfc5424Complete(): Either[PrintException, ByteString] =
    SyslogPrinterBench.Rfc5424Complete.print(SyslogPrinterBench.InputRfc5424)

  @Benchmark def rfc5424Message(): Either[PrintException, ByteString] =
    SyslogPrinterBench.Rfc5424Message.print(SyslogPrinterBench.InputRfc5424)

  @Benchmark def rfc3164Complete(): Either[PrintException, ByteString] =
    SyslogPrinterBench.Rfc3164Complete.print(SyslogPrinterBench.InputRfc3164)

  @Benchmark def rfc3164Message(): Either[PrintException, ByteString] =
    SyslogPrinterBench.Rfc3164Message.print(SyslogPrinterBench.InputRfc3164)

}

private object SyslogPrinterBench {

  val InputRfc5424: Json = Json.obj(
    SyslogTransformer.Rfc5424.Id.Facility -> 4,
    SyslogTransformer.Rfc5424.Id.Severity -> 2,
    SyslogTransformer.Rfc5424.Id.Timestamp -> "2003-10-11T22:14:15.003Z",
    SyslogTransformer.Rfc5424.Id.Hostname -> "mymachine.example.com",
    SyslogTransformer.Rfc5424.Id.AppName -> "streamy",
    SyslogTransformer.Rfc5424.Id.ProcId -> "77042",
    SyslogTransformer.Rfc5424.Id.MsgId -> "ID47",
    SyslogTransformer.Rfc5424.Id.Message -> "'su root' failed for lonvick on /dev/pts/8"
  )

  val InputRfc3164: Json = Json.obj(
    SyslogTransformer.Rfc3164.Id.Facility -> 4,
    SyslogTransformer.Rfc3164.Id.Severity -> 2,
    SyslogTransformer.Rfc3164.Id.Timestamp -> "2003-10-11T22:14:15.003Z",
    SyslogTransformer.Rfc3164.Id.Hostname -> "mymachine.example.com",
    SyslogTransformer.Rfc3164.Id.AppName -> "streamy",
    SyslogTransformer.Rfc3164.Id.ProcId -> "77042",
    SyslogTransformer.Rfc3164.Id.MsgId -> "ID47",
    SyslogTransformer.Rfc3164.Id.Message -> "'su root' failed for lonvick on /dev/pts/8"
  )

  val Rfc5424Complete: ByteStringPrinter[Json] =
    SyslogPrinter.rfc5424(Rfc5424.Config(
      binding = Rfc5424.Binding(
        facility = Some(SyslogTransformer.Rfc5424.Id.Facility),
        severity = Some(SyslogTransformer.Rfc5424.Id.Severity),
        timestamp = Some(SyslogTransformer.Rfc5424.Id.Timestamp),
        hostname = Some(SyslogTransformer.Rfc5424.Id.Hostname),
        appName = Some(SyslogTransformer.Rfc5424.Id.AppName),
        procId = Some(SyslogTransformer.Rfc5424.Id.ProcId),
        msgId = Some(SyslogTransformer.Rfc5424.Id.MsgId),
        structData = Some(SyslogTransformer.Rfc5424.Id.StructData),
        message = Some(SyslogTransformer.Rfc5424.Id.Message)
      )))

  val Rfc5424Message: ByteStringPrinter[Json] =
    SyslogPrinter.rfc5424(Rfc5424.Config(
      binding = Rfc5424.Binding(
        message = Some(SyslogTransformer.Rfc5424.Id.Message)
      )))

  val Rfc3164Complete: ByteStringPrinter[Json] =
    SyslogPrinter.rfc3164(Rfc3164.Config(
      binding = Rfc3164.Binding(
        facility = Some(SyslogTransformer.Rfc3164.Id.Facility),
        severity = Some(SyslogTransformer.Rfc3164.Id.Severity),
        timestamp = Some(SyslogTransformer.Rfc3164.Id.Timestamp),
        hostname = Some(SyslogTransformer.Rfc3164.Id.Hostname),
        appName = Some(SyslogTransformer.Rfc3164.Id.AppName),
        procId = Some(SyslogTransformer.Rfc3164.Id.ProcId),
        message = Some(SyslogTransformer.Rfc3164.Id.Message)
      )))

  val Rfc3164Message: ByteStringPrinter[Json] =
    SyslogPrinter.rfc3164(Rfc3164.Config(
      binding = Rfc3164.Binding(
        message = Some(SyslogTransformer.Rfc3164.Id.Message)
      )))

}
