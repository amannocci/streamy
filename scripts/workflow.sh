#!/usr/bin/env bash

# Found current script directory
RELATIVE_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

# Load common
# shellcheck disable=SC1090
source "${RELATIVE_DIR}/common.sh"

function help() {
  echo "-- Help Menu"
  echo "> 1. ./scripts/workflow.sh setup    | Setup the project"
  echo "> 2. ./scripts/workflow.sh build    | Build the project"
  echo "> 3. ./scripts/workflow.sh test     | Run tests on this project"
  echo "> 4. ./scripts/workflow.sh package  | Create all packages for this project"
  echo "> 5. ./scripts/workflow.sh release  | Prepare project for release"
  echo "> 6. ./scripts/workflow.sh help     | Display this help menu"
}

function check() {
  log_action "Checking if needed commands are installs"
  command_is_present "java"
  command_is_present "sbt"
  if [ "${1}" == "test" ]; then
    command_is_present "bats"
  fi
  if [ "${1}" == "package" ]; then
    command_is_present "dpkg-deb"
  fi
}

function setup() {
  # Constants
  HOOK_DIR=${BASE_PROJECT}/.git/hooks

  # Create directory
  mkdir -p "${HOOK_DIR}"

  # Remove all old hooks before anything
  log_success "remove old hooks"
  rm -f "${HOOK_DIR}/commit-msg"
  rm -f "${HOOK_DIR}/pre-commit"

  # Copy new ones
  log_success "copy new hooks"
  cp "${RELATIVE_DIR}/hook-commit-msg.sh" "${HOOK_DIR}/commit-msg"
  cp "${RELATIVE_DIR}/hook-pre-commit.sh" "${HOOK_DIR}/pre-commit"
}

function build() {
  try "run build" sbt -batch -mem 2048 compile
}

function test() {
  try "run tests" sbt -batch -mem 2048 test
}

function package() {
  case "${1}" in
    debian)
      try "create a debian package" sbt -batch -mem 2048 debian:packageBin
      ;;
    universal)
      try "create a universal package" sbt -batch -mem 2048 universal:packageBin
      ;;
    *)
      try "create a universal package" sbt -batch -mem 2048 universal:packageBin
      try "create a debian package" sbt -batch -mem 2048 debian:packageBin
      ;;
  esac
}

function release() {
  current_version=$(grep -oP 'version := "(.*)"' "${BASE_PROJECT}/build.sbt" | cut -f2 -d'"')
  echo "Current version: ${current_version}"
  echo -n "New version: "
  read -r new_version
  grep -R "version = \"${current_version}\"" "${BASE_PROJECT}" | cut -f1 -d':' | xargs -i sed -i "s/version = \"${current_version}\"/version = \"${new_version}\"/g" {}
  sed -i "s/version := \"${current_version}\"/version := \"${new_version}\"/g" "${BASE_PROJECT}/build.sbt"
}

# Parse argument
arg="${1}"
if [[ -z "${arg}" ]] ; then
  echo "Expected arg to be present"
  help
else
  case "${arg}" in
    help)
      help
      ;;
    setup)
      setup
      ;;
    build)
      check "build"
      build
      ;;
    test)
      check "test"
      test
      ;;
    package)
      check "package"
      package "${2}"
      ;;
    release)
      release
      ;;
    *)
      echo "Unknown argument: ${arg}"
      help
      ;;
  esac
fi
