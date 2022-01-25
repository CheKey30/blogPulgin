#!/bin/bash
mvn clean package -U -Pbuild-jar -DskipTests

rm -rf output

mkdir -p output/lib
mkdir -p output/config
mkdir -p output/bin


cp target/bin/* output/bin/
cp target/classes/*.yml output/config/
cp target/classes/*.xml output/config/
cp target/lib/*.jar output/lib/
