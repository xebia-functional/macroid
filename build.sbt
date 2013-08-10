name := "macroid"

organization := "org.macroid"

version := "1.0-SNAPSHOT"

scalaVersion := "2.10.2"

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots")
)

autoCompilerPlugins := true

libraryDependencies ++= Seq(
  compilerPlugin("org.scala-lang.plugins" % "continuations" % "2.10.2"),
  compilerPlugin("org.scala-lang.plugins" % "macro-paradise_2.10.2" % "2.0.0-SNAPSHOT"),
  "org.scala-lang" % "scala-reflect" % "2.10.2"
)

unmanagedClasspath in Compile += new File(System.getenv("ANDROID_SDK_HOME")) / "platforms/android-17/android.jar"

unmanagedClasspath in Compile += new File(System.getenv("ANDROID_SDK_HOME")) / "extras/android/support/v13/android-support-v13.jar"

scalacOptions += "-P:continuations:enable"

libraryDependencies ++= Seq(
  "org.effectful" %% "effectful" % "0.1-SNAPSHOT",
  "com.typesafe.akka" %% "akka-dataflow" % "2.2.0"
)