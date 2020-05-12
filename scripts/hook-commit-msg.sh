#!/usr/bin/env bash

# Found current script directory
RELATIVE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

# Load common
# shellcheck disable=SC1090
source "${RELATIVE_DIR}/../../scripts/common.sh"

# Constants
FILENAME=${BASE_PROJECT}/.git/${1##*/}

# Validate commit message prefix
test "" != "$(grep -E '^\[(Added|Updated|Removed|Improved|Fixed|Released)\]' "${FILENAME}")" || {
  log_failure "Use an allowed prefix in commit message (Added|Fixed|Updated|Removed|Improved|Released)"
  exit 1
}
log_success "The commit message is correct"

exit 0
