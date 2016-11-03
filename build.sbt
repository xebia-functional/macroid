import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import sbt.Keys._

import scalariform.formatter.preferences._

lazy val gpgFolder = sys.env.getOrElse("GPG_FOLDER", ".")

lazy val publishSnapshot = taskKey[Unit]("Publish only if the version is a SNAPSHOT")

lazy val micrositeSettings = Seq(
  micrositeName := "macroid",
  micrositeDescription := "A modular functional user interface creation language for Android, implemented with Scala macros",
  micrositeBaseUrl := "macroid",
  micrositeDocumentationUrl := "/macroid/docs/",
  micrositeGithubOwner := "47deg",
  micrositeGithubRepo := "macroid",
  includeFilter in makeSite := "*.html" | "*.css" | "*.png" | "*.jpg" | "*.gif" | "*.js" | "*.swf" | "*.md",
  micrositePalette := Map(
    "brand-primary"     -> "#F24130",
    "brand-secondary"   -> "#203040",
    "brand-tertiary"    -> "#1B2A38",
    "gray-dark"         -> "#4E4E4E",
    "gray"              -> "#7C7C7C",
    "gray-light"        -> "#E9E9E9",
    "gray-lighter"      -> "#F7F7F7",
    "white-color"       -> "#FFFFFF")
)

val androidV = "24.2.0"

val commonSettings = androidBuildAar ++ Seq(
  platformTarget in Android := "android-23",
  typedResources := false,

  version := "2.0-SNAPSHOT",
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),

  scalaVersion := "2.11.7",
  crossScalaVersions := Seq("2.10.6", "2.11.7"),
  javacOptions ++= Seq("-source", "1.7", "-target", "1.7"),
  scalacOptions ++= Seq(
    "-feature",
    "-deprecation",
    "-target:jvm-1.7",
    "-encoding", "UTF-8",
    "-unchecked",
    "-Xlint",
    "-Yno-adapted-args",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen"
  ) ++ (scalaBinaryVersion.value match {
    case "2.10" => Seq.empty
    case v => Seq("-Ywarn-unused-import")
  }),

  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "2.2.6" % Test,
    "com.geteit" %% "robotest" % "0.12" % Test,
    "com.android.support" % "support-v4" % androidV
  ),

  parallelExecution in Test := false,
  fork in Test := true,
  unmanagedClasspath in Test ++= (bootClasspath in Android).value,

  ScalariformKeys.preferences := ScalariformKeys.preferences.value
    .setPreference(PlaceScaladocAsterisksBeneathSecondAsterisk, true)
    .setPreference(DoubleIndentClassDeclaration, false)
    .setPreference(MultilineScaladocCommentsStartOnFirstLine, true)
    .setPreference(RewriteArrowSymbols, true),

  organization := "org.macroid",

  organizationName := "macroid",

  organizationHomepage := Some(new URL("http://macroid.github.io")),

  publishMavenStyle := true,

  startYear := Some(2015),

  description := "A Scala GUI DSL for Android",

  homepage := Some(url("http://macroid.github.io")),

  scmInfo := Some(ScmInfo(url("https://github.com/47deg/macroid"), "https://github.com/47deg/macroid.git")),

  pomExtra :=
    <developers>
      <developer>
        <name>macroid</name>
      </developer>
    </developers>,

  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },

  pgpPassphrase := Some(sys.env.getOrElse("GPG_PASSPHRASE", "").toCharArray),

  pgpPublicRing := file(s"$gpgFolder/local.pubring.asc"),

  pgpSecretRing := file(s"$gpgFolder/local.secring.asc"),

  credentials += Credentials("Sonatype Nexus Repository Manager",
    "oss.sonatype.org",
    sys.env.getOrElse("PUBLISH_USERNAME", ""),
    sys.env.getOrElse("PUBLISH_PASSWORD", "")),

  publishArtifact in Test := false,

  publishSnapshot := Def.taskDyn {
    if (isSnapshot.value) Def.task { PgpKeys.publishSigned.value }
    else Def.task(println("Actual version is not a Snapshot. Skipping publish."))
  }.value


) ++ addCommandAlias("testAndCover", "; clean; coverage; test; coverageReport; coverageAggregate")

val paradiseVersion = "2.1.0"

val macroSettings = Seq(
  libraryDependencies ++= Seq(
    "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    // required by macro-compat
    "org.typelevel" %% "macro-compat" % "1.1.1",
    "org.scala-lang" % "scala-compiler" % scalaVersion.value,
    compilerPlugin("org.scalamacros" % "paradise" % paradiseVersion cross CrossVersion.full)
  ),

  libraryDependencies ++= (scalaBinaryVersion.value match {
    case "2.10" => Seq("org.scalamacros" %% "quasiquotes" % paradiseVersion)
    case _ ⇒ Seq()
  })
)

lazy val core = (project in file("macroid-core"))
  .settings(commonSettings: _*)
  .settings(macroSettings: _*)
  .settings(
    name := "macroid",
    description := "A Scala GUI DSL for Android",
    homepage := Some(url("http://macroid.github.io")),

    unmanagedSourceDirectories in Test := Seq(baseDirectory.value / "src" / "test" / "scala"),

    libraryDependencies ++= Seq(
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

lazy val docs = (project in file("macroid-docs"))
  .settings(commonSettings: _*)
  .settings(micrositeSettings: _*)
  .settings(moduleName := "macroid-docs")
  .enablePlugins(MicrositesPlugin)
  .settings(
    name := "docs",
    description := "Macroid Documentation")
  .dependsOn(core)

lazy val extras = (project in file("macroid-extras"))
  .settings(commonSettings: _*)
  .settings(
    name := "macroid-extras",
    description := "Tweaks and utilities for android views",
    homepage := Some(url("http://macroid.github.io/modules/Extras.html")),

    libraryDependencies ++= Seq(
      "com.android.support" % "appcompat-v7" % androidV,
      "com.android.support" % "recyclerview-v7" % androidV,
      "com.android.support" % "cardview-v7" % androidV,
      "com.android.support" % "design" % androidV)
  )
  .dependsOn(core)

lazy val root = (project in file("."))
  .aggregate(core, viewable, akka, extras)
  .settings(
    publish := (),
    publishLocal := (),
    scalaVersion := "2.11.7",
    scalacOptions in (Compile, doc) ++= Seq(
      "-sourcepath", baseDirectory.value.getAbsolutePath,
      "-doc-source-url", "https://github.com/macroid/macroid/tree/master€{FILE_PATH}.scala"
    )
  )
