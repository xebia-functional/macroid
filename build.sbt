name := "macroid"

description := "A Scala GUI DSL for Android"

homepage := Some(url("http://github.com/stanch/macroid"))

organization := "org.macroid"

version := "1.0.0-20130918"

scalaVersion := "2.10.3-RC1"

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)

autoCompilerPlugins := true

scalacOptions += "-P:continuations:enable"

addCompilerPlugin("org.scala-lang.plugins" % "continuations" % "2.10.3-RC1")

addCompilerPlugin("org.scala-lang.plugins" % "macro-paradise_2.10.3-RC1" % "2.0.0-SNAPSHOT")

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % "2.10.3-RC1"
)

unmanagedClasspath in Compile ++= Seq(
  new File(System.getenv("ANDROID_SDK_HOME")) / "platforms/android-17/android.jar",
  new File(System.getenv("ANDROID_SDK_HOME")) / "extras/android/support/v13/android-support-v13.jar"
)

libraryDependencies ++= Seq(
  "org.pelotom" %% "effectful" % "1.0.0",
  "com.typesafe.akka" %% "akka-dataflow" % "2.2.0",
  "io.dylemma" %% "scala-frp" % "1.0"
)

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))