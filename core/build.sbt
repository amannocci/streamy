/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017
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

import Dependencies._

name := name.value + "-core"

// Custom resolvers
resolvers ++= Seq(
  "Techcode" at "https://nexus.techcode.io/repository/maven-public"
)

// All akka libraries
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor", // Apache 2 License
  "com.typesafe.akka" %% "akka-stream", // Apache 2 License
  "com.typesafe.akka" %% "akka-slf4j" // Apache 2 License
).map(_ % akkaVersion % Compile)

// All jackson libraries
libraryDependencies ++= Seq(
  "com.fasterxml.jackson.core" % "jackson-core", // Apache 2 License
  "com.fasterxml.jackson.core" % "jackson-databind" // Apache 2 License
).map(_ % jacksonVersion % Compile)

// All other libraries
libraryDependencies ++= Seq(
  "org.apache.commons" % "commons-lang3" % commonsLangVersion, // Apache 2 License
  "ch.qos.logback" % "logback-classic" % logbackVersion, // EPL/LGPL License
  "io.techcode.logback.contrib" % "logback-json-layout" % logbackContribVersion, // MIT License
  "com.google.guava" % "guava" % guavaVersion, // Apache 2 License
  "nl.grons" %% "metrics-scala" % metricsScalaVersion, // Apache 2 License
  "io.dropwizard.metrics" % "metrics-jvm" % metricsJvmVersion, // Apache 2 License
  "org.scala-lang" % "scala-reflect" % scalaVersion.value
).map(_ % Compile)

// Add container contraints detection
bashScriptExtraDefines +=
  """
    |ceiling() {
    |  awk -vnumber="$1" -vdiv="$2" '
    |    function ceiling(x){
    |      return x%1 ? int(x)+1 : x
    |    }
    |    BEGIN{
    |      print ceiling(number/div)
    |    }
    |  '
    |}
    |
    |# Based on the cgroup limits, figure out the max number of core we should utilize
    |core_limit() {
    |  # Manually defined
    |  if [ "JVM_CPU_LIMIT" != x ]; then
    |    echo "${JVM_CPU_LIMIT}"
    |    return
    |  fi
    |
    |  # Number of cpu on host
    |  local cpu_limit="$(nproc)"
    |
    |  # Read cgroups limit
    |  local cpu_period_file="/sys/fs/cgroup/cpu/cpu.cfs_period_us"
    |  local cpu_quota_file="/sys/fs/cgroup/cpu/cpu.cfs_quota_us"
    |  if [ -r "${cpu_period_file}" ]; then
    |    local cpu_period="$(cat ${cpu_period_file})"
    |
    |    if [ -r "${cpu_quota_file}" ]; then
    |      local cpu_quota="$(cat ${cpu_quota_file})"
    |      # cfs_quota_us == -1 --> no restrictions
    |      if [ "x$cpu_quota" != "x-1" ]; then
    |        ceiling "$cpu_quota" "$cpu_period"
    |      else
    |        echo "$cpu_limit"
    |      fi
    |    else
    |      echo "$cpu_limit"
    |    fi
    |  else
    |    echo "$cpu_limit"
    |  fi
    |}
    |
    |memory_limit() {
    |  # Manually defined
    |  if [ "x$JVM_MEMORY_LIMIT" != x ]; then
    |    echo "${JVM_MEMORY_LIMIT}"
    |    return
    |  fi
    |
    |  # Max memory of host
    |  local mem_limit="$(free -b | grep "Mem:" | awk '{print $2;}')"
    |
    |  # High number which is the max limit unti which memory is supposed to be
    |  # unbounded.
    |  local max_mem_unbounded="$(cat /sys/fs/cgroup/memory/memory.memsw.limit_in_bytes)"
    |  local mem_file="/sys/fs/cgroup/memory/memory.limit_in_bytes"
    |  if [ -r "${mem_file}" ]; then
    |    local max_mem="$(cat ${mem_file})"
    |    if [ ${max_mem} -lt ${max_mem_unbounded} ]; then
    |      echo "${max_mem}"
    |    else
    |      echo "${mem_limit}"
    |    fi
    |  else
    |    echo "${mem_limit}"
    |  fi
    |}
    |
    |# Cpu Limit
    |export JVM_CPU_LIMIT="$(core_limit)"
    |export JVM_CPU_LIMIT_X2="$((2 * $JVM_CPU_LIMIT))"
    |echo "Jvm Cpu Limit: ${JVM_CPU_LIMIT}"
    |echo "Jvm Cpu Limit * 2: ${JVM_CPU_LIMIT_X2}"
    |addJava "-XX:ParallelGCThreads=${JVM_CPU_LIMIT}"
    |addJava "-XX:ConcGCThreads=${JVM_CPU_LIMIT}"
    |addJava "-Djava.util.concurrent.ForkJoinPool.common.parallelism=${JVM_CPU_LIMIT}"
    |
    |# Memory Limit
    |export JVM_MEMORY_LIMIT="$(memory_limit)"
    |echo "Jvm Memory Limit: ${JVM_MEMORY_LIMIT}"
    |max_mem="${JVM_MEMORY_LIMIT}"
    |ratio=${JVM_MEMORY_RATIO:-50}
    |mx=$(echo "${max_mem} ${ratio} 1048576" | awk '{printf "%d\n" , ($1*$2)/(100*$3) + 0.5}')
    |addJava "-Xmx${mx}M"
    |unset max_mem
    |unset ratio
    |unset mx
  """.stripMargin

// Enable some plugins
enablePlugins(JavaAppPackaging, JmhPlugin)