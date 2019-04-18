final case class FlinkConfig(kafkaConfig: KafkaConfig, cassandraConfig: CassandraConfig, jobName: String)

final case class KafkaConfig(port: String, groupId: String)
final case class CassandraConfig(port: Int, host: String)