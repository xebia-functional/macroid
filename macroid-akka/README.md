## Akka Fragments

This project contains some glue to setup message-passing between Android Fragments using [Akka](http://akka.io).

It has no dependencies baked in — you have to add your version of Akka yourself (see below).

### How to use

Add to your `bulid.sbt`:

```scala
libraryDependencies ++= Seq(
  // this library
  // right now you have to `publish-local` it yourself
  "org.macroid" %% "akka-fragments" % "1.0.0-SNAPSHOT",
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
  "-keep class org.macroid.akkafragments.AkkaAndroidLogger { *; }",
  "-keep class akka.event.Logging$LogExt { *; }"
)
```

Add to your `Activity`:

```scala
import org.macroid.akkafragments.AkkaActivity

class MyActivity extends Activity with AkkaActivity {
  // define actor system name
  val actorSystemName = "my-system"
  
  // use the provided actor system to create some actors
  lazy val actor1 = actorSystem.actorOf(..., ...)
  
  ...
}
```

Now the idea is that we create an actor per each fragment. The actors will live as long as the activity lives.
The fragments, on the other hand, come and go, as they normally do. Thus our actors can be “attached” and “detached”
from the user interface they control.

Here is a fragment actor:

```scala
import org.macroid.akkafragments.FragmentActor

object MyActor {
  // this is a common Akka pattern: http://doc.akka.io/docs/akka/snapshot/scala/actors.html
  // IMPORTANT: notice how we use `new MyActor` and not `Props[MyActor]`
  // this forces Proguard to keep the class `MyActor`
  // you might add a Proguard rule as well though
  def props = Props(new MyActor)
}

class MyActor extends FragmentActor[MyFragment] {
  // receiveUi handles attaching and detaching UI
  // and then (sic!) passes the message to us
  def receive = receiveUi andThen {
    case MyMessage(x) ⇒ ...
    
    // we can use `withUi`, which will provide us
    // with the currently attached fragment, if any
    // the code inside `withUi` runs on the UI thread
    case MyOtherMessage ⇒ withUi { fragment ⇒
      ...
    }
  }
}
```

Finally, a fragment:

```scala
import android.support.v4.app.Fragment
import org.macroid.akkafragments.AkkaFragment

class MyFragment extends Fragment with AkkaFragment {
  // find the actor for this fragment
  // which we created in the Activity
  // if this fragment does not have a dedicated actor,
  // set to `None`
  lazy val actor = Some(actorSystem.actorSelection("/user/actor1"))
  
  ...
  // `AkkaFragment` uses “stackable trait” pattern
  // (http://www.artima.com/scalazine/articles/stackable_trait_pattern.html)
  // to override `onStart` and `onStop` with attaching and detaching of the actor
}
```

There is also an Android-compatible Akka logger. To use it, add to your `src/main/resources/application.conf`:
```javascript
akka {
  loggers = ["org.macroid.akkafragments.AkkaAndroidLogger"]
}
```
