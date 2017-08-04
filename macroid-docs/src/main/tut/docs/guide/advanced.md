---
layout: docs
title: Advances techniques
section: guide
---

# Advanced techniques

This section explains why [tweaking](Tweaks.html#tweaking) and [snailing](Snails.html#-snailing-) are much more powerful
than they seem at the first glance.

## Options

What are options? My favorite explanation is in the [Neophyte’s guide to Scala](http://danielwestheide.com/blog/2012/12/19/the-neophytes-guide-to-scala-part-5-the-option-type.html). Anyway, as you may already know, [`searching for widgets`](Searching.html) or
[`media queries`](MediaQueries.html) often force you to deal with `Option[...]` or even `Ui[Option[...]]`:

```scala
val display: Option[Tweak[View]] = widerThan(100 dp) ? show

val button: Ui[Option[Button]] = this.find[Button](Id.button)
```

However this poses no problem for either tweaking or snailing:

```scala
// if display is None, nothing happens
w[TextView] <~ (display: Option[Tweak[View]])

// if button is None (e.g. not found), nothing happens
(button: Ui[Option[Button]]) <~ show

// this works as well!
(button: Ui[Option[Button]]) <~ (display: Option[Tweak[View]])

// the same with snailing
buton <~~ fadeOut(300)
```

All this is possible thanks to a typeclass-based design (recommended reading:
[Neophyte’s guide to Scala](http://danielwestheide.com/blog/2013/02/06/the-neophytes-guide-to-scala-part-12-type-classes.html))
Now that we see the flexibility under the hood, let’s find out what else works!

## Lists

A widget can be tweaked with a list (or any `TraversableOnce`) of tweaks:

```scala
textView <~ List(show, text("Hi"))
```

Perhaps more interestinly, widgets can be tweaked in batches:

```scala
List(button1, button2) <~ show
```

Of course, you can use lists on both sides:

```scala
List(button1, button2) <~ List(show, text("Hi"))
```

Or use lists with options:

```scala
List(button1, button2) <~ (hdpi ? show)
```

## Futures

Here’s where it gets really interesting. Suppose we have a `Future[String]` with a caption,
that will become available only in a few moments:

```scala
val futureCaption = Future {
  Thread.sleep(4000) // used here for demonstration purposes only, never use in real code ;)
  "Wait for it!"
}
```

But we can use it in tweaking right now!

```scala
// the caption will be set 
textView <~ futureCaption.map(text)
```

## Functional reactive programming

Using a functional reactive library like [scala.rx](https://github.com/lihaoyi/scala.rx), we can go even further:

```scala
// create a reactive variable
val caption = rx.Var("Olá")

// set text to “Olá”
textView <~ caption.map(text)

// text automatically updates to “Adeus”
caption.update("Adeus")
```

`EventStream`s from [scala.frp](https://github.com/dylemma/scala.frp) are very similar, but do not have an initial value:

```scala
// create an event source
val clicks = EventSource[Unit]

// setup a button to fire events
button <~ On.click(Ui(clicks.fire(())))

// a more useful event stream
val randomInts = clicks.map(_ ⇒ scala.util.Random.nextInt().toString)

// the caption will update with each button click
textView <~ randomInts.map(text)
```

The respective helper declarations have been cut from Macroid with the intention of putting them into the newly-born
[macroid-frp library](../Related.html). Please stay tuned!

## Extending

If you want to use tweaking with your own container types, like `AdvancedFuture` or `CrazyOption`, there are
two ways of going about it:

* Provide an instance of a [`macroid.util.Effector` typeclass](../api/core/macroid/util/Effector.html),<br>
  which requires just a `foreach` method;
* Specify a [`macroid.CanTweak[W, T, R]` functional dependency](../api/core/macroid/CanTweak.html),<br>
  which allows to tweak `W` with `T` and get an `Ui[R]` as a result (see the sources for examples).

Extending snailing offers only the latter option:

* Specify a [`macroid.CanSnail[W, T, R]` functional dependency](../api/core/macroid/CanSnail.html),<br>
  which allows to snail `W` with `T` and get an `Ui[Future[R]]` as a result (see the sources for examples).

