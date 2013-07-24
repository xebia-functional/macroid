resolvers ++= Seq(
  "jgit-repo" at "http://download.eclipse.org/jgit/maven",
  "hexx-releases" at "http://hexx.github.com/maven/releases"
)

addSbtPlugin("com.github.hexx" % "sbt-github-repo" % "0.0.1")