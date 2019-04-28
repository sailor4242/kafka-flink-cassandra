package me.sandbox.sql.streaming.sink.memory

import me.sandbox.sql.streaming.sink.StreamingSink
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.streaming.{OutputMode, StreamingQuery, Trigger}


class MemorySink(trigger: Trigger = Trigger.Once(),
                 outputMode: OutputMode = OutputMode.Update())
  extends StreamingSink {

  override def writeStream(data: DataFrame): StreamingQuery = {
    data.writeStream
      .format("memory")
      .trigger(trigger)
      .outputMode(outputMode)
      .option("checkpointLocation", checkpointLocation + "/memory")
      .start()
  }

}