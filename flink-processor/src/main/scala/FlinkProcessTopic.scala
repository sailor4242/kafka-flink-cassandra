
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.{Properties, UUID}

import com.typesafe.scalalogging.Logger
import io.circe.parser.decode
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment, _}
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011
import pureconfig.loadConfigOrThrow
import org.apache.flink.streaming.api.datastream._
import org.apache.flink.streaming.api.functions.timestamps.AscendingTimestampExtractor
import org.apache.flink.streaming.connectors.cassandra.CassandraSink
import pureconfig.generic.auto._


/**
  * Reads from kafka topic and processes it using flink in windows
  */
object FlinkProcessTopic extends App {
  // Some general config to get the actor system up and running
  val className = this.getClass.getName.split("\\$").last
  val streamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
  streamExecutionEnvironment.setParallelism(1)
  implicit lazy val config: FlinkConfig = loadConfigOrThrow[FlinkConfig]
  implicit val logger = Logger(className)


  // Set Flink properties
  val properties = new Properties()
  properties.setProperty("bootstrap.servers", config.kafkaConfig.port)
  properties.setProperty("group.id", config.kafkaConfig.groupId)


  val stream = streamExecutionEnvironment
    .addSource(
      new FlinkKafkaConsumer011[String]("FXUS", new SimpleStringSchema(),
        properties)
        .setStartFromEarliest()
    )

  final case class TickerCassandra(id: UUID,
                                   ticker: String,
                                   time: String,
                                   price: String,
                                   volume: Int,
                                   currency: String
                                  ){
    override def toString: _root_.java.lang.String = s"$ticker $time $price $volume $currency"
  }
  val tickerStream: DataStream[Ticker] = stream.map { str =>
    val tick = decode[Ticker](str).right.get
    tick
    }.assignTimestampsAndWatermarks(new AscendingTimestampExtractor[Ticker]() {
      val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
      override def extractAscendingTimestamp(element: Ticker): Long = {
        val ts = new Timestamp(sdf.parse(element.time).getTime)
        //println(ts.getTime)
        ts.getTime
      }
    })
  tickerStream.print()
  val cassandraTickersStream = tickerStream.map{ tick =>
    TickerCassandra(
      UUID.randomUUID(),
      tick.tickerType.toString,
      tick.time,
      tick.last.value.toString,
      tick.volume,
      tick.last.currency.toString
    )
  }
  val bar1MinStream: DataStream[OHLC1Min] = tickerStream
    .timeWindowAll(Time.minutes(1)).process(FlinkWindowProcessFunc)
  bar1MinStream.print()

   CassandraSink.addSink(bar1MinStream)
     .setHost("localhost", 9042)
     .setQuery("INSERT INTO " +
       "test_keyspace.table_ohlc1m(id, ticker, timeStart, open, high, low, close , volume, " +
       "currency) " +
       "values (?, ?, ?, ?, ?, ?, ?, ?, ? );").build()

   CassandraSink.addSink(cassandraTickersStream)
   .setHost("localhost", 9042)
   .setQuery("INSERT INTO test_keyspace" +
     ".table_ticker(id, ticker, time, price, volume, currency )" +
     "values (?, ?, ?, ?, ?, ?);").build()

  streamExecutionEnvironment.execute(config.jobName)
}

// start up optional steps ( win )  :
// cd C:\zookeeper-3.4.12
// zkServer.cmd
// cd C:\kafka_2.12-2.1.0
// .\bin\windows\kafka-server-start.bat .\config\server.properties
// cassandra -f

// cassandra prep STEPS:
// docker exec -it cassandra cqlsh -e "CREATE KEYSPACE IF NOT EXISTS test_keyspace WITH REPLICATION = {'class' : 'SimpleStrategy', 'replication_factor' : 1 };"
// docker exec -it cassandra cqlsh -e "CREATE TABLE IF NOT EXISTS test_keyspace.table_ticker (id uuid, ticker text, time text,
// price text, volume int, currency text, PRIMARY KEY (id));"

// docker exec -it cassandra cqlsh -e "CREATE TABLE IF NOT EXISTS test_keyspace.table_ohlc1m (id uuid, ticker text timeStart text, open text, high text, low text, close text, volume int, currency text, PRIMARY KEY(id));"
// docker exec -ot cassandra cqlsh -e "DROP TABLE test_keyspace.table_ticker;"