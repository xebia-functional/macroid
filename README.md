# *Macroid* — a Scala GUI DSL for Android

### Requirements

* Scala `2.10+`
* Android `API 11+` (uses `Fragment`s from the support library and the stock `ActionBar`)

### What’s the buzz

```scala
// in the activity/fragment class, prepare two member variables
// to hold references to our `View`s
var status: TextView = _
var bar: ProgressBar = _
...
// create a LinearLayout
val view = l[LinearLayout](

  // a TextView
  // note how we use `~>` to tweak each View
  // adventurous unicode lovers can of course stick with `⇝`
  w[TextView] ~>
    // if screen width is more than 400 dp, set long text, otherwise — the short one
    text(minWidth(400 dp) ? "Loooooooading..." | "Loading...")) ~>
    // assign to `var status` above
    wire(status),
    
  // a ProgressBar
  w[ProgressBar] ~>
    // set id to a freshly generated id
    id(Id.progress) ~>
    // assign to `var bar` above
    wire(bar),
    
  // an Easter Egg for people with high-density screens
  w[ImageView] ~> (xhdpi ? show | hide) ~> { x ⇒
    // we can use closures for extra initialization
    x.setImageResource(R.drawable.enjoyingXhdpiHuh)
  },
    
  // finally, a button
  w[Button] ~>
    // a shortcut to setOnClickListener
    On.click {
      // hide the progress bar
      bar ~> hide
      // set status text
      status ~> text("Finished!")
    } ~>
    // button’s caption
    text("Click me!") ~>
    // stretch horizontally
    layoutParams(WRAP_CONTENT, MATCH_PARENT),
    
  // create a fragment with freshly generated id and tag
  // convert the rest of the arguments to a Bundle and pass to the fragment
  // put the fragment inside a FrameLayout and insert into our layout
  f[MyAwesomeFragment](Id.stuff, Tag.stuff, "items" → 4, "title" → "buzz")
)
setContentView(view)
```

#### Layouts, Widgets, Fragments

* `l[LayoutClass](child1, child2, ..., childn)` creates a layout. `LayoutClass` should inherit from `ViewGroup`.
* `w[WidgetClass]` or `w[WidgetClass](arg1, arg2, ..., argn)` creates widgets.
  Supports arbitrary `View`s; the only requirement is that `Context` parameter is the first one in `WidgetClass`’s constructor.
  Oh, and you don’t have to ever mention `Context`, since it’s taken from the `implicit` scope.
* `f[FragmentClass](id, tag, arg1, arg2, ... argn)` deals with fragments. It creates the fragment if not already created, wraps in a `FrameLayout` and returns it.
  The macro tries to create the fragment using `newInstance()` from its companion and passing the arguments through.
  If there is no suitable overload of `newInstance()`, it treats the args as a sequence of 2-tuples, converts them to a `Bundle`,
  creates the fragment with the primary constructor and passes the bundle to `setArguments`.

#### Tweaks

The central concept of *Macroid* is that of `Tweak`s. These are little things that are chained and applied to widgets
with `~>` (or fancier `⇝`). Every `Tweak` is mutating the widget, doing something useful. Tweaks are better than member methods, because they can be easily combined:
```scala
def idAndText[A <: TextView](i: Int, t: String) = id(i) + text(t)
```

For functional junkies out there it will come as no surprise that `Tweak`s form a `Monoid`:
```scala
implicit def tweakMonoid[A] = new Monoid[Tweak[A]] {
  def zero = { x ⇒ () }
  def append(t1: Tweak[A], t2: ⇒ Tweak[A]) = t1 + t2
}
```
which we will see helpful later.

Here’s how to make a `Tweak`:
```scala
def id[A <: View](id: Int): Tweak[A] = x ⇒ x.setId(id)
```

Finally, note that `Tweaks` can be applied to any `View` type they are defined at. You can use `~>` in your code freely,
even if you don’t use layout creation DSL.

#### Media Queries

Media queries are inspired by the eponymous CSS feature. Here are some examples:
```scala
// if screen width > 1000 dip, make layout horizontal, else make it vertical
l[LinearLayout](...) ~> (minWidth(1000 dp) ? horizontal | vertical)

// use different widget depending on screen width
(widerThan(1000 dp) ? w[BigTextView] | widerThan(600 dp) ? w[MediumTextView] | w[TextView]) ~> text("Hi there")

// remember Tweaks form a Monoid? so we don’t have to supply the alternative here, tweakMonoid.zero will be used
w[TextView] ~> text("Balderdash!") ~> (minWidth(500) ?! largeAppearance)
```

Queries act like `Boolean`s and can be converted to them implicitly.
`query ? x` returns `Some(x)` if the query condition holds, otherwise — `None`.
We extend `Option`s with a `|` operator to provide a more streamlined API.
`query ?! x` is a special form to be used with `Monoid`s, where `zero` element is used if the query condition does not hold.

Currently supported queries:
* `minWidth`/`widerThan`, `maxWidth`/`narrowerThan`
* `minHeight`/`higherThan`, `maxHeight`/`lowerThan`
* `ldpi`, `mdpi`, `hdpi`, `xhdpi`

Units:
* `dp` density-independend points (use for everything except text)
* `sp` scale-independent points (use for text)
* `px` (seriously though, don’t use it)

#### Searching for widgets and fragments

The preferred way of referencing the widgets is the following:
```scala
// set up a member variable
var myButton: Button = _
...
def onCreate(...) {
  ...
  // wire the widget to the variable
  w[Button] ~> wire(myButton) ~> ...
  ...
}
```
* It’s typesafe
* It’s easy to read and write
* If the activity/fragment’s layout has been created, the `var` is guaranteed to be initialized.
  Before that, you shouldn’t touch your UI anyway!

That being said, you can use `Id.something` to generate ids ( *by default ids are generated in range [1000..]* ).
`Id.foo` will always return the same number inside a particular activity or fragment. To find widgets by id,
use one of the following:
* `def findView[A <: View](id: Int): A` — search in the root `View`
* `def findView[A <: View](root: View, id: Int): A` — search in `root`

Similarly, for fragments `Tag.something` generates a new tag.
Just kidding, it simply returns `"something"`, but isn’t that fancy? Use this to search for fragments:
* `def findFrag[A <: Fragment](tag: String): A`

#### Smashing some boilerplate

* `layoutParams` or `lp`:
  Automatically uses `LayoutParams` constructor from the parent layout or its superclass.
  ```scala
  import ViewGroup.LayoutParams._ // to get WRAP_CONTENT and MATCH_PARENT
  import org.macroid.Transforms._ // to get layoutParams or lp
  
  l[MyShinyLayout](
      w[Button] ~> layoutParams(WRAP_CONTENT, MATCH_PARENT) ~> text("Click me")
  )
  ```
  To use `layoutParams` ouside the layout, take a look at `layoutParams.of[B](...)`,
  for which you can supply the layout type in `B`.
* You can setup almost any `View` event listener with the following syntax:
  ```scala
  import org.scaloid.common._ // to get toast
  import org.macroid.Tweaks._ // to get tweaks
  import org.macroid.Util.Thunk

  // Thunks are a convenient way to store blocks of code without evaluating them
  // Practically Thunk(a) is the same as () ⇒ a
  val balderdash = ByName {
    toast("Balderdash!")
    true
  }

  l[LinearLayout](
      w[Button] ~>
        text("Click me") ~>
        // use by-name argument
        On.click(toast("Howdy?")) ~>
        // use by-name argument, returning a value
        On.longClick { toast("Splash!"); true },
        
      w[Button] ~>
        text("Don’t click me") ~>
        // use function of the same type signature as OnClickListener.onClick
        FuncOn.click { v: View ⇒ toast(v.getText) } ~>
        // use a thunk
        ThunkOn.longClick(balderdash)
  )
  ```

#### Staying in the UI thread

*Macroid* provides a bunch of great ways to run things on UI thread.

Using [Scala `Future`s](http://docs.scala-lang.org/sips/pending/futures-promises.html):
```scala
import org.macroid.Concurrency._
future {
  // some asynchronous calculation
} onSuccessUi { case x ⇒
  // manipulate the result on UI thread
} onFailureUi { case t ⇒
  // handle the failure on UI thread
}
```

Using [Akka Dataflow](http://doc.akka.io/docs/akka/snapshot/scala/dataflow.html):
```scala
import akka.dataflow._
import org.macroid.Concurrency._
flow {
  // asynchronously wait for completion of some calculations
  val a = await(someFuture)
  val b = await(otherFuture)
  // execute the rest of the flow block on UI thread
  switchToUiThread()
  findView[Button](Id.myButton) ~> text(a + b)
} onFailureUi { case t ⇒
  // handle the failure on UI thread
  t.printStackTrace()
  findView[TextView](Id.error) ~> text("Oops...")
}
```

Finally, there is a `runOnUiThread` method, that returns a `Future`,
so that you can monitor when the UI code finishes executing.

### Installation

To use dataflow (example for SBT < 0.13):
```scala
autoCompilerPlugins := true

libraryDependencies <+= scalaVersion {
  v => compilerPlugin("org.scala-lang.plugins" % "continuations" % v)
}

scalacOptions += "-P:continuations:enable"
```

To include macroid itself:
```scala
resolvers ++= Seq(
  "JCenter" at "http://jcenter.bintray.com",
  Resolver.sonatypeRepo("snapshots")
)

libraryDependencies += "org.macroid" %% "macroid" % "1.0.0-20130918"
```

### Imports and traits

Most of the stuff comes both in ```trait``` and ```object``` flavors. You can use
```scala
import org.macroid.Concurrency
class MyActivity extends Activity with Concurrency {
...
}
```
or
```scala
import org.macroid.Concurrency._
class MyActivity extends Activity {
...
}
```

Likewise, to use `~>`, `l`, and `w` inherit from `LayoutDsl` or import from it.

The `f`, however, requires `FragmentDsl` *trait*, which in its turn depends on either `ActivityViewSearch` or `FragmentViewSearch`.
Thus, the usage is
```scala
import org.macroid._
class MyActivity extends Activity with ActivityViewSearch with LayoutDsl with FragmentDsl {
...
}
class MyFragment extends Fragment with FragmentViewSearch with LayoutDsl with FragmentDsl {
...
}
```

`Tweaks` come both in a trait and in an object.

`MediaQueries` come in a trait `MediaQueries`, as well as in
objects `MediaQueries` and `MQ` (the latter being a useful shortcut to allow `MQ.minWidth(...)`).

### Credits

* https://github.com/pocorall/scaloid for inspiration
* @xeno_by for macros in Scala
* see Issues for future ideas and related scientific papers

Nick (stanch), 2013
