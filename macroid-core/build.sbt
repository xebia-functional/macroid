name := "macroid"

description := "A Scala GUI DSL for Android"

homepage := Some(url("http://github.com/stanch/macroid"))

organization := "org.macroid"

version := "1.0.0-20130925"

scalaVersion := "2.10.3-RC2"

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  "Google Repository" at (new File(System.getenv("ANDROID_SDK_HOME")) / "extras" / "google" / "m2repository").getAbsolutePath
)

autoCompilerPlugins := true

scalacOptions += "-P:continuations:enable"

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  compilerPlugin("org.scala-lang.plugins" % "continuations" % scalaVersion.value),
  compilerPlugin("org.scala-lang.plugins" % "macro-paradise" % "2.0.0-SNAPSHOT" cross CrossVersion.full)
)

libraryDependencies ++= Seq(
  "com.google.android" % "android" % "4.1.1.4" % "provided",
  "com.android.support" % "support-v13" % "13.0.0",
  "org.scalaz" %% "scalaz-core" % "7.0.3",
  "com.typesafe.akka" %% "akka-dataflow" % "2.2.0",
  "io.dylemma" %% "scala-frp" % "1.0",
  "com.scalarx" %% "scalarx" % "0.1" % "provided"
)

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))