package me.sandbox.sql.streaming.sink.console

import me.sandbox.sql.streaming.sink.StreamingSink
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.streaming.{OutputMode, StreamingQuery, Trigger}

class ConsoleSink(trigger: Trigger = Trigger.Once(),
                  outputMode: OutputMode = OutputMode.Update())
  extends StreamingSink {
// todo this file didn't connect to actual kafka to console stream
  override def writeStream(data: DataFrame): StreamingQuery = {
    data.writeStream
      .format("console")
      .trigger(trigger)
      .outputMode(outputMode)
      .option("checkpointLocation", checkpointLocation + "/console")
      .start()
  }

}
