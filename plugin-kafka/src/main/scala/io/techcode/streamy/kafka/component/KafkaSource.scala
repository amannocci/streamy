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

import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.CommittableOffset
import akka.kafka._
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.kafka.scaladsl.{Committer, Consumer}
import akka.stream.scaladsl.{Flow, Sink}
import akka.util.ByteString
import akka.{Done, NotUsed}
import io.techcode.streamy.event.{AttributeKey, StreamEvent}
import io.techcode.streamy.kafka.event.KafkaEvent
import io.techcode.streamy.util.json._
import org.apache.kafka.clients.consumer.ConsumerConfig
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
    handler: Flow[StreamEvent, StreamEvent, NotUsed],
    bootstrapServers: String,
    groupId: String,
    autoOffsetReset: String = "latest",
    topics: TopicConfig,
    binding: Binding = Binding(),
    maxPartitions: Int = DefaultMaxPartition
  )

  // Generic topic configuration
  trait TopicConfig {
    def toSubscription: AutoSubscription
  }

  // Static topic configuration
  case class StaticTopicConfig(
    statics: Set[String]
  ) extends TopicConfig {
    override def toSubscription: AutoSubscription = Subscriptions.topics(statics)
  }

  // Topic pattern configuration
  case class PatternTopicConfig(
    pattern: String
  ) extends TopicConfig {
    override def toSubscription: AutoSubscription = Subscriptions.topicPattern(pattern)
  }

  // Component binding
  case class Binding(
    key: Option[String] = None,
    offset: Option[String] = None,
    value: Option[String] = None,
    partition: Option[String] = None,
    topic: Option[String] = None,
    timestamp: Option[String] = None
  )

  /**
    * Create a new kafka source with at least once semantics.
    *
    * @param config source configuration.
    */
  def atLeastOnce(config: Config)(implicit system: ActorSystem): Consumer.DrainingControl[Done] = {
    // Set consumer settings
    val consumerSettings = ConsumerSettings(system, new StringDeserializer, new ByteArrayDeserializer)
      .withBootstrapServers(config.bootstrapServers)
      .withGroupId(config.groupId)
      .withStopTimeout(Duration.Zero)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, config.autoOffsetReset)

    // Set committer settings
    val committerSettings = CommitterSettings(system)

    // Default flow
    val process: Flow[ConsumerMessage.CommittableMessage[String, Array[Byte]], StreamEvent, NotUsed] =
      Flow[ConsumerMessage.CommittableMessage[String, Array[Byte]]]
        .map { msg =>
          val record = msg.record
          val binding = config.binding
          implicit val builder: JsObjectBuilder = Json.objectBuilder()

          binding.key.foreach(bind => builder += bind -> record.key())
          binding.offset.foreach(bind => builder += bind -> record.offset())
          binding.value.foreach(bind => builder += bind -> ByteString.fromArrayUnsafe(record.value()))
          binding.partition.foreach(bind => builder += bind -> record.partition())
          binding.topic.foreach(bind => builder += bind -> record.topic())
          binding.timestamp.foreach(bind => builder += bind -> record.timestamp())
          StreamEvent(builder.result()).mutate(CommittableOffsetKey, msg.committableOffset)
        }

    // Run source
    Consumer
      .committablePartitionedSource(consumerSettings, config.topics.toSubscription)
      .mapAsyncUnordered(config.maxPartitions) {
        case (topicPartition, source) =>
          system.eventStream.publish(KafkaEvent.Consumer.TopicPartitionConsume(topicPartition, running = true))
          source
            .via(process)
            .via(config.handler)
            .map(_.attribute(CommittableOffsetKey).get) // Must exist in any case
            .alsoTo(Sink.onComplete { _ =>
              system.eventStream.publish(KafkaEvent.Consumer.TopicPartitionConsume(topicPartition, running = false))
            })
            .runWith(Committer.sink(committerSettings))
      }
      .toMat(Sink.ignore)(DrainingControl.apply)
      .run()
  }

}
