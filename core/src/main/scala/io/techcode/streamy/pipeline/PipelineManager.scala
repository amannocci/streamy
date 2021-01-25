/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017-2021
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
package io.techcode.streamy.pipeline

import akka.Done
import akka.actor.{ActorSystem, CoordinatedShutdown, ExtendedActorSystem, Extension, ExtensionId}
import akka.event.Logging
import com.typesafe.config.{Config => TConfig}
import pureconfig._
import pureconfig.generic.auto._

import scala.jdk.javaapi.CollectionConverters._
import java.util.concurrent.ConcurrentHashMap
import scala.concurrent.Future

/**
  * Pipeline manager system.
  */
class PipelineManager(system: ActorSystem) extends Extension {

  // Logging system
  private val log = Logging(system, classOf[PipelineManager])

  // All registries
  private val pipelines = new ConcurrentHashMap[String, Pipeline]

  // Configuration
  private val conf: TConfig = system.settings.config.getConfig("streamy.pipeline")

  /**
    * Start all pipelines.
    */
  private def onStart(): Unit = {
    log.info("Starting all pipelines")
    for (pipelineId <- asScala(conf.root().entrySet()).map(_.getKey)) {
      val pipelineConf = ConfigSource.fromConfig(conf.getConfig(pipelineId)).loadOrThrow[Pipeline.Config]
      val pipeline = new Pipeline(system, pipelineId, pipelineConf)

      try {
        pipeline.onStart()
        pipelines.put(pipelineId, pipeline)
      } catch {
        case ex: PipelineException => log.error(ex.msg)
      }
    }
  }

  /**
    * Stop all pipelines.
    */
  private def onStop(): Unit = {
    log.info("Stopping all pipelines")
    asScala(pipelines.values()).foreach(_.onStop())
    pipelines.clear()
  }

}

/**
  * Pipeline manager companion.
  */
object PipelineManager extends ExtensionId[PipelineManager] {

  /**
    * Is used by Akka to instantiate the Extension identified by this ExtensionId,
    * internal use only.
    */
  def createExtension(system: ExtendedActorSystem): PipelineManager = {
    import system.dispatcher

    val pipelineManager = new PipelineManager(system)
    pipelineManager.onStart()
    CoordinatedShutdown(system)
      .addTask(CoordinatedShutdown.PhaseServiceStop, "pipeline-manager-shutdown") { () =>
        pipelineManager.onStop()
        Future(Done)
      }

    CoordinatedShutdown(system).addJvmShutdownHook(() => pipelineManager.onStop())
    pipelineManager
  }

}
