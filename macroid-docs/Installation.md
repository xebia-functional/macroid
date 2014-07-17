# Installation

## Version `2.0.0-M3` and above

*Macroid* is packaged in the AAR format and published to [Bintray](https://bintray.com/stanch/maven/macroid/view).

Assuming sbt version `0.13.x` and [android-sdk-plugin](https://github.com/pfn/android-sdk-plugin) 
version `1.2.20` and above, you can add it to your project like this:

```scala
resolvers += "jcenter" at "http://jcenter.bintray.com"

libraryDependencies ++= Seq(
  aar("org.macroid" %% "macroid" % "2.0.0-M3"),
  "com.android.support" % "support-v4" % "20.0.0"
)
```

It is also recommended to add a linter (yes, this is ugly, see
[this issue](https://github.com/typelevel/wartremover/pull/107)):

```scala
addCompilerPlugin("org.brianmckenna" %% "wartremover" % "0.10")

scalacOptions in (Compile, compile) ++=
  (dependencyClasspath in Compile).value.files.map("-P:wartremover:cp:" + _.toURI.toURL)

scalacOptions in (Compile, compile) ++= Seq(
  "-P:wartremover:traverser:macroid.warts.CheckUi"
)
```

## Version `2.0.0-M2` and below

*Macroid* is an ordinary Scala library and can be added to your sbt project like this:

```scala
resolvers += "jcenter" at "http://jcenter.bintray.com"

libraryDependencies += "org.macroid" %% "macroid" % "2.0.0-M2"
```

It is also recommended to add a linter (the snippet below assumes sbt `0.13.x`):

```scala
addCompilerPlugin("org.brianmckenna" %% "wartremover" % "0.10")

scalacOptions in (Compile, compile) ++= Seq(
  "-P:wartremover:cp:" + (dependencyClasspath in Compile).value
  	.files.map(_.toURL.toString)
  	.find(_.contains("org.macroid/macroid_")).get,
  "-P:wartremover:traverser:macroid.warts.CheckUi"
)
```

If you are confused about sbt or using Scala on Android in general, [check this out](ScalaOnAndroid.html).