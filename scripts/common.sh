#!/usr/bin/env bash

info() {
  echo -e "\e[32mo $1\e[0m"
}

error() {
  echo -e "\e[31mx $1\e[0m"
}

is_install() {
  # set to 1 initially
  local return_=1
  # set to 0 if not found
  type "$1" >/dev/null 2>&1 || { local return_=0; }
  # return value
  echo "$return_"
}

# Run a command and check for fail
function try {
  "$@"
  if [ $? -ne 0 ]; then
    error "The project isn't valid"
    exit 1
  fi
}

info "Loading common"

# Correct path
info "Correcting working directory"
cd "$(dirname "$0")"
BASE_DIR=$PWD
BASE_PROJECT=$(dirname "$BASE_DIR")