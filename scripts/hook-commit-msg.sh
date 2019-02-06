#!/usr/bin/env bash

# Load common
source "${PWD}/scripts/common.sh"

# Constants
FILENAME=${BASE_PROJECT}/${1##*/}

# Validate commit message prefix
test "" != "$(grep -E '^\[(Added|Updated|Removed|Improved|Fixed|Released)\]' "${FILENAME}")" || {
  error "Use an allowed prefix in commit message (Added|Fixed|Updated|Removed|Improved|Released)"
  exit 1
}
info "The commit message is correct"

exit 0
