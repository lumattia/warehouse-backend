#!/bin/sh
BASEDIR=$(cd "$(dirname "$0")"; pwd -P)
JAVA_EXE="java"
exec "$JAVA_EXE" -classpath "$BASEDIR/.mvn/wrapper/maven-wrapper.jar" "-Dmaven.multiModuleProjectDirectory=$BASEDIR" org.apache.maven.wrapper.MavenWrapperMain "$@"
