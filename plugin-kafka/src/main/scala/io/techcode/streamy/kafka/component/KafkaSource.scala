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

import akka.NotUsed
import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.CommittableOffset
import akka.kafka._
import akka.kafka.scaladsl.{Consumer, PartitionAssignmentHandler}
import akka.stream.scaladsl.Source
import akka.util.ByteString
import io.techcode.streamy.event.{AttributeKey, StreamEvent}
import io.techcode.streamy.kafka.component.KafkaSource.AutoOffsetReset.AutoOffsetReset
import io.techcode.streamy.kafka.event.KafkaEvent
import io.techcode.streamy.util.json._
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.{ByteArrayDeserializer, StringDeserializer}

import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Kafka source companion.
  */
object KafkaSource {

  // Defaults
  val DefaultMaxPartition: Int = 1024

  // Commitable offset attribute key
  val CommittableOffsetKey: AttributeKey[CommittableOffset] = AttributeKey("kafka-commitable-offset")

  // Component configuration
  case class Config(
    bootstrapServers: String,
    groupId: String,
    autoOffsetReset: AutoOffsetReset = AutoOffsetReset.Latest,
    topics: TopicConfig,
    properties: Map[String, String] = Map.empty,
    binding: Binding = Binding()
  ) {
    def toConsumerSettings()(implicit system: ActorSystem): ConsumerSettings[String, Array[Byte]] =
      ConsumerSettings(system, new StringDeserializer, new ByteArrayDeserializer)
        .withBootstrapServers(bootstrapServers)
        .withGroupId(groupId)
        .withStopTimeout(Duration.Zero)
        .withProperties(properties)
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset match {
          case AutoOffsetReset.Latest => "latest"
          case AutoOffsetReset.Earliest => "earliest"
        })
  }

  // Auto offset reset implementation
  object AutoOffsetReset extends Enumeration {
    type AutoOffsetReset = Value
    val Latest, Earliest = Value
  }

  // Generic topic configuration
  trait TopicConfig {
    def toSubscription(implicit system: ActorSystem): AutoSubscription

    /**
      * Create a assignment handler able to publish kafka events.
      *
      * @return assignment handler.
      */
    protected def assignmentHandler(implicit system: ActorSystem): PartitionAssignmentHandler =
      new PartitionAssignmentHandler {
        override def onRevoke(revokedTps: Set[TopicPartition], consumer: RestrictedConsumer): Unit =
          revokedTps.foreach(t => system.eventStream.publish(KafkaEvent.Consumer.TopicPartitionRevoke(t)))

        override def onAssign(assignedTps: Set[TopicPartition], consumer: RestrictedConsumer): Unit =
          assignedTps.foreach(t => system.eventStream.publish(KafkaEvent.Consumer.TopicPartitionAssign(t)))

        override def onLost(lostTps: Set[TopicPartition], consumer: RestrictedConsumer): Unit =
          lostTps.foreach(t => system.eventStream.publish(KafkaEvent.Consumer.TopicPartitionLost(t)))

        override def onStop(currentTps: Set[TopicPartition], consumer: RestrictedConsumer): Unit =
          currentTps.foreach(t => system.eventStream.publish(KafkaEvent.Consumer.TopicPartitionStop(t)))
      }
  }

  // Static topic configuration
  case class StaticTopicConfig(
    statics: Set[String]
  ) extends TopicConfig {
    override def toSubscription(implicit system: ActorSystem): AutoSubscription =
      Subscriptions.topics(statics).withPartitionAssignmentHandler(assignmentHandler)
  }

  // Topic pattern configuration
  case class PatternTopicConfig(
    pattern: String
  ) extends TopicConfig {
    override def toSubscription(implicit system: ActorSystem): AutoSubscription =
      Subscriptions.topicPattern(pattern).withPartitionAssignmentHandler(assignmentHandler)
  }

  // Component binding
  case class Binding(
    key: Option[String] = None,
    offset: Option[String] = None,
    value: Option[String] = None,
    partition: Option[String] = None,
    topic: Option[String] = None,
    timestamp: Option[String] = None
  ) {
    val numberOfBind: Int = List(key, offset, value, partition, topic, timestamp).count(_.isDefined)
  }

  /**
    * Create a new kafka source exposing a source with implicit commit.
    *
    * @param config source configuration.
    */
  def plain(config: Config)(implicit system: ActorSystem): Source[StreamEvent, Consumer.Control] = {
    // Set consumer settings
    val consumerSettings = config.toConsumerSettings()
      .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")

    // Run source
    Consumer
      .plainSource(consumerSettings, config.topics.toSubscription)
      .map { record =>
        val binding = config.binding
        val builder: JsObjectBuilder = Json.objectBuilder(binding.numberOfBind)
        binding.key.foreach(k => builder += k -> record.key())
        binding.offset.foreach(k => builder += k -> record.offset())
        binding.value.foreach(k => builder += k -> ByteString.fromArrayUnsafe(record.value()))
        binding.partition.foreach(k => builder += k -> record.partition())
        binding.topic.foreach(k => builder += k -> record.topic())
        binding.timestamp.foreach(k => builder += k -> record.timestamp())
        StreamEvent(builder.result())
      }
  }

  /**
    * Create a new kafka source exposing a source per partition with implicit commit.
    *
    * @param config source configuration.
    */
  def plainPartitioned(config: Config)(implicit system: ActorSystem): Source[(TopicPartition, Source[StreamEvent, NotUsed]), Consumer.Control] = {
    // Set consumer settings
    val consumerSettings = config.toConsumerSettings
      .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true")

    // Run source
    Consumer
      .plainPartitionedSource(consumerSettings, config.topics.toSubscription)
      .map {
        case (topicPartition, source) => (topicPartition, source.map { record =>
          val binding = config.binding
          val builder: JsObjectBuilder = Json.objectBuilder(binding.numberOfBind)
          binding.key.foreach(k => builder += k -> record.key())
          binding.offset.foreach(k => builder += k -> record.offset())
          binding.value.foreach(k => builder += k -> ByteString.fromArrayUnsafe(record.value()))
          binding.partition.foreach(k => builder += k -> record.partition())
          binding.topic.foreach(k => builder += k -> record.topic())
          binding.timestamp.foreach(k => builder += k -> record.timestamp())
          StreamEvent(builder.result())
        })
      }
  }

  /**
    * Create a new kafka source exposing a source with explicit commit.
    *
    * @param config source configuration.
    */
  def committable(config: Config)(implicit system: ActorSystem): Source[StreamEvent, Consumer.Control] = {
    // Set consumer settings
    val consumerSettings = config.toConsumerSettings
      .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")

    // Adapted source
    Consumer
      .committableSource(consumerSettings, config.topics.toSubscription)
      .map { msg =>
        val record = msg.record
        val binding = config.binding
        val builder: JsObjectBuilder = Json.objectBuilder(binding.numberOfBind)
        binding.key.foreach(k => builder += k -> record.key())
        binding.offset.foreach(k => builder += k -> record.offset())
        binding.value.foreach(k => builder += k -> ByteString.fromArrayUnsafe(record.value()))
        binding.partition.foreach(k => builder += k -> record.partition())
        binding.topic.foreach(k => builder += k -> record.topic())
        binding.timestamp.foreach(k => builder += k -> record.timestamp())
        StreamEvent(builder.result()).mutate(CommittableOffsetKey, msg.committableOffset)
      }
  }

  /**
    * Create a new kafka source exposing a source per partition with explicit commit.
    *
    * @param config source configuration.
    */
  def committablePartitioned(config: Config)(implicit system: ActorSystem): Source[(TopicPartition, Source[StreamEvent, NotUsed]), Consumer.Control] = {
    // Set consumer settings
    val consumerSettings = config.toConsumerSettings
      .withProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")

    // Adapted source
    Consumer
      .committablePartitionedSource(consumerSettings, config.topics.toSubscription)
      .map {
        case (topicPartition, source) => (topicPartition, source.map { msg =>
          val record = msg.record
          val binding = config.binding
          val builder: JsObjectBuilder = Json.objectBuilder(binding.numberOfBind)

          binding.key.foreach(k => builder += k -> record.key())
          binding.offset.foreach(k => builder += k -> record.offset())
          binding.value.foreach(k => builder += k -> ByteString.fromArrayUnsafe(record.value()))
          binding.partition.foreach(k => builder += k -> record.partition())
          binding.topic.foreach(k => builder += k -> record.topic())
          binding.timestamp.foreach(k => builder += k -> record.timestamp())
          StreamEvent(builder.result()).mutate(CommittableOffsetKey, msg.committableOffset)
        })
      }
  }

}
