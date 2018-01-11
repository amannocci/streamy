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
  def bind(value: Boolean): (String, Json)

  /**
    * Bind an [[Int]] value to [[Json]]
    *
    * @param value value to bind.
    * @return value mapping.
    */
  def bind(value: Int): (String, Json)

  /**
    * Bind an [[Long]] value to [[Json]]
    *
    * @param value value to bind.
    * @return value mapping.
    */
  def bind(value: Long): (String, Json)

  /**
    * Bind an [[Float]] value to [[Json]]
    *
    * @param value value to bind.
    * @return value mapping.
    */
  def bind(value: Float): (String, Json)

  /**
    * Bind an [[Double]] value to [[Json]]
    *
    * @param value value to bind.
    * @return value mapping.
    */
  def bind(value: Double): (String, Json)

  /**
    * Bind an [[String]] value to [[Json]]
    *
    * @param value value to bind.
    * @return value mapping.
    */
  def bind(value: String): (String, Json)

  /**
    * Bind an [[ByteString]] value to [[Json]]
    *
    * @param value value to bind.
    * @return value mapping.
    */
  def bind(value: ByteString): (String, Json)

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

  def bind(value: Boolean): (String, Json) = (key, JsString(value.toString))

  def bind(value: Int): (String, Json) = (key, JsString(value.toString))

  def bind(value: Long): (String, Json) = (key, JsString(value.toString))

  def bind(value: Float): (String, Json) = (key, JsString(value.toString))

  def bind(value: Double): (String, Json) = (key, JsString(value.toString))

  def bind(value: String): (String, Json) = (key, JsString(value))

  def bind(value: ByteString): (String, Json) = (key, JsString(value.decodeString(charset)))

  def bind(value: Json): ByteString = value.asString.map(ByteString(_)).getOrElse(ByteString.empty)

}

/**
  * Specific bytes binder implementation.
  *
  * @param key key if binding.
  */
case class BytesBinder(override val key: String) extends Binder(key) {

  def bind(value: Boolean): (String, Json) = (key, JsBytes(ByteString(value.toString)))

  def bind(value: Int): (String, Json) = (key, JsBytes(ByteString(value.toString)))

  def bind(value: Long): (String, Json) = (key, JsBytes(ByteString(value.toString)))

  def bind(value: Float): (String, Json) = (key, JsBytes(ByteString(value.toString)))

  def bind(value: Double): (String, Json) = (key, JsBytes(ByteString(value.toString)))

  def bind(value: String): (String, Json) = (key, JsBytes(ByteString(value)))

  def bind(value: ByteString): (String, Json) = (key, JsBytes(value))

  def bind(value: Json): ByteString = value.asBytes.getOrElse(ByteString.empty)

}

/**
  * Specific int binder implementation.
  *
  * @param key key if binding.
  */
case class IntBinder(override val key: String) extends Binder(key) {

  def bind(value: Boolean): (String, Json) = (key, JsInt(if (value) 1 else 0))

  def bind(value: Int): (String, Json) = (key, JsInt(value))

  def bind(value: Long): (String, Json) = (key, JsInt(value.toInt))

  def bind(value: Float): (String, Json) = (key, JsInt(value.toInt))

  def bind(value: Double): (String, Json) = (key, JsInt(value.toInt))

  def bind(value: String): (String, Json) = (key, JsInt(value.toInt))

  def bind(value: ByteString): (String, Json) = (key, JsInt(value.decodeString(StandardCharsets.US_ASCII).toInt))

  def bind(value: Json): ByteString = value.asInt.map(x => ByteString(x.toString)).getOrElse(ByteString.empty)

}

/**
  * Specific long binder implementation.
  *
  * @param key key if binding.
  */
case class LongBinder(override val key: String) extends Binder(key) {

  def bind(value: Boolean): (String, Json) = (key, JsLong(if (value) 1 else 0))

  def bind(value: Int): (String, Json) = (key, JsLong(value))

  def bind(value: Long): (String, Json) = (key, JsLong(value))

  def bind(value: Float): (String, Json) = (key, JsLong(value.toLong))

  def bind(value: Double): (String, Json) = (key, JsLong(value.toLong))

  def bind(value: String): (String, Json) = (key, JsLong(value.toLong))

  def bind(value: ByteString): (String, Json) = (key, JsLong(value.decodeString(StandardCharsets.US_ASCII).toLong))

  def bind(value: Json): ByteString = value.asLong.map(x => ByteString(x.toString)).getOrElse(ByteString.empty)

}

/**
  * Specific float binder implementation.
  *
  * @param key key if binding.
  */
case class FloatBinder(override val key: String) extends Binder(key) {

  def bind(value: Boolean): (String, Json) = (key, JsFloat(if (value) 1 else 0))

  def bind(value: Int): (String, Json) = (key, JsFloat(value.toFloat))

  def bind(value: Long): (String, Json) = (key, JsFloat(value.toFloat))

  def bind(value: Float): (String, Json) = (key, JsFloat(value))

  def bind(value: Double): (String, Json) = (key, JsFloat(value.toFloat))

  def bind(value: String): (String, Json) = (key, JsFloat(value.toFloat))

  def bind(value: ByteString): (String, Json) = (key, JsFloat(value.decodeString(StandardCharsets.US_ASCII).toFloat))

  def bind(value: Json): ByteString = value.asFloat.map(x => ByteString(x.toString)).getOrElse(ByteString.empty)

}

/**
  * Specific double binder implementation.
  *
  * @param key key if binding.
  */
case class DoubleBinder(override val key: String) extends Binder(key) {

  def bind(value: Boolean): (String, Json) = (key, JsDouble(if (value) 1 else 0))

  def bind(value: Int): (String, Json) = (key, JsDouble(value.toDouble))

  def bind(value: Long): (String, Json) = (key, JsDouble(value.toDouble))

  def bind(value: Float): (String, Json) = (key, JsDouble(value))

  def bind(value: Double): (String, Json) = (key, JsDouble(value.toDouble))

  def bind(value: String): (String, Json) = (key, JsDouble(value.toDouble))

  def bind(value: ByteString): (String, Json) = (key, JsDouble(value.decodeString(StandardCharsets.US_ASCII).toDouble))

  def bind(value: Json): ByteString = value.asDouble.map(x => ByteString(x.toString)).getOrElse(ByteString.empty)

}
