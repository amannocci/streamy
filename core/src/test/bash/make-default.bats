#!/usr/bin/env bats

source core/src/universal/bin/make-default.sh

@test "create correctly default application configuration if absent" {
  export APP_CONF_FILE=/tmp/application.conf
  rm ${APP_CONF_FILE} || true
  make_default
  if [[ ! -e $APP_CONF_FILE ]]; then exit; fi
  rm ${APP_CONF_FILE}
}

@test "handle correctly application configuration if present" {
  export APP_CONF_FILE=/tmp/application.conf
  echo '1' > ${APP_CONF_FILE}
  make_default
  local test=$(cat ${APP_CONF_FILE})
  [ "$test" == "1" ]
  rm ${APP_CONF_FILE}
}