/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2020
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
package io.techcode.streamy.util.lang

/**
  * Helper for garbage collectors.
  */
object GarbageCollectors {

  // Common types of gc
  private val young: String = "young"
  private val old: String = "old"
  private val survivor: String = "survivor"

  // Support mapping between pool names and type of garbage collector
  private val poolNames: Map[String, String] = Map(
    "Eden Space" -> young,
    "PS Eden Space" -> young,
    "Par Eden Space" -> young,
    "G1 Eden Space" -> young,
    "Survivor Space" -> survivor,
    "PS Survivor Space" -> survivor,
    "Par Survivor Space" -> survivor,
    "G1 Survivor Space" -> survivor,
    "Tenured Gen" -> old,
    "PS Old Gen" -> old,
    "CMS Old Gen" -> old,
    "G1 Old Gen" -> old
  )

  // Support mapping between gc names and type of garbage collector
  private val gcNames: Map[String, String] = Map(
    "Copy" -> young,
    "PS Scavenge" -> young,
    "ParNew" -> young,
    "G1 Young Generation" -> young,
    "MarkSweepCompact" -> old,
    "PS MarkSweep" -> old,
    "ConcurrentMarkSweep" -> old,
    "G1 Old Generation" -> old
  )

  /**
    * Returns type name of the garbage collector based on pool name.
    *
    * @param poolName name of the pool.
    * @return type name or name of the pool if not support.
    */
  def nameFromPool(poolName: String): String = poolNames.getOrElse(poolName, poolName)

  /**
    * Returns type name of the garbage collector based on gc name.
    *
    * @param gcName name of the pool.
    * @return type name or name of the gc if not support.
    */
  def nameFromGc(gcName: String): String = gcNames.getOrElse(gcName, gcName)

}
