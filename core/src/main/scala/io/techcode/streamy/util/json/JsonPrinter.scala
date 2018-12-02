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
package io.techcode.streamy.util.json

import java.util.Base64

import io.techcode.streamy.util.printer.StringPrinter

import scala.annotation.tailrec

/**
  * A JsonPrinter serializes a JSON AST to a String.
  * It's an adaptation of the amazing spray-json project.
  * All credits goes to it's initial contributor.
  */
private[json] class JsonPrinter extends StringPrinter {

  override def run(): String = {
    printValue(data)
    builder.toString
  }

  /**
    * Print an arbitrary json value.
    *
    * @param value json value.
    */
  def printValue(value: Json): Unit = {
    value match {
      case x: JsObject => printObject(x)
      case x: JsArray => printArray(x)
      case x: JsString => printString(x.value)
      case x: JsNumber => printNumber(x)
      case x: JsBytes => printBytes(x)
      case _: JsTrue.type => builder.append(JsonPrinter.True)
      case _: JsFalse.type => builder.append(JsonPrinter.False)
      case _: JsNull.type => builder.append(JsonPrinter.Null)
    }
  }

  /**
    * Print an arbitrary json object value.
    *
    * @param value json object value.
    */
  def printObject(value: JsObject) {
    builder.append(JsonPrinter.OpenBrace)
    if (value.underlying.nonEmpty) {
      var firstValue = true
      value.underlying.foreach { curr =>
        if (firstValue) {
          firstValue = false
        } else {
          builder.append(JsonPrinter.Comma)
        }

        printString(curr._1)
        builder.append(JsonPrinter.Colon)
        printValue(curr._2)
      }
    }
    builder.append(JsonPrinter.CloseBrace)
  }

  /**
    * Print an arbitrary json array value.
    *
    * @param value json array value.
    */
  def printArray(value: JsArray) {
    builder.append(JsonPrinter.OpenBracket)
    if (value.underlying.nonEmpty) {
      var firstValue = true
      value.underlying.foreach { curr =>
        if (firstValue) {
          firstValue = false
        } else {
          builder.append(JsonPrinter.Comma)
        }

        printValue(curr)
      }
    }
    builder.append(JsonPrinter.CloseBracket)
  }

  /**
    * Print a json number value.
    *
    * @param value json number value.
    */
  private def printNumber(value: JsNumber): Unit = {
    value match {
      case x: JsInt => builder.append(x.value)
      case x: JsLong => builder.append(x.value)
      case x: JsFloat => builder.append(x.value)
      case x: JsDouble => builder.append(x.value)
      case x: JsBigDecimal => builder.append(x.value)
    }
  }

  /**
    * Print a json bytes value.
    *
    * @param value json bytes value.
    */
  private def printBytes(value: JsBytes): Unit = {
    builder.append(JsonPrinter.Quote)
      .append(Base64.getMimeEncoder.encodeToString(value.value.toArray[Byte]))
      .append(JsonPrinter.Quote)
  }

  /**
    * Print a string value.
    *
    * @param value string value.
    */
  private def printString(value: String) {
    @tailrec def firstToBeEncoded(ix: Int = 0): Int =
      if (ix == value.length) -1 else if (requiresEncoding(value.charAt(ix))) ix else firstToBeEncoded(ix + 1)

    builder.append(JsonPrinter.Quote)
    firstToBeEncoded() match {
      case -1 ⇒ builder.append(value)
      case first ⇒
        builder.append(value, 0, first)

        @tailrec def append(ix: Int): Unit =
          if (ix < value.length) {
            value.charAt(ix) match {
              case c if !requiresEncoding(c) => builder.append(c)
              case '"' => builder.append("\\\"")
              case '\\' => builder.append("\\\\")
              case '\b' => builder.append("\\b")
              case '\f' => builder.append("\\f")
              case '\n' => builder.append("\\n")
              case '\r' => builder.append("\\r")
              case '\t' => builder.append("\\t")
              case x if x <= 0xF => builder.append("\\u000").append(Integer.toHexString(x))
              case x if x <= 0xFF => builder.append("\\u00").append(Integer.toHexString(x))
              case x if x <= 0xFFF => builder.append("\\u0").append(Integer.toHexString(x))
              case x => builder.append("\\u").append(Integer.toHexString(x))
            }
            append(ix + 1)
          }

        append(first)
    }
    builder.append(JsonPrinter.Quote)
  }

  private def requiresEncoding(c: Char): Boolean = {
    // From RFC 4627
    // unescaped = %x20-21 / %x23-5B / %x5D-10FFFF
    c match {
      case '"' => true
      case '\\' => true
      case _ => c < 0x20
    }
  }

}

/**
  * Json printer companion.
  */
object JsonPrinter {

  // Constants
  val Null: String = "null"
  val True: String = "true"
  val False: String = "false"
  val Quote: Char = '"'
  val Colon: Char = ':'
  val OpenBrace: Char = '{'
  val CloseBrace: Char = '}'
  val OpenBracket: Char = '['
  val CloseBracket: Char = ']'
  val Comma: Char = ','

  /**
    * Create a new json printer.
    */
  def apply(): JsonPrinter = new JsonPrinter()

}
