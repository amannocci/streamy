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

  /**
    * Bridge from optional json to optional value.
    *
    * @param self optional json value.
    */
  implicit class JsonBridge(val self: MaybeJson) extends AnyVal {

    /**
      * Returns current optional json value as json object.
      *
      * @return current optional json value as json object if possible, otherwise [[None]].
      */
    def asObject: Option[JsObject] = self match {
      case x: JsObject => Some(x)
      case _ => None
    }

    /**
      * Returns current optional json value as json array.
      *
      * @return current optional json value as json array if possible, otherwise [[None]].
      */
    def asArray: Option[JsArray] = self match {
      case x: JsArray => Some(x)
      case _ => None
    }

    /**
      * Returns current optional json value as json number.
      *
      * @return current optional json value as json number if possible, otherwise [[None]].
      */
    def asNumber: Option[JsNumber] = self match {
      case x: JsNumber => Some(x)
      case _ => None
    }

    /**
      * Returns current optional json value as byte string.
      *
      * @return current optional json value as byte string if possible, otherwise [[None]].
      */
    def asBytes: Option[ByteString] = self match {
      case x: JsBytes => Some(x.value)
      case _ => None
    }

    /**
      * Returns current optional json value as json boolean.
      *
      * @return current optional json value as json boolean if possible, otherwise [[None]].
      */
    def asBoolean: Option[Boolean] = self match {
      case _: JsTrue.type => Some(true)
      case _: JsFalse.type => Some(false)
      case _ => None
    }

    /**
      * Returns current optional json value as json string.
      *
      * @return current optional json value as json string if possible, otherwise [[None]].
      */
    def asString: Option[String] = self match {
      case x: JsString => Some(x.value)
      case _ => None
    }

    /**
      * Returns current optional json value as json null.
      *
      * @return current optional json value as json null if possible, otherwise [[None]].
      */
    def asNull: Option[Unit] = self match {
      case _: JsNull.type => Some(())
      case _ => None
    }

    /**
      * Returns current optional json value as json int.
      *
      * @return current optional json value as json int if possible, otherwise [[None]].
      */
    def asInt: Option[Int] = self match {
      case x: JsInt => Some(x.value)
      case _ => None
    }

    /**
      * Returns current optional json value as json long.
      *
      * @return current optional json value as json long if possible, otherwise [[None]].
      */
    def asLong: Option[Long] = self match {
      case x: JsLong => Some(x.value)
      case _ => None
    }

    /**
      * Returns current optional json value as json big decimal.
      *
      * @return current optional json value as json big decimal if possible, otherwise [[None]].
      */
    def asBigDecimal: Option[BigDecimal] = self match {
      case x: JsBigDecimal => Some(x.value)
      case _ => None
    }

    /**
      * Returns current optional json value as json double.
      *
      * @return current optional json value as json double if possible, otherwise [[None]].
      */
    def asDouble: Option[Double] = self match {
      case x: JsDouble => Some(x.value)
      case _ => None
    }

    /**
      * Returns current optional json value as json float.
      *
      * @return current optional json value as json float if possible, otherwise [[None]].
      */
    def asFloat: Option[Float] = self match {
      case x: JsFloat => Some(x.value)
      case _ => None
    }

  }

}
