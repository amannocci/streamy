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

import java.io.InputStream

import akka.util.ByteString
import com.fasterxml.jackson.core._
import com.fasterxml.jackson.databind.Module.SetupContext
import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.databind.`type`.TypeFactory
import com.fasterxml.jackson.databind.deser.Deserializers
import com.fasterxml.jackson.databind.module.SimpleModule

import scala.annotation.{switch, tailrec}
import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.util.control.NonFatal

private[json] object JsonConverter {

  // Json mapper
  private val mapper = (new ObjectMapper).registerModule(StreamyModule)

  // Json factory
  private val factory = new JsonFactory(mapper)

  /**
    * Parses a bytestring representing a Json input, and returns it as a [[Json]].
    *
    * @param data the bytestring to parse.
    */
  def parse(data: ByteString): Either[Throwable, Json] = try {
    Right(mapper.readValue(factory.createParser(data.toArray[Byte]), classOf[Json]))
  } catch {
    case NonFatal(error) => Left(error)
  }

  /**
    * Parses a string representing a Json input, and returns it as a [[Json]].
    *
    * @param data the string to parse.
    */
  def parse(data: Array[Byte]): Either[Throwable, Json] = try {
    Right(mapper.readValue(factory.createParser(data), classOf[Json]))
  } catch {
    case NonFatal(error) => Left(error)
  }

  /**
    * Parses a string representing a Json input, and returns it as a [[Json]].
    *
    * @param input the string to parse.
    */
  def parse(input: String): Either[Throwable, Json] = try {
    Right(mapper.readValue(factory.createParser(input), classOf[Json]))
  } catch {
    case NonFatal(error) => Left(error)
  }

  /**
    * Parses a stream representing a Json input, and returns it as a [[Json]].
    *
    * @param stream the InputStream to parse.
    */
  def parse(stream: InputStream): Either[Throwable, Json] = try {
    Right(mapper.readValue(factory.createParser(stream), classOf[Json]))
  } catch {
    case NonFatal(error) => Left(error)
  }

  /**
    * Converts a [[Json]] to its string representation.
    *
    * @return a String with the json representation.
    */
  def print(json: Json): String = JsonPrinter(json).print().get

}

/**
  * Register streamy json stuff in jackson.
  */
private[json] object StreamyModule extends SimpleModule("StreamyJson", Version.unknownVersion()) {

  override def setupModule(context: SetupContext) {
    context.addDeserializers(new StreamyDeserializers)
  }

}

/**
  * Streamy deserializers implementations.
  */
private[json] class StreamyDeserializers extends Deserializers.Base {

  override def findBeanDeserializer(javaType: JavaType, config: DeserializationConfig, bean: BeanDescription): JsonValueDeserializer = {
    val classType = javaType.getRawClass
    if (classOf[Json].isAssignableFrom(classType)) {
      new JsonValueDeserializer(config.getTypeFactory, classType)
    } else {
      null
    }
  }

}

/**
  * Json deserializer implementation.
  */
private[json] class JsonValueDeserializer(factory: TypeFactory, classType: Class[_]) extends JsonDeserializer[Object] {

  override final def isCachable: Boolean = true

  override def deserialize(parser: JsonParser, ctx: DeserializationContext): Json = {
    val value: Json = deserialize(parser, ctx, mutable.ListBuffer())
    if (!classType.isAssignableFrom(value.getClass)) {
      ctx.handleUnexpectedToken(classType, parser)
    }
    value
  }

  @tailrec final def deserialize(parser: JsonParser, ctx: DeserializationContext, parserContext: ListBuffer[DeserializerContext]): Json = {
    if (parser.currentToken() == null) parser.nextToken()

    val (maybeValue, nextContext) = (parser.getCurrentToken.id(): @switch) match {
      case JsonTokenId.ID_NUMBER_INT =>
        val inter = parser.getBigIntegerValue
        if (inter.bitLength() >= 32) {
          (Some(JsLong(inter.longValueExact())), parserContext)
        } else {
          (Some(JsInt(inter.intValueExact())), parserContext)
        }
      case JsonTokenId.ID_NUMBER_FLOAT => (Some(JsBigDecimal(parser.getDecimalValue)), parserContext)
      case JsonTokenId.ID_STRING => (Some(JsString(parser.getText)), parserContext)
      case JsonTokenId.ID_TRUE => (Some(JsTrue), parserContext)
      case JsonTokenId.ID_FALSE => (Some(JsFalse), parserContext)
      case JsonTokenId.ID_NULL => (Some(JsNull), parserContext)
      case JsonTokenId.ID_START_ARRAY => (None, ReadingList(new ArrayBuffer[Json]) +: parserContext)

      case JsonTokenId.ID_END_ARRAY => parserContext match {
        case ReadingList(content) +: stack => (Some(JsArray(content)), stack)
        case _ => throw new RuntimeException("We weren't reading a list, something went wrong")
      }

      case JsonTokenId.ID_START_OBJECT => (None, ReadingMap(mutable.AnyRefMap[String, Json]()) +: parserContext)

      case JsonTokenId.ID_FIELD_NAME => parserContext match {
        case (ctx: ReadingMap) +: stack => (None, ctx.setField(parser.getCurrentName) +: stack)
        case _ => throw new RuntimeException("We weren't reading an object, something went wrong")
      }

      case JsonTokenId.ID_END_OBJECT => parserContext match {
        case ReadingMap(content) +: stack => (Some(JsObject(content)), stack)
        case _ => throw new RuntimeException("We weren't reading an object, something went wrong")
      }

      case JsonTokenId.ID_NOT_AVAILABLE =>
        throw new RuntimeException("We weren't reading an object, something went wrong")

      case JsonTokenId.ID_EMBEDDED_OBJECT =>
        throw new RuntimeException("We weren't reading an object, something went wrong")
    }

    maybeValue match {
      case Some(v) if nextContext.isEmpty => v
      case _ =>
        parser.nextToken()
        val toPass = maybeValue.map { v =>
          val previous +: stack = nextContext
          previous.addValue(v) +: stack
        }.getOrElse(nextContext)

        deserialize(parser, ctx, toPass)
    }
  }

  // This is used when the root object is null, ie when deserializing "null"
  override val getNullValue: JsNull.type = JsNull

}

/**
  * Deserializer context.
  */
private[json] sealed trait DeserializerContext {

  def addValue(value: Json): DeserializerContext

}

/**
  * Deserializer array context.
  */
private[json] final case class ReadingList(content: mutable.ArrayBuffer[Json]) extends DeserializerContext {

  def addValue(value: Json): DeserializerContext = ReadingList {
    content += value
  }

}

/**
  * Deserializer map key context.
  */
private[json] final case class KeyRead(content: mutable.Map[String, Json], fieldName: String) extends DeserializerContext {

  def addValue(value: Json): DeserializerContext = ReadingMap {
    content += (fieldName -> value)
  }

}

/**
  * Deserializer map context.
  */
private[json] final case class ReadingMap(content: mutable.Map[String, Json]) extends DeserializerContext {

  def setField(fieldName: String): DeserializerContext = KeyRead(content, fieldName)

  def addValue(value: Json): DeserializerContext = throw new Exception("Cannot add a value on an object without a key, malformed Json object!")

}
