#!/bin/bash

VERSION=1.0.3

mvn versions:set -DnewVersion=$VERSION
mvn clean install -DskipTests source:jar