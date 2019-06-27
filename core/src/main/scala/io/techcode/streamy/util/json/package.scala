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

import akka.util.ByteString

package object json extends JsonImplicit {

  // Root json pointer
  val Root: JsonPointer = JsonPointer()

  // Json typed wrapper for ifExists implementation
  sealed trait JsTyped[A] {
    def ifExists(self: Json, f: A => Unit): Unit

    def map(self: Json, f: A => Json): MaybeJson = flatMap(self, f)

    def flatMap(self: Json, f: A => MaybeJson): MaybeJson
  }

  // Json identity
  implicit case object JsIdentityTyped extends JsTyped[Json] {

    def ifExists(self: Json, f: Json => Unit): Unit = f(self)

    def flatMap(self: Json, f: Json => MaybeJson): MaybeJson = f(self)

  }

  // Json object validation
  implicit case object JsObjectTyped extends JsTyped[JsObject] {

    def ifExists(self: Json, f: JsObject => Unit): Unit = if (self.isObject) f(self.asObject)

    def flatMap(self: Json, f: JsObject => MaybeJson): MaybeJson =
      if (self.isObject) {
        f(self.asObject)
      } else {
        JsUndefined
      }

  }

  // Json array validation
  implicit case object JsArrayTyped extends JsTyped[JsArray] {

    def ifExists(self: Json, f: JsArray => Unit): Unit = if (self.isArray) f(self.asArray)

    def flatMap(self: Json, f: JsArray => MaybeJson): MaybeJson =
      if (self.isArray) {
        f(self.asArray)
      } else {
        JsUndefined
      }

  }

  // Json boolean validation
  implicit case object JsBooleanTyped extends JsTyped[Boolean] {

    def ifExists(self: Json, f: Boolean => Unit): Unit = if (self.isBoolean) f(self.asBoolean)

    def flatMap(self: Json, f: Boolean => MaybeJson): MaybeJson =
      if (self.isBoolean) {
        f(self.asBoolean)
      } else {
        JsUndefined
      }

  }

  // Json number validation
  implicit case object JsNumberTyped extends JsTyped[JsNumber] {

    def ifExists(self: Json, f: JsNumber => Unit): Unit = if (self.isNumber) f(self.asNumber)

    def flatMap(self: Json, f: JsNumber => MaybeJson): MaybeJson =
      if (self.isNumber) {
        f(self.asNumber)
      } else {
        JsUndefined
      }

  }

  // Json int validation
  implicit case object JsIntTyped extends JsTyped[Int] {

    def ifExists(self: Json, f: Int => Unit): Unit = if (self.isInt) f(self.asInt)

    def flatMap(self: Json, f: Int => MaybeJson): MaybeJson =
      if (self.isInt) {
        f(self.asInt)
      } else {
        JsUndefined
      }

  }

  // Json long validation
  implicit case object JsLongTyped extends JsTyped[Long] {

    def ifExists(self: Json, f: Long => Unit): Unit = if (self.isLong) f(self.asLong)

    def flatMap(self: Json, f: Long => MaybeJson): MaybeJson =
      if (self.isLong) {
        f(self.asLong)
      } else {
        JsUndefined
      }

  }

  // Json float validation
  implicit case object JsFloatTyped extends JsTyped[Float] {

    def ifExists(self: Json, f: Float => Unit): Unit = if (self.isFloat) f(self.asFloat)

    def flatMap(self: Json, f: Float => MaybeJson): MaybeJson =
      if (self.isFloat) {
        f(self.asFloat)
      } else {
        JsUndefined
      }

  }

  // Json double validation
  implicit case object JsDoubleTyped extends JsTyped[Double] {

    def ifExists(self: Json, f: Double => Unit): Unit = if (self.isDouble) f(self.asDouble)

    def flatMap(self: Json, f: Double => MaybeJson): MaybeJson =
      if (self.isDouble) {
        f(self.asDouble)
      } else {
        JsUndefined
      }

  }

  // Json big decimal validation
  implicit case object JsBigDecimalTyped extends JsTyped[BigDecimal] {

    def ifExists(self: Json, f: BigDecimal => Unit): Unit = if (self.isBigDecimal) f(self.asBigDecimal)

    def flatMap(self: Json, f: BigDecimal => MaybeJson): MaybeJson =
      if (self.isBigDecimal) {
        f(self.asBigDecimal)
      } else {
        JsUndefined
      }

  }

  // Json string validation
  implicit case object JsStringTyped extends JsTyped[String] {

    def ifExists(self: Json, f: String => Unit): Unit = if (self.isString) f(self.asString)

    def flatMap(self: Json, f: String => MaybeJson): MaybeJson =
      if (self.isString) {
        f(self.asString)
      } else {
        JsUndefined
      }

  }

  // Json bytes validation
  implicit case object JsBytesTyped extends JsTyped[ByteString] {

    def ifExists(self: Json, f: ByteString => Unit): Unit = if (self.isBytes) f(self.asBytes)

    def flatMap(self: Json, f: ByteString => MaybeJson): MaybeJson =
      if (self.isBytes) {
        f(self.asBytes)
      } else {
        JsUndefined
      }

  }

}
