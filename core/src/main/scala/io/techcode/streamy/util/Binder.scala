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
package io.techcode.streamy.util

import java.lang.{StringBuilder => JStringBuilder}
import java.nio.charset.{Charset, StandardCharsets}

import akka.util.{ByteString, ByteStringBuilder}
import com.google.common.primitives.{Doubles, Floats, Ints, Longs}
import io.techcode.streamy.util.json._

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
  def applyByteString(value: Json, hook: => Unit = () => ())(implicit builder: ByteStringBuilder): Boolean

  /**
    * Bind an [[Json]] value to [[JStringBuilder]]
    *
    * @param value value to bind.
    * @return whether binding succeded or failed.
    */
  def applyString(value: Json, hook: => Unit = () => ())(implicit builder: JStringBuilder): Boolean

}

/**
  * Binder companion object.
  */
object Binder {

  // No operation hook
  val NoOp: () => Unit = () => ()

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

  def applyString(value: Json, hook: => Unit = Binder.NoOp)(implicit builder: JStringBuilder): Boolean = true

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
    builder.put(key, JsString(value.toString))
    true
  }

  def apply(value: Int)(implicit builder: JsObjectBuilder): Boolean = {
    builder.put(key, JsString(value.toString))
    true
  }

  def apply(value: Long)(implicit builder: JsObjectBuilder): Boolean = {
    builder.put(key, JsString(value.toString))
    true
  }

  def apply(value: Float)(implicit builder: JsObjectBuilder): Boolean = {
    builder.put(key, JsString(value.toString))
    true
  }

  def apply(value: Double)(implicit builder: JsObjectBuilder): Boolean = {
    builder.put(key, JsString(value.toString))
    true
  }

  def apply(value: String)(implicit builder: JsObjectBuilder): Boolean = {
    builder.put(key, JsString(value))
    true
  }

  def apply(value: ByteString)(implicit builder: JsObjectBuilder): Boolean = {
    builder.put(key, JsString(value.decodeString(charset)))
    true
  }

  def applyByteString(value: Json, hook: => Unit = Binder.NoOp)(implicit builder: ByteStringBuilder): Boolean =
    value.evaluate(path).getOrElse(JsNull) match {
      case v: JsString =>
        hook
        builder ++= ByteString(v.value)
        true
      case _ => false // Nothing
    }

  def applyString(value: Json, hook: => Unit = Binder.NoOp)(implicit builder: JStringBuilder): Boolean =
    value.evaluate(path).getOrElse(JsNull) match {
      case v: JsString =>
        hook
        builder.append(v.value)
        true
      case _ => false // Nothing
    }

}

/**
  * Specific bytes binder implementation.
  *
  * @param key key if binding.
  */
case class BytesBinder(override val key: String) extends SomeBinder(key) {

  def apply(value: Boolean)(implicit builder: JsObjectBuilder): Boolean = {
    builder.put(key, JsBytes(ByteString(value.toString)))
    true
  }

  def apply(value: Int)(implicit builder: JsObjectBuilder): Boolean = {
    builder.put(key, JsBytes(ByteString(value.toString)))
    true
  }

  def apply(value: Long)(implicit builder: JsObjectBuilder): Boolean = {
    builder.put(key, JsBytes(ByteString(value.toString)))
    true
  }

  def apply(value: Float)(implicit builder: JsObjectBuilder): Boolean = {
    builder.put(key, JsBytes(ByteString(value.toString)))
    true
  }

  def apply(value: Double)(implicit builder: JsObjectBuilder): Boolean = {
    builder.put(key, JsBytes(ByteString(value.toString)))
    true
  }

  def apply(value: String)(implicit builder: JsObjectBuilder): Boolean = {
    builder.put(key, JsBytes(ByteString(value)))
    true
  }

  def apply(value: ByteString)(implicit builder: JsObjectBuilder): Boolean = {
    builder.put(key, JsBytes(value))
    true
  }

  def applyByteString(value: Json, hook: => Unit = Binder.NoOp)(implicit builder: ByteStringBuilder): Boolean =
    value.evaluate(path).getOrElse(JsNull) match {
      case v: JsBytes =>
        hook
        builder ++= v.value
        true
      case _ => false // Nothing
    }

  def applyString(value: Json, hook: => Unit = Binder.NoOp)(implicit builder: JStringBuilder): Boolean =
    value.evaluate(path).getOrElse(JsNull) match {
      case v: JsBytes =>
        hook
        builder.append(v.value.utf8String)
        true
      case _ => false // Nothing
    }

}

/**
  * Specific int binder implementation.
  *
  * @param key key if binding.
  */
case class IntBinder(override val key: String) extends SomeBinder(key) {

  def apply(value: Boolean)(implicit builder: JsObjectBuilder): Boolean = {
    builder.put(key, JsInt(if (value) 1 else 0))
    true
  }

  def apply(value: Int)(implicit builder: JsObjectBuilder): Boolean = {
    builder.put(key, JsInt(value))
    true
  }

  def apply(value: Long)(implicit builder: JsObjectBuilder): Boolean = {
    builder.put(key, JsInt(value.toInt))
    true
  }

  def apply(value: Float)(implicit builder: JsObjectBuilder): Boolean = {
    builder.put(key, JsInt(value.toInt))
    true
  }

  def apply(value: Double)(implicit builder: JsObjectBuilder): Boolean = {
    builder.put(key, JsInt(value.toInt))
    true
  }

  def apply(value: String)(implicit builder: JsObjectBuilder): Boolean = {
    Option(Ints.tryParse(value)).exists { v =>
      builder.put(key, JsInt(v))
      true
    }
  }

  def apply(value: ByteString)(implicit builder: JsObjectBuilder): Boolean =
    Option(Ints.tryParse(value.decodeString(StandardCharsets.US_ASCII))).exists { v =>
      builder.put(key, JsInt(v))
      true
    }

  def applyByteString(value: Json, hook: => Unit = Binder.NoOp)(implicit builder: ByteStringBuilder): Boolean =
    value.evaluate(path).getOrElse(JsNull) match {
      case v: JsInt =>
        hook
        builder ++= ByteString(v.value.toString)
        true
      case _ => false // Nothing
    }

  def applyString(value: Json, hook: => Unit = Binder.NoOp)(implicit builder: JStringBuilder): Boolean =
    value.evaluate(path).getOrElse(JsNull) match {
      case v: JsInt =>
        hook
        builder.append(v.value)
        true
      case _ => false // Nothing
    }

}

/**
  * Specific long binder implementation.
  *
  * @param key key if binding.
  */
case class LongBinder(override val key: String) extends SomeBinder(key) {

  def apply(value: Boolean)(implicit builder: JsObjectBuilder): Boolean = {
    builder.put(key, JsLong(if (value) 1 else 0))
    true
  }

  def apply(value: Int)(implicit builder: JsObjectBuilder): Boolean = {
    builder.put(key, JsLong(value))
    true
  }

  def apply(value: Long)(implicit builder: JsObjectBuilder): Boolean = {
    builder.put(key, JsLong(value))
    true
  }

  def apply(value: Float)(implicit builder: JsObjectBuilder): Boolean = {
    builder.put(key, JsLong(value.toLong))
    true
  }

  def apply(value: Double)(implicit builder: JsObjectBuilder): Boolean = {
    builder.put(key, JsLong(value.toLong))
    true
  }

  def apply(value: String)(implicit builder: JsObjectBuilder): Boolean =
    Option(Longs.tryParse(value)).exists { v =>
      builder.put(key, JsLong(v))
      true
    }

  def apply(value: ByteString)(implicit builder: JsObjectBuilder): Boolean =
    Option(Longs.tryParse(value.decodeString(StandardCharsets.US_ASCII))).exists { v =>
      builder.put(key, JsLong(v))
      true
    }

  def applyByteString(value: Json, hook: => Unit = Binder.NoOp)(implicit builder: ByteStringBuilder): Boolean =
    value.evaluate(path).getOrElse(JsNull) match {
      case v: JsLong =>
        hook
        builder ++= ByteString(v.value.toString)
        true
      case _ => false // Nothing
    }

  def applyString(value: Json, hook: => Unit = Binder.NoOp)(implicit builder: JStringBuilder): Boolean =
    value.evaluate(path).getOrElse(JsNull) match {
      case v: JsLong =>
        hook
        builder.append(v.value)
        true
      case _ => false // Nothing
    }

}

/**
  * Specific float binder implementation.
  *
  * @param key key if binding.
  */
case class FloatBinder(override val key: String) extends SomeBinder(key) {

  def apply(value: Boolean)(implicit builder: JsObjectBuilder): Boolean = {
    builder.put(key, JsFloat(if (value) 1 else 0))
    true
  }

  def apply(value: Int)(implicit builder: JsObjectBuilder): Boolean = {
    builder.put(key, JsFloat(value.toFloat))
    true
  }

  def apply(value: Long)(implicit builder: JsObjectBuilder): Boolean = {
    builder.put(key, JsFloat(value.toFloat))
    true
  }

  def apply(value: Float)(implicit builder: JsObjectBuilder): Boolean = {
    builder.put(key, JsFloat(value))
    true
  }

  def apply(value: Double)(implicit builder: JsObjectBuilder): Boolean = {
    builder.put(key, JsFloat(value.toFloat))
    true
  }

  def apply(value: String)(implicit builder: JsObjectBuilder): Boolean =
    Option(Floats.tryParse(value)).exists { v =>
      builder.put(key, JsFloat(v))
      true
    }

  def apply(value: ByteString)(implicit builder: JsObjectBuilder): Boolean =
    Option(Floats.tryParse(value.decodeString(StandardCharsets.US_ASCII))).exists { v =>
      builder.put(key, JsFloat(v))
      true
    }

  def applyByteString(value: Json, hook: => Unit = Binder.NoOp)(implicit builder: ByteStringBuilder): Boolean =
    value.evaluate(path).getOrElse(JsNull) match {
      case v: JsFloat =>
        hook
        builder ++= ByteString(v.value.toString)
        true
      case _ => false // Nothing
    }

  def applyString(value: Json, hook: => Unit = Binder.NoOp)(implicit builder: JStringBuilder): Boolean =
    value.evaluate(path).getOrElse(JsNull) match {
      case v: JsFloat =>
        hook
        builder.append(v.value)
        true
      case _ => false // Nothing
    }

}

/**
  * Specific double binder implementation.
  *
  * @param key key if binding.
  */
case class DoubleBinder(override val key: String) extends SomeBinder(key) {

  def apply(value: Boolean)(implicit builder: JsObjectBuilder): Boolean = {
    builder.put(key, JsDouble(if (value) 1 else 0))
    true
  }

  def apply(value: Int)(implicit builder: JsObjectBuilder): Boolean = {
    builder.put(key, JsDouble(value.toDouble))
    true
  }

  def apply(value: Long)(implicit builder: JsObjectBuilder): Boolean = {
    builder.put(key, JsDouble(value.toDouble))
    true
  }

  def apply(value: Float)(implicit builder: JsObjectBuilder): Boolean = {
    builder.put(key, JsDouble(value))
    true
  }

  def apply(value: Double)(implicit builder: JsObjectBuilder): Boolean = {
    builder.put(key, JsDouble(value.toDouble))
    true
  }

  def apply(value: String)(implicit builder: JsObjectBuilder): Boolean =
    Option(Doubles.tryParse(value)).exists { v =>
      builder.put(key, JsDouble(v))
      true
    }

  def apply(value: ByteString)(implicit builder: JsObjectBuilder): Boolean =
    Option(Doubles.tryParse(value.decodeString(StandardCharsets.US_ASCII))).exists { v =>
      builder.put(key, JsDouble(v))
      true
    }

  def applyByteString(value: Json, hook: => Unit = Binder.NoOp)(implicit builder: ByteStringBuilder): Boolean =
    value.evaluate(path).getOrElse(JsNull) match {
      case v: JsDouble =>
        hook
        builder ++= ByteString(v.value.toString)
        true
      case _ => false // Nothing
    }

  def applyString(value: Json, hook: => Unit = Binder.NoOp)(implicit builder: JStringBuilder): Boolean =
    value.evaluate(path).getOrElse(JsNull) match {
      case v: JsDouble =>
        hook
        builder.append(v.value)
        true
      case _ => false // Nothing
    }

}
