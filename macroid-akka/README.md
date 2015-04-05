### Macroid-Akka

This module contains some glue to setup message-passing between Android Fragments using [Akka](http://akka.io).

#### Installation

Assuming you already have *Macroid* `2.0.0-M4` installed, add this line to your `build.sbt`:

```scala
libraryDependencies ++= Seq(
  // this library
  aar("org.macroid" %% "macroid-akka" % "2.0.0-M4"),
  
  // akka, if not included before
  "com.typesafe.akka" %% "akka-actor" % "2.2.3"
)

// Proguard rules
proguardOptions ++= Seq(
  "-keep class akka.actor.LightArrayRevolverScheduler { *; }",
  "-keep class akka.actor.LocalActorRefProvider { *; }",
  "-keep class akka.actor.CreatorFunctionConsumer { *; }",
  "-keep class akka.actor.TypedCreatorFunctionConsumer { *; }",
  "-keep class akka.dispatch.BoundedDequeBasedMessageQueueSemantics { *; }",
  "-keep class akka.dispatch.UnboundedMessageQueueSemantics { *; }",
  "-keep class akka.dispatch.UnboundedDequeBasedMessageQueueSemantics { *; }",
  "-keep class akka.dispatch.DequeBasedMessageQueueSemantics { *; }",
  "-keep class akka.actor.LocalActorRefProvider$Guardian { *; }",
  "-keep class akka.actor.LocalActorRefProvider$SystemGuardian { *; }",
  "-keep class akka.dispatch.UnboundedMailbox { *; }",
  "-keep class akka.actor.DefaultSupervisorStrategy { *; }",
  "-keep class macroid.akka.AkkaAndroidLogger { *; }",
  "-keep class akka.event.Logging$LogExt { *; }"
)
```

Documentation is available here: http://macroid.github.io/modules/Akka.html
