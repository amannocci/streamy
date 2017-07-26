#!/usr/bin/env bash

# Load common
source ${PWD}/scripts/common.sh

# Constants
HOOK_DIR=${BASE_PROJECT}/.git/hooks

# Remove all old hooks before anything
info "Removing old hooks"
rm -f ${HOOK_DIR}/commit-msg

# Copy new ones
info "Copy new hooks"
cp ${BASE_DIR}/hook-commit-msg.sh ${HOOK_DIR}/commit-msg

exit 0