import play.core.PlayVersion
import play.sbt.PlayImport
import sbt.Keys.libraryDependencies
import sbt._

object AppDependencies {
  val bootstrapVersion = "7.12.0"
  val hmrcMongoVersion = "0.74.0"
  val catsVersion      = "2.9.0"

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-28"   % bootstrapVersion,
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-28"          % hmrcMongoVersion,
    "uk.gov.hmrc.objectstore" %% "object-store-client-play-28" % "1.0.0",
    "uk.gov.hmrc"             %% "play-json-union-formatter"   % "1.18.0-play-28",
    "org.typelevel"           %% "cats-core"                   % catsVersion,
    "io.lemonlabs"            %% "scala-uri"                   % "3.5.0",
    "com.lihaoyi"             %% "pprint"                      % "0.8.1",
    "com.typesafe.akka"       %% "akka-slf4j"                  % PlayVersion.akkaVersion,
    "com.lightbend.akka"      %% "akka-stream-alpakka-xml"     % "3.0.4",
    PlayImport.ws
  )

  val test = Seq(
    "org.scalatest"         %% "scalatest"                % "3.2.15",
    "org.scalacheck"        %% "scalacheck"               % "1.17.0",
    "org.scalatestplus"     %% "scalacheck-1-17"          % "3.2.15.0",
    "uk.gov.hmrc"           %% "bootstrap-test-play-28"   % bootstrapVersion,
    "uk.gov.hmrc.mongo"     %% "hmrc-mongo-test-play-28"  % hmrcMongoVersion,
    "com.typesafe.akka"     %% "akka-testkit"             % PlayVersion.akkaVersion,
    "org.mockito"           %% "mockito-scala-scalatest"  % "1.17.12",
    "com.github.tomakehurst" % "wiremock-jre8-standalone" % "2.35.0",
    "com.vladsch.flexmark"   % "flexmark-all"             % "0.64.0"
  ).map(_ % s"$Test,$IntegrationTest")

  val dependencySchemes = Seq(
    "org.scala-lang.modules" %% "scala-java8-compat" % "always"
  )
}
