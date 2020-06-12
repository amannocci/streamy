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
package io.techcode.streamy.syslog.util.parser

import akka.util.ByteString
import io.techcode.streamy.syslog.component.SyslogTransformer
import io.techcode.streamy.util.json._
import io.techcode.streamy.util.parser.{ByteStringParser, ParseException}
import org.openjdk.jmh.annotations._

/**
  * Syslog parser bench.
  */
class SyslogParserBench {

  @Benchmark def rfc5424Complete(): Either[ParseException, Json] = SyslogParserBench.Rfc5424Complete.parse(SyslogParserBench.InputRfc5424)

  @Benchmark def rfc5424Message(): Either[ParseException, Json] = SyslogParserBench.Rfc5424Message.parse(SyslogParserBench.InputRfc5424)

  @Benchmark def rfc5424Failure(): Either[ParseException, Json] = SyslogParserBench.Rfc5424Failure.parse(SyslogParserBench.InputMalformed)

}

private object SyslogParserBench {

  val InputRfc5424 = ByteString("""<34>1 2003-10-11T22:14:15.003Z mymachine.example.com su 77042 ID47 [sigSig ver="1"] 'su root' failed for lonvick on /dev/pts/8""")

  val InputMalformed = ByteString("""4400672761""")

  val Rfc5424Complete: ByteStringParser[Json] =
    SyslogParser.rfc5424(SyslogTransformer.Rfc5424.Config(binding = SyslogTransformer.Rfc5424.Binding(
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


  val Rfc5424Message: ByteStringParser[Json] =
    SyslogParser.rfc5424(SyslogTransformer.Rfc5424.Config(binding = SyslogTransformer.Rfc5424.Binding(
      message = Some(SyslogTransformer.Rfc5424.Id.Message)
    )))

  val Rfc5424Failure: ByteStringParser[Json] =
    SyslogParser.rfc5424(SyslogTransformer.Rfc5424.Config(binding = SyslogTransformer.Rfc5424.Binding()))

}
