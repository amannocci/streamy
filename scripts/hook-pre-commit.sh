#!/usr/bin/env bash

# Found current script directory
RELATIVE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

# Load common
# shellcheck disable=SC1090
source "${RELATIVE_DIR}/../../scripts/common.sh"

# Constants
if [[ $BASE_PROJECT == *".git"* ]]; then
  BASE_PROJECT=$(dirname "$BASE_PROJECT")
fi
cd "${BASE_PROJECT}"

# Validate new version
HOOK_DIR=${BASE_PROJECT}/.git/hooks
if [ "$(diff "${BASE_PROJECT}/scripts/hook-pre-commit.sh" "${HOOK_DIR}/pre-commit" | wc -l)" -ne 0 ]; then
  log_failure "use current version of scripts\nPlease run './scripts/workflow.sh' setup again !"
  exit 1
fi

# Validate bash project
try "validate bash source" bats -t core/src/test/bash

# Validate scala project
try "clean project" sbt -batch -mem 2048 clean
try "lint scala style" sbt -batch -mem 2048 scalastyle
try "compile project" sbt -batch -mem 2048 compile
try "test project" sbt -batch -mem 2048 test
log_success "validate project"
