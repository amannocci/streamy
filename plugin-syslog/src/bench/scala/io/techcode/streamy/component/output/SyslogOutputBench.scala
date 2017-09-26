/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017
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
package io.techcode.streamy.component.output

import akka.util.ByteString
import io.techcode.streamy.component.output.SyslogOutput.{RFC3164Config, RFC5424Config}
import org.openjdk.jmh.annotations.Benchmark
import play.api.libs.json._

/**
  * Syslog output bench.
  *
  * Benchmark                                                    Mode  Cnt        Score      Error  Units
  * i.t.s.c.output.SyslogOutputBench.benchSimpleMessageRFC3164  thrpt  200  1174470,547 ± 4150,416  ops/s
  * i.t.s.c.output.SyslogOutputBench.benchSimpleMessageRFC5424  thrpt  200  1417185,753 ± 4276,020  ops/s
  * i.t.s.c.output.SyslogOutputBench.benchSimpleRFC3164         thrpt  200  1259505,547 ± 7281,476  ops/s
  * i.t.s.c.output.SyslogOutputBench.benchSimpleRFC5424         thrpt  200  1070430,041 ± 3007,258  ops/s
  */
class SyslogOutputBench {

  @Benchmark def benchSimpleRFC5424(): ByteString = {
    SyslogOutput.createRFC5424(RFC5424Config(
      facility = Some(SyslogOutput.Id.Facility),
      severity = Some(SyslogOutput.Id.Severity),
      timestamp = Some(SyslogOutput.Id.Timestamp),
      hostname = Some(SyslogOutput.Id.Hostname),
      app = Some(SyslogOutput.Id.App),
      proc = Some(SyslogOutput.Id.Proc),
      msgId = Some(SyslogOutput.Id.MsgId),
      message = Some(SyslogOutput.Id.Message)
    )).apply(SyslogOutputBench.Simple)
  }

  @Benchmark def benchSimpleMessageRFC5424(): ByteString = {
    SyslogOutput.createRFC5424(RFC5424Config(message = Some(SyslogOutput.Id.Message))).apply(SyslogOutputBench.Simple)
  }

  @Benchmark def benchSimpleRFC3164(): ByteString = {
    SyslogOutput.createRFC3164(RFC3164Config(
      facility = Some(SyslogOutput.Id.Facility),
      severity = Some(SyslogOutput.Id.Severity),
      timestamp = Some(SyslogOutput.Id.Timestamp),
      hostname = Some(SyslogOutput.Id.Hostname),
      app = Some(SyslogOutput.Id.App),
      proc = Some(SyslogOutput.Id.Proc),
      message = Some(SyslogOutput.Id.Message)
    )).apply(SyslogOutputBench.Simple)
  }

  @Benchmark def benchSimpleMessageRFC3164(): ByteString = {
    SyslogOutput.createRFC3164(RFC3164Config(message = Some(SyslogOutput.Id.Message))).apply(SyslogOutputBench.Simple)
  }

}

private[this] object SyslogOutputBench {
  val Simple: JsObject = Json.obj(
    SyslogOutput.Id.Facility -> 4,
    SyslogOutput.Id.Severity -> 2,
    SyslogOutput.Id.Timestamp -> "2003-10-11T22:14:15.003Z",
    SyslogOutput.Id.Hostname -> "mymachine.example.com",
    SyslogOutput.Id.App -> "streamy",
    SyslogOutput.Id.Proc -> "77042",
    SyslogOutput.Id.MsgId -> "ID47",
    SyslogOutput.Id.Message -> "'su root' failed for lonvick on /dev/pts/8"
  )
}
