# Macroid-Viewable

This library provides typeclasses to convert data to Android layouts. It offers two advantages:

* A clear and reusable way to declare how your data should be displayed
* Boilerplate-free `ListAdapter`s and `PagerAdapter`s

## Installation

Assuming you already have *Macroid* `2.0.0-M3` installed, add this line to your `build.sbt`:

```scala
libraryDependencies += aar("org.macroid" %% "macroid-viewable" % "2.0.0-M3")
```

## Viewable

A *viewable* is an instace of `trait Viewable[A, +W <: View]` that tells how to display `A` using `W`. For example:

```scala
import macroid.viewable.Viewable

case class User(name: String)

def userViewable(implicit ctx: ActivityContext, appCtx: AppContext): Viewable[User, TextView] =
  Viewable[User] { user ⇒
    w[TextView] <~ TextTweaks.large <~ text(user.name)
  }
```

A good idea is to keep *viewables* in a separate module of your project and reuse them as much as possible.

Now we can use the above declaration in two ways:

### Explicit

Producing a layout:

```scala
userViewable.view(User("Nick"))
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

def userListable(implicit ctx: ActivityContext, appCtx: AppContext): Listable[User, TextView] =
  Listable[User] {
    // the makeView step
    w[TextView] <~ TextSize.large
  } { view ⇒ data ⇒
    // the fillView step
    view <~ text(data.name)
  }
```

Similarly to *viewables*, *listables* can be used both explicitly and implicitly:

```scala
import macroid.viewable._

// create a ListAdapter
userListable.listAdapter(User("Terry"), User("Graham"))

// or
User("Terry"), User("Graham").listAdapter

// create a tweak for ListAdapter
userListable.listAdapterTweak(User("Terry"), User("Graham"))

// or
User("Terry"), User("Graham").listAdapter
```

`Listable` does not inherit from `Viewable` directly, because they have different variance in the `W` parameter, but
it’s possible to convert a *listable* to a *viewable*:

```scala
userListable.toViewable.view(User("Nick"))
```

Additionally, if there is an implicit `Listable[A, W]`, it can serve as an implicit `Viewable[A, W]`.

## SlottedListable

## Using combinators

## Composing from alternatives
