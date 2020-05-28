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
package io.techcode.streamy.config

import java.nio.file.Path

import com.typesafe.config.Config

import scala.concurrent.duration.FiniteDuration

/**
  * Streamy configuration
  */
case class StreamyConfig(
  lifecycle: StreamyConfig.Lifecycle,
  folder: StreamyConfig.Folder,
  monitor: StreamyConfig.Monitor,
  plugin: Config
)

object StreamyConfig {

  // Streamy lifecycle config
  case class Lifecycle(
    gracefulTimeout: FiniteDuration,
    shutdownTimeout: FiniteDuration
  )

  // Streamy folder config
  case class Folder(
    data: Path,
    conf: Path,
    plugin: Path
  )

  // Streamy monitor config
  case class Monitor(
    process: ProcessMonitor,
    os: OsMonitor,
    jvm: JvmMonitor,
    garbageCollector: GarbageCollectorMonitor
  )

  // Streamy process monitor config
  case class ProcessMonitor(
    enabled: Boolean = true,
    refreshInterval: FiniteDuration
  )

  // Streamy os monitor config
  case class OsMonitor(
    enabled: Boolean = true,
    refreshInterval: FiniteDuration
  )

  // Stream jvm monitor config
  case class JvmMonitor(
    enabled: Boolean = true,
    refreshInterval: FiniteDuration
  )

  // Stream garbage collector monitor config
  case class GarbageCollectorMonitor(
    enabled: Boolean = true,
    refreshInterval: FiniteDuration,
    thresholdWarn: Short,
    thresholdInfo: Short,
    thresholdDebug: Short
  ) {
    require(thresholdDebug >= 0 && thresholdDebug <= 100)
    require(thresholdInfo >= 0 && thresholdInfo <= 100)
    require(thresholdWarn >= 0 && thresholdWarn <= 100)
    require(thresholdDebug < thresholdInfo)
    require(thresholdInfo < thresholdWarn)
  }

}
