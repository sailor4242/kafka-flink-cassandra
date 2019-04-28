#### Apache Spark 2.4+ under Windows 10 OS 
- first of all you need to download C:\Hadoop\bin\winutils to cheat / hack / simulate presents of 
hdfs under your dummy Win operation system ( google how to do this )

- then it is most likely that you encounter to the problem like describe here 
https://stackoverflow.com/questions/41825871/exception-while-deleting-spark-temp-dir-in-windows-7-64-bit

to solve the problem use the CMD commnds 

- `cd => to your winutils `
- `cd C:\Hadoop\bin`

- `winutils chmod -R 777 C:\Users\yourUserName\AppData\Local\Temp\`

- command prompt should be run under administrator ( of course ) 

- however, the described solution won't work if you try to start up spark in embedded mode  
 
This is the schema of a consuming a ProducerRecord from kafka. Value is the actual payload and
  the rest of the fields are metadata

  root
   |-- key: binary (nullable = true)
   |-- value: binary (nullable = true)
   |-- topic: string (nullable = true)
   |-- partition: integer (nullable = true)
   |-- offset: long (nullable = true)
   |-- timestamp: timestamp (nullable = true)
   |-- timestampType: integer (nullable = true)

   At this point, our key and values are in UTF8/binary, which was serialized this way by the
   KafkaProducer for transmission of data through the kafka brokers.

   from https://spark.apache.org/docs/2.4.0/structured-streaming-kafka-integration.html
   "Keys/Values are always deserialized as byte arrays with ByteArrayDeserializer.
   Use DataFrame operations to explicitly deserialize the keys/values"
   
   
####useful links
 
- https://github.com/datastax/spark-cassandra-connector/blob/master/doc/reference.md#cassandra-connection-parameters

- https://github.com/datastax/spark-cassandra-connector/blob/master/doc/1_connecting.md#connection-pooling

- https://github.com/ansrivas/spark-structured-streaming/

- https://spark.apache.org/docs/latest/structured-streaming-programming-guide.html#using-foreach
  