#!/usr/bin/env bats

source core/src/universal/bin/setup-container.sh

@test "detect core limit when undefined" {
  core_limit
  local proc=$(nproc)
  [ "$JVM_CPU_LIMIT" == "$(proc)" ]
}

@test "detect core limit when defined" {
  export JVM_CPU_LIMIT=2
  core_limit
  [ "$JVM_CPU_LIMIT" == "2" ]
}

@test "detect mem limit when undefined" {
  memory_limit
  local mem_limit="$(free -b | grep "Mem:" | awk '{print $2;}')"
  [ "$JVM_MEM_LIMIT" == "$(mem_limit)" ]
}

@test "detect mem limit when defined" {
  export JVM_MEM_LIMIT=2
  memory_limit
  [ "$JVM_MEM_LIMIT" == "2" ]
}