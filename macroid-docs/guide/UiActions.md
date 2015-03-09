# UI actions

It is well-known that

> The Andoid UI toolkit is not thread-safe. So, you must not manipulate your UI from a worker thread—you must do all manipulation to your user interface from the UI thread.

In order to enforce correct thread usage, *Macroid* introduces UI actions.

## What are they

UI actions are pieces of code that are supposed to be run on the UI thread.
A simple UI action can be created like this:

```scala
import macroid.Ui

val action = Ui {
  textView.setText("Hi")
  3 + 3
}
```

It does not do anything yet and has a type `Ui[Int]`, since it returns an integer.
If you know [Haskell](http://www.haskell.org/haskellwiki/Haskell) or
[scalaz](https://github.com/scalaz/scalaz), so far that’s your typical `IO` monad.

An UI action can be sent to the UI thread to be run:

```scala
action.run
// or
import macroid.FullDsl._
runUi(action)
```

Running an action of type `Ui[A]` returns a `Future[A]` (in this case — `Future[Int]`),
so that you can monitor its completion. If the action fails,
you can handle the failure with the standard [Future API](http://docs.scala-lang.org/overviews/core/futures.html).

*Note that UI actions are not memoized, so running an action will recalculate the result each time*.

In some cases you will also want to run it on the current thread (but you have to be 100% sure you are inside
the UI thread):

```scala
action.get
// or
import macroid.FullDsl._
getUi(action)
```

## Advantages

The major advantages of using UI actions are:

* We can ensure that the UI code is always run on the correct thread;
* When submitting several UI commands from a background thread, we can combine them and batch in a single `Runnable`, so that the operation is atomic.

## Usage in *Macroid* APIs

In *Macroid* all UI-related methods return UI actions. This includes the layout bricks:

```scala
val button: Ui[Button] = w[Button]
```

tweaking:

```scala
val action: Ui[Button] = button <~ text("Hi")
```

and so on. The first case is one of the reasons why `getUi` is needed sometimes, as `setContentView` expects a `View` and
not a `Ui[View]`:

```scala
setContentView {
  getUi {
    w[TextView] <~ text("Hi")
  }
}
```

## Composing

UI actions can be composed in several ways.

* Using the `~` operator (the type of the last action is preserved)

  ```scala
  val action: Ui[Button] =
    (startButton <~ disable) ~
    (stopButton <~ enable)
  ```

* Using the `~~` operator (in case of `Ui[Future]`).
  See [Snailing workflows](Snails.html#snailing-workflows) for more details, or
  [Understanding operators](Operators.html), if you are confused.

* Using `Ui.sequence` (the sequence type is preserved)

  ```scala
  val action: Ui[Seq[Button]] = Ui.sequence(
    startButton <~ disable,
    stopButton <~ enable
  )
  ```

* If you feel like depending on `scalaz`, you can define an instance of `Monad[Ui]`
  and use [Effectful](https://github.com/pelotom/effectful):

  ```scala
  val action: Ui[Button] = effectfully {
    (startButton <~ disable).!
    // some other code, 
    // that will also be run
    // on the UI thread
    (stopButton <~ enable).!
  }
  ```

## Best practices

Since the UI thread has to be cherished, it is a good practice to demarkate the parts of
your code that need to use it. Returning UI actions instead of just executing the code
allows to do just that. That’s why, for example, setting an event handler in *Macroid*
requires you to provide a UI action. Declaring your methods as UI actions also helps
when they should be evaluated each time they are called (which is normally the case with event handlers).

For an example of how to take advantage of UI actions together with [Akka](http://akka.io) actors,
take a look at [macroid-akka-pingpong](http://typesafe.com/activator/template/macroid-akka-pingpong) Activator
template.

## Caveats

Finally, it’s worth noting that Scala does not have an effect tracking system.
(All right, it does have a [seemingly frozen one](https://github.com/lrytz/efftp)).
Therefore if for example you use tweaking in a statement position, like this:

```scala
def wrong = {
  button <~ text("Hi")
  45
}
```

the UI action will not be run and the compiler will normally not help you. Don’t panic.
You can either enable `-Xlint`, under which non-`Unit` statements produce warnings, or use *Macroid*’s custom linter
written with [Wartremover](https://github.com/typelevel/wartremover), which will produce a compile error.
For instructions refer to the [installation](../Installation.html) section.

## Low-level threading utilities

*Macroid* also offers several low-level threading utilities:

* `macroid.util.UiThreadExecutionContext` allows to run futures on the UI thread.
You probably wouldn’t need to use this directly.
* `UiFuture` extension class provides `mapUi`, `flatMapUi` and other `xxxUi` methods, where the supplied function will be called on the UI thread:

  ```scala
  import macroid.UiThreading._
  import scala.concurrent.ExecutionContext.Implicits.global

  Future {
    ... // happens on background thread
  } mapUi { x ⇒
    ... // happens on the UI thread
  }
  ```
