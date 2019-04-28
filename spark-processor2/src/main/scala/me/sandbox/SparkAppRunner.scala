package me.sandbox

import me.sandbox.sql.streaming.sink.kafka.MessagePublisher

object SparkAppRunner extends App {
  		// // Set up Kafka brokers and KafkaProducer to send messages to brokers
  		MessagePublisher
  		// // Set up Spark session to read stream (consume) from kafka brokers,
  		// // do any transformations/processing
	println("it will take more than 1 minute before first result of apache spark appear ( after" +
		" start ). You can watch spark job at localhost:4040. Don't forget to check the log " +
		"activite of the microservice 'WebSocketClientToKafka' - and restart it if needed")
	println(" the spark healty activity will be look like ' Process new [[2019-02-14 09:59:30.0," +
		"2019-02-14 10:00:00.0],FXUS,3.225,3.225,3.225,3.225,1,RUB]' in white on the console ")
	SparkStructStreaming

}




