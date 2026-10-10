#!/usr/bin/env bash
# FoodFrenzy Maven Wrapper
set -e

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

if [ -z "$JAVA_HOME" ]; then
    if [ -d "/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home" ]; then
        export JAVA_HOME="/Library/Java/JavaVirtualMachines/temurin-25.jdk/Contents/Home"
    elif [ -x "/usr/libexec/java_home" ]; then
        export JAVA_HOME="$(/usr/libexec/java_home 2>/dev/null || true)"
    fi
fi

if [ -n "$JAVA_HOME" ]; then
    export PATH="$JAVA_HOME/bin:$PATH"
fi

mkdir -p "$DIR/.m2/repository"

MVN_CMD="$DIR/.tools/maven/bin/mvn"
if [ ! -x "$MVN_CMD" ]; then
    MVN_CMD="mvn"
fi

exec "$MVN_CMD" -Dmaven.repo.local="$DIR/.m2/repository" -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Dmaven.artifact.threads=8 "$@"
