#!/usr/bin/env bash

# Load common
source ${PWD}/scripts/common.sh

# Constants
SCRIPT_DIR=${BASE_PROJECT}/scripts

# Changing permission
info "Make sbt executable"
chmod +x ${BASE_PROJECT}/sbt
chmod +x -R ${BASE_PROJECT}/sbt-dist

info "Make scripts executable"
chmod +x -R ${SCRIPT_DIR}

# Copy new ones
info "Install hooks"
cd ${BASE_PROJECT}
${SCRIPT_DIR}/install-hooks.sh

exit 0
