#!/usr/bin/env bash

# kill previously running ncats
pkill ncat

# Certificate files' path relative to tcp tests
WORKING_DIR=$(pwd)
CERT_FILES="$WORKING_DIR/plugin-tcp/src/test/resources"

# Run ncats and redirect output to /dev/null
ncat -lk 5000 > /dev/null &
ncat -l 5001 -i 3s --ssl --ssl-cert "$CERT_FILES/test-cert.pem" --ssl-key "$CERT_FILES/test-key.pem" > /dev/null &
ncat -l 5002 -i 3s --ssl --ssl-cert "$CERT_FILES/test-cert.pem" --ssl-key "$CERT_FILES/test-key.pem" > /dev/null &
ncat -l 5003 -i 3s --ssl --ssl-cert "$CERT_FILES/test-cert.pem" --ssl-key "$CERT_FILES/test-key.pem" > /dev/null &
ncat -l 5004 -i 3s --ssl --ssl-cert "$CERT_FILES/test-cert.pem" --ssl-key "$CERT_FILES/test-key.pem" > /dev/null &
