#!/usr/bin/env bash

# goto project root
cd ${BASH_SOURCE%/*}/..

echo "Kill all running sbt"
ps aux | grep sbt | grep Cellar | grep -v grep | awk '{print $2}' | xargs -I{} kill {}

echo "Build project"
sbt  compile

echo "Cleanup kafka containers"
docker stop kafka
docker rm kafka

echo "Start Websocket Server"
sbt "project websocket-server" run &

echo "Start Docker"
docker run -d -p 2181:2181 -p 9092:9092 --env ADVERTISED_HOST=127.0.0.1 --env ADVERTISED_PORT=9092 --name kafka johnnypark/kafka-zookeeper

sleep 20
echo "Start Websocket Client -> Kafka"
sbt "project websocket-client-kafka" run &

sleep 30
echo "Start Kafka -> Flink"
sbt "project flink-processor" run &
