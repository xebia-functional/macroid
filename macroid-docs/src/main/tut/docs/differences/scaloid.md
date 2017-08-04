---
layout: docs
title: Scaloid
section: differences
---

# Differences from Scaloid

If you know [Scaloid](https://github.com/pocorall/scaloid), you might be wondering, why I created yet another
library to do basically the same thing. I am going to elaborate on that here.
Keep in mind that Macroid is first and foremost a consistent and powerful UI language,
so if you need extra bells and whistles, like Scala-idiomatic preferences management, you might want to use both projects.

## Full widget compatibility

*Scaloid* hugely depends on its own `STextView`, `SImageView`, etc.

Macroid works with any `View`, custom or not. 

## Cleaner layout structure

The structure of *Scaloid* UI code is very irregular:

```scala
new SVerticalLayout {
  STextView("ID")
  val userId = SEditText()
  SButton("Sign in", signin(userId.text))
}.padding(20 dip)
```

Here some objects are created with `new`, and some — without it; every widget class takes a different
number of arguments; the `userId` variable stands out and breaks the layout visually.
This is the equivalent Macroid code:

```scala
var userId = slot[EditText]
 
l[VerticalLinearLayout](
  w[TextView] <~ text("ID"),
  w[EditText] <~ wire(userId),
  w[Button] <~ text("Sign in") <~ On.click(signin(userId.get.getText))
) ~> padding(all = 20 dp)
```

The layout is much cleaner, even if a bit more verbose.
You may find it strange that `slot[EditText]` actually returns an `Option[EditText]`,
but this is in line with `find`, which also returns an `Option`.
(Read about `Option` in the [Neophyte’s guide to Scala](http://danielwestheide.com/blog/2012/12/19/the-neophytes-guide-to-scala-part-5-the-option-type.html).)
If you don’t call `get` directly
as in this example and only use `map` and other combinators, it’s a great way to save yourself from
`NullPointerException`s when the slot is not wired, or the view is not found.

## Composable components

Things like `text("ID")` or `TextSize.large` in Macroid are called `tweak`s.
The big win of `myTextView <~ text(...) <~ ...` over `myTextView.text(...)....` is that `tweak`s exist on their own,
so you can combine them and store in variables, traits or objects in any way you want. You can make your own `tweak`,
say

```scala
def largeText(str: String)(implicit ctx: ContextWrapper) =
  text(str) + TextSize.large
```

and use it somewhere. The documentation has a section on [tweaks](../guide/Tweaks.html).

## High-level abstractions

Macroid supports generalized widget tweaking with `Future`s and `EventStreams`, e.g.

```scala
val showInASecond = Future {
  Thread.sleep(1000) // used as an example, never use sleep in real code ;)
  show
}
myTextView <~ hide <~ showInASecond
```

## Fragment support

Macroid natively supports Fragments, allowing to embed them inside the layout code
(`Fragment`s are created if necessary and wrapped in `FrameLayout`s):

```scala
l[LinearLayout](
  // It.stuff generates a new id
  // Tag.stuff just returns "stuff", but boy! isn’t that fancy?
  f[MapFragment].framed(Id.map, Tag.mainMap)
)
```

Both `android.app.Fragment` and `android.support.v4.app.Fragment` are supported.

## Integrated media queries

The media queries are an integrated part of the UI language. For example, `hdpi ? text("Welcome to HD!")`
just returns an `Option[Tweak[TextView]]`, so you can store and use it anywhere.
This contrasts with *Scaloid*, where `if` conditionals are used.

## Safer implicit context

*Scaloid* always holds on to the Activity instance to store its `implicit` Context,
which [may result in memory leaks](http://android-developers.blogspot.co.at/2009/01/avoiding-memory-leaks.html).

Macroid uses `WeakReference`s to store `Context`s other than `Application`, avoiding the above problem.

## Safer threading

As we know from the Android documentation,

> The Andoid UI toolkit is not thread-safe. So, you must not manipulate your UI from a worker thread—you must do all manipulation to your user interface from the UI thread.

In *Scaloid* only a few methods, such as `toast`, ensure that they are run on the UI thread. Macroid uses a completely different approach,
wrapping any UI manipulation in a so-called UI action. UI actions can be combined to be run in a batch, and have a method to be sent to the
UI thread for execution.

## Unique features

Macroid boasts several unique features. For example, [Snails](../guide/Snails.html) provide
a mini data-flow language to create animations and other “slow” behaviors.
[Layout transformers](../guide/Transformers.html) employ the power of pattern matching
to tweak nested layouts. Browsing the [guide](Guide.html) will give you a sense of what
else is possible with Macroid.