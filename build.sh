#!/bin/bash

VERSION=1.0.5

JAVA_HOME=$JAVA_11_HOME mvn versions:set -DnewVersion=$VERSION
JAVA_HOME=$JAVA_11_HOME mvn clean install -DskipTests source:jar