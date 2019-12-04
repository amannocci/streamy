/*
 * The MIT License (MIT)
 * <p>
 * Copyright (C) 2017-2019
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
package io.techcode.streamy.util

import java.lang.{StringBuilder => JStringBuilder}
import java.nio.charset.{Charset, StandardCharsets}

import akka.util.{ByteString, ByteStringBuilder}
import com.google.common.primitives.{Doubles, Floats, Ints, Longs}
import io.techcode.streamy.util.json._
import io.techcode.streamy.util.lang.CharBuilder

import scala.collection.mutable

/**
  * Common trait binder.
  */
trait Binder {

  /**
    * Returns true if the binder is an instance of [[SomeBinder]], false otherwise.
    */
  def isDefined: Boolean

  /**
    * Bind an [[Boolean]] value into [[JsObjectBuilder]]
    *
    * @param builder json object builder.
    * @param value   value to bind.
    * @return whether binding succeded or failed.
    */
  def apply(value: Boolean)(implicit builder: JsObjectBuilder): Boolean

  /**
    * Bind an [[Int]] value to [[JsObjectBuilder]]
    *
    * @param builder json object builder.
    * @param value   value to bind.
    * @return whether binding succeded or failed.
    */
  def apply(value: Int)(implicit builder: JsObjectBuilder): Boolean

  /**
    * Bind an [[Long]] value to [[JsObjectBuilder]]
    *
    * @param builder json object builder.
    * @param value   value to bind.
    * @return whether binding succeded or failed.
    */
  def apply(value: Long)(implicit builder: JsObjectBuilder): Boolean

  /**
    * Bind an [[Float]] value to [[JsObjectBuilder]]
    *
    * @param builder json object builder.
    * @param value   value to bind.
    * @return whether binding succeded or failed.
    */
  def apply(value: Float)(implicit builder: JsObjectBuilder): Boolean

  /**
    * Bind an [[Double]] value to [[JsObjectBuilder]]
    *
    * @param builder json object builder.
    * @param value   value to bind.
    * @return whether binding succeded or failed.
    */
  def apply(value: Double)(implicit builder: JsObjectBuilder): Boolean

  /**
    * Bind an [[String]] value to [[JsObjectBuilder]]
    *
    * @param builder json object builder.
    * @param value   value to bind.
    * @return whether binding succeded or failed.
    */
  def apply(value: String)(implicit builder: JsObjectBuilder): Boolean

  /**
    * Bind an [[ByteString]] value to [[JsObjectBuilder]]
    *
    * @param builder json object builder.
    * @param value   value to bind.
    * @return whether binding succeded or failed.
    */
  def apply(value: ByteString)(implicit builder: JsObjectBuilder): Boolean

  /**
    * Bind an [[Json]] value to [[ByteStringBuilder]]
    *
    * @param value value to bind.
    * @return whether binding succeded or failed.
    */
  def applyByteString(value: Json, hook: => Unit = Binder.NoOp)(implicit builder: ByteStringBuilder): Boolean

  /**
    * Bind an [[Json]] value to [[JStringBuilder]]
    *
    * @param value value to bind.
    * @return whether binding succeded or failed.
    */
  def applyString(value: Json, hook: => Unit = Binder.NoOp)(implicit builder: CharBuilder): Boolean

}

/**
  * Binder companion object.
  */
object Binder {

  // No operation hook
  val NoOp: Unit = ()

}

/**
  * Represent a binder that bind to nothing.
  * Usefull to skip binding.
  */
case object NoneBinder extends Binder {

  def isDefined: Boolean = false

  def apply(value: Boolean)(implicit builder: JsObjectBuilder): Boolean = true

  def apply(value: Int)(implicit builder: JsObjectBuilder): Boolean = true

  def apply(value: Long)(implicit builder: JsObjectBuilder): Boolean = true

  def apply(value: Float)(implicit builder: JsObjectBuilder): Boolean = true

  def apply(value: Double)(implicit builder: JsObjectBuilder): Boolean = true

  def apply(value: String)(implicit builder: JsObjectBuilder): Boolean = true

  def apply(value: ByteString)(implicit builder: JsObjectBuilder): Boolean = true

  def applyByteString(value: Json, hook: => Unit = Binder.NoOp)(implicit builder: ByteStringBuilder): Boolean = true

  def applyString(value: Json, hook: => Unit = Binder.NoOp)(implicit builder: CharBuilder): Boolean = true

}

/**
  * Represent a binder able to convert a raw value to a Json value.
  */
sealed abstract class SomeBinder(val key: String) extends Binder {

  protected val path: JsonPointer = Root / key

  def isDefined: Boolean = true

}

/**
  * Specific string binder implementation.
  *
  * @param key     key if binding.
  * @param charset charset specification.
  */
case class StringBinder(override val key: String, charset: Charset = StandardCharsets.UTF_8) extends SomeBinder(key) {

  def apply(value: Boolean)(implicit builder: JsObjectBuilder): Boolean = {
    builder += (key -> JsString(value.toString))
    true
  }

  def apply(value: Int)(implicit builder: JsObjectBuilder): Boolean = {
    builder += (key -> JsString(value.toString))
    true
  }

  def apply(value: Long)(implicit builder: JsObjectBuilder): Boolean = {
    builder += (key -> JsString(value.toString))
    true
  }

  def apply(value: Float)(implicit builder: JsObjectBuilder): Boolean = {
    builder += (key -> JsString(value.toString))
    true
  }

  def apply(value: Double)(implicit builder: JsObjectBuilder): Boolean = {
    builder += (key -> JsString(value.toString))
    true
  }

  def apply(value: String)(implicit builder: JsObjectBuilder): Boolean = {
    builder += (key -> JsString(value))
    true
  }

  def apply(value: ByteString)(implicit builder: JsObjectBuilder): Boolean = {
    builder += (key -> JsString(value.decodeString(charset)))
    val test: mutable.Builder[(String, Json), JsObject] = Json.objectBuilder()
    true
  }

  def applyByteString(value: Json, hook: => Unit = Binder.NoOp)(implicit builder: ByteStringBuilder): Boolean = {
    val result = value.evaluate(path)
    if (result.isString) {
      hook
      builder ++= ByteString(result.get[String])
      true
    } else {
      false
    }
  }

  def applyString(value: Json, hook: => Unit = Binder.NoOp)(implicit builder: CharBuilder): Boolean = {
    val result = value.evaluate(path)
    if (result.isString) {
      hook
      builder.append(result.get[String])
      true
    } else {
      false
    }
  }

}

/**
  * Specific bytes binder implementation.
  *
  * @param key key if binding.
  */
case class BytesBinder(override val key: String) extends SomeBinder(key) {

  def apply(value: Boolean)(implicit builder: JsObjectBuilder): Boolean = {
    builder += (key -> JsBytes(ByteString(value.toString)))
    true
  }

  def apply(value: Int)(implicit builder: JsObjectBuilder): Boolean = {
    builder += (key -> JsBytes(ByteString(value.toString)))
    true
  }

  def apply(value: Long)(implicit builder: JsObjectBuilder): Boolean = {
    builder += (key -> JsBytes(ByteString(value.toString)))
    true
  }

  def apply(value: Float)(implicit builder: JsObjectBuilder): Boolean = {
    builder += (key -> JsBytes(ByteString(value.toString)))
    true
  }

  def apply(value: Double)(implicit builder: JsObjectBuilder): Boolean = {
    builder += (key -> JsBytes(ByteString(value.toString)))
    true
  }

  def apply(value: String)(implicit builder: JsObjectBuilder): Boolean = {
    builder += (key -> JsBytes(ByteString(value)))
    true
  }

  def apply(value: ByteString)(implicit builder: JsObjectBuilder): Boolean = {
    builder += (key -> JsBytes(value))
    true
  }

  def applyByteString(value: Json, hook: => Unit = Binder.NoOp)(implicit builder: ByteStringBuilder): Boolean = {
    val result = value.evaluate(path)
    if (result.isBytes) {
      hook
      builder ++= result.get[ByteString]
      true
    } else {
      false
    }
  }

  def applyString(value: Json, hook: => Unit = Binder.NoOp)(implicit builder: CharBuilder): Boolean = {
    val result = value.evaluate(path)
    if (result.isBytes) {
      hook
      builder.append(result.get[ByteString].utf8String)
      true
    } else {
      false
    }
  }

}

/**
  * Specific int binder implementation.
  *
  * @param key key if binding.
  */
case class IntBinder(override val key: String) extends SomeBinder(key) {

  def apply(value: Boolean)(implicit builder: JsObjectBuilder): Boolean = {
    builder += (key -> JsInt(if (value) 1 else 0))
    true
  }

  def apply(value: Int)(implicit builder: JsObjectBuilder): Boolean = {
    builder += (key -> JsInt(value))
    true
  }

  def apply(value: Long)(implicit builder: JsObjectBuilder): Boolean = {
    builder += (key -> JsInt(value.toInt))
    true
  }

  def apply(value: Float)(implicit builder: JsObjectBuilder): Boolean = {
    builder += (key -> JsInt(value.toInt))
    true
  }

  def apply(value: Double)(implicit builder: JsObjectBuilder): Boolean = {
    builder += (key -> JsInt(value.toInt))
    true
  }

  def apply(value: String)(implicit builder: JsObjectBuilder): Boolean = {
    Option(Ints.tryParse(value)).exists { v =>
      builder += (key -> JsInt(v))
      true
    }
  }

  def apply(value: ByteString)(implicit builder: JsObjectBuilder): Boolean =
    Option(Ints.tryParse(value.decodeString(StandardCharsets.US_ASCII))).exists { v =>
      builder += (key -> JsInt(v))
      true
    }

  def applyByteString(value: Json, hook: => Unit = Binder.NoOp)(implicit builder: ByteStringBuilder): Boolean = {
    val result = value.evaluate(path)
    if (result.isInt) {
      hook
      builder ++= ByteString(result.get[Int].toString)
      true
    } else {
      false
    }
  }

  def applyString(value: Json, hook: => Unit = Binder.NoOp)(implicit builder: CharBuilder): Boolean = {
    val result = value.evaluate(path)
    if (result.isInt) {
      hook
      builder.append(result.get[Int])
      true
    } else {
      false
    }
  }

}

/**
  * Specific long binder implementation.
  *
  * @param key key if binding.
  */
case class LongBinder(override val key: String) extends SomeBinder(key) {

  def apply(value: Boolean)(implicit builder: JsObjectBuilder): Boolean = {
    builder += (key -> JsLong(if (value) 1 else 0))
    true
  }

  def apply(value: Int)(implicit builder: JsObjectBuilder): Boolean = {
    builder += (key -> JsLong(value))
    true
  }

  def apply(value: Long)(implicit builder: JsObjectBuilder): Boolean = {
    builder += (key -> JsLong(value))
    true
  }

  def apply(value: Float)(implicit builder: JsObjectBuilder): Boolean = {
    builder += (key -> JsLong(value.toLong))
    true
  }

  def apply(value: Double)(implicit builder: JsObjectBuilder): Boolean = {
    builder += (key -> JsLong(value.toLong))
    true
  }

  def apply(value: String)(implicit builder: JsObjectBuilder): Boolean =
    Option(Longs.tryParse(value)).exists { v =>
      builder += (key -> JsLong(v))
      true
    }

  def apply(value: ByteString)(implicit builder: JsObjectBuilder): Boolean =
    Option(Longs.tryParse(value.decodeString(StandardCharsets.US_ASCII))).exists { v =>
      builder += (key -> JsLong(v))
      true
    }

  def applyByteString(value: Json, hook: => Unit = Binder.NoOp)(implicit builder: ByteStringBuilder): Boolean = {
    val result = value.evaluate(path)
    if (result.isLong) {
      hook
      builder ++= ByteString(result.get[Long].toString)
      true
    } else {
      false
    }
  }

  def applyString(value: Json, hook: => Unit = Binder.NoOp)(implicit builder: CharBuilder): Boolean = {
    val result = value.evaluate(path)
    if (result.isLong) {
      hook
      builder.append(result.get[Long])
      true
    } else {
      false
    }
  }

}

/**
  * Specific float binder implementation.
  *
  * @param key key if binding.
  */
case class FloatBinder(override val key: String) extends SomeBinder(key) {

  def apply(value: Boolean)(implicit builder: JsObjectBuilder): Boolean = {
    builder += (key -> JsFloat(if (value) 1 else 0))
    true
  }

  def apply(value: Int)(implicit builder: JsObjectBuilder): Boolean = {
    builder += (key -> JsFloat(value.toFloat))
    true
  }

  def apply(value: Long)(implicit builder: JsObjectBuilder): Boolean = {
    builder += (key -> JsFloat(value.toFloat))
    true
  }

  def apply(value: Float)(implicit builder: JsObjectBuilder): Boolean = {
    builder += (key -> JsFloat(value))
    true
  }

  def apply(value: Double)(implicit builder: JsObjectBuilder): Boolean = {
    builder += (key -> JsFloat(value.toFloat))
    true
  }

  def apply(value: String)(implicit builder: JsObjectBuilder): Boolean =
    Option(Floats.tryParse(value)).exists { v =>
      builder += (key -> JsFloat(v))
      true
    }

  def apply(value: ByteString)(implicit builder: JsObjectBuilder): Boolean =
    Option(Floats.tryParse(value.decodeString(StandardCharsets.US_ASCII))).exists { v =>
      builder += (key -> JsFloat(v))
      true
    }

  def applyByteString(value: Json, hook: => Unit = Binder.NoOp)(implicit builder: ByteStringBuilder): Boolean = {
    val result = value.evaluate(path)
    if (result.isFloat) {
      hook
      builder ++= ByteString(result.toString)
      true
    } else {
      false
    }
  }

  def applyString(value: Json, hook: => Unit = Binder.NoOp)(implicit builder: CharBuilder): Boolean = {
    val result = value.evaluate(path)
    if (result.isFloat) {
      hook
      builder.append(result)
      true
    } else {
      false
    }
  }

}

/**
  * Specific double binder implementation.
  *
  * @param key key if binding.
  */
case class DoubleBinder(override val key: String) extends SomeBinder(key) {

  def apply(value: Boolean)(implicit builder: JsObjectBuilder): Boolean = {
    builder += (key -> JsDouble(if (value) 1 else 0))
    true
  }

  def apply(value: Int)(implicit builder: JsObjectBuilder): Boolean = {
    builder += (key -> JsDouble(value.toDouble))
    true
  }

  def apply(value: Long)(implicit builder: JsObjectBuilder): Boolean = {
    builder += (key -> JsDouble(value.toDouble))
    true
  }

  def apply(value: Float)(implicit builder: JsObjectBuilder): Boolean = {
    builder += (key -> JsDouble(value))
    true
  }

  def apply(value: Double)(implicit builder: JsObjectBuilder): Boolean = {
    builder += (key -> JsDouble(value.toDouble))
    true
  }

  def apply(value: String)(implicit builder: JsObjectBuilder): Boolean =
    Option(Doubles.tryParse(value)).exists { v =>
      builder += (key -> JsDouble(v))
      true
    }

  def apply(value: ByteString)(implicit builder: JsObjectBuilder): Boolean =
    Option(Doubles.tryParse(value.decodeString(StandardCharsets.US_ASCII))).exists { v =>
      builder += (key -> JsDouble(v))
      true
    }

  def applyByteString(value: Json, hook: => Unit = Binder.NoOp)(implicit builder: ByteStringBuilder): Boolean = {
    val result = value.evaluate(path)
    if (result.isDouble) {
      hook
      builder ++= ByteString(result.toString)
      true
    } else {
      false
    }
  }

  def applyString(value: Json, hook: => Unit = Binder.NoOp)(implicit builder: CharBuilder): Boolean = {
    val result = value.evaluate(path)
    if (result.isDouble) {
      hook
      builder.append(result)
      true
    } else {
      false
    }
  }

}
