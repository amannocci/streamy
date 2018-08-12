#!/usr/bin/env bash

# Load common
source "${PWD}/scripts/common.sh"

# Constants
SCRIPT_DIR=${BASE_PROJECT}/scripts

info "Make scripts executable"
chmod +x -R "${SCRIPT_DIR}"

# Copy new ones
info "Install hooks"
cd "${BASE_PROJECT}"
bash "${SCRIPT_DIR}/install-hooks.sh"

exit 0
