#!/usr/bin/env bash

# Load common
source ${PWD}/scripts/common.sh

# Remove every target in sub projects
function clean {
  rm -rf $1 2> /dev/null
}
clean ${BASE_PROJECT}/core/target
clean ${BASE_PROJECT}/plugin-fingerprint/target
clean ${BASE_PROJECT}/plugin-json/target
clean ${BASE_PROJECT}/plugin-metric/target
clean ${BASE_PROJECT}/plugin-syslog/target
clean ${BASE_PROJECT}/plugin-graphite/target
clean ${BASE_PROJECT}/plugin-elasticsearch/target
clean ${BASE_PROJECT}/project/target
clean ${BASE_PROJECT}/project/project

exit 0
