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
package io.techcode.streamy.util.parser

import java.nio.charset.{Charset, StandardCharsets}

import akka.util.ByteString
import com.google.common.primitives.{Doubles, Floats, Ints, Longs}
import io.techcode.streamy.util.json.{JsBytes, JsDouble, JsFloat, JsInt, JsLong, JsString, Json}

/**
  * Represent a binder able to convert a raw value to a Json value.
  */
sealed abstract class Binder(val key: String) {

  /**
    * Bind an [[Boolean]] value to [[Json]]
    *
    * @param value value to bind.
    * @return value mapping.
    */
  def bind(value: Boolean): Json

  /**
    * Bind an [[Int]] value to [[Json]]
    *
    * @param value value to bind.
    * @return value mapping.
    */
  def bind(value: Int): Json

  /**
    * Bind an [[Long]] value to [[Json]]
    *
    * @param value value to bind.
    * @return value mapping.
    */
  def bind(value: Long): Json

  /**
    * Bind an [[Float]] value to [[Json]]
    *
    * @param value value to bind.
    * @return value mapping.
    */
  def bind(value: Float): Json

  /**
    * Bind an [[Double]] value to [[Json]]
    *
    * @param value value to bind.
    * @return value mapping.
    */
  def bind(value: Double): Json

  /**
    * Bind an [[String]] value to [[Json]]
    *
    * @param value value to bind.
    * @return value mapping.
    */
  def bind(value: String): Option[Json]

  /**
    * Bind an [[ByteString]] value to [[Json]]
    *
    * @param value value to bind.
    * @return value mapping.
    */
  def bind(value: ByteString): Option[Json]

  /**
    * Bind an [[Json]] value to [[ByteString]]
    *
    * @param value value to bind.
    * @return value mapping.
    */
  def bind(value: Json): ByteString

}

/**
  * Specific string binder implementation.
  *
  * @param key     key if binding.
  * @param charset charset specification.
  */
case class StringBinder(override val key: String, charset: Charset = StandardCharsets.UTF_8) extends Binder(key) {

  def bind(value: Boolean): Json = JsString(value.toString)

  def bind(value: Int): Json = JsString(value.toString)

  def bind(value: Long): Json = JsString(value.toString)

  def bind(value: Float): Json = JsString(value.toString)

  def bind(value: Double): Json = JsString(value.toString)

  def bind(value: String): Option[Json] = Some(JsString(value))

  def bind(value: ByteString): Option[Json] = Some(JsString(value.decodeString(charset)))

  def bind(value: Json): ByteString = value.asString.map(ByteString(_)).getOrElse(ByteString.empty)

}

/**
  * Specific bytes binder implementation.
  *
  * @param key key if binding.
  */
case class BytesBinder(override val key: String) extends Binder(key) {

  def bind(value: Boolean): Json = JsBytes(ByteString(value.toString))

  def bind(value: Int): Json = JsBytes(ByteString(value.toString))

  def bind(value: Long): Json = JsBytes(ByteString(value.toString))

  def bind(value: Float): Json = JsBytes(ByteString(value.toString))

  def bind(value: Double): Json = JsBytes(ByteString(value.toString))

  def bind(value: String): Option[Json] = Some(JsBytes(ByteString(value)))

  def bind(value: ByteString): Option[Json] = Some(JsBytes(value))

  def bind(value: Json): ByteString = value.asBytes.getOrElse(ByteString.empty)

}

/**
  * Specific int binder implementation.
  *
  * @param key key if binding.
  */
case class IntBinder(override val key: String) extends Binder(key) {

  def bind(value: Boolean): Json = JsInt(if (value) 1 else 0)

  def bind(value: Int): Json = JsInt(value)

  def bind(value: Long): Json = JsInt(value.toInt)

  def bind(value: Float): Json = JsInt(value.toInt)

  def bind(value: Double): Json = JsInt(value.toInt)

  def bind(value: String): Option[Json] = Option(Ints.tryParse(value)).map(JsInt(_))

  def bind(value: ByteString): Option[Json] = Option(Ints.tryParse(value.decodeString(StandardCharsets.US_ASCII)))
    .map(JsInt(_))

  def bind(value: Json): ByteString = value.asInt.map(x => ByteString(x.toString)).getOrElse(ByteString.empty)

}

/**
  * Specific long binder implementation.
  *
  * @param key key if binding.
  */
case class LongBinder(override val key: String) extends Binder(key) {

  def bind(value: Boolean): Json = JsLong(if (value) 1 else 0)

  def bind(value: Int): Json = JsLong(value)

  def bind(value: Long): Json = JsLong(value)

  def bind(value: Float): Json = JsLong(value.toLong)

  def bind(value: Double): Json = JsLong(value.toLong)

  def bind(value: String): Option[Json] = Option(Longs.tryParse(value)).map(JsLong(_))

  def bind(value: ByteString): Option[Json] = Option(Longs.tryParse(value.decodeString(StandardCharsets.US_ASCII)))
    .map(JsLong(_))

  def bind(value: Json): ByteString = value.asLong.map(x => ByteString(x.toString)).getOrElse(ByteString.empty)

}

/**
  * Specific float binder implementation.
  *
  * @param key key if binding.
  */
case class FloatBinder(override val key: String) extends Binder(key) {

  def bind(value: Boolean): Json = JsFloat(if (value) 1 else 0)

  def bind(value: Int): Json = JsFloat(value.toFloat)

  def bind(value: Long): Json = JsFloat(value.toFloat)

  def bind(value: Float): Json = JsFloat(value)

  def bind(value: Double): Json = JsFloat(value.toFloat)

  def bind(value: String): Option[Json] = Option(Floats.tryParse(value)).map(JsFloat(_))

  def bind(value: ByteString): Option[Json] = Option(Floats.tryParse(value.decodeString(StandardCharsets.US_ASCII)))
    .map(JsFloat(_))

  def bind(value: Json): ByteString = value.asFloat.map(x => ByteString(x.toString)).getOrElse(ByteString.empty)

}

/**
  * Specific double binder implementation.
  *
  * @param key key if binding.
  */
case class DoubleBinder(override val key: String) extends Binder(key) {

  def bind(value: Boolean): Json = JsDouble(if (value) 1 else 0)

  def bind(value: Int): Json = JsDouble(value.toDouble)

  def bind(value: Long): Json = JsDouble(value.toDouble)

  def bind(value: Float): Json = JsDouble(value)

  def bind(value: Double): Json = JsDouble(value.toDouble)

  def bind(value: String): Option[Json] = Option(Doubles.tryParse(value)).map(JsDouble(_))

  def bind(value: ByteString): Option[Json] = Option(Doubles.tryParse(value.decodeString(StandardCharsets.US_ASCII)))
    .map(JsDouble(_))

  def bind(value: Json): ByteString = value.asDouble.map(x => ByteString(x.toString)).getOrElse(ByteString.empty)

}
