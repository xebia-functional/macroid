import android.Keys._

android.Plugin.androidBuildAar

platformTarget in Android := "android-21"

name := "macroid-akka-fragments"

description := "Some helpers to attach Akka Actors to Android Fragments"

homepage := Some(url("http://github.com/macroid/macroid-akka-fragments"))

organization := "org.macroid"

version := "2.0.0-SNAPSHOT"

scalaVersion := "2.10.4"

scalacOptions ++= Seq("-feature", "-deprecation")

crossScalaVersions := Seq("2.10.4", "2.11.1")

resolvers ++= Seq(
  "Typesafe" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= Seq(
  aar("org.macroid" %% "macroid" % "2.0.0-SNAPSHOT"),
  "com.typesafe.akka" %% "akka-actor" % "2.3.3" % "provided"
)

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
