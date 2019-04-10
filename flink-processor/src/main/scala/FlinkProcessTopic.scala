import java.util.Properties

import com.typesafe.scalalogging.Logger
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time
import pureconfig.loadConfigOrThrow
import pureconfig.generic.auto._
import io.circe.parser.decode
//import org.apache.flink.streaming.connectors.cassandra.CassandraSink

/**
  * Reads from kafka topic and processes it using flink in windows
  */
object FlinkProcessTopic extends App {
  // Some general config to get the actor system up and running
  val className = this.getClass.getName.split("\\$").last
  val streamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
  implicit lazy val config: FlinkConfig = loadConfigOrThrow[FlinkConfig]
  implicit val logger = Logger(className)


  // Set Flink properties
  val properties = new Properties()
  properties.setProperty("bootstrap.servers", config.kafkaConfig.port)
  properties.setProperty("group.id", config.kafkaConfig.groupId)

  val stream = streamExecutionEnvironment
    .addSource(
      new FlinkKafkaConsumer011[String]("FXUS", new SimpleStringSchema(), properties)
        .setStartFromEarliest()
    )
    .map(str => decode[Ticker](str).right.get)
    .timeWindowAll(Time.minutes(1L))
    .reduce((t1, t2) => Ticker(t1.tickerType, t1.time, t1.last, t1.volume + t2.volume)) // TODO: change for bar
    .print // first log will appear in 1 minute

//  CassandraSink.addSink(stream)
//    .setQuery("INSERT INTO dev.emp (ticker) VALUES (?)")
//    .setHost("127.0.0.1")
//    .build()

  streamExecutionEnvironment.execute(config.jobName)
}
