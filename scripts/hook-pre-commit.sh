#!/usr/bin/env bash

# Load common
source ${PWD}/scripts/common.sh

# Constants
if [[ $BASE_PROJECT == *".git"* ]]; then
  BASE_PROJECT=$(dirname "$BASE_PROJECT")
fi
cd ${BASE_PROJECT}

# Validate bash project
try bats core/src/test/bash

# Validate scala project
try ./sbt ";clean;compile;test;scalastyle"
info "The project is valid"
