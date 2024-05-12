#!/bin/sh
set -e
SCRIPTDIR="${0%/*}"
# macOS .command files launch from user home, so switch to script directory
cd "$SCRIPTDIR"
java -jar "JDatPacker-0.2.1.jar" "$@"
