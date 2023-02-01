import play.sbt.routes.RoutesKeys
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin

val appName = "transit-movements-message-parser"

lazy val microservice = Project(appName, file("."))
  .configs(IntegrationTest)
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(
    JUnitXmlReportPlugin
  ) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(inThisBuild(buildSettings))
  .settings(DefaultBuildSettings.integrationTestSettings())
  .settings(inConfig(IntegrationTest)(ScalafmtPlugin.scalafmtConfigSettings))
  .settings(inConfig(IntegrationTest)(scalafixConfigSettings(IntegrationTest)))
  .settings(scalacSettings)
  .settings(scoverageSettings)
  // .settings(addCompilerPlugin("io.tryp" % "splain" % "1.0.1" cross CrossVersion.patch))
  .settings(
    majorVersion := 0,
    scalaVersion := "2.13.8",
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    libraryDependencySchemes ++= AppDependencies.dependencySchemes,
    PlayKeys.playDefaultPort := 10210,
    RoutesKeys.routesImport ++= Seq(
      "models.values._",
      "models.RequestMessageType"
    ),
    javaOptions ++= Seq(
      "-Djdk.xml.maxOccurLimit=99999"
    )
  )

lazy val buildSettings = Def.settings(
  scalafmtOnCompile := true,
  scalafixDependencies ++= Seq(
    "com.github.liancheng" %% "organize-imports" % "0.6.0"
  )
)

lazy val scalacSettings = Def.settings(
  // Disable warnings arising from generated routing code
  scalacOptions += "-Wconf:src=routes/.*:silent",
  // Disable fatal warnings and warnings from discarding values
  scalacOptions ~= { opts =>
    opts.filterNot(Set("-Xfatal-warnings", "-Ywarn-value-discard"))
  }
)

lazy val scoverageSettings = Def.settings(
  Test / parallelExecution := false,
  ScoverageKeys.coverageMinimumStmtTotal := 40,
  ScoverageKeys.coverageFailOnMinimum := true,
  ScoverageKeys.coverageHighlighting := true,
  ScoverageKeys.coverageExcludedPackages := Seq(
    "<empty>",
    "Reverse.*",
    ".*(config|views.*)",
    ".*(BuildInfo|Routes).*"
  ).mkString(";"),
  ScoverageKeys.coverageExcludedFiles := Seq(
    "<empty>",
    "Reverse.*",
    ".*BuildInfo.*",
    ".*javascript.*",
    ".*Routes.*",
    ".*GuiceInjector"
  ).mkString(";")
)
