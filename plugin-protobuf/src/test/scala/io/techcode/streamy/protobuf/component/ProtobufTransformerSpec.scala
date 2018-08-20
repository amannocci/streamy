/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2018
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
package io.techcode.streamy.protobuf.component

import akka.NotUsed
import akka.stream.scaladsl.{Flow, Framing}
import akka.util.ByteString
import io.techcode.streamy.component.TestTransformer
import io.techcode.streamy.protobuf.Data
import io.techcode.streamy.protobuf.Data.{Pkt, Pkts}
import io.techcode.streamy.util.json._

import scala.language.postfixOps

/**
  * Protobuf transformer spec.
  */
class ProtobufTransformerSpec extends TestTransformer {

  "Protobuf transformer" should {
    "parser data correctly" in {
      except[ByteString, Json](
        ProtobufTransformerSpec.Parser.Transformer.Simple,
        ProtobufTransformerSpec.Parser.Input.Simple,
        ProtobufTransformerSpec.Parser.Output.Simple
      )
    }

    "print data correctly" in {
      except[Json, ByteString](
        ProtobufTransformerSpec.Printer.Transformer.Simple,
        ProtobufTransformerSpec.Printer.Input.Simple,
        ProtobufTransformerSpec.Printer.Output.Simple
      )
    }
  }

}

object ProtobufTransformerSpec {

  object Parser {

    object Input {

      val Simple: ByteString = ByteString(Pkts.newBuilder().addPkt(Pkt.newBuilder().putAttrs("test", "test")).build().toByteArray)

    }

    object Transformer {

      val Simple: Flow[ByteString, Json, NotUsed] =
        Framing.simpleFramingProtocolEncoder(Int.MaxValue - 4)
          .via(ProtobufTransformer.parser(ProtobufTransformer.Parser.Config(
            proto = Data.Pkts.getDefaultInstance,
            decoder = (pkts: Pkts) => Json.obj("test" -> pkts.getPkt(0).getAttrsMap.get("test"))
          )))

    }

    object Output {

      val Simple: Json = Json.obj("test" -> "test")

    }

  }

  object Printer {

    object Input {

      val Simple: Json = Json.obj("test" -> "test")

    }

    object Transformer {

      val Simple: Flow[Json, ByteString, NotUsed] =
        ProtobufTransformer.printer(ProtobufTransformer.Printer.Config(
          proto = Data.Pkts.getDefaultInstance,
          encoder = (doc: Json) => Pkts.newBuilder().addPkt(Pkt.newBuilder().putAttrs("test", doc.evaluate(Root / "test").asString.get)).build()
        )).via(Framing.simpleFramingProtocolDecoder(Int.MaxValue - 4))
    }

    object Output {

      val Simple: ByteString = ByteString(Pkts.newBuilder().addPkt(Pkt.newBuilder().putAttrs("test", "test")).build().toByteArray)

    }

  }

}
