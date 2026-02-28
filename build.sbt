import sbtassembly.AssemblyPlugin.autoImport._

ThisBuild / scalaVersion := "3.8.0"

assembly / assemblyMergeStrategy := {
  case PathList("module-info.class") => MergeStrategy.discard
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x                             => MergeStrategy.first
}

// ---------- Project ----------
lazy val root = project
  .in(file("."))
  .settings(
    name := "Decklify",
    version := "0.1.3",
    libraryDependencies ++= Seq(
      "org.scalafx" %% "scalafx" % "24.0.2-R36",
      "org.scalameta" %% "munit" % "1.0.0" % Test,
      "io.circe" %% "circe-core" % "0.14.15",
      "io.circe" %% "circe-generic" % "0.14.15",
      "io.circe" %% "circe-parser" % "0.14.15"
    ),
    semanticdbEnabled := true,
    scalacOptions += "-Wall"
  )
