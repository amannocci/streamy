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
package io.techcode.streamy.kafka.component

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.{CommitterSettings, ConsumerSettings, ProducerMessage, ProducerSettings}
import akka.kafka.scaladsl.{Committer, Producer}
import akka.stream.scaladsl.{Flow, Keep, Sink}
import akka.util.ByteString
import com.google.common.base.Strings
import io.techcode.streamy.event.StreamEvent
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, ByteArraySerializer, StringDeserializer, StringSerializer}
import io.techcode.streamy.util.json._

import scala.concurrent.Future

/**
  * Kafka sink companion.
  */
object KafkaSink {

  // Represent an empty key
  private val emptyKey: String = ""

  // Component configuration
  case class Config(
    bootstrapServers: String,
    properties: Map[String, String] = Map.empty,
    topic: String,
    binding: Binding = Binding()
  ) {
    def toProducerSettings()(implicit system: ActorSystem): ProducerSettings[String, Array[Byte]] =
      ProducerSettings(system, new StringSerializer, new ByteArraySerializer)
        .withBootstrapServers(bootstrapServers)
        .withProperties(properties)
  }

  // Component binding
  case class Binding(
    topic: Option[String] = None,
    key: Option[String] = None,
    value: Option[String] = None
  ) {
    val topicPointer: Option[JsonPointer] = topic.map(Root / _)
    val keyPointer: Option[JsonPointer] = topic.map(Root / _)
    val valuePointer: Option[JsonPointer] = topic.map(Root / _)
  }

  /**
    * Create a new kafka sink exposing a sink with implicit commit.
    *
    * @param config sink configuration.
    */
  def plain(config: Config)(implicit system: ActorSystem): Sink[StreamEvent, Future[Done]] = {
    // Set producer settings
    val producerSettings = config.toProducerSettings()

    // Run source
    Flow[StreamEvent].map { evt =>
      val binding = config.binding
      val topic = binding.topicPointer
        .map(t => evt.payload.evaluate(t).getOrElse[String](config.topic))
        .getOrElse(config.topic)
      val key = Strings.emptyToNull(binding.keyPointer
        .map(p => evt.payload.evaluate(p).getOrElse[String](emptyKey))
        .getOrElse(emptyKey))
      val value = binding.valuePointer
        .map(p => evt.payload.evaluate(p).getOrElse[ByteString](ByteString.empty))
        .getOrElse(ByteString.empty)
      new ProducerRecord(topic, key, value.toArray[Byte])
    }.toMat(Producer.plainSink(producerSettings))(Keep.right)
  }

  /**
    * Create a new kafka sink exposing a sink with implicit commit.
    *
    * @param config sink configuration.
    */
  def committable(config: Config, committerSettings: CommitterSettings)(implicit system: ActorSystem): Sink[StreamEvent, Future[Done]] = {
    // Set producer settings
    val producerSettings = config.toProducerSettings()

    // Run source
    Flow[StreamEvent]
      .filter(_.attribute(KafkaSource.CommittableOffsetKey).isDefined)
      .map { evt =>
        val binding = config.binding
        val topic = binding.topicPointer
          .map(t => evt.payload.evaluate(t).getOrElse[String](config.topic))
          .getOrElse(config.topic)
        val key = Strings.emptyToNull(binding.keyPointer
          .map(p => evt.payload.evaluate(p).getOrElse[String](emptyKey))
          .getOrElse(emptyKey))
        val value = binding.valuePointer
          .map(p => evt.payload.evaluate(p).getOrElse[ByteString](ByteString.empty))
          .getOrElse(ByteString.empty)
        ProducerMessage.single(new ProducerRecord(topic, key, value.toArray[Byte]), evt.attribute(KafkaSource.CommittableOffsetKey).get)
      }.toMat(Producer.committableSink(producerSettings, committerSettings))(Keep.right)
  }

  /**
    * Batches offsets and commits them to Kafka.
    */
  def committer()(implicit system: ActorSystem): Sink[StreamEvent, Future[Done]] = Flow[StreamEvent]
    .filter(_.attribute(KafkaSource.CommittableOffsetKey).isDefined)
    .map(_.attribute(KafkaSource.CommittableOffsetKey).get)
    .toMat(Committer.sink(CommitterSettings(system)))(Keep.right)

}
