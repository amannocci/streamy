/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017
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

import gnieh.diffson.Pointer
import gnieh.diffson.circe._
import io.circe._

/**
  * Contains some helpers to support json patch and evaluation.
  */
trait JsonHelper {

  // Err handler for exist function
  private val errHandler: PartialFunction[(Json, String, Pointer), Json] = {
    case _ => Json.Null
  }

  // Pointer with err handler
  private implicit val existPointer: JsonPointer = {
    val p = new JsonPointer()
    p.handler_=(errHandler)
    p
  }

  // Pointer root
  val root: Pointer = Pointer.root

  /**
    * Evaluate if a value exist at given path.
    *
    * @param pkt  json value to evaluate.
    * @param path path to evaluate.
    * @return true if there is a value at given path, otherwise false.
    */
  @inline def exist(pkt: Json, path: Pointer): Boolean = existPointer.evaluate(pkt, path) != Json.Null

  /**
    * Evaluate the given path in the given Json.
    *
    * @param pkt  json value to evaluate.
    * @param path path to evaluate.
    * @return value pointed by the path.
    */
  @inline def evaluate(pkt: Json, path: Pointer): Json = pointer.evaluate(pkt, path)

  /**
    * Apply a patch on a given Json.
    *
    * @param pkt json value to evaluate.
    * @param ops operations to apply.
    * @return value patched.
    */
  def patch(pkt: Json, ops: Operation*): Json = JsonPatch(ops.toList)(pkt)

}
