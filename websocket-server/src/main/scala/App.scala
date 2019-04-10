import cats.effect._
import cats.implicits._
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import monix.eval.{Task, TaskApp}

object App extends TaskApp {

  val app = new WebSocketServerApp[Task](Slf4jLogger.unsafeCreate[Task])

  def run(args: List[String]): Task[ExitCode] =
    app.run.use(_ => Task.never).as(ExitCode.Success)
}
