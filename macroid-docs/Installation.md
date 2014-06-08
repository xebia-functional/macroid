# Installation

*Macroid* is an ordinary Scala library and can be added to your sbt project like this:

```scala
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