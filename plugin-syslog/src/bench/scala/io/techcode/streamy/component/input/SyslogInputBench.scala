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
package io.techcode.streamy.component.input

import akka.util.ByteString
import io.techcode.streamy.component.input.SyslogInput.RFC5424Config
import io.techcode.streamy.stream.StreamException
import org.openjdk.jmh.annotations.Benchmark
import play.api.libs.json._

/**
  * Syslog input bench.
  *
  * Benchmark                                                    Mode  Cnt        Score       Error  Units
  * i.t.s.c.input.SyslogInputBench.benchSimpleFailureRFC5424    thrpt  200  1901373,090 ± 14922,741  ops/s
  * i.t.s.c.input.SyslogInputBench.benchSimpleMessageRFC5424    thrpt  200  2316509,137 ±  8539,845  ops/s
  * i.t.s.c.input.SyslogInputBench.benchSimpleRFC5424           thrpt  200   774565,181 ±  5865,947  ops/s
  */
class SyslogInputBench {

  @Benchmark def benchSimpleRFC5424(): JsObject = {
    SyslogInput.createRFC5424(RFC5424Config(
      facility = Some(SyslogInput.Id.Facility),
      severity = Some(SyslogInput.Id.Severity),
      timestamp = Some(SyslogInput.Id.Timestamp),
      hostname = Some(SyslogInput.Id.Hostname),
      app = Some(SyslogInput.Id.App),
      proc = Some(SyslogInput.Id.Proc),
      msgId = Some(SyslogInput.Id.MsgId),
      structData = Some(SyslogInput.Id.StructData),
      message = Some(SyslogInput.Id.Message)
    )).apply(SyslogInputBench.Simple)
  }

  @Benchmark def benchSimpleMessageRFC5424(): JsObject = {
    SyslogInput.createRFC5424(RFC5424Config(message = Some(SyslogInput.Id.Message))).apply(SyslogInputBench.Simple)
  }

  @Benchmark def benchSimpleFailureRFC5424(): JsObject = {
    try {
      SyslogInput.createRFC5424(RFC5424Config()).apply(SyslogInputBench.Failure)
    } catch {
      case _: StreamException => Json.obj()
    }
  }

}

private[this] object SyslogInputBench {
  val Simple = ByteString("""<34>1 2003-10-11T22:14:15.003Z mymachine.example.com su 77042 ID47 [sigSig ver="1"] 'su root' failed for lonvick on /dev/pts/8""")
  val Failure = ByteString("""<34> 2003-10-11T22:14:15.003Z mymachine.example.com su 77042 ID47 [sigSig ver="1"] 'su root' failed for lonvick on /dev/pts/8""")
}
