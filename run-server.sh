#!/bin/sh

SCRIPT_DIR=$(cd $(dirname $0);pwd)
cd ${SCRIPT_DIR}/spider-server
lein ring server-headless 3100
