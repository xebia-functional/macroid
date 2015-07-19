# Installation

## Version `2.0.0-M3` and above

*Macroid* is packaged in the AAR format and published to [Bintray](https://bintray.com/stanch/maven/macroid/view).

Assuming sbt version `0.13.x` and [android-sdk-plugin](https://github.com/pfn/android-sdk-plugin) 
version `1.2.20` and above, you can add it to your project like this:

```scala
resolvers += Resolver.jcenterRepo

libraryDependencies ++= Seq(
  aar("org.macroid" %% "macroid" % "2.0.0-M4"),
  "com.android.support" % "support-v4" % "20.0.0"
)
```

## Version `2.0.0-M2` and below

*Macroid* is an ordinary Scala library and can be added to your sbt project like this:

```scala
resolvers += Resolver.jcenterRepo

libraryDependencies += "org.macroid" %% "macroid" % "2.0.0-M2"
```

If you are confused about sbt or using Scala on Android in general, [check this out](ScalaOnAndroid.html).
