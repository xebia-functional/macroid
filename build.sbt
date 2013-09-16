name := "macroid"

organization := "org.macroid"

version := "1.0-SNAPSHOT"

scalaVersion := "2.10.2"

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)

autoCompilerPlugins := true

scalacOptions += "-P:continuations:enable"

libraryDependencies ++= Seq(
  compilerPlugin("org.scala-lang.plugins" % "continuations" % "2.10.2"),
  compilerPlugin("org.scala-lang.plugins" % "macro-paradise_2.10.2" % "2.0.0-SNAPSHOT"),
  "org.scala-lang" % "scala-reflect" % "2.10.2"
)

unmanagedClasspath in Compile ++= Seq(
  new File(System.getenv("ANDROID_SDK_HOME")) / "platforms/android-17/android.jar",
  new File(System.getenv("ANDROID_SDK_HOME")) / "extras/android/support/v13/android-support-v13.jar"
)

libraryDependencies ++= Seq(
  "org.effectful" %% "effectful" % "0.1-SNAPSHOT",
  "com.typesafe.akka" %% "akka-dataflow" % "2.2.0",
  "io.dylemma" %% "scala-frp" % "1.0"
)