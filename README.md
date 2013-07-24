# *Macroid* — a Scala layout DSL for Android

### Requirements

* Scala ```2.10+```
* Android ```API 17+```

### What’s the buzz

*Macroid* is intended to complement the excellent [scaloid](https://github.com/pocorall/scaloid) project. It’s by no means
a replacement, but rather a collection of some useful things under a slightly different sauce :)

Let’s see what we have.

#### The DSL

```scala
l[LinearLayout](
  w[TextView] ~> text("Loading...") ~> { x ⇒
    // extra initialization
  },
  w[ProgressBar](null, android.R.attr.progressBarStyleLarge) ~>
    id(Id.progress) ~>
    center(),
  f[MyAwesomeFragment](Id.stuff, Tag.stuff, Map("number" → 4))
)
```

The three main components are:
* ```l[...]``` — a macro to create layouts. Supports arbitrary ```ViewGroup```s
* ```w[...]``` — a macro to create widgets. Supports arbitrary ```View```s, even with parameters (as in ```ProgressBar``` example. The only requirement is that ```Context``` parameter is the first one in ```View```’s constructor.
* ```f[...]``` — a macro to create fragments. It creates the fragment if not already created, wraps in a ```FrameLayout``` and returns it.

### Installation

If you plan to use dataflow:
```scala
autoCompilerPlugins := true

libraryDependencies <+= scalaVersion {
  v => compilerPlugin("org.scala-lang.plugins" % "continuations" % v)
}

scalacOptions += "-P:continuations:enable"
```

To include macroid itself:
```scala
resolvers ++= Seq(
  "Macroid snapshots" at "http://stanch.github.com/macroid/snapshots/",
  Resolver.sonatypeRepo("snapshots")
)

libraryDependencies += ("org.macroid" %% "macroid" % "1.0-SNAPSHOT") exclude ("org.scala-lang.macro-paradise", "scala-library")
```
