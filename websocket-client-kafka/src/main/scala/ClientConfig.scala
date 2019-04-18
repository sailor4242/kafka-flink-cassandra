final case class ClientConfig(
  kafkaConfig: KafkaConfig,
  host: String,
  port: Int)

final case class KafkaConfig(port: String)