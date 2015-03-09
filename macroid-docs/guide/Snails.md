# Snails

Snails provide a dataflow mini-language for animations and things like that.

## What are they

Snails are almost like [tweaks](Tweaks.html), but are used when changing a widget property takes time.
A snail is defined by a function `WidgetClass ⇒ Future[Unit]`.

## Defining

Snails can be defined using the `Snail` companion object:

```scala
import macroid.Snail

// plays a sound
// and returns a future to track when it ends
def playSound = {
  val done = Promise[Unit]()
  // set up media player
  // play sound
  // when complete, call `done.success(())`
  done.future
}

// set focus and play a sound
val focusLoudly = Snail[View] { view ⇒
  view.setFocus()
  playSound
}
```

## Composing

Snails can be composed by using the `++` operator:

```scala
val wink = fadeIn ++ fadeOut
```

It is also possible to combine snails with tweaks by using either `+` or `++`.
More specifically, snails are added with `++`, and tweaks are added with `+`
(see [Understanding operators](Operators.html)):

```scala
def textAndFade(str: String) =
  text(str) ++ fadeIn

val fadeAndDisappear =
  fadeOut + hide
```

## “Snailing”

“Snailing” is a made-up word that denotes the process of applying snails to widgets.
Snailing is very similar to [tweaking](Tweaks.html#tweaking), except it uses the `<~~` operator
(see [Changelog](../Changelog.html) for the recent syntax changes):

```scala
textView <~~ fadeIn
```

It can be chained:

```scala
textView <~~ fadeIn <~~ fadeOut
```

Snailing can be also mixed with tweaking:

```scala
editText <~ text("foo") <~~ fadeIn <~ enable
```

It you want to apply the snail without waiting for it to finish, you can use `<~`.
By the way, if you are confused about all these operators, check the [Understanding operators](Operators.html) section.

Snailing returns a [UI action](UiActions.html), to be more specific, a `Ui[Future[...]]`.

## Snailing workflows

By combining UI actions, we can program complex asynchronous snailing (and tweaking) workflows,
for example:

```scala
(myProgressBar <~~ fadeOut(400)) ~~
(myTextView <~~ blink) ~~
(myOtherTextView <~ text("’SUP?"))
```

Note the use of `~~` operator: it denotes that we need to wait for completion of the action.

## Standard snails

A number of standard snails are included. You can always find them in the [scaladoc](../api/macroid/Snails$.html).
Note that all these snails can be imported with `import macroid.FullDsl._` or just `import macroid.Snails._`.

A quick rundown:

* Basic snails
  * `delay` — wait for a given time
  * `wait` — wait for a given `Future`
* Progress
  * `waitProgress` — modify a `ProgressBar` to track a `Future` or a list of `Future`s
* Animation
  * `anim` — apply an animation
  * `fadeIn` — apply a fade-in
  * `fadeOut` — apply a fade-out