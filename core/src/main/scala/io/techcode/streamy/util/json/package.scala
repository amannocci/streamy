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
    def get(self: Json): A

    def getOrElse(self: MaybeJson, default: A): A

    def ifExists(self: MaybeJson, f: A => Unit): Unit

    def map(self: MaybeJson, f: A => Json): MaybeJson = flatMap(self, f)

    def flatMap(self: MaybeJson, f: A => MaybeJson): MaybeJson
  }

  // Json identity
  implicit case object JsIdentityTyped extends JsTyped[Json] {
    def get(self: Json): Json = self

    def getOrElse(self: MaybeJson, default: Json): Json =
      if (self.isDefined) {
        self.get(this)
      } else {
        default
      }

    def ifExists(self: MaybeJson, f: Json => Unit): Unit = f(self.get(this))

    def flatMap(self: MaybeJson, f: Json => MaybeJson): MaybeJson = f(self.get(this))
  }

  // Json object validation
  implicit case object JsObjectTyped extends JsTyped[JsObject] {
    def get(self: Json): JsObject = self.asInstanceOf[JsObject]

    def getOrElse(self: MaybeJson, default: JsObject): JsObject =
      if (self.isObject) {
        self.get(this)
      } else {
        default
      }

    def ifExists(self: MaybeJson, f: JsObject => Unit): Unit = if (self.isObject) f(self.get(this))

    def flatMap(self: MaybeJson, f: JsObject => MaybeJson): MaybeJson =
      if (self.isObject) {
        f(self.get(this))
      } else {
        JsUndefined
      }
  }

  // Json array validation
  implicit case object JsArrayTyped extends JsTyped[JsArray] {
    def get(self: Json): JsArray = self.asInstanceOf[JsArray]

    def getOrElse(self: MaybeJson, default: JsArray): JsArray =
      if (self.isArray) {
        self.get(this)
      } else {
        default
      }

    def ifExists(self: MaybeJson, f: JsArray => Unit): Unit = if (self.isArray) f(self.get(this))

    def flatMap(self: MaybeJson, f: JsArray => MaybeJson): MaybeJson =
      if (self.isArray) {
        f(self.get(this))
      } else {
        JsUndefined
      }
  }

  // Json boolean validation
  implicit case object JsBooleanTyped extends JsTyped[Boolean] {
    def get(self: Json): Boolean = self match {
      case JsTrue => true
      case JsFalse => false
      case _ => throw new ClassCastException
    }

    def getOrElse(self: MaybeJson, default: Boolean): Boolean =
      if (self.isBoolean) {
        self.get(this)
      } else {
        default
      }

    def ifExists(self: MaybeJson, f: Boolean => Unit): Unit = if (self.isBoolean) f(self.get(this))

    def flatMap(self: MaybeJson, f: Boolean => MaybeJson): MaybeJson =
      if (self.isBoolean) {
        f(self.get(this))
      } else {
        JsUndefined
      }
  }

  // Json number validation
  implicit case object JsNumberTyped extends JsTyped[JsNumber] {
    def get(self: Json): JsNumber = self.asInstanceOf[JsNumber]

    def getOrElse(self: MaybeJson, default: JsNumber): JsNumber =
      if (self.isNumber) {
        self.get(this)
      } else {
        default
      }

    def ifExists(self: MaybeJson, f: JsNumber => Unit): Unit = if (self.isNumber) f(self.get(this))

    def flatMap(self: MaybeJson, f: JsNumber => MaybeJson): MaybeJson =
      if (self.isNumber) {
        f(self.get(this))
      } else {
        JsUndefined
      }
  }

  // Json int validation
  implicit case object JsIntTyped extends JsTyped[Int] {
    def get(self: Json): Int = self.asInstanceOf[JsInt].value

    def getOrElse(self: MaybeJson, default: Int): Int =
      if (self.isInt) {
        self.get(this)
      } else {
        default
      }

    def ifExists(self: MaybeJson, f: Int => Unit): Unit = if (self.isInt) f(self.get(this))

    def flatMap(self: MaybeJson, f: Int => MaybeJson): MaybeJson =
      if (self.isInt) {
        f(self.get(this))
      } else {
        JsUndefined
      }
  }

  // Json long validation
  implicit case object JsLongTyped extends JsTyped[Long] {
    def get(self: Json): Long = self.asInstanceOf[JsLong].value

    def getOrElse(self: MaybeJson, default: Long): Long =
      if (self.isLong) {
        self.get(this)
      } else {
        default
      }

    def ifExists(self: MaybeJson, f: Long => Unit): Unit = if (self.isLong) f(self.get(this))

    def flatMap(self: MaybeJson, f: Long => MaybeJson): MaybeJson =
      if (self.isLong) {
        f(self.get(this))
      } else {
        JsUndefined
      }
  }

  // Json float validation
  implicit case object JsFloatTyped extends JsTyped[Float] {
    def get(self: Json): Float = self.asInstanceOf[JsFloat].value

    def getOrElse(self: MaybeJson, default: Float): Float =
      if (self.isFloat) {
        self.get(this)
      } else {
        default
      }

    def ifExists(self: MaybeJson, f: Float => Unit): Unit = if (self.isFloat) f(self.get(this))

    def flatMap(self: MaybeJson, f: Float => MaybeJson): MaybeJson =
      if (self.isFloat) {
        f(self.get(this))
      } else {
        JsUndefined
      }
  }

  // Json double validation
  implicit case object JsDoubleTyped extends JsTyped[Double] {
    def get(self: Json): Double = self.asInstanceOf[JsDouble].value

    def getOrElse(self: MaybeJson, default: Double): Double =
      if (self.isDouble) {
        self.get(this)
      } else {
        default
      }

    def ifExists(self: MaybeJson, f: Double => Unit): Unit = if (self.isDouble) f(self.get(this))

    def flatMap(self: MaybeJson, f: Double => MaybeJson): MaybeJson =
      if (self.isDouble) {
        f(self.get(this))
      } else {
        JsUndefined
      }
  }

  // Json big decimal validation
  implicit case object JsBigDecimalTyped extends JsTyped[BigDecimal] {
    def get(self: Json): BigDecimal = self.asInstanceOf[JsBigDecimal].value

    def getOrElse(self: MaybeJson, default: BigDecimal): BigDecimal =
      if (self.isBigDecimal) {
        self.get(this)
      } else {
        default
      }

    def ifExists(self: MaybeJson, f: BigDecimal => Unit): Unit = if (self.isBigDecimal) f(self.get(this))

    def flatMap(self: MaybeJson, f: BigDecimal => MaybeJson): MaybeJson =
      if (self.isBigDecimal) {
        f(self.get(this))
      } else {
        JsUndefined
      }
  }

  // Json string validation
  implicit case object JsStringTyped extends JsTyped[String] {
    def get(self: Json): String = self.asInstanceOf[JsString].value

    def getOrElse(self: MaybeJson, default: String): String =
      if (self.isString) {
        self.get(this)
      } else {
        default
      }

    def ifExists(self: MaybeJson, f: String => Unit): Unit = if (self.isString) f(self.get(this))

    def flatMap(self: MaybeJson, f: String => MaybeJson): MaybeJson =
      if (self.isString) {
        f(self.get(this))
      } else {
        JsUndefined
      }
  }

  // Json bytes validation
  implicit case object JsBytesTyped extends JsTyped[ByteString] {
    def get(self: Json): ByteString = self.asInstanceOf[JsBytes].value

    def getOrElse(self: MaybeJson, default: ByteString): ByteString =
      if (self.isBytes) {
        self.get(this)
      } else {
        default
      }

    def ifExists(self: MaybeJson, f: ByteString => Unit): Unit = if (self.isBytes) f(self.get(this))

    def flatMap(self: MaybeJson, f: ByteString => MaybeJson): MaybeJson =
      if (self.isBytes) {
        f(self.get(this))
      } else {
        JsUndefined
      }
  }

}
