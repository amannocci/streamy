#!/usr/bin/env bash

# Load common
source ${PWD}/scripts/common.sh

# Constants
if [[ $BASE_PROJECT == *".git"* ]]; then
  BASE_PROJECT=$(dirname "$BASE_PROJECT")
fi
cd ${BASE_PROJECT}

# Validate project
./sbt ";compile;test;scalastyle"
if [ $? -ne 0 ]
then
  error "The project isn't valid"
else
  info "The project is valid"
fi
