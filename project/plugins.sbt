resolvers += MavenRepository(
  "HMRC-open-artefacts-maven2",
  "https://open.artefacts.tax.service.gov.uk/maven2"
)

resolvers += Resolver.url(
  "HMRC-open-artefacts-ivy2",
  url("https://open.artefacts.tax.service.gov.uk/ivy2")
)(Resolver.ivyStylePatterns)

resolvers += Resolver.typesafeRepo("releases")

addSbtPlugin("uk.gov.hmrc"               % "sbt-auto-build"       % "3.6.0")
addSbtPlugin("uk.gov.hmrc"               % "sbt-distributables"   % "2.1.0")
addSbtPlugin("com.typesafe.play"         % "sbt-plugin"           % "2.8.11")
addSbtPlugin("org.scoverage"             % "sbt-scoverage"        % "1.9.2")
addSbtPlugin("org.scalameta"             % "sbt-scalafmt"         % "2.4.6")
addSbtPlugin("ch.epfl.scala"             % "sbt-scalafix"         % "0.10.4")
addSbtPlugin("com.timushev.sbt"          % "sbt-updates"          % "0.6.0")
addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat"         % "0.4.2")
