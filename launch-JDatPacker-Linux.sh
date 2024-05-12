#!/bin/sh
set -e
SCRIPTDIR="${0%/*}"
cd "$SCRIPTDIR"
java -jar "JDatPacker-0.2.2.jar" "$@"
