#!/usr/bin/env bash

# Current script location
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Load common
source "${DIR}/load-common.sh"

# Make template if supported
function make_template {
  if [ "$(is_install envtpl)" == 1 ]; then
    log "info" "envtpl is installed"
    for file in $PWD/conf/*.tpl; do
      log "info" "Make template $file"
      dirname="$(dirname "$file")"
      filename="$(basename "$file")"
      filename="${filename%.*}"
      try envtpl < "$file" > "$dirname/$filename"
    done
  else
    log "info" "envtpl isn't installed"
  fi
}