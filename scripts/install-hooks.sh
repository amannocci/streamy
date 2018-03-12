#!/usr/bin/env bash

# Load common
source "${PWD}/scripts/common.sh"

# Constants
HOOK_DIR=${BASE_PROJECT}/.git/hooks

# Create directory
mkdir -p "${HOOK_DIR}"

# Remove all old hooks before anything
info "Removing old hooks"
rm -f "${HOOK_DIR}/commit-msg"
rm -f "${HOOK_DIR}/pre-commit"

# Copy new ones
info "Copy new hooks"
cp "${BASE_DIR}/hook-commit-msg.sh" "${HOOK_DIR}/commit-msg"
cp "${BASE_DIR}/hook-pre-commit.sh" "${HOOK_DIR}/pre-commit"

exit 0