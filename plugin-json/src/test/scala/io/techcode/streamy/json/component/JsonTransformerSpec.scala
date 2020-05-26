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
package io.techcode.streamy.json.component

import akka.NotUsed
import akka.util.ByteString
import io.techcode.streamy.component.TestTransformer
import io.techcode.streamy.event.StreamEvent
import io.techcode.streamy.json.component.JsonTransformer.{Bind, Config, Mode}
import io.techcode.streamy.util.json.{Json, _}

/**
  * Json transformer spec.
  */
class JsonTransformerSpec extends TestTransformer {

  "Json transformer" should {
    "deserialize correctly a packet inplace from string" in {
      except(
        JsonTransformerSpec.Transformer.DeserializeSource,
        JsonTransformerSpec.Input.DeserializeInplace,
        JsonTransformerSpec.Output.DeserializeInplace
      )
    }

    "deserialize correctly a packet with a root target from string" in {
      except(
        JsonTransformerSpec.Transformer.DeserializeSourceToRoot,
        JsonTransformerSpec.Input.DeserializeSourceToRoot,
        JsonTransformerSpec.Output.DeserializeSourceToRoot
      )
    }

    "deserialize correctly a packet with a root target equal to an existing field from string" in {
      except(
        JsonTransformerSpec.Transformer.DeserializeSourceToExistingRoot,
        JsonTransformerSpec.Input.DeserializeSourceToExistingRoot,
        JsonTransformerSpec.Output.DeserializeSourceToExistingRoot
      )
    }

    "fast skip correctly a packet with an empty source field from string" in {
      except(
        JsonTransformerSpec.Transformer.DeserializeSource,
        JsonTransformerSpec.Input.SkipEmptyStringSource,
        JsonTransformerSpec.Input.SkipEmptyStringSource
      )
    }

    "fast skip correctly a packet with a wrong source field from string" in {
      except(
        JsonTransformerSpec.Transformer.DeserializeSource,
        JsonTransformerSpec.Input.SkipStringSource,
        JsonTransformerSpec.Input.SkipStringSource
      )
    }

    "skip correctly a packet with a wrong source field from string" in {
      except(
        JsonTransformerSpec.Transformer.DeserializeSource,
        JsonTransformerSpec.Input.SkipWrongJsonSource,
        JsonTransformerSpec.Input.SkipWrongJsonSource
      )
    }

    "deserialize correctly a packet inplace from bytestring" in {
      except(
        JsonTransformerSpec.Transformer.DeserializeSourceBytes,
        JsonTransformerSpec.Input.DeserializeInplaceByteString,
        JsonTransformerSpec.Output.DeserializeInplace
      )
    }

    "deserialize correctly a packet with a root target from bytestring" in {
      except(
        JsonTransformerSpec.Transformer.DeserializeSourceToRootBytes,
        JsonTransformerSpec.Input.DeserializeSourceToRootByteString,
        JsonTransformerSpec.Output.DeserializeSourceToRootByteString
      )
    }

    "deserialize correctly a packet with a root target equal to an existing field from bytestring" in {
      except(
        JsonTransformerSpec.Transformer.DeserializeSourceToExistingRootBytes,
        JsonTransformerSpec.Input.DeserializeSourceToExistingRootByteString,
        JsonTransformerSpec.Output.DeserializeSourceToExistingRoot
      )
    }

    "fast skip correctly a packet with an empty source field from bytestring" in {
      except(
        JsonTransformerSpec.Transformer.DeserializeSourceBytes,
        JsonTransformerSpec.Input.SkipEmptyByteStringSource,
        JsonTransformerSpec.Input.SkipEmptyByteStringSource
      )
    }

    "fast skip correctly a packet with a wrong source field from bytestring" in {
      except(
        JsonTransformerSpec.Transformer.DeserializeSourceBytes,
        JsonTransformerSpec.Input.SkipByteStringSource,
        JsonTransformerSpec.Input.SkipByteStringSource
      )
    }

    "skip correctly a packet with a wrong source field from bytestring" in {
      except(
        JsonTransformerSpec.Transformer.DeserializeSourceBytes,
        JsonTransformerSpec.Input.SkipWrongJsonByteStringSource,
        JsonTransformerSpec.Input.SkipWrongJsonByteStringSource
      )
    }

    "serialize correctly a packet inplace" in {
      except(
        JsonTransformerSpec.Transformer.SerializeSource,
        JsonTransformerSpec.Input.SerializeInplace,
        JsonTransformerSpec.Output.SerializeInplace
      )
    }

    "serialize correctly a packet inplace with bytes input" in {
      except(
        JsonTransformerSpec.Transformer.SerializeSourceBytes,
        JsonTransformerSpec.Input.SerializeInplace,
        JsonTransformerSpec.Output.SerializeInplaceBytes
      )
    }

    "serialize correctly a packet with a root source" in {
      except(
        JsonTransformerSpec.Transformer.SerializeRootToTarget,
        JsonTransformerSpec.Input.SerializeRootToTarget,
        JsonTransformerSpec.Output.SerializeRootToTarget
      )
    }

    "serialize correctly a packet with a root target equal to an existing field" in {
      except(
        JsonTransformerSpec.Transformer.SerializeRootToExistingTarget,
        JsonTransformerSpec.Input.SerializeRootToExistingTarget,
        JsonTransformerSpec.Output.SerializeRootToExistingTarget
      )
    }

    "handle correctly unexpected input when expected string" in {
      except(
        JsonTransformerSpec.Transformer.WrongTypeString,
        JsonTransformerSpec.Input.WrongType,
        JsonTransformerSpec.Input.WrongType
      )
    }

    "handle correctly unexpected input when expected bytes" in {
      except(
        JsonTransformerSpec.Transformer.WrongTypeBytes,
        JsonTransformerSpec.Input.WrongType,
        JsonTransformerSpec.Input.WrongType
      )
    }
  }

}


object JsonTransformerSpec {

  object Input {

    val SkipEmptyStringSource: StreamEvent = StreamEvent(Json.obj("message" -> ""))

    val SkipStringSource: StreamEvent = StreamEvent(Json.obj("message" -> "foobar"))

    val SkipWrongJsonSource: StreamEvent = StreamEvent(Json.obj("message" -> "{foobar}"))

    val SkipEmptyByteStringSource: StreamEvent = StreamEvent(Json.obj("message" -> ByteString()))

    val SkipByteStringSource: StreamEvent = StreamEvent(Json.obj("message" -> ByteString("foobar")))

    val SkipWrongJsonByteStringSource: StreamEvent = StreamEvent(Json.obj("message" -> ByteString("{foobar}")))

    val DeserializeInplace: StreamEvent = StreamEvent(Json.obj("message" -> """{"message":"foobar"}"""))

    val DeserializeSourceToRoot: StreamEvent = StreamEvent(Json.obj("message" -> """{"test":"foobar"}"""))

    val DeserializeSourceToExistingRoot: StreamEvent = StreamEvent(Json.obj("message" -> """{"message":"foobar"}"""))

    val DeserializeInplaceByteString: StreamEvent = StreamEvent(Json.obj("message" -> ByteString("""{"message":"foobar"}""")))

    val DeserializeSourceToRootByteString: StreamEvent = StreamEvent(Json.obj("message" -> ByteString("""{"test":"foobar"}""")))

    val DeserializeSourceToExistingRootByteString: StreamEvent = StreamEvent(Json.obj("message" -> ByteString("""{"message":"foobar"}""")))

    val SerializeInplace: StreamEvent = StreamEvent(Json.obj("message" -> Json.obj("message" -> "foobar")))

    val SerializeRootToTarget: StreamEvent = StreamEvent(Json.obj("test" -> "foobar"))

    val SerializeRootToExistingTarget: StreamEvent = StreamEvent(Json.obj("test" -> "foobar"))

    val WrongType: StreamEvent = StreamEvent(Json.obj("test" -> 10))

  }

  object Transformer {

    val DeserializeSource = JsonTransformer[NotUsed](Config(Root / "message", mode = Mode.Deserialize))

    val DeserializeSourceBytes = JsonTransformer[NotUsed](Config(Root / "message", mode = Mode.Deserialize, bind = Bind.Bytes))

    val DeserializeSourceToRoot = JsonTransformer[NotUsed](Config(Root / "message", Some(Root), mode = Mode.Deserialize))

    val DeserializeSourceToRootBytes = JsonTransformer[NotUsed](Config(Root / "message", Some(Root), mode = Mode.Deserialize, bind = Bind.Bytes))

    val DeserializeSourceToExistingRoot = JsonTransformer[NotUsed](Config(Root / "message", Some(Root), mode = Mode.Deserialize))

    val DeserializeSourceToExistingRootBytes = JsonTransformer[NotUsed](Config(Root / "message", Some(Root), mode = Mode.Deserialize, bind = Bind.Bytes))

    val SerializeSource = JsonTransformer[NotUsed](Config(Root / "message", mode = Mode.Serialize))

    val SerializeSourceBytes = JsonTransformer[NotUsed](Config(Root / "message", mode = Mode.Serialize, bind = Bind.Bytes))

    val SerializeRootToTarget = JsonTransformer[NotUsed](Config(Root, Some(Root / "message"), mode = Mode.Serialize))

    val SerializeRootToExistingTarget = JsonTransformer[NotUsed](Config(Root, Some(Root / "test"), mode = Mode.Serialize))

    val WrongTypeString = JsonTransformer[NotUsed](Config(Root, Some(Root / "test"), mode = Mode.Deserialize))

    val WrongTypeBytes = JsonTransformer[NotUsed](Config(Root, Some(Root / "test"), mode = Mode.Deserialize, bind = Bind.Bytes))

  }

  object Output {

    val DeserializeInplace: StreamEvent = StreamEvent(Json.obj("message" -> Json.obj("message" -> "foobar")))

    val DeserializeSourceToRoot: StreamEvent = StreamEvent(Json.obj(
      "message" -> """{"test":"foobar"}""",
      "test" -> "foobar"
    ))

    val DeserializeSourceToRootByteString: StreamEvent = StreamEvent(Json.obj(
      "message" -> ByteString("""{"test":"foobar"}"""),
      "test" -> "foobar"
    ))

    val DeserializeSourceToExistingRoot: StreamEvent = StreamEvent(Json.obj("message" -> "foobar"))

    val SerializeInplace: StreamEvent = StreamEvent(Json.obj("message" -> """{"message":"foobar"}"""))

    val SerializeInplaceBytes: StreamEvent = StreamEvent(Json.obj("message" -> ByteString("""{"message":"foobar"}""")))

    val SerializeRootToTarget: StreamEvent = StreamEvent(Json.obj(
      "message" -> """{"test":"foobar"}""",
      "test" -> "foobar"
    ))

    val SerializeRootToExistingTarget: StreamEvent = StreamEvent(Json.obj("test" -> """{"test":"foobar"}"""))

  }

}