#!/usr/bin/env bash

# goto project root
cd ${BASH_SOURCE%/*}/..

echo "Kill all running sbt"
ps aux | grep sbt | grep Cellar | grep -v grep | awk '{print $2}' | xargs -I{} kill {}



