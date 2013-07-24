import scalariform.formatter.preferences._

scalariformSettings

ScalariformKeys.preferences := FormattingPreferences()
.setPreference(RewriteArrowSymbols, true)
.setPreference(PreserveDanglingCloseParenthesis, true)

seq(githubRepoSettings: _*)

localRepo := Path.userHome / "macroid-staging"

githubRepo := "https://github.com/stanch/macroid.git"

name := "macroid"

organization := "org.macroid"

version := "1.0-SNAPSHOT"

scalaVersion := "2.10.3-SNAPSHOT"

scalaOrganization := "org.scala-lang.macro-paradise"

compileOrder := CompileOrder.JavaThenScala

autoCompilerPlugins := true

libraryDependencies <+= (scalaVersion) {
    v => compilerPlugin("org.scala-lang.plugins" % "continuations" % v)
}

libraryDependencies <+= (scalaVersion) {
	v => "org.scala-lang.macro-paradise" % "scala-reflect" % v
}

unmanagedClasspath in Compile += new File(System.getenv("ANDROID_SDK_HOME")) / "platforms" / "android-17" / "android.jar"

scalacOptions += "-P:continuations:enable"

resolvers ++= Seq(
	Resolver.sonatypeRepo("snapshots")
)

libraryDependencies ++= Seq(
	"com.typesafe.akka" %% "akka-dataflow" % "2.2.0-RC1",
	"com.scalarx" %% "scalarx" % "0.1",
	"me.lessis" %% "retry-core" % "0.1.0"
)