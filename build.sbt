import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import scalariform.formatter.preferences._

val commonSettings = androidBuildAar ++ bintrayPublishSettings ++ Seq(
  platformTarget in Android := "android-23",
  typedResources := false,

  organization := "org.macroid",
  version := "2.0.0-M4",
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),

  scalaVersion := "2.11.7",
  crossScalaVersions := Seq("2.10.5", "2.11.7"),
  javacOptions ++= Seq("-source", "1.7", "-target", "1.7"),
  scalacOptions ++= Seq("-feature", "-deprecation", "-target:jvm-1.7"),

  libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.6" % "test",

  ScalariformKeys.preferences := ScalariformKeys.preferences.value
    .setPreference(PlaceScaladocAsterisksBeneathSecondAsterisk, true)
    .setPreference(DoubleIndentClassDeclaration, false)
    .setPreference(MultilineScaladocCommentsStartOnFirstLine, true)
    .setPreference(RewriteArrowSymbols, true)
)

val paradiseVersion = "2.1.0"

val paradiseSettings = Seq(
  libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    compilerPlugin("org.scalamacros" % "paradise" % paradiseVersion cross CrossVersion.full)
  ),

  libraryDependencies ++= (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 10)) ⇒
      Seq("org.scalamacros" %% "quasiquotes" % paradiseVersion)
    case _ ⇒
      Seq()
  })
)

lazy val core = (project in file("macroid-core"))
  .settings(commonSettings: _*)
  .settings(paradiseSettings: _*)
  .settings(
    name := "macroid",
    description := "A Scala GUI DSL for Android",
    homepage := Some(url("http://macroid.github.io")),

    unmanagedSourceDirectories in Test := Seq(baseDirectory.value / "src" / "test" / "scala"),

    libraryDependencies ++= Seq(
      "com.android.support" % "support-v4" % "23.1.1",
      "org.scala-lang.modules" %% "scala-async" % "0.9.5",
      "org.scalatest" %% "scalatest" % "2.2.6" % "test"
    )
  )

lazy val viewable = (project in file("macroid-viewable"))
  .settings(commonSettings: _*)
  .settings(
    name := "macroid-viewable",
    description := "Typeclasses to turn data into Android layouts",
    homepage := Some(url("http://macroid.github.io/modules/Viewable.html"))
  )
  .dependsOn(core)

lazy val akka = (project in file("macroid-akka"))
  .settings(commonSettings: _*)
  .settings(
    name := "macroid-akka",
    description := "Helpers to attach Akka Actors to Android Fragments",
    homepage := Some(url("http://macroid.github.io/modules/Akka.html")),

    libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.14" % "provided"
  )
  .dependsOn(core)

lazy val root = (project in file("."))
  .aggregate(core, viewable, akka)
  .settings(
    publish := (),
    publishLocal := (),
    scalacOptions in (Compile, doc) ++= Seq(
      "-sourcepath", baseDirectory.value.getAbsolutePath,
      "-doc-source-url", "https://github.com/macroid/macroid/tree/master€{FILE_PATH}.scala"
    )
  )
