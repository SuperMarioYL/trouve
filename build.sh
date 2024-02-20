#!/bin/bash

VERSION=1.0.13

JAVA_HOME=$JAVA_11_HOME mvn versions:set -DnewVersion=$VERSION
JAVA_HOME=$JAVA_11_HOME mvn --batch-mode clean install -DskipTests source:jar -Dmaven.javadoc.skip=true