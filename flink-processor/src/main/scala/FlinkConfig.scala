final case class FlinkConfig(kafkaConfig: KafkaConfig, jobName: String)

final case class KafkaConfig(port: String, groupId: String)