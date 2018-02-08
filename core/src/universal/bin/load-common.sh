#!/usr/bin/env bash

shopt -s nullglob

# Determine if a command is available
function is_install {
  # set to 1 initially
  local return_=1
  # set to 0 if not found
  type $1 >/dev/null 2>&1 || { local return_=0; }
  # return value
  echo "$return_"
}

# Log something in json format
function log {
  echo "{\"level\":\"$1\",\"type\":\"entrypoint\",\"message\":\"$2\"}"
}
