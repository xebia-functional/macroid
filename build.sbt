name := "macroid"

description := "A Scala GUI DSL for Android"

homepage := Some(url("http://github.com/stanch/macroid"))

organization := "org.macroid"

version := "2.0.0-SNAPSHOT"

scalaVersion := "2.10.3"

scalacOptions ++= Seq("-feature", "-deprecation")

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  "Typesafe" at "http://repo.typesafe.com/typesafe/releases/",
  "Google Repository" at (new File(System.getenv("ANDROID_SDK_HOME")) / "extras" / "android" / "m2repository").getAbsolutePath
)

autoCompilerPlugins := true

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  compilerPlugin("org.scalamacros" % "paradise" % "2.0.0-M3" cross CrossVersion.full)
)

libraryDependencies ++= Seq(
  "com.google.android" % "android" % "4.1.1.4" % "provided",
  "com.android.support" % "support-v13" % "19.0.0",
  "io.dylemma" %% "scala-frp" % "1.1",
  "org.scala-lang.modules" %% "scala-async" % "0.9.0-M4",
  "org.scalatest" %% "scalatest" % "2.0" % "test"
)

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))