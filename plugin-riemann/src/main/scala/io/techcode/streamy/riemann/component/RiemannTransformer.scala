/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2018-2021
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
package io.techcode.streamy.riemann.component

import akka.NotUsed
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import io.riemann.riemann.Proto
import io.techcode.streamy.event.StreamEvent
import io.techcode.streamy.protobuf.component.ProtobufTransformer
import io.techcode.streamy.util.json._
import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

/**
  * Riemann transformer companion.
  */
object RiemannTransformer {

  /**
    * Create a riemann flow that transform incoming [[ByteString]] to [[StreamEvent]].
    *
    * @param conf flow configuration.
    * @return new riemann flow.
    */
  def parser(conf: Parser.Config): Flow[ByteString, StreamEvent, NotUsed] =
    ProtobufTransformer.parser[Proto.Msg](ProtobufTransformer.Parser.Config(
      proto = Proto.Msg.getDefaultInstance,
      maxSize = conf.maxSize,
      decoder = new ParserLogic(conf.binding)
    ))

  /**
    * Create a riemann flow that transform incoming [[StreamEvent]] to [[ByteString]].
    *
    * @param conf flow configuration.
    * @return new riemann flow.
    */
  def printer(conf: Printer.Config): Flow[StreamEvent, ByteString, NotUsed] =
    ProtobufTransformer.printer[Proto.Msg](ProtobufTransformer.Printer.Config(
      proto = Proto.Msg.getDefaultInstance,
      maxSize = conf.maxSize,
      encoder = new PrinterLogic(conf.binding)
    ))

  private class ParserLogic(binding: Parser.Binding) extends (Proto.Msg => StreamEvent) {

    val eventBinding: Parser.EventBinding = binding.event

    // scalastyle:off cyclomatic.complexity
    override def apply(pkt: Proto.Msg): StreamEvent = {
      val builder = Json.objectBuilder()
      if (pkt.hasOk) builder += (binding.ok -> pkt.getOk)
      if (pkt.hasError) builder += (binding.error -> pkt.getError)
      if (pkt.getEventsCount > 0) {
        val events = Json.arrayBuilder()
        for (i <- 0 until pkt.getEventsCount) {
          val el = pkt.getEvents(i)
          val evt = Json.objectBuilder()
          if (el.hasTime) evt += (eventBinding.time -> el.getTime)
          if (el.hasState) evt += (eventBinding.state -> el.getState)
          if (el.hasService) evt += (eventBinding.service -> el.getService)
          if (el.hasHost) evt += (eventBinding.host -> el.getHost)
          if (el.hasDescription) evt += (eventBinding.description -> el.getDescription)
          if (el.getTagsCount > 0) {
            val tags = Json.arrayBuilder()
            el.getTagsList.forEach(x => tags += x)
            evt += (eventBinding.tags -> tags.result())
          }
          if (el.hasTtl) evt += (eventBinding.ttl -> el.getTtl)
          if (el.getAttributesCount > 0) {
            val attrs = Json.objectBuilder()
            el.getAttributesList.forEach(a => attrs += (a.getKey -> a.getValue))
            evt += (eventBinding.attributes -> attrs.result())
          }
          if (el.hasTimeMicros) evt += (eventBinding.timeMicros -> el.getTimeMicros)
          if (el.hasMetricSint64) evt += (eventBinding.metricSint64 -> el.getMetricSint64)
          if (el.hasMetricD) evt += (eventBinding.metricD -> el.getMetricD)
          if (el.hasMetricF) evt += (eventBinding.metricF -> el.getMetricF)
          events += evt.result()
        }
        builder += (binding.events -> events.result())
      }
      StreamEvent(builder.result())
    }

    // scalastyle:on cyclomatic.complexity

  }

  private class PrinterLogic[T](binding: Printer.Binding) extends (StreamEvent => Proto.Msg) {

    val eventBinding: Printer.EventBinding = binding.event

    // scalastyle:off cyclomatic.complexity
    override def apply(event: StreamEvent): Proto.Msg = {
      val builder = Proto.Msg.newBuilder()

      val payload = event.payload
      payload.evaluate(binding.ok).ifExists[Boolean](builder.setOk)
      payload.evaluate(binding.error).ifExists[String](builder.setError)
      payload.evaluate(binding.events).ifExists[JsArray] { x =>
        val it = x.iterator
        while (it.hasNext) {
          val event = Proto.Event.newBuilder()
          it.next().ifExists[JsObject] { evt =>
            evt.evaluate(eventBinding.time).ifExists[JsNumber](x => event.setTime(x.toLong))
            evt.evaluate(eventBinding.state).ifExists[String](event.setState)
            evt.evaluate(eventBinding.service).ifExists[String](event.setService)
            evt.evaluate(eventBinding.host).ifExists[String](event.setHost)
            evt.evaluate(eventBinding.description).ifExists[String](event.setDescription)
            evt.evaluate(eventBinding.tags).ifExists[JsArray] { el =>
              val tagIt = el.iterator
              while (tagIt.hasNext) {
                tagIt.next().ifExists[String](event.addTags)
              }
              el.ifExists[String](event.addTags)
            }
            evt.evaluate(eventBinding.ttl).ifExists[Float](event.setTtl)
            evt.evaluate(eventBinding.attributes).ifExists[JsObject] { el =>
              el.foreach { pair =>
                pair._2 match {
                  case JsString(value) =>
                    event.addAttributes(Proto.Attribute.newBuilder().setKey(pair._1).setValue(value))
                  case x: Json =>
                    event.addAttributes(Proto.Attribute.newBuilder().setKey(pair._1).setValue(x.toString))
                }
              }
            }
            evt.evaluate(eventBinding.timeMicros).ifExists[Long](event.setTimeMicros)
            evt.evaluate(eventBinding.metricSint64).ifExists[Long](event.setMetricSint64)
            evt.evaluate(eventBinding.metricD).ifExists[Double](event.setMetricD)
            evt.evaluate(eventBinding.metricF).ifExists[Float](event.setMetricF)
          }
          builder.addEvents(event)
        }
      }

      builder.build()
    }

    // scalastyle:on cyclomatic.complexity

  }

  // Parser related stuff
  object Parser {

    // Default values
    val DefaultBinding: RiemannTransformer.Parser.Binding = RiemannTransformer.Parser.Binding()
    val DefaultMaxSize: Int = Int.MaxValue - 4

    // Configuration readers
    implicit val eventBindingReader: ConfigReader[RiemannTransformer.Parser.EventBinding] =
      deriveReader[RiemannTransformer.Parser.EventBinding]

    implicit val bindingReader: ConfigReader[RiemannTransformer.Parser.Binding] =
      deriveReader[RiemannTransformer.Parser.Binding]

    implicit val configReader: ConfigReader[RiemannTransformer.Parser.Config] =
      deriveReader[RiemannTransformer.Parser.Config]

    // Binding
    case class Binding(
      ok: String = "ok",
      error: String = "error",
      events: String = "events",
      event: EventBinding = EventBinding()
    )

    // Event binding
    case class EventBinding(
      time: String = "time",
      state: String = "state",
      service: String = "service",
      host: String = "host",
      description: String = "description",
      tags: String = "tags",
      ttl: String = "ttl",
      attributes: String = "attributes",
      timeMicros: String = "time_micros",
      metricSint64: String = "metric_sint64",
      metricD: String = "metric_d",
      metricF: String = "metric_f"
    )

    // Configuration
    case class Config(
      maxSize: Int = DefaultMaxSize,
      binding: Binding = DefaultBinding
    )

  }

  // Printer related stuff
  object Printer {

    // Default values
    val DefaultBinding: RiemannTransformer.Printer.Binding = RiemannTransformer.Printer.Binding()
    val DefaultMaxSize: Int = Int.MaxValue - 4

    // Configuration readers
    implicit val eventBindingReader: ConfigReader[RiemannTransformer.Printer.EventBinding] =
      deriveReader[RiemannTransformer.Printer.EventBinding]

    implicit val bindingReader: ConfigReader[RiemannTransformer.Printer.Binding] =
      deriveReader[RiemannTransformer.Printer.Binding]

    implicit val configReader: ConfigReader[RiemannTransformer.Printer.Config] =
      deriveReader[RiemannTransformer.Printer.Config]

    // Binding
    case class Binding(
      ok: JsonPointer = Root / "ok",
      error: JsonPointer = Root / "error",
      events: JsonPointer = Root / "events",
      event: EventBinding = EventBinding()
    )

    // Event binding
    case class EventBinding(
      time: JsonPointer = Root / "time",
      state: JsonPointer = Root / "state",
      service: JsonPointer = Root / "service",
      host: JsonPointer = Root / "host",
      description: JsonPointer = Root / "description",
      tags: JsonPointer = Root / "tags",
      ttl: JsonPointer = Root / "ttl",
      attributes: JsonPointer = Root / "attributes",
      timeMicros: JsonPointer = Root / "time_micros",
      metricSint64: JsonPointer = Root / "metric_sint64",
      metricD: JsonPointer = Root / "metric_d",
      metricF: JsonPointer = Root / "metric_f"
    )

    // Configuration
    case class Config(
      maxSize: Int = DefaultMaxSize,
      binding: Binding = DefaultBinding
    )

  }

}
