#!/usr/bin/env bash

echo "Compile mobiTopp Rastatt model."
cd ..
./gradlew --refresh-dependencies clean dependencies build

echo "Build finished."