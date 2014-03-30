name := "macroid"

description := "A Scala GUI DSL for Android"

homepage := Some(url("http://github.com/macroid/macroid"))

organization := "org.macroid"

version := "2.0.0-SNAPSHOT"

scalaVersion := "2.10.3"

scalacOptions ++= Seq("-feature", "-deprecation")

crossScalaVersions := Seq("2.10.3", "2.11.0-RC3")

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases"),
  "Typesafe" at "http://repo.typesafe.com/typesafe/releases/",
  "Android" at (file(System.getenv("ANDROID_SDK_HOME")) / "extras" / "android" / "m2repository").getCanonicalFile.toURI.toString
)

autoCompilerPlugins := true

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  compilerPlugin("org.scalamacros" % "paradise" % "2.0.0-M6" cross CrossVersion.full)
)

libraryDependencies ++= (CrossVersion.partialVersion(scalaVersion.value) match {
  case Some((2, 10)) ⇒
    Seq("org.scalamacros" % "quasiquotes" % "2.0.0-M6" cross CrossVersion.full)
  case _ ⇒
    Seq()
})

libraryDependencies ++= Seq(
  "com.google.android" % "android" % "4.1.1.4" % "provided",
  "com.android.support" % "support-v13" % "19.0.0",
  "org.scala-lang.modules" %% "scala-async" % "0.9.0",
  "org.brianmckenna" %% "wartremover" % "0.8" % "provided",
  "org.scalatest" %% "scalatest" % "2.1.2" % "test"
)

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
