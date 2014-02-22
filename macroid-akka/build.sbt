name := "akka-fragments"

description := "Some helpers to attach Akka Actors to Android Fragments"

homepage := Some(url("http://github.com/stanch/akka-fragments"))

organization := "org.macroid"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.10.3"

scalacOptions ++= Seq("-feature", "-deprecation")

resolvers ++= Seq(
  "Typesafe" at "http://repo.typesafe.com/typesafe/releases/",
  "Google Repository" at (new File(System.getenv("ANDROID_SDK_HOME")) / "extras" / "google" / "m2repository").getAbsolutePath
)

libraryDependencies ++= Seq(
  "com.google.android" % "android" % "4.1.1.4" % "provided",
  "com.android.support" % "support-v13" % "19.0.0",
  "com.typesafe.akka" %% "akka-actor" % "2.2.3" % "provided"
)

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
