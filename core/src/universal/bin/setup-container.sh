#!/usr/bin/env bash

ceiling() {
  awk -vnumber="$1" -vdiv="$2" '
    function ceiling(x){
      return x%1 ? int(x)+1 : x
    }
    BEGIN{
      print ceiling(number/div)
    }
  '
}

# Based on the cgroup limits, figure out the max number of core we should utilize
core_limit() {
  # Manually defined
  if [ "x$JVM_CPU_LIMIT" != x ]; then
    echo "${JVM_CPU_LIMIT}"
    return
  fi

  # Number of cpu on host
  local cpu_limit="$(nproc)"

  # Read cgroups limit
  local cpu_period_file="/sys/fs/cgroup/cpu/cpu.cfs_period_us"
  local cpu_quota_file="/sys/fs/cgroup/cpu/cpu.cfs_quota_us"
  if [ -r "${cpu_period_file}" ]; then
    local cpu_period="$(cat ${cpu_period_file})"

    if [ -r "${cpu_quota_file}" ]; then
      local cpu_quota="$(cat ${cpu_quota_file})"
      # cfs_quota_us == -1 --> no restrictions
      if [ "x$cpu_quota" != "x-1" ]; then
        cpu_limit=$(ceiling "$cpu_quota" "$cpu_period")
      fi
    fi
  fi
  echo "$cpu_limit"
}

memory_limit() {
  # Manually defined
  if [ "x$JVM_MEMORY_LIMIT" != x ]; then
    echo "${JVM_MEMORY_LIMIT}"
    return
  fi

  # Max memory of host
  local mem_limit="$(free -b | grep "Mem:" | awk '{print $2;}')"

  # High number which is the max limit unti which memory is supposed to be  unbounded.
  local max_mem_unbounded="$(cat /sys/fs/cgroup/memory/memory.memsw.limit_in_bytes)"
  local mem_file="/sys/fs/cgroup/memory/memory.limit_in_bytes"
  if [ -r "${mem_file}" ]; then
    local max_mem="$(cat ${mem_file})"
    if [ ${max_mem} -lt ${max_mem_unbounded} ]; then
      mem_limit=${max_mem}
    fi
  fi
  echo "${mem_limit}"
}

# Cpu Limit
export JVM_CPU_LIMIT="$(core_limit)"
export JVM_CPU_LIMIT_X2="$((2 * $JVM_CPU_LIMIT))"
echo "{\"level\":\"info\",\"type\":\"entrypoint\",\"message\":\"Minimum jvm cpu limit: ${JVM_CPU_LIMIT}\"}"
echo "{\"level\":\"info\",\"type\":\"entrypoint\",\"message\":\"Maximum jvm cpu limit: ${JVM_CPU_LIMIT_X2}\"}"
addJava "-XX:ParallelGCThreads=${JVM_CPU_LIMIT}"
addJava "-XX:ConcGCThreads=${JVM_CPU_LIMIT}"
addJava "-Djava.util.concurrent.ForkJoinPool.common.parallelism=${JVM_CPU_LIMIT}"

# Memory Limit
export JVM_MEMORY_LIMIT="$(memory_limit)"
echo "{\"level\":\"info\",\"type\":\"entrypoint\",\"message\":\"Maximum jvm mem limit: ${JVM_MEMORY_LIMIT}\"}"
max_mem="${JVM_MEMORY_LIMIT}"
ratio=${JVM_MEMORY_RATIO:-50}
mx=$(echo "${max_mem} ${ratio} 1048576" | awk '{printf "%d\n" , ($1*$2)/(100*$3) + 0.5}')
addJava "-Xmx${mx}M"
unset max_mem
unset ratio
unset mx