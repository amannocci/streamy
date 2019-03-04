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
package io.techcode.streamy.util.printer

import io.techcode.streamy.util.json.Json

import scala.util.control.NoStackTrace

/**
  * Represent an abstract printer that provide an efficient way to print [[In]].
  */
trait Printer[In, Out] {

  // Local access
  protected var data: In = null.asInstanceOf[In]

  /**
    * Attempt to print input [[Json]].
    *
    * @return [[Out]] object result of printing.
    */
  final def print(doc: In): Either[PrintException, Out] =
    try {
      data = doc
      Right(run())
    } catch {
      case ex: PrintException => Left(ex)
    } finally {
      data = null.asInstanceOf[In]
      cleanup()
    }

  /**
    * Process printing based on [[data]] and current context.
    *
    * @return printing result.
    */
  def run(): Out

  /**
    * Cleanup printer context for next usage.
    */
  def cleanup(): Unit = ()

}

/**
  * Print exception.
  *
  * @param msg reason of print exception.
  */
class PrintException(msg: => String) extends RuntimeException(msg) with NoStackTrace {

  def canEqual(a: Any): Boolean = a.isInstanceOf[PrintException]

  override def equals(that: Any): Boolean =
    that match {
      case that: PrintException => that.canEqual(this) && this.hashCode == that.hashCode
      case _ => false
    }

  override def hashCode:Int = 31 + msg.hashCode

}
