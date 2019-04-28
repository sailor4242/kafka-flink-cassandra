import cats.effect.IO
import pureconfig.loadConfigOrThrow
import pureconfig.generic.auto._

final case class ServerConfig(
  httpServer: HttpServer,
  dataFile: String,
  route: String,
  sendBufferSize: Int,
  sleepIntervalMs: Int,
  idleTimeoutS: Int,
  lingerTimeoutS: Int,
  epochDelay: Long)

final case class HttpServer(interface: String, port: Int)

object ServerConfig {
  def read: IO[ServerConfig] = IO { loadConfigOrThrow[ServerConfig] }
}