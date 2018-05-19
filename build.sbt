lazy val akkaHttpVersion = "10.1.1"
lazy val akkaVersion    = "2.5.12"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.mysterria.ik.sapog",
      scalaVersion    := "2.12.5",
      scalacOptions ++= Seq("-deprecation", "-feature", "-encoding", "utf8", "-Ywarn-dead-code", "-unchecked", "-Xlint", "-Ywarn-unused-import")
    )),
    name := "Sapog Interstellar Foundation",

    resolvers += Resolver.bintrayRepo("hseeberger", "maven"),

    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml"        % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream"          % akkaVersion,

      "com.typesafe.play" %%  "play-json"           % "2.6.9",
      "de.heikoseeberger" %% "akka-http-play-json" % "1.20.1",
      "net.codingwell"    %% "scala-guice"          % "4.2.0",

      "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
      "ch.qos.logback"    %  "logback-classic"      % "1.2.3"
    )
  )

