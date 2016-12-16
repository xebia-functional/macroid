---
layout: docs
title: Installation
section: docs
---

# Installation

## Version `2.0.0-M5`

*Macroid* is packaged in the AAR format and published to [Maven Central](http://central.maven.org/maven2/org/macroid/macroid_2.11/2.0.0-M5/).

Assuming sbt version `0.13.x` and [android-sdk-plugin](https://github.com/pfn/android-sdk-plugin) 
version `1.2.20` and above, you can add it to your project like this:

```scala
libraryDependencies ++= Seq(
  aar("org.macroid" %% "macroid" % "2.0")
```

If you want to use the SNAPSHOT version you need to add the Sonatype SNAPSHOT repo

```scala
resolvers +=
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= Seq(
  aar("org.macroid" %% "macroid" % "2.0.1-SNAPSHOT")
```

## Version `2.0.0-M2` and below

*Macroid* is an ordinary Scala library and can be added to your sbt project like this:

```scala
resolvers += Resolver.jcenterRepo

libraryDependencies += "org.macroid" %% "macroid" % "2.0.0-M2"
```

If you are confused about sbt or using Scala on Android in general, [check this out](installation/ScalaOnAndroid.html).
