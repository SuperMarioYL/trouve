#!/bin/bash

JAVA_HOME=$JAVA_11_HOME mvn -s .github/maven/settings.xml --batch-mode clean deploy -DskipTests