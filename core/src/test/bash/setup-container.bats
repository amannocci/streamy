#!/usr/bin/env bats

source core/src/universal/bin/setup-container.sh

@test "detect core limit when undefined based on sys" {
  export JVM_CPU_LIMIT=$(core_limit)
  [ "$JVM_CPU_LIMIT" == "$(nproc)" ]
}

@test "detect core limit when undefined based on cgroups" {
  export SYS_CPU_PERIOD_FILE=$(mktemp)
  export SYS_CPU_QUOTA_FILE=$(mktemp)
  echo '100000' > ${SYS_CPU_PERIOD_FILE}
  echo '200000' > ${SYS_CPU_QUOTA_FILE}
  export JVM_CPU_LIMIT=$(core_limit)
  [ "$JVM_CPU_LIMIT" == "2" ]
  rm ${SYS_CPU_PERIOD_FILE}
  rm ${SYS_CPU_QUOTA_FILE}
}

@test "detect core limit when defined" {
  export JVM_CPU_LIMIT=2
  export JVM_CPU_LIMIT=$(core_limit)
  [ "$JVM_CPU_LIMIT" == "2" ]
}

@test "detect mem limit when undefined based on sys" {
  export JVM_MEMORY_LIMIT=$(memory_limit)
  [ "$JVM_MEMORY_LIMIT" == "$(awk '/MemTotal/ {printf "%.0f", $2*1024}' /proc/meminfo)" ]
}

@test "detect mem limit when undefined based on cgroups" {
  export SYS_MAX_MEM_UNBOUNDED_FILE=$(mktemp)
  export SYS_MEM_FILE=$(mktemp)
  echo '134217728' > ${SYS_MAX_MEM_UNBOUNDED_FILE}
  echo '67108864' > ${SYS_MEM_FILE}
  export JVM_MEMORY_LIMIT=$(memory_limit)
  [ "$JVM_MEMORY_LIMIT" == "67108864" ]
  rm ${SYS_MAX_MEM_UNBOUNDED_FILE}
  rm ${SYS_MEM_FILE}
}

@test "detect mem limit when undefined based on cgroups and when swap is disabled" {
  export SYS_MAX_MEM_UNBOUNDED_FILE=$(mktemp)
  export SYS_MEM_FILE=$(mktemp)
  echo '67108864' > ${SYS_MAX_MEM_UNBOUNDED_FILE}
  echo '67108864' > ${SYS_MEM_FILE}
  export JVM_MEMORY_LIMIT=$(memory_limit)
  [ "$JVM_MEMORY_LIMIT" == "67108864" ]
  rm ${SYS_MAX_MEM_UNBOUNDED_FILE}
  rm ${SYS_MEM_FILE}
}

@test "detect mem limit when defined" {
  export JVM_MEMORY_LIMIT=2
  export JVM_MEMORY_LIMIT=$(memory_limit)
  [ "$JVM_MEMORY_LIMIT" == "2" ]
}