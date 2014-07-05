import android.Keys._
import android.Dependencies.aar

android.Plugin.androidBuildAar

platformTarget in Android := "android-19"

name := "macroid"

description := "A Scala GUI DSL for Android"

homepage := Some(url("http://github.com/macroid/macroid"))

organization := "org.macroid"

version := "2.0.0-SNAPSHOT"

scalaVersion := "2.10.4"

scalacOptions ++= Seq("-feature", "-deprecation")

crossScalaVersions := Seq("2.10.4", "2.11.1")

scalacOptions in (Compile, doc) ++= Seq(
  "-sourcepath", baseDirectory.value.getAbsolutePath,
  "-doc-source-url", "https://github.com/macroid/macroid/tree/master€{FILE_PATH}.scala"
)

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases")
)

autoCompilerPlugins := true

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  compilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full)
)

libraryDependencies ++= (CrossVersion.partialVersion(scalaVersion.value) match {
  case Some((2, 10)) ⇒
    Seq("org.scalamacros" %% "quasiquotes" % "2.0.1")
  case _ ⇒
    Seq()
})

libraryDependencies ++= Seq(
  "com.android.support" % "support-v4" % "20.0.0",
  "org.scala-lang.modules" %% "scala-async" % "0.9.1",
  "org.brianmckenna" %% "wartremover" % "0.10",
  "org.scalatest" %% "scalatest" % "2.1.5" % "test"
)

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
