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
package io.techcode.streamy.util.json

import akka.util.ByteString
import pureconfig.{ConfigReader, ConvertHelpers}

import scala.language.implicitConversions

/**
  * Contains all json implicits conversion.
  */
trait JsonImplicit {

  /**
    * Convert a json value to string.
    *
    * @param value js value.
    * @return string.
    */
  implicit def jsonToString(value: Json): String = value.toString

  /**
    * Convert a string to json value.
    *
    * @param value string value.
    * @return json.
    */
  implicit def stringToJson(value: String): Json = JsString(value)

  /**
    * Convert a float to json value.
    *
    * @param value float value.
    * @return json.
    */
  implicit def floatToJson(value: Float): Json = JsFloat(value)

  /**
    * Convert a double to json value.
    *
    * @param value double value.
    * @return json.
    */
  implicit def doubleToJson(value: Double): Json = JsDouble(value)

  /**
    * Convert a byte to json value.
    *
    * @param value byte value.
    * @return json.
    */
  implicit def byteToJson(value: Byte): Json = JsInt(value)

  /**
    * Convert a short to json value.
    *
    * @param value short value.
    * @return json.
    */
  implicit def shortToJson(value: Short): Json = JsInt(value)

  /**
    * Convert a int to json value.
    *
    * @param value int value.
    * @return json.
    */
  implicit def intToJson(value: Int): Json = JsInt(value)

  /**
    * Convert a long to json value.
    *
    * @param value long value.
    * @return json.
    */
  implicit def longToJson(value: Long): Json = JsLong(value)

  /**
    * Convert a boolean to json value.
    *
    * @param value boolean value.
    * @return json.
    */
  implicit def booleanToJson(value: Boolean): Json = if (value) JsTrue else JsFalse

  /**
    * Convert a byte string to json bytes.
    *
    * @param value byte string value.
    * @return json.
    */
  implicit def byteStringToJson(value: ByteString): Json = JsBytes(value)

  /**
    * Convert a big decimal to json value.
    *
    * @param value big decimal value.
    * @return json.
    */
  implicit def bigDecimalToJson(value: BigDecimal): Json = JsBigDecimal(value)

  // Json reader to support pureconfig
  implicit val jsonReader: ConfigReader[Json] = ConfigReader.fromString[Json](ConvertHelpers.tryF(Json.parse(_).toTry))

}
