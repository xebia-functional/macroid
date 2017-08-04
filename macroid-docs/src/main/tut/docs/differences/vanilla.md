---
layout: docs
title: Vanilla Android
section: differences
---

# Differences from vanilla Android

## XML vs code

Android XML UI layouts have quite a few disadvantages:

* *Only one layout per file*

  If you want to build a layout from smaller pieces, each one has to be put in its own file,
  greatly boosting the number of files and making it hard to track.

  Macroid lets you define layouts anywhere in any quantities, using plain Scala code.

* *No namespacing*

  No folders are allowed, thus all layouts, including those small pieces, that may be only
  relevant for a particular activity, pollute the IDE sidebars.

  Macroid leverages the power of Scala’s modular aspect: by using `object`s and flexible
  `import`s, any desired namespacing can be achieved for layouts, styles, behaviors, etc.

* *Folders for different screen sizes*

  While this adaptation mechanism is relatively easy to use, it’s not very flexible.

  Macroid provides media queries, which allow you to define composable adaptive
  pieces of layouts or behaviors.

* *Code required anyway*

  Glue code finding widgets by ids and assigning event handlers feels a bit weird.

  Before you tell me that it’s about separating looks from behaviors, Macroid
  does not stand in the way of this separation at all! Widgets can be wired to
  variables and manipulated somewhere else.

Of course, there are advantages too:

* *XML layouts can be edited visually*

  (Although it does not help much if you are populating them dynamically.)

* *Immediate feedback*

  Editing XML layouts is very fast and straightforward. On the contrary,
  updating Macroid layouts requires recompiling and rerunning the app.
  Still, with proper incremental compiler ([sbt](http://www.scala-sbt.org/)) and
  ProGuard cache ([android-sdk-plugin](https://github.com/pfn/android-sdk-plugin))
  this normaly takes a mere 20 seconds. And you can always use dedicated sketching
  tools to develop the UI look even faster than with XML.

## High-level abstractions

Macroid supports generalized widget tweaking, for example you can take
a textbox and assign it some text, that will only be available in a few seconds:

```scala
val futureText = Future {
  Thread.sleep(5000) // used as an example, never use sleep in real code ;)
  text("Hi!")
}
myTextView <~ futureText
```

## Safer implicit context

Macroid uses `WeakReference`s to store `Context`s other than `Application`. This helps to automatically avoid
[memory leaks](http://stackoverflow.com/questions/3346080/android-references-to-a-context-and-memory-leaks).

## Safer threading

As we know from the Android documentation,

> The Andoid UI toolkit is not thread-safe. So, you must not manipulate your UI from a worker thread—you must do all manipulation to your user interface from the UI thread.

Macroid wraps any UI manipulation in a so-called UI action.
UI actions can be combined to be run in a batch, and have a method to be sent to the
UI thread for execution.

## Unique features

Macroid boasts several unique features. For example, [Snails](../guide/Snails.html) provide
a mini data-flow language to create animations and other “slow” behaviors.
[Layout transformers](../guide/Transformers.html) employ the power of pattern matching
to tweak nested layouts. Browsing the [guide](Guide.html) will give you a sense of what
else is possible with Macroid.