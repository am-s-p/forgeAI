#!/bin/bash
# Automatically use Java 21 to avoid class version errors
export JAVA_HOME=/opt/homebrew/opt/openjdk@21
echo "Starting ForgeAI Backend on port 8080 with Java 21..."
./mvnw spring-boot:run
