#!/bin/bash
# Run ALL unit tests (logic + Robolectric UI verification) with JUnitCore.
#
# Why not `gradle testDebugUnitTest`? On the build VM the Gradle test
# executor's result channel (loopback) is intercepted and the executor dies
# with a NullPointerException in TestResultProcessor — the tests themselves
# are fine. JUnitCore direct execution is the established route (same as the
# other projects on this VM). Builds go through build.sh, which serializes
# via ~/workspace/tools/with-build-lock.sh.
set -e
cd "$(dirname "$0")"

./build.sh :app:assembleDebug :app:compileDebugUnitTestKotlin :app:writeTestClasspath

# Classpath notes:
# - the AGP-generated test_config.properties directory must be present or
#   Robolectric cannot find the merged manifest/resources;
# - the SDK stub android.jar goes LAST: JUnit parses @Config defaults in the
#   system classloader and needs android.app.Application to exist there
#   (inside the tests, Robolectric's sandbox supplies the real framework).
ANDROID_HOME="${ANDROID_HOME:-$HOME/workspace/tools/android-sdk}"
RAW_CP="$(cat app/build/test-classpath.txt)"
# The dumped configuration classpath lists .aar files as-is; the JVM cannot
# load classes from them, so extract each AAR's classes.jar once and put
# those on the classpath instead (RoborazziRule etc. live in there).
mkdir -p app/build/aar-jars
AAR_JARS=""
IFS=':' read -ra ENTRIES <<< "$RAW_CP"
for e in "${ENTRIES[@]}"; do
  if [[ "$e" == *.aar ]]; then
    name="$(basename "$e" .aar)"
    out="app/build/aar-jars/$name.jar"
    if [ ! -f "$out" ]; then
      (cd app/build/aar-jars && unzip -o -q "$e" classes.jar && mv classes.jar "$name.jar")
    fi
    AAR_JARS="$AAR_JARS:$PWD/$out"
  fi
done
ROOT="$PWD"
CP="$ROOT/app/build/tmp/kotlin-classes/debug:$ROOT/app/build/tmp/kotlin-classes/debugUnitTest:$ROOT/app/build/intermediates/unit_test_config_directory/debugUnitTest/generateDebugUnitTestConfig/out:$RAW_CP$AAR_JARS:$ANDROID_HOME/platforms/android-35/android.jar"
export JAVA_HOME=~/workspace/tools/jdk21
# Robolectric resolves the merged manifest/resource paths from
# test_config.properties relative to the working directory, so — exactly
# like the Gradle test task — the JVM must run in the app module dir.
cd app
"$JAVA_HOME/bin/java" -Xmx1g -Dfile.encoding=UTF-8 -Djava.net.preferIPv4Stack=true \
  -Drobolectric.offline=true \
  -Drobolectric.dependency.dir="$ROOT/.local/robolectric-deps" \
  -Droborazzi.test.record=true \
  -cp "$CP" org.junit.runner.JUnitCore \
  com.taigashinohara.otonalevel100.LevelSystemTest \
  com.taigashinohara.otonalevel100.SessionDesignTest \
  com.taigashinohara.otonalevel100.ReviewLogicTest \
  com.taigashinohara.otonalevel100.QuestionsTest \
  com.taigashinohara.otonalevel100.LocalDayTest \
  com.taigashinohara.otonalevel100.UiVerificationTest
