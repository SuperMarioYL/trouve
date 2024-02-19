#!/bin/bash

JAVA_HOME=$JAVA_11_HOME mvn clean deploy -P ossrh -DskipTests