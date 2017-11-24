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
package io.techcode.streamy.component.transformer

import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.stream.testkit.scaladsl.TestSink
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.testkit.{ImplicitSender, TestKit}
import akka.util.ByteString
import io.techcode.streamy.util.json.{Json, intToJson}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
  * Syslog transformer spec.
  */
class SyslogTransformerSpec extends TestKit(ActorSystem("SyslogTransformerSpec"))
  with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll {

  implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(system))

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  "Syslog transformer" should {
    "be used in a flow RFC5424 compliant to read record" in {
      Source.single(SyslogInputSpec.Simple)
        .via(SyslogTransformer.rfc5424(SyslogInputSpec.CaptureFacility))
        .runWith(TestSink.probe[Json])
        .requestNext() should equal(Json.obj(SyslogInput.Id.Facility -> intToJson(4)))
    }

    "be used in a flow RFC5424 compliant to write record" in {
      Source.single(SyslogOutputSpec.RFC5424Simple)
        .via(SyslogTransformer.rfc5424(SyslogOutputSpec.RFC5424FormatAll))
        .runWith(TestSink.probe[ByteString])
        .requestNext() should equal(ByteString("<34>1 2003-10-11T22:14:15.003Z mymachine.example.com su 77042 ID47 - 'su root' failed for lonvick on /dev/pts/8"))
    }

    "be used in a flow RFC3164 compliant to write record" in {
      Source.single(SyslogOutputSpec.RFC3164Simple)
        .via(SyslogTransformer.rfc3164(SyslogOutputSpec.RFC3164FormatAll))
        .runWith(TestSink.probe[ByteString])
        .requestNext() should equal(ByteString("<34>Aug 24 05:34:00 mymachine.example.com su[77042]: 'su root' failed for lonvick on /dev/pts/8\n"))
    }
  }

}
