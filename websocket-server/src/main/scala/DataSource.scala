import java.io.File
import java.nio.file.Paths

import akka.http.scaladsl.model.ws.TextMessage
import akka.stream.scaladsl.{FileIO, Framing, Merge, Source}
import akka.util.ByteString
import cats.effect._
import io.circe.syntax._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object DataSource {

  /** Generate a file sources to read records from CSV files
    * Merge file source into one
    * Map the file source via a delayed source and convert the csv lines into JSON tickers
    */
  def getDataSource(config: ServerConfig, mapper: TickerMapper, blocking: ExecutionContext)
                   (implicit basic: ContextShift[IO]) = {
    val read = IO {
      val sources = getFilePaths(config.dataFile)
        .map(path => FileIO
          .fromPath(Paths.get(path))
          .via(Framing.delimiter(ByteString("\n"), Int.MaxValue)))

      val source = sources match {
        case List(s1, s2, t) => Source.combine(s1, s2, t)(Merge(_))
        case List(s1, s2) => Source.combine(s1, s2)(Merge(_))
        case List(s) => s
        case _ => Source.empty
      }

      source
        .throttle(elements = 1, per = config.sleepIntervalMs.millis)
        .map(line => TextMessage(mapper.map(line).asJson.toString()))
    }

    for {
      _       <- IO.shift(blocking)
      sources <- read
      _       <- IO.shift(basic)
    } yield sources
  }

  def getFilePaths(path: String): List[String] =
    new File(path).listFiles().toList.map(_.getAbsolutePath)

}
