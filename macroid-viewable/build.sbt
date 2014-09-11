import android.Keys._

android.Plugin.androidBuildAar

platformTarget in Android := "android-19"

name := "macroid-viewable"

description := "Typeclasses to turn data into Android layouts"

homepage := Some(url("http://github.com/macroid/macroid-viewable"))

organization := "org.macroid"

version := "2.0.0-SNAPSHOT"

scalaVersion := "2.10.4"

scalacOptions ++= Seq("-feature", "-deprecation")

crossScalaVersions := Seq("2.10.4", "2.11.1")

scalacOptions in (Compile, doc) ++= Seq(
  "-sourcepath", baseDirectory.value.getAbsolutePath,
  "-doc-source-url", "https://github.com/macroid/macroid-viewable/tree/master€{FILE_PATH}.scala"
)

resolvers += "jcenter" at "http://jcenter.bintray.com"

libraryDependencies ++= Seq(
  aar("org.macroid" %% "macroid" % "2.0.0-M3"),
  "com.android.support" % "support-v4" % "20.0.0"
)

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
