#!/usr/bin/env bash

shopt -s nullglob

is_install() {
  # set to 1 initially
  local return_=1
  # set to 0 if not found
  type $1 >/dev/null 2>&1 || { local return_=0; }
  # return value
  echo "$return_"
}

# Make template if supported
if [ $(is_install envtpl) == 1 ]; then
  echo "{\"level\":\"info\",\"type\":\"entrypoint\",\"message\":\"envtpl is installed\"}"
  for file in $PWD/conf/*.tpl; do
    echo "{\"level\":\"info\",\"type\":\"entrypoint\",\"message\":\"Make template $file\"}"
    dirname="$(dirname "$file")"
    filename="$(basename "$file")"
    filename="${filename%.*}"
    envtpl < $file > "$dirname/$filename"
  done
else
  echo "{\"level\":\"info\",\"type\":\"entrypoint\",\"message\":\"envtpl isn't installed\"}"
fi
