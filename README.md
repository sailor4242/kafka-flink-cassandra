_Under construction_

Pet project to experiment with kafka, flink, cassandra with scala and it's ecosystem.

Project itself represents a market data flow from websocket client to kafka and cassandra with some underlying transformations.

To set up the environment (kafka, cassandra) just simply run:
- `sbt docker:stage`
- `docker-compose up` 

from the projects root. Make sure all necessary ports are available

#### Websocket server

To be in control over the incomning data I've setup a websocket server that reads from huge CSV files and posts each line to it's clients

##### Websocket client -> Kafka Producer

Client to split data by tickers and push them to kafka topics

##### Kafka Consumer -> Flink -> Cassandra

A consumer for Flink to read and transform ticks into bars and then save them to cassandra

##### User Rest Server

REST server to interact with users

Query examples :

Create user account:
`POST localhost:8081/account`
`Body: {"firstName": "Joe", lastName: "Johnes""}`

Get user account:
`GET localhost:8081/account/{uid}`
 
 ### Portainer
 Portainer is a useful WebUI to manage your docker containers.
 
 `docker run -d -p 9000:9000 -v /var/run/docker.sock:/var/run/docker.sock -v portainer_data:/data portainer/portainer
 `




