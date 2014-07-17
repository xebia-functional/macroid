import android.Keys._

android.Plugin.androidBuildAar

platformTarget in Android := "android-19"

name := "macroid-viewable"

organization := "org.macroid"

version := "2.0.0-SNAPSHOT"

scalaVersion := "2.10.4"

scalacOptions ++= Seq("-feature", "-deprecation")

resolvers ++= Seq(
  "jcenter" at "http://jcenter.bintray.com"
)

libraryDependencies ++= Seq(
  aar("org.macroid" %% "macroid" % "2.0.0-SNAPSHOT"),
  "com.android.support" % "support-v4" % "20.0.0"
)

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
