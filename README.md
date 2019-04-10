Pet project to experiment with kafka, flink & cassandra

### Websocket server

To be in control over the incomning data I've setup a websocket server that reads from a huge CSV file and post each line in regular intervals to it's clients

Run:

`sbt "project websocket-server" "run"`

### Kafka 

Start Kafka on Docker with zookeeper

`docker run -d -p 2181:2181 -p 9092:9092 --env ADVERTISED_HOST=127.0.0.1 --env ADVERTISED_PORT=9092 --name kafka johnnypark/kafka-zookeeper`

`ADVERTISTED_HOST` was set to `127.0.0.1`, which will allow other containers to be able to run Producers and Consumers.

Setting `ADVERTISED_HOST` to `localhost`, `127.0.0.1`, or `0.0.0.0` will work great only if Producers and Consumers are started within the `kafka` container itself, or if you are using DockerForMac (like me) and you want to run Producers and Consumers from OSX.


### Websocket client -> Kafka Producer

When websocket server & kafka are running we can run the websocket client / kafka producer


`sbt "project websocket-client-kafka" "run"`

### Cassandra

`docker run --name cassandra  -d -p 9042:9042 cassandra
`
### Portainer

`docker run -d -p 9000:9000 -v /var/run/docker.sock:/var/run/docker.sock -v portainer_data:/data portainer/portainer
`

### Kafka Consumer -> Flink -> Cassandra

Have a kafka consumer to Flink read from the kafka stream and 
*process the data
*store the results in cassandra




