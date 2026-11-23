#!/bin/bash
# OtonaLevel100 build helper for this VM (same conventions as Scriptlet, see
# AGENTS.md "Android build in this VM"): local mirror + IPv4 + in-process
# Gradle 8.9 (8.14.3 forks a single-use daemon whose TCP is intercepted).
set -e
cd "$(dirname "$0")"
export OTONA_LOCAL_MIRROR=1
export JAVA_HOME=~/workspace/tools/jdk21
export ANDROID_HOME=~/workspace/tools/android-sdk
# Project-local Gradle home: the shared home's journal lock still collides
# with builds that have not adopted the serialization wrapper yet (observed
# 2026-11-23 during the verification rollout). The flock wrapper below
# serializes execution; the local home removes the journal contention.
export GRADLE_USER_HOME=~/workspace/otona-level/.local/gradle-home
export GRADLE_OPTS="-Xmx768m -Dfile.encoding=UTF-8 -Djava.net.preferIPv4Stack=true"
if ! curl -s -o /dev/null --max-time 3 http://127.0.0.1:18080/central/; then
  (cd ~/workspace/tools && nohup python3 mirror.py > /tmp/mirror.log 2>&1 &)
  sleep 1
fi
# Build serialization rule (AGENTS.md, 2026-11-23): only one build at a time
# on this VM, enforced by the shared flock wrapper.
exec ~/workspace/tools/with-build-lock.sh \
  ~/workspace/tools/gradle-8.9/bin/gradle "$@" \
  -Dorg.gradle.daemon=false -Dkotlin.daemon.enabled=false --max-workers=1
