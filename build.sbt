lazy val gpgFolder = sys.env.getOrElse("GPG_FOLDER", ".")

pgpPassphrase := Some(sys.env.getOrElse("GPG_PASSPHRASE", "").toCharArray)
pgpPublicRing := file(s"$gpgFolder/local.pubring.asc")
pgpSecretRing := file(s"$gpgFolder/local.secring.asc")

lazy val macroid = (project in file("."))
  .aggregate(core, viewable, akka, extras)
  .settings(
    publish := (),
    publishLocal := (),
    scalaVersion := "2.11.11",
    scalacOptions in (Compile, doc) ++= Seq(
      "-sourcepath",
      baseDirectory.value.getAbsolutePath,
      "-doc-source-url",
      "https://github.com/macroid/macroid/tree/master€{FILE_PATH}.scala"
    )
  )

lazy val core = (project in file("macroid-core"))
  .enablePlugins(AndroidLib)
  .settings(commonSettings: _*)
  .settings(macroSettings: _*)
  .settings(
    name := "macroid",
    description := "A Scala GUI DSL for Android",
    homepage := Some(url("http://47deg.github.io/macroid")),
    platformTarget in Android := platformV,
    unmanagedClasspath in Test ++= (bootClasspath in Android).value,
    unmanagedSourceDirectories in Test := Seq(
      baseDirectory.value / "src" / "test" / "scala"),
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-async" % "0.9.6"
    )
  )

lazy val viewable = (project in file("macroid-viewable"))
  .enablePlugins(AndroidLib)
  .settings(commonSettings: _*)
  .settings(
    name := "macroid-viewable",
    description := "Typeclasses to turn data into Android layouts",
    platformTarget in Android := platformV,
    unmanagedClasspath in Test ++= (bootClasspath in Android).value,
    homepage := Some(
      url("http://47deg.github.io/macroid/docs/modules/Viewable.html"))
  )
  .dependsOn(core)

lazy val akka = (project in file("macroid-akka"))
  .enablePlugins(AndroidLib)
  .settings(commonSettings: _*)
  .settings(
    name := "macroid-akka",
    description := "Helpers to attach Akka Actors to Android Fragments",
    platformTarget in Android := platformV,
    unmanagedClasspath in Test ++= (bootClasspath in Android).value,
    homepage := Some(
      url("http://47deg.github.io/macroid/docs/modules/Akka.html")),
    libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.5.3"
  )
  .dependsOn(core)

lazy val docs = (project in file("macroid-docs"))
  .enablePlugins(AndroidLib)
  .settings(commonSettings: _*)
  .settings(micrositeSettings: _*)
  .settings(moduleName := "macroid-docs")
  .enablePlugins(MicrositesPlugin)
  .settings(name := "docs",
            platformTarget in Android := platformV,
            unmanagedClasspath in Test ++= (bootClasspath in Android).value,
            description := "Macroid Documentation")
  .dependsOn(core)

lazy val extras = (project in file("macroid-extras"))
  .enablePlugins(AndroidLib)
  .settings(commonSettings: _*)
  .settings(
    name := "macroid-extras",
    description := "Tweaks and utilities for android views",
    platformTarget in Android := platformV,
    unmanagedClasspath in Test ++= (bootClasspath in Android).value,
    homepage := Some(
      url("http://47deg.github.io/macroid/docs/modules/Extras.html")),
    libraryDependencies ++= Seq(
      "com.android.support" % "appcompat-v7" % androidV,
      "com.android.support" % "recyclerview-v7" % androidV,
      "com.android.support" % "cardview-v7" % androidV,
      "com.android.support" % "design" % androidV
    )
  )
  .dependsOn(core)
