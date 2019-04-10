import sbt._
import sbt.Keys._
import sbt.addCompilerPlugin

name := "kafka-flink-cassandra"

version := "0.1"


val commonDependencies = {
}

lazy val projectSettings = Seq(
  scalaVersion := "2.11.12",
  fork in Test := true,
  scalacOptions := Seq("-unchecked", "-feature", "-deprecation", "-encoding", "utf8")
)

def baseProject(name: String): Project = (
  Project(name, file(name))
    settings (projectSettings: _*)
  )

lazy val root = (
  project.in(file("."))
    aggregate(websocketServer, websocketClientKafka)
  )

lazy val metaParadiseVersion = "3.0.0-M11"
lazy val catsMTLVersion = "0.4.0"
lazy val catsVersion = "1.4.0"
lazy val log4CatsVersion = "0.2.0"
lazy val circeDerivationVersion = "0.10.0-M1"
lazy val circeVersion = "0.10.1"
lazy val pureConfigVersion = "0.10.0"
lazy val enumeratumVersion = "1.5.13"

lazy val websocketServer = (project in file("websocket-server"))
  .dependsOn(model)
  .settings(
    baseSettings,
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full),
    addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.7"),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.2.4"),
    libraryDependencies ++= Seq(
        "io.chrisdavenport" %% "log4cats-core" % log4CatsVersion,
        "io.chrisdavenport" %% "log4cats-slf4j" % log4CatsVersion,
        "io.chrisdavenport" %% "cats-par" % "0.2.0",
        "io.monix" %% "monix" % "3.0.0-RC2",
        "com.typesafe" % "config" % "1.3.2",
        "com.typesafe.akka" %% "akka-stream" % "2.5.12",
        "com.typesafe.akka" %% "akka-http" % "10.1.1",
        "com.typesafe.akka" %% "akka-http-core" % "10.1.1",
        "de.heikoseeberger" %% "akka-http-circe" % "1.24.3",
        "io.circe" %% "circe-core" % circeVersion,
        "io.circe" %% "circe-derivation" % circeDerivationVersion,
        "io.circe" %% "circe-generic" % circeVersion,
        "io.circe" %% "circe-java8" % circeVersion,
        "io.circe" %% "circe-parser" % circeVersion,
        "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
        "com.beachape" %% "enumeratum" % enumeratumVersion,
        "com.beachape" %% "enumeratum-circe" % enumeratumVersion,
        "com.github.pureconfig" %% "pureconfig" % pureConfigVersion,
        "ch.qos.logback" % "logback-classic" % "1.2.3",
      ),
    mainClass in Compile := Some("App")
  )

lazy val websocketClientKafka = (project in file("websocket-client-kafka"))
  .dependsOn(model)
  .settings(
    libraryDependencies ++= Seq(
        "com.typesafe" % "config" % "1.3.2",
        "com.typesafe.akka" %% "akka-stream" % "2.5.12",
        "com.typesafe.akka" %% "akka-http" % "10.1.1",
        "com.typesafe.akka" %% "akka-http-core" % "10.1.1",
        "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.1",
        "com.typesafe.akka" %% "akka-stream-kafka" % "0.20",
        "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
        "com.github.pureconfig" %% "pureconfig" % pureConfigVersion,
        "ch.qos.logback" % "logback-classic" % "1.2.3",
      ),
    mainClass in Compile := Some("WebSocketClientToKafka")
  )

lazy val flinkProcessor = (project in file("flink-processor"))
  .dependsOn(model)
  .settings(
    baseSettings,
    libraryDependencies ++= Seq(
      compilerPlugin("org.spire-math" %% "kind-projector" % "0.9.9"),
      compilerPlugin("org.scalameta" % "paradise" % metaParadiseVersion cross CrossVersion.full),
        "com.typesafe" % "config" % "1.3.2",
        "org.apache.flink" %% "flink-scala" % "1.7.2",
        "org.apache.flink" %% "flink-streaming-scala" % "1.7.2",
        "org.apache.flink" %% "flink-connector-kafka-0.11" % "1.7.2",
        "org.apache.flink" %% "flink-connector-cassandra" % "1.7.2",
        "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0",
        "com.github.pureconfig" %% "pureconfig" % pureConfigVersion,
        "ch.qos.logback" % "logback-classic" % "1.2.3",
      ),
    mainClass in Compile := Some("FlinkProcessTopic")
  )

lazy val model = (project in file("model"))
  .settings(
    baseSettings,
    libraryDependencies ++= Seq(
      compilerPlugin("org.spire-math" %% "kind-projector" % "0.9.9"),
      compilerPlugin("org.scalameta" % "paradise" % metaParadiseVersion cross CrossVersion.full),
      "com.beachape" %% "enumeratum" % enumeratumVersion,
      "com.beachape" %% "enumeratum-circe" % enumeratumVersion,
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-derivation" % circeDerivationVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-java8" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
    ),
    scalacOptions += "-Xplugin-require:macroparadise",
    mainClass in Compile := Some("model")
  )

lazy val baseSettings = Seq(
  scalaVersion in ThisBuild := "2.12.7",
  resolvers ++= Seq(
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots"),
    Resolver.bintrayRepo("ovotech", "maven")
  ),
  scalacOptions in(Compile, console) ~= {
    _.filterNot(unusedWarnings.toSet + "-Ywarn-value-discard")
  },
  scalacOptions ++= commonScalacOptions,
  scalacOptions ++= Seq("-Xmax-classfile-name", "128"),
  parallelExecution in Test := false,
  sources in(Compile, doc) := Nil,
  dockerExposedPorts := Seq(9000),
  dockerBaseImage := "java:8.161",
  publishTo := None,
  cancelable in Global := true
)

lazy val commonScalacOptions = Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:experimental.macros",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture",
  "-Ypartial-unification"
) ++ unusedWarnings

lazy val unusedWarnings = Seq("-Ywarn-unused", "-Ywarn-unused-import")