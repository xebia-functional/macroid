# Searching

Sometimes you need to search for a particular widget, layout, or fragment. Let’s see how it can be done.

## Slots and wires

This is a better and safer (and thus the preferred) way. First, declare a slot:

```scala
var button = slot[Button]
```

The variable is given a type `Option[Button]` with a value of `None`.
(Read about `Option`s in
[Neophyte’s guide to Scala](http://danielwestheide.com/blog/2012/12/19/the-neophytes-guide-to-scala-part-5-the-option-type.html))
Now you can wire a widget to it:

```scala
w[Button] <~ wire(button)
```

Once the layout is created, `button` will point to the actual widget.

Even though slots are `Option`s,
tweaking works with them as expected ([see why](Advanced.html)). On the other hand,
using an `Option` protects you from `NullPointerException`s in cases when the layout is
not yet created or the slot is not wired.

```scala
var button = slot[Button]
...
button <~ text("Hi")
```

Note that getting a property back from the slot, e.g. the text from the `EditText` will
require you to either call through `get` or deal with an `Option`:

```scala
var edit = slot[EditText]
...
val text1: String = edit.get.getText
// or
val text2: Option[String] = edit.map(_.getText)
```

This may be addressed in future versions of *Macroid*.

## Id and tag generation

Creating your widget ids in code might not be very rewarding. *Macroid* offers an elegant solution
using Scala’s [`Dynamic` feature](http://docs.scala-lang.org/sips/completed/type-dynamic.html).

First, create a global id generator. You can specify the starting id:

```scala
import macroid.IdGenerator

object Id extends IdGenerator(start = 1000)
```

Now `Id.something` will generate a new id for you:

```scala
// Id.button creates a new id
w[Button] <~ id(Id.button)

// once created, the id stays the same
assert(Id.button == Id.button)
```

Sometimes you also need fragment tags, which are plain strings.
For symmetry, *Macroid* includes a `Tag` singleton:

```scala
// Doesn’t this look better than just "map"?
f[MapFragment].framed(Id.map, Tag.map)

// tags are just strings
assert(Tag.map == "map")
```

## `find` and `findFrag`

If a `View` has an id, it can be found like this:

```scala
import macroid.FullDsl._
// or just
// import macroid.Searching._

someParentView.find[ViewClass](id)
// or
activity.find[ViewClass](id)
// or
fragment.find[ViewClass](id)
```

For example:

```scala
class MyActivity extends Activity with Contexts[Activity] {
  ...
  w[Button] <~ id(Id.button)
  ...
  val button = this.find[Button](Id.button)
}
```

Note that `find` returns an `Ui[Option[...]]`: if the `View` is not found or has a wrong type, the underlying result will be `None`.
This however [poses no problem](Advanced.html) for tweaking:

```scala
this.find[Button](Id.button) <~ text("foo")
```

There is also a `findFrag` method:

```scala
activity.findFrag[FragmentClass](tag)
// or
fragment.findFrag[FragmentClass](tag)
```
Be aware that it will only be able to find fragment in ```onStart``` and later stages in the lifecycle.
For example:

```scala
class MyActivity extends Activity with Contexts[Activity] {
  override def onCreate(savedInstanceState: Bundle) = {
    ...
    f[MapFragment].framed(Id.map, Tag.map)
    ...
  }

  override def onStart: Unit = {
    val map = this.findFrag[MapFragment](Tag.map)
  }
}
```

`findFrag` returns an `Ui[Option[...]]` as well.
