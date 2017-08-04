---
layout: docs
title: Viewable
section: modules
---

# Macroid-Viewable

This module provides typeclasses to convert data to Android layouts. It offers three advantages:

* A clear way to declare how your data should be displayed
* Boilerplate-free `ListAdapter`s and `PagerAdapter`s
* Macroid’s trademark emphasis on composability

## Installation

Assuming you already have Macroid `2.1.0` installed, add this line to your `build.sbt`:

```scala
libraryDependencies += aar("org.macroid" %% "macroid-viewable" % "2.1.0")
```

## Viewable

A *viewable* is an instace of `trait Viewable[A, +W <: View]` that tells how to display `A` using `W`. For example:

```scala
import macroid.viewable.Viewable

case class User(name: String)

def userViewable(implicit ctx: ContextWrapper): Viewable[User, TextView] =
  Viewable[User] { user ⇒
    w[TextView] <~ TextTweaks.large <~ text(user.name)
  }
```

A good idea is to keep *viewables* in a separate module of your project and reuse them as much as possible.

Now we can play with the above declaration in two ways:

### Explicit

Producing a layout:

```scala
val view: Ui[TextView] = userViewable.view(User("Nick"))
```

Producing a `PagerAdapter`:

```scala
val adapter = userViewable.pagerAdapter(List(
  User("John"),
  User("Michael")))

pager <~ PagerTweaks.adapter(adapter)
```

Even more concise:

```scala
pager <~ userViewable.pagerAdapterTweak(List(
  User("John"),
  User("Michael")))
```

### Implicit

By declaring your *viewable* implicitly and importing `macroid.viewable._`, you can go further:

```scala
import macroid.viewable._

implicit val userViewable: Viewable[User, TextView] = ...

// produce a layout
User("Nick").view

// produce an adapter:
List(User("John"), User("Michael")).pagerAdapter

// produce an adapter tweak:
List(User("John"), User("Michael")).pagerAdapterTweak
```

## Listable

A *listable* is an instace of `trait Listable[A, W <: View]` that tells how to display `A` using `W` in two steps:

1) Create an empty layout

2) Fill it with the data

Because of that, *listables* can be used with `ListAdapter`s, hence the name. Here is an example:

```scala
import macroid.viewable.Listable

case class User(name: String)

def userListable(implicit ctx: ContextWrapper): Listable[User, TextView] =
  Listable[User] {
    // create the layout
    w[TextView] <~ TextTweaks.large
  } { view ⇒ user ⇒
    // fill it with data
    view <~ text(user.name)
  }
```

Similarly to *viewables*, *listables* can be used both explicitly and implicitly:

```scala
import macroid.viewable._

// create a ListAdapter
userListable.listAdapter(User("Terry"), User("Graham"))

// or
List(User("Terry"), User("Graham")).listAdapter

// create a tweak for ListAdapter
userListable.listAdapterTweak(User("Terry"), User("Graham"))

// or
List(User("Terry"), User("Graham")).listAdapterTweak
```

`Listable` does not inherit from `Viewable` directly, because they have different variance in the `W` parameter, but
it’s possible to convert a *listable* to a *viewable*:

```scala
userListable.toViewable.view(User("Nick"))
```

Additionally, if there is an implicit `Listable[A, W]`, it can serve as an implicit `Viewable[A, W]`.

## Using combinators

To ease the task of defining new *viewables* and *listables*, the library provides some factory methods
and combinators. We have already seen the direct creation of both `Viewable` and `Listable`.
Let’s now look into other means available:

### `text`

Creating a *viewable* or a *listable* for strings is easy, just provide the styles:

```scala
val textViewable: Viewable[String, TextView] = Viewable.text {
  TextTweaks.large + TextTweaks.color(Color.RED)
}

val textListable: Listable[String, TextView] = Listable.text {
  TextTweaks.large + TextTweaks.color(Color.RED)
}
```

### `contraMap`

A *viewable*/*listable* for a string does not help much.
But we can use it to build something more complex through delegation:

```scala
val userViewable = textViewable.contraMap[User](user ⇒ user.name)
```

In other words, if we have a `Viewable[A, W]` and a function `B ⇒ A`, we can make `Viewable[B, W]`. The same applies
to `Listable`.

### `Listable.tw`

The “fill view” step in *listables* can be defined with a tweak:

```scala
case class User(name: String, age: Int)

def userListable(implicit ctx: ContextWrapper) =
  Listable[User].tw {
    // create layout
    w[TextView]
  } { user ⇒
    text(user.name) + TextTweaks.size(user.age + 10)
  }
```

### `Listable.tr`

Similarly, the same “fill view” step in *listables* can be defined with a transformer:

```scala
case class User(name: String, picture: Bitmap)

def userListable(implicit ctx: ContextWrapper) =
  Listable[User].tr {
    l[HorizontalLinearLayout](
      w[ImageView],
      w[TextView] <~ TextTweaks.large
    )
  }(user ⇒ Transformer {
    case img: ImageView ⇒ img <~ ImageTweaks.bitmap(user.picture)
    case txt: TextView ⇒ txt <~ text(user.name)
  })
```

### `Listable.wrap`

It is often required to have the *listable* enclosed into some container, for example, `CardView`.
Here’s how this can be done:

```scala
def cardListable[A, W <: View](listable: Listable[A, W])(implicit ctx: ContextWrapper) =
  Listable.wrap(listable) { w ⇒
    l[CardView](w) <~ Styles.card
  }
```

### `Listable.combine`

Stacking two *listables* together is another common task. For example, we have a class like this:

```scala
case class TimedPicture(timestamp: Long, picture: Bitmap)
```

and we have already defined *listables* for `Long` and `Bitmap`. To combine them:

```scala
// this will be a Listable[(Long, Bitmap), View]
def combinedListable(implicit ctx: ContextWrapper) =
  Listable.combine(timestampListable, pictureListable) { (t, p) ⇒
    l[VerticalLinearLayout](t, p)
  }

// now convert it to Listable[TimedPicture, View]
def timedPictureListable =
  combinedListable.contraMap[TimedPicture] { tp ⇒
    (tp.timestamp, tp.picture)
  }
```

## Slotted listable

`SlottedListable` implements the well-known
[holder pattern](http://developer.android.com/training/improving-layouts/smooth-scrolling.html#ViewHolder),
but in true Macroid style. It’s often what you want to use when the list element has two or more widgets.
Let’s jump straight to the code:

```scala
case class User(name: String, picture: Bitmap)

object UserListable extends SlottedListable[User] {
  // our ViewHolder
  class Slots {
    var name = slot[TextView]
    var picture = slot[ImageView]
  }
  
  // now we just need to implement two abstract methods of SlottedListable:

  // make and wire the slots
  def makeSlots(viewType: Int)(implicit ctx: ContextWrapper) = {
    val slots = new Slots
    val view = l[LinearLayout](
      w[ImageView] <~ wire(slots.picture),
      w[TextView] <~ wire(slots.name)
    )
    (view, slots)
  }

  // fill the slots
  def fillSlots(slots: Slots, data: User)(implicit ctx: ContextWrapper) = {
    (slots.name <~ text(data.name)) ~
    (slots.picture <~ data.picture)
  }
}
```

## Composing from alternatives

Both *viewables* and *listables* support composition from alternatives — the most
obvious use-case is joining several layout types in one. Everything below applies to
both `Viewable` and `Listable`, but we’ll proceed with just `Viewable` for simplicity.

Consider this toy “Macroid Now” example:

```scala
sealed trait MacroidNowCard

case class Weather(temp: Float) extends MacroidNowCard

case class PhotosNearby(photos: List[URL]) extends MacroidNowCard
```

Now suppose we have defined these:

```scala
val weatherViewable: Viewable[Weather, CardView] = ...
val photosNearbyViewable: Viewable[PhotosNearby, CardView] = ...
```

How do we define this?

```scala
val macroidNowViewable: Viewable[MacroidNowCard, CardView] = ???
```

This is where `PartialViewable` comes into play: it’s a *viewable*,
which is defined only for a certain subset of the data. *Partial viewables*
can be combined with the `orElse` operator, much like Scala’s `PartialFunction`s.

A *partial viewable* can be obtained in a few ways:

```scala
// this will give a PartialViewable[Weather, CardView],
// which is only defined for really hot days
weatherViewable.cond(_.temp > 40)

// this will give a PartialViewable[MacroidNowCard, CardView],
// which is only defined for cards that are instanceOf[Weather]
weatherViewable.toParent[MacroidNowCard]
```

The solution is thus to convert both our *viewables* to *partial viewables*,
combine them, and go back to the normal (or *total*) *viewable*:

```scala
val macroidNowViewable = {
  weatherViewable.toParent[MacroidNowCard] orElse
  photosNearbyViewable.toParent[MacroidNowCard]
}.toTotal
```

Finally, keep in mind that `Listable` provides exactly the same API.
