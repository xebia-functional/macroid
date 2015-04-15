val commonSettings = androidBuildAar ++ bintrayPublishSettings ++ Seq(
  platformTarget in Android := "android-21",
  typedResources := false,

  organization := "org.macroid",
  version := "2.0.0-M4",
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),

  scalaVersion := "2.10.5",
  crossScalaVersions := Seq("2.10.5", "2.11.6"),
  javacOptions ++= Seq("-source", "1.7", "-target", "1.7"),
  scalacOptions ++= Seq("-feature", "-deprecation", "-target:jvm-1.7"),

  libraryDependencies += "org.scalatest" %% "scalatest" % "2.1.5" % "test"
)

val paradiseSettings = Seq(
  libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    compilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full)
  ),

  libraryDependencies ++= (CrossVersion.partialVersion(scalaVersion.value) match {
    case Some((2, 10)) ⇒
      Seq("org.scalamacros" %% "quasiquotes" % "2.0.1")
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
      "com.android.support" % "support-v4" % "21.0.3",
      "org.scala-lang.modules" %% "scala-async" % "0.9.2",
      "org.brianmckenna" %% "wartremover" % "0.10",
      "org.scalatest" %% "scalatest" % "2.1.5" % "test"
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

    libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.9" % "provided"
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
