#!/usr/bin/env bash

# Current script location
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# Load common
source ${DIR}/load-common.sh

# Create default file if not exist
function make_default {
  local app_conf_file=${APP_CONF_FILE:-conf/application.conf}
  touch $app_conf_file || exit
}