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
package io.techcode.streamy.kafka.event

import akka.actor.DeadLetterSuppression
import org.apache.kafka.common.TopicPartition

/**
  * Kafka events.
  */
object KafkaEvent {

  // Marker interface for kafka events
  sealed trait All extends DeadLetterSuppression

  object Consumer {

    /**
      * This event is fired when a partition topic is revoked.
      *
      * @param topicPartition partition topic is revoked.
      */
    case class TopicPartitionRevoke(topicPartition: TopicPartition) extends All

    /**
      * This event is fired when a partition topic is assigned.
      *
      * @param topicPartition partition topic is assigned.
      */
    case class TopicPartitionAssign(topicPartition: TopicPartition) extends All

    /**
      * This event is fired when a partition topic is losted.
      *
      * @param topicPartition partition topic is losted.
      */
    case class TopicPartitionLost(topicPartition: TopicPartition) extends All

    /**
      * This event is fired when a partition topic is stopped.
      *
      * @param topicPartition partition topic is stopped.
      */
    case class TopicPartitionStop(topicPartition: TopicPartition) extends All

  }

}
