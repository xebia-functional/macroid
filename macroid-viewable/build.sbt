name := "macroid-viewable"

organization := "org.macroid"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.10.3"

scalacOptions ++= Seq("-feature", "-deprecation")

resolvers ++= Seq(
  "Google Repository" at (new File(System.getenv("ANDROID_SDK_HOME")) / "extras" / "google" / "m2repository").getAbsolutePath
)

libraryDependencies ++= Seq(
  "com.google.android" % "android" % "4.1.1.4" % "provided",
  "com.android.support" % "support-v13" % "19.0.0",
  "org.macroid" %% "macroid" % "2.0.0-20140312"
)

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))