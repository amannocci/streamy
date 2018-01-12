#!/usr/bin/env bash

# Load common
source ${PWD}/scripts/common.sh

# Constants
if [[ $BASE_PROJECT == *".git"* ]]; then
  BASE_PROJECT=$(dirname "$BASE_PROJECT")
fi
cd ${BASE_PROJECT}

# Validate bash project
bats core/src/test/bash
if [ $? -ne 0 ]
then
  error "The project isn't valid"
  exit 1
fi

# Validate scala project
./sbt ";clean;compile;test;scalastyle"
if [ $? -ne 0 ]
then
  error "The project isn't valid"
  exit 1
else
  info "The project is valid"
fi
