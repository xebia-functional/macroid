---
layout: home
technologies:
 - first: ["Scala", "Macroid has been implemented with Scala macros"]
 - second: ["Android", "Modular functional user interface creation language for Android"]
 - third: ["Open Source", "By opening its development, we hope that the community helps us to expand and improve this project. Macroid wants you!"]
---

# Macroid

Macroid is a modular functional user interface creation language for Android, implemented with 
Scala macros.

Striving to be focused on one thing (GUI), Macroid promotes composability and high-level abstractions.

Prerequisites: Scala `2.10.x` or `2.11.x`, Android `API 19+`.

[![Join the chat at https://gitter.im/macroid/macroid](https://img.shields.io/badge/GITTER-Join%20chat%20→-brightgreen.svg?style=flat)](https://gitter.im/47deg/macroid?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

# Installation

## Version `2.1.0`

Macroid is packaged in the AAR format and published to [Maven Central](http://central.maven.org/maven2/org/macroid/macroid_2.11/2.1.0/).

Assuming sbt version `0.13.x` and [android-sdk-plugin](https://github.com/scala-android/sbt-android) 
version `1.2.20` and above, you can add it to your project like this:

```scala
libraryDependencies ++= Seq(
  aar("org.macroid" %% "macroid" % "2.1.0")
```

If you want to use the SNAPSHOT version you need to add the Sonatype SNAPSHOT repo

```scala
resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= Seq(
  aar("org.macroid" %% "macroid" % "2.1.0-SNAPSHOT")
```
