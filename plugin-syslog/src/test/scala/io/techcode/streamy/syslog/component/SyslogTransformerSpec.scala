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

import java.net.InetAddress

import akka.NotUsed
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import io.techcode.streamy.component.TestTransformer
import io.techcode.streamy.event.StreamEvent
import io.techcode.streamy.syslog.component.SyslogTransformer.Framing
import io.techcode.streamy.syslog.component.SyslogTransformer.Rfc5424.Mode
import io.techcode.streamy.util.json._

/**
  * Syslog transformer spec.
  */
class SyslogTransformerSpec extends TestTransformer {

  "Syslog transformer" should {
    "parser Rfc3164 with delimiter handle correctly simple syslog message in strict mode" in {
      except(
        SyslogTransformerSpec.Rfc3164.Transformer.ParserStrictDelimiter,
        SyslogTransformerSpec.Rfc3164.Input.ParserSimpleDelimiter,
        SyslogTransformerSpec.Rfc3164.Output.ParserSimple
      )
    }

    "parser Rfc3164 with delimiter handle correctly simple syslog message in lenient mode" in {
      except(
        SyslogTransformerSpec.Rfc3164.Transformer.ParserLenientDelimiter,
        SyslogTransformerSpec.Rfc3164.Input.ParserSimpleDelimiter,
        SyslogTransformerSpec.Rfc3164.Output.ParserSimple
      )
    }

    "parser Rfc3164 with delimiter throw an error when syslog message is malformed" in {
      exceptError(
        SyslogTransformerSpec.Rfc3164.Transformer.ParserLenientDelimiter,
        SyslogTransformerSpec.Rfc3164.Input.ParserMalformedDelimiter
      )
    }

    "parser Rfc3164 with count handle correctly simple syslog message in strict mode" in {
      except(
        SyslogTransformerSpec.Rfc3164.Transformer.ParserStrictCount,
        SyslogTransformerSpec.Rfc3164.Input.ParserSimpleCount,
        SyslogTransformerSpec.Rfc3164.Output.ParserSimple
      )
    }

    "parser Rfc3164 with count handle correctly simple syslog message in lenient mode" in {
      except(
        SyslogTransformerSpec.Rfc3164.Transformer.ParserLenientCount,
        SyslogTransformerSpec.Rfc3164.Input.ParserSimpleCount,
        SyslogTransformerSpec.Rfc3164.Output.ParserSimple
      )
    }

    "parser Rfc3164 with count handle correctly syslog message in multiple parsing" in {
      except(
        SyslogTransformerSpec.Rfc3164.Transformer.ParserLenientCount,
        SyslogTransformerSpec.Rfc3164.Input.ParserSimpleCount.grouped(2),
        SyslogTransformerSpec.Rfc3164.Output.ParserSimple
      )
    }

    "parser Rfc3164 with count throw an error when syslog message is malformed" in {
      exceptError(
        SyslogTransformerSpec.Rfc3164.Transformer.ParserLenientCount,
        SyslogTransformerSpec.Rfc3164.Input.ParserMalformedCount
      )
    }

    "parser Rfc3164 with count throw an error when framing is malformed" in {
      exceptError(
        SyslogTransformerSpec.Rfc3164.Transformer.ParserLenientCount,
        SyslogTransformerSpec.Rfc3164.Input.ParserMalformedDelimiter
      )
    }

    "parser Rfc3164 with count throw an error when a syslog message is prefix by negative count" in {
      exceptError(
        SyslogTransformerSpec.Rfc3164.Transformer.ParserLenientCount,
        SyslogTransformerSpec.Rfc3164.Input.ParserMalformedCountNegative
      )
    }

    "parser Rfc3164 with count throw an error when a syslog message count is greater than max allowed" in {
      exceptError(
        SyslogTransformerSpec.Rfc3164.Transformer.ParserLenientCountMax,
        SyslogTransformerSpec.Rfc3164.Input.ParserMalformedCountMax
      )
    }

    "parser Rfc3164 with count throw an error when a prefix syslog message count is truncated" in {
      exceptError(
        SyslogTransformerSpec.Rfc3164.Transformer.ParserLenientCountMax,
        SyslogTransformerSpec.Rfc3164.Input.ParserTruncated
      )
    }

    "parser Rfc3164 with count throw an error when a prefix syslog message count is greater than max allowed" in {
      exceptError(
        SyslogTransformerSpec.Rfc3164.Transformer.ParserLenientCountMax,
        SyslogTransformerSpec.Rfc3164.Input.ParserMalformedPrefix
      )
    }

    "parser Rfc5424 with delimiter handle correctly simple syslog message in strict mode" in {
      except(
        SyslogTransformerSpec.Rfc5424.Transformer.ParserStrictDelimiter,
        SyslogTransformerSpec.Rfc5424.Input.ParserSimpleDelimiter,
        SyslogTransformerSpec.Rfc5424.Output.ParserSimple
      )
    }

    "parser Rfc5424 with delimiter handle correctly simple syslog message in lenient mode" in {
      except(
        SyslogTransformerSpec.Rfc5424.Transformer.ParserLenientDelimiter,
        SyslogTransformerSpec.Rfc5424.Input.ParserSimpleDelimiter,
        SyslogTransformerSpec.Rfc5424.Output.ParserSimple
      )
    }

    "parser Rfc5424 with delimiter handle correctly alternative syslog message" in {
      except(
        SyslogTransformerSpec.Rfc5424.Transformer.ParserLenientDelimiter,
        SyslogTransformerSpec.Rfc5424.Input.ParserAlternativeDelimiter,
        SyslogTransformerSpec.Rfc5424.Output.ParserAlternative
      )
    }

    "parser Rfc5424 with delimiter throw an error when syslog message is malformed" in {
      exceptError(
        SyslogTransformerSpec.Rfc5424.Transformer.ParserLenientDelimiter,
        SyslogTransformerSpec.Rfc5424.Input.ParserMalformedDelimiter
      )
    }

    "parser Rfc5424 with count handle correctly simple syslog message in strict mode" in {
      except(
        SyslogTransformerSpec.Rfc5424.Transformer.ParserStrictCount,
        SyslogTransformerSpec.Rfc5424.Input.ParserSimpleCount,
        SyslogTransformerSpec.Rfc5424.Output.ParserSimple
      )
    }

    "parser Rfc5424 with count handle correctly simple syslog message in lenient mode" in {
      except(
        SyslogTransformerSpec.Rfc5424.Transformer.ParserLenientCount,
        SyslogTransformerSpec.Rfc5424.Input.ParserSimpleCount,
        SyslogTransformerSpec.Rfc5424.Output.ParserSimple
      )
    }

    "parser Rfc5424 with count handle correctly syslog message in multiple parsing" in {
      except(
        SyslogTransformerSpec.Rfc5424.Transformer.ParserLenientCount,
        SyslogTransformerSpec.Rfc5424.Input.ParserAlternativeCount.grouped(2),
        SyslogTransformerSpec.Rfc5424.Output.ParserAlternative
      )
    }

    "parser Rfc5424 with count handle correctly alternative syslog message" in {
      except(
        SyslogTransformerSpec.Rfc5424.Transformer.ParserLenientCount,
        SyslogTransformerSpec.Rfc5424.Input.ParserAlternativeCount,
        SyslogTransformerSpec.Rfc5424.Output.ParserAlternative
      )
    }

    "parser Rfc5424 with count throw an error when syslog message is malformed" in {
      exceptError(
        SyslogTransformerSpec.Rfc5424.Transformer.ParserLenientCount,
        SyslogTransformerSpec.Rfc5424.Input.ParserMalformedCount
      )
    }

    "parser Rfc5424 with count throw an error when framing is malformed" in {
      exceptError(
        SyslogTransformerSpec.Rfc5424.Transformer.ParserLenientCount,
        SyslogTransformerSpec.Rfc5424.Input.ParserMalformedDelimiter
      )
    }

    "parser Rfc5424 with count throw an error when a syslog message is prefix by negative count" in {
      exceptError(
        SyslogTransformerSpec.Rfc5424.Transformer.ParserLenientCount,
        SyslogTransformerSpec.Rfc5424.Input.ParserMalformedCountNegative
      )
    }

    "parser Rfc5424 with count throw an error when a syslog message count is greater than max allowed" in {
      exceptError(
        SyslogTransformerSpec.Rfc5424.Transformer.ParserLenientCountMax,
        SyslogTransformerSpec.Rfc5424.Input.ParserMalformedCountMax
      )
    }

    "parser Rfc5424 with count throw an error when a prefix syslog message count is truncated" in {
      exceptError(
        SyslogTransformerSpec.Rfc5424.Transformer.ParserLenientCountMax,
        SyslogTransformerSpec.Rfc5424.Input.ParserTruncated
      )
    }

    "parser Rfc5424 with count throw an error when a prefix syslog message count is greater than max allowed" in {
      exceptError(
        SyslogTransformerSpec.Rfc5424.Transformer.ParserLenientCountMax,
        SyslogTransformerSpec.Rfc5424.Input.ParserMalformedPrefix
      )
    }

    "printer Rfc3164 with frame delimiter" in {
      except(
        SyslogTransformerSpec.Rfc3164.Transformer.PrinterDelimiter,
        SyslogTransformerSpec.Rfc3164.Input.PrinterSimple,
        SyslogTransformerSpec.Rfc3164.Output.PrinterSimpleDelimiter
      )
    }

    "printer Rfc3164 with frame count" in {
      except(
        SyslogTransformerSpec.Rfc3164.Transformer.PrinterCount,
        SyslogTransformerSpec.Rfc3164.Input.PrinterSimple,
        SyslogTransformerSpec.Rfc3164.Output.PrinterSimpleCount
      )
    }

    "printer Rfc5424 with frame delimiter" in {
      except(
        SyslogTransformerSpec.Rfc5424.Transformer.PrinterDelimiter,
        SyslogTransformerSpec.Rfc5424.Input.PrinterSimple,
        SyslogTransformerSpec.Rfc5424.Output.PrinterSimpleDelimiter
      )
    }

    "printer Rfc5424 with frame count" in {
      except(
        SyslogTransformerSpec.Rfc5424.Transformer.PrinterCount,
        SyslogTransformerSpec.Rfc5424.Input.PrinterSimple,
        SyslogTransformerSpec.Rfc5424.Output.PrinterSimpleCount
      )
    }

    "printer Rfc5424 with default values" in {
      except(
        SyslogTransformerSpec.Rfc5424.Transformer.PrinterDefault,
        SyslogTransformerSpec.Rfc5424.Input.PrinterSimple,
        SyslogTransformerSpec.Rfc5424.Output.PrinterDefault
      )
    }
  }

}

object SyslogTransformerSpec {

  val Hostname: String = InetAddress.getLocalHost.getHostName

  def framingDelimiter(data: ByteString): ByteString = {
    data ++ ByteString('\n')
  }

  def framingCount(data: ByteString): ByteString = {
    val count = ByteString(data.length.toString)
    count ++ ByteString(' ') ++ data
  }

  object Rfc3164 {

    object Input {

      val ParserSimple: ByteString = ByteString("""<34>Apr  4 13:51:20 mymachine.example.com su[77042]: 'su root' failed for lonvick on /dev/pts/8""")
      val ParserSimpleDelimiter: ByteString = framingDelimiter(ParserSimple)
      val ParserSimpleCount: ByteString = framingCount(ParserSimple)

      val ParserTruncated: ByteString = ByteString("""200 <34>Apr  4 13:51:20 mymachine.example.com su[77042]: 'su root' failed for lonvick on /dev/pts/8""")
      val ParserMalformed: ByteString = ByteString("""<34> Apr 24 13:51:20 mymachine.example.com su[77042]: 'su root' failed for lonvick on /dev/pts/8""")
      val ParserMalformedDelimiter: ByteString = framingDelimiter(ParserMalformed)
      val ParserMalformedCount: ByteString = framingCount(ParserMalformed)
      val ParserMalformedPrefix: ByteString = ByteString("1000000")
      val ParserMalformedCountMax: ByteString = ParserMalformedPrefix ++ framingCount(ParserSimple)
      val ParserMalformedCountNegative: ByteString = ByteString("-") ++ framingCount(ParserSimple)

      val PrinterSimple: StreamEvent = StreamEvent(Json.obj(
        SyslogTransformer.Rfc3164.Id.Facility -> 4,
        SyslogTransformer.Rfc3164.Id.Severity -> 2,
        SyslogTransformer.Rfc3164.Id.Timestamp -> "Aug 24 05:34:00",
        SyslogTransformer.Rfc3164.Id.Hostname -> "mymachine.example.com",
        SyslogTransformer.Rfc3164.Id.AppName -> "su",
        SyslogTransformer.Rfc3164.Id.ProcId -> "77042",
        SyslogTransformer.Rfc3164.Id.MsgId -> "ID47",
        SyslogTransformer.Rfc3164.Id.Message -> "'su root' failed for lonvick on /dev/pts/8"
      ))

    }

    object Transformer {
      val Binding = SyslogTransformer.Rfc3164.Binding(
        facility = Some(SyslogTransformer.Rfc3164.Id.Facility),
        severity = Some(SyslogTransformer.Rfc3164.Id.Severity),
        timestamp = Some(SyslogTransformer.Rfc3164.Id.Timestamp),
        hostname = Some(SyslogTransformer.Rfc3164.Id.Hostname),
        appName = Some(SyslogTransformer.Rfc3164.Id.AppName),
        procId = Some(SyslogTransformer.Rfc3164.Id.ProcId),
        message = Some(SyslogTransformer.Rfc3164.Id.Message)
      )

      val ParserStrictDelimiter: Flow[ByteString, StreamEvent, NotUsed] = SyslogTransformer.parser(SyslogTransformer.Rfc3164.Config(
        binding = Binding
      ))

      val ParserLenientDelimiter: Flow[ByteString, StreamEvent, NotUsed] = SyslogTransformer.parser(SyslogTransformer.Rfc3164.Config(
        mode = SyslogTransformer.Rfc3164.Mode.Lenient,
        binding = Binding
      ))

      val ParserStrictCount: Flow[ByteString, StreamEvent, NotUsed] = SyslogTransformer.parser(SyslogTransformer.Rfc3164.Config(
        framing = Framing.Count,
        binding = Binding
      ))

      val ParserLenientCount: Flow[ByteString, StreamEvent, NotUsed] = SyslogTransformer.parser(SyslogTransformer.Rfc3164.Config(
        mode = SyslogTransformer.Rfc3164.Mode.Lenient,
        framing = Framing.Count,
        binding = Binding
      ))

      val ParserLenientCountMax: Flow[ByteString, StreamEvent, NotUsed] = SyslogTransformer.parser(SyslogTransformer.Rfc3164.Config(
        mode = SyslogTransformer.Rfc3164.Mode.Lenient,
        framing = Framing.Count,
        maxSize = 1024,
        binding = Binding
      ))

      val PrinterDelimiter: Flow[StreamEvent, ByteString, NotUsed] = SyslogTransformer.printer[NotUsed](SyslogTransformer.Rfc3164.Config(
        binding = Binding
      ))

      val PrinterCount: Flow[StreamEvent, ByteString, NotUsed] = SyslogTransformer.printer[NotUsed](SyslogTransformer.Rfc3164.Config(
        framing = Framing.Count,
        binding = Binding
      ))

    }

    object Output {

      val ParserSimple: StreamEvent = StreamEvent(Json.obj(
        SyslogTransformer.Rfc3164.Id.Facility -> 4,
        SyslogTransformer.Rfc3164.Id.Severity -> 2,
        SyslogTransformer.Rfc3164.Id.Timestamp -> "Apr  4 13:51:20",
        SyslogTransformer.Rfc3164.Id.Hostname -> "mymachine.example.com",
        SyslogTransformer.Rfc3164.Id.AppName -> "su",
        SyslogTransformer.Rfc3164.Id.ProcId -> "77042",
        SyslogTransformer.Rfc3164.Id.Message -> "'su root' failed for lonvick on /dev/pts/8"
      ))

      val PrinterSimple: ByteString = ByteString("<34>Aug 24 05:34:00 mymachine.example.com su[77042]: 'su root' failed for lonvick on /dev/pts/8")

      val PrinterSimpleDelimiter: ByteString = framingDelimiter(PrinterSimple)

      val PrinterSimpleCount: ByteString = framingCount(PrinterSimple)

    }

  }

  object Rfc5424 {

    object Input {

      val ParserSimple: ByteString = ByteString("""<34>1 2003-10-11T22:14:15.003Z mymachine.example.com su 77042 ID47 [sigSig ver="1"] 'su root' failed for lonvick on /dev/pts/8""")
      val ParserSimpleDelimiter: ByteString = framingDelimiter(ParserSimple)
      val ParserSimpleCount: ByteString = framingCount(ParserSimple)

      val ParserAlternative: ByteString = ByteString("""<34>1 1985-04-12T19:20:50.52-04:00 mymachine.example.com su 77042 ID47 [sigSig ver="1"] 'su root' failed for lonvick on /dev/pts/8""")
      val ParserAlternativeDelimiter: ByteString = framingDelimiter(ParserAlternative)
      val ParserAlternativeCount: ByteString = framingCount(ParserAlternative)

      val ParserTruncated: ByteString = ByteString("""200 <34>1 2003-10-11T22:14:15.003Z mymachine.example.com su 77042 ID47 [sigSig ver="1"] 'su root' failed for lonvick on /dev/pts/8""")
      val ParserMalformed: ByteString = ByteString("""<34> 2003-10-11T22:14:15.003Z mymachine.example.com su 77042 ID47 [sigSig ver="1"] 'su root' failed for lonvick on /dev/pts/8""")
      val ParserMalformedDelimiter: ByteString = framingDelimiter(ParserMalformed)
      val ParserMalformedCount: ByteString = framingCount(ParserMalformed)
      val ParserMalformedPrefix: ByteString = ByteString("1000000")
      val ParserMalformedCountMax: ByteString = ParserMalformedPrefix ++ framingCount(ParserSimple)
      val ParserMalformedCountNegative: ByteString = ByteString("-") ++ framingCount(ParserSimple)
      val PrinterSimple: StreamEvent = StreamEvent(Json.obj(
        SyslogTransformer.Rfc5424.Id.Facility -> 4,
        SyslogTransformer.Rfc5424.Id.Severity -> 2,
        SyslogTransformer.Rfc5424.Id.Timestamp -> "2003-10-11T22:14:15.003Z",
        SyslogTransformer.Rfc5424.Id.Hostname -> "mymachine.example.com",
        SyslogTransformer.Rfc5424.Id.AppName -> "su",
        SyslogTransformer.Rfc5424.Id.ProcId -> "77042",
        SyslogTransformer.Rfc5424.Id.MsgId -> "ID47",
        SyslogTransformer.Rfc5424.Id.Message -> "'su root' failed for lonvick on /dev/pts/8"
      ))

    }

    object Transformer {

      private val Binding = SyslogTransformer.Rfc5424.Binding(
        facility = Some(SyslogTransformer.Rfc5424.Id.Facility),
        severity = Some(SyslogTransformer.Rfc5424.Id.Severity),
        timestamp = Some(SyslogTransformer.Rfc5424.Id.Timestamp),
        hostname = Some(SyslogTransformer.Rfc5424.Id.Hostname),
        appName = Some(SyslogTransformer.Rfc5424.Id.AppName),
        procId = Some(SyslogTransformer.Rfc5424.Id.ProcId),
        msgId = Some(SyslogTransformer.Rfc5424.Id.MsgId),
        structData = Some(SyslogTransformer.Rfc5424.Id.StructData),
        message = Some(SyslogTransformer.Rfc5424.Id.Message)
      )

      val ParserStrictDelimiter: Flow[ByteString, StreamEvent, NotUsed] = SyslogTransformer.parser(SyslogTransformer.Rfc5424.Config(
        binding = Binding
      ))

      val ParserLenientDelimiter: Flow[ByteString, StreamEvent, NotUsed] = SyslogTransformer.parser(SyslogTransformer.Rfc5424.Config(
        mode = Mode.Lenient,
        binding = Binding
      ))

      val ParserStrictCount: Flow[ByteString, StreamEvent, NotUsed] = SyslogTransformer.parser(SyslogTransformer.Rfc5424.Config(
        framing = Framing.Count,
        binding = Binding
      ))

      val ParserLenientCount: Flow[ByteString, StreamEvent, NotUsed] = SyslogTransformer.parser(SyslogTransformer.Rfc5424.Config(
        mode = Mode.Lenient,
        framing = Framing.Count,
        binding = Binding
      ))

      val ParserLenientCountMax: Flow[ByteString, StreamEvent, NotUsed] = SyslogTransformer.parser(SyslogTransformer.Rfc5424.Config(
        mode = Mode.Lenient,
        framing = Framing.Count,
        maxSize = 1024,
        binding = Binding
      ))

      val PrinterDelimiter: Flow[StreamEvent, ByteString, NotUsed] = SyslogTransformer.printer[NotUsed](SyslogTransformer.Rfc5424.Config(
        binding = Binding
      ))

      val PrinterCount: Flow[StreamEvent, ByteString, NotUsed] = SyslogTransformer.printer[NotUsed](SyslogTransformer.Rfc5424.Config(
        framing = Framing.Count,
        binding = Binding
      ))

      val PrinterDefault: Flow[StreamEvent, ByteString, NotUsed] = SyslogTransformer.printer[NotUsed](SyslogTransformer.Rfc5424.Config(
        binding = Binding.copy(
          facility = None,
          severity = None,
          procId = None
        )
      ))

    }

    object Output {

      val ParserSimple: StreamEvent = StreamEvent(Json.obj(
        SyslogTransformer.Rfc5424.Id.Facility -> 4,
        SyslogTransformer.Rfc5424.Id.Severity -> 2,
        SyslogTransformer.Rfc5424.Id.Timestamp -> "2003-10-11T22:14:15.003Z",
        SyslogTransformer.Rfc5424.Id.Hostname -> "mymachine.example.com",
        SyslogTransformer.Rfc5424.Id.AppName -> "su",
        SyslogTransformer.Rfc5424.Id.ProcId -> "77042",
        SyslogTransformer.Rfc5424.Id.MsgId -> "ID47",
        SyslogTransformer.Rfc5424.Id.StructData -> Json.obj(
          "ver" -> "1"
        ),
        SyslogTransformer.Rfc5424.Id.Message -> "'su root' failed for lonvick on /dev/pts/8"
      ))

      val ParserAlternative: StreamEvent = StreamEvent(ParserSimple.payload.patch(Replace(Root / SyslogTransformer.Rfc5424.Id.Timestamp, "1985-04-12T19:20:50.52-04:00")).get[Json])

      val PrinterSimple: ByteString = ByteString("<34>1 2003-10-11T22:14:15.003Z mymachine.example.com su 77042 ID47 - 'su root' failed for lonvick on /dev/pts/8")

      val PrinterSimpleDelimiter: ByteString = framingDelimiter(PrinterSimple)

      val PrinterDefault: ByteString = framingDelimiter(ByteString("<30>1 2003-10-11T22:14:15.003Z mymachine.example.com su - ID47 - 'su root' failed for lonvick on /dev/pts/8"))

      val PrinterSimpleCount: ByteString = framingCount(PrinterSimple)

    }

  }


}