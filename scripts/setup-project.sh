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

# Create symbolic links
info "Create symbolic links"
ln -sfn "${BASE_PROJECT}/core/runtime/plugin" "${BASE_PROJECT}/plugin"

exit 0
