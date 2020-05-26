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
package io.techcode.streamy.event

import io.techcode.streamy.util.StreamException
import io.techcode.streamy.util.json.{JsNull, Json}

import scala.reflect.ClassTag

trait StreamEvent {

  /**
    * Returns payload value.
    */
  def payload: Json

  /**
    * Create a new stream event based on the current and mutate payload with `newPayload`
    *
    * @param newPayload payload replacement.
    * @return new stream event.
    */
  def mutate(newPayload: Json): StreamEvent

  /**
    * Create a new stream event based on the current and mutate attributes with `key` and `value`
    *
    * @param key   attribute key.
    * @param value value.
    * @return new stream event.
    */
  def mutate[T](key: AttributeKey[T], value: T): StreamEvent

  /**
    * Create a new stream event based on the current and mutate attributes by removing `key` and associate `value`
    *
    * @param key attribute key.
    * @return new stream event.
    */
  def mutate[T](key: AttributeKey[T]): StreamEvent

  /**
    * Returns attribute value based on attribute key.
    *
    * @param key attribute key.
    * @tparam T type of the value.
    * @return optional attribute value.
    */
  def attribute[T](key: AttributeKey[T]): Option[T]

  /**
    * Discard this stream event.
    *
    * @param msg message cause.
    * @return nothing.
    */
  def discard(msg: String): StreamEvent

  /**
    * Discard this stream event.
    *
    * @param cause exception cause.
    * @return nothing.
    */
  def discard(cause: Throwable): StreamEvent

}

/**
  * Basic streamy event.
  *
  * @param payload    json payload.
  * @param attributes attributes.
  */
private case class StreamEventImpl(
  payload: Json,
  private val attributes: Map[AttributeKey[_], _] = Map.empty
) extends StreamEvent {

  def mutate(newPayload: Json): StreamEvent = StreamEventImpl(newPayload, attributes)

  def mutate[T](key: AttributeKey[T], value: T): StreamEvent = StreamEventImpl(payload, attributes.updated(key, value))

  def mutate[T](key: AttributeKey[T]): StreamEvent = StreamEventImpl(payload, attributes.removed(key))

  def attribute[T](key: AttributeKey[T]): Option[T] = attributes.get(key).map(_.asInstanceOf[T])

  def discard(msg: String): StreamEvent = throw new StreamException(this, msg)

  def discard(cause: Throwable): StreamEvent = throw new StreamException(this, cause)

}

/**
  * Stream event companion.
  */
object StreamEvent {

  // Empty stream event
  val Empty: StreamEvent = StreamEventImpl(JsNull, Map.empty)

  /**
    * Create an stream event only using payload.
    *
    * @param payload payload to wrap.
    * @return new stream event.
    */
  def apply(payload: Json): StreamEvent = StreamEventImpl(payload, Map.empty)

}

/**
  * Represent an attribute key.
  *
  * @param name  name of the attribute.
  * @param clazz class type.
  * @tparam T type of key.
  */
case class AttributeKey[T](name: String, private val clazz: Class[_])

/**
  * Attribute key companion.
  */
object AttributeKey {
  def apply[T: ClassTag](name: String): AttributeKey[T] =
    new AttributeKey[T](name, implicitly[ClassTag[T]].runtimeClass)
}
