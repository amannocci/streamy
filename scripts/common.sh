#!/usr/bin/env bash

# Found current script directory
RELATIVE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
export RELATIVE_DIR

# Found project directory
BASE_PROJECT="$(dirname "${RELATIVE_DIR}")"
export BASE_PROJECT

function log_action() {
  echo -e "\033[33m⇒\033[0m $*"
}

function log_failure() {
  echo -e "\033[31m✗\033[0m Failed to $*"
}

function log_success() {
  echo -e "\033[32m✓\033[0m Succeeded to $*"
}

function command_is_present() {
  if ! [ -x "$(command -v "${1}")" ]; then
    log_failure "locate command '${1}'" >&2
    exit 1
  fi
}

function try() {
  "${@:2}"
  status="$?"
  if [ "$status" -eq 0 ]; then
    log_success "${1}"
  else
    log_failure "${1}"
    exit "$status"
  fi
}

log_action "The script you are running has basename $(basename "$0"), dirname $(dirname "$0")"
log_action "The present working directory is $(pwd)"
