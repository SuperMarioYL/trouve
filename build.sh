#!/bin/bash

VERSION=1.1.0

JAVA_HOME=$JAVA_11_HOME mvn versions:set -DnewVersion=$VERSION
JAVA_HOME=$JAVA_11_HOME mvn --batch-mode clean install -DskipTests source:jar -Dmaven.javadoc.skip=true