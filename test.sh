#!/bin/bash

rm -rf build

./gradlew build jar

mv build/libs/create_fly_wheels-0.1.jar /home/bekka/.local/share/PrismLauncher/instances/CrissyCraft_reactive_stress_test/minecraft/mods/
