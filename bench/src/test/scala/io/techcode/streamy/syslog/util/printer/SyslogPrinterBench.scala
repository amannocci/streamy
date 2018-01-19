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

import akka.util.ByteString
import io.techcode.streamy.component.SinkTransformer
import io.techcode.streamy.syslog.component.transformer.SyslogTransformer
import io.techcode.streamy.syslog.component.transformer.SyslogTransformer.{Rfc3164, Rfc5424}
import io.techcode.streamy.util.json._
import io.techcode.streamy.util.parser.{BytesBinder, StringBinder}
import org.openjdk.jmh.annotations.Benchmark

/**
  * Syslog printer bench.
  *
  * Benchmark                            Mode  Cnt        Score      Error  Units
  * SyslogPrinterBench.rfc3164Complete  thrpt   20  1039837,588 ± 1659,463  ops/s
  * SyslogPrinterBench.rfc3164Message   thrpt   20  1703064,583 ± 1944,598  ops/s
  * SyslogPrinterBench.rfc5424Complete  thrpt   20   869605,047 ±  745,519  ops/s
  * SyslogPrinterBench.rfc5424Message   thrpt   20  1575703,087 ± 2507,885  ops/s
  */
class SyslogPrinterBench {

  @Benchmark def rfc5424Complete(): ByteString =
    SyslogPrinterBench.Rfc5424Complete(SyslogPrinterBench.InputRfc5424)

  @Benchmark def rfc5424Message(): ByteString =
    SyslogPrinterBench.Rfc5424Message(SyslogPrinterBench.InputRfc5424)

  @Benchmark def rfc3164Complete(): ByteString =
    SyslogPrinterBench.Rfc3164Complete(SyslogPrinterBench.InputRfc3164)

  @Benchmark def rfc3164Message(): ByteString =
    SyslogPrinterBench.Rfc3164Message(SyslogPrinterBench.InputRfc3164)

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

  val Rfc5424Complete: SinkTransformer = (pkt: Json) =>
    SyslogPrinter.rfc5424(pkt, Rfc5424.Binding(
      facility = Some(StringBinder(SyslogTransformer.Rfc5424.Id.Facility)),
      severity = Some(StringBinder(SyslogTransformer.Rfc5424.Id.Severity)),
      timestamp = Some(StringBinder(SyslogTransformer.Rfc5424.Id.Timestamp)),
      hostname = Some(StringBinder(SyslogTransformer.Rfc5424.Id.Hostname)),
      appName = Some(StringBinder(SyslogTransformer.Rfc5424.Id.AppName)),
      procId = Some(StringBinder(SyslogTransformer.Rfc5424.Id.ProcId)),
      msgId = Some(StringBinder(SyslogTransformer.Rfc5424.Id.MsgId)),
      structData = Some(StringBinder(SyslogTransformer.Rfc5424.Id.StructData)),
      message = Some(BytesBinder(SyslogTransformer.Rfc5424.Id.Message))
    ))

  val Rfc5424Message: SinkTransformer = (pkt: Json) =>
    SyslogPrinter.rfc5424(pkt, Rfc5424.Binding(
      message = Some(BytesBinder(SyslogTransformer.Rfc5424.Id.Message))
    ))

  val Rfc3164Complete: SinkTransformer = (pkt: Json) =>
    SyslogPrinter.rfc3164(pkt, Rfc3164.Binding(
      facility = Some(StringBinder(SyslogTransformer.Rfc3164.Id.Facility)),
      severity = Some(StringBinder(SyslogTransformer.Rfc3164.Id.Severity)),
      timestamp = Some(StringBinder(SyslogTransformer.Rfc3164.Id.Timestamp)),
      hostname = Some(StringBinder(SyslogTransformer.Rfc3164.Id.Hostname)),
      appName = Some(StringBinder(SyslogTransformer.Rfc3164.Id.AppName)),
      procId = Some(StringBinder(SyslogTransformer.Rfc3164.Id.ProcId)),
      message = Some(BytesBinder(SyslogTransformer.Rfc3164.Id.Message))
    ))

  val Rfc3164Message: SinkTransformer = (pkt: Json) =>
    SyslogPrinter.rfc3164(pkt, Rfc3164.Binding(
      message = Some(BytesBinder(SyslogTransformer.Rfc3164.Id.Message))
    ))

}
