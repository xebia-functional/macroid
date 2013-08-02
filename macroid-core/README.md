# *Macroid* — a Scala layout DSL for Android

### Requirements

* Scala ```2.10+```
* Android ```API 11+``` (uses ```Fragment```s from the support library and the stock ```ActionBar```)

### What’s the buzz

*Macroid* is intended to complement the excellent [scaloid](https://github.com/pocorall/scaloid) project. It’s by no means
a replacement, but rather a collection of some useful things under a slightly different sauce :)

Let’s see what we have.

#### The DSL

```scala
var bar: ProgressBar = _
val view = l[LinearLayout](
  w[TextView] ~> text("Loading...") ~> { x ⇒
    // extra initialization
  },
  w[ProgressBar](null, android.R.attr.progressBarStyleLarge) ~>
    id(Id.progress) ~>
    center() ~>
    wire(bar),
  f[MyAwesomeFragment](Id.stuff, Tag.stuff, Map("number" → 4))
)
```

The three main components are:
* ```l[...]``` — a macro to create layouts. Supports arbitrary ```ViewGroup```s
* ```w[...]``` — a macro to create widgets. Supports arbitrary ```View```s, even with parameters (as in ```ProgressBar``` example). The only requirement is that ```Context``` parameter is the first one in ```View```’s constructor.
* ```f[...]``` — a macro to create fragments. It creates the fragment if not already created, wraps in a ```FrameLayout``` and returns it. The map is converted to a ```Bundle``` and passed to ```setArguments```.

Notice these little things:
* ```Id.foo``` automatically generates and id for you. The id will be the same upon consequent calls. **Note that ids are not checked.** They are generated starting from 1000. You can override the default id generator.
* ```Tag.bar``` is a fancy way of saying ```"bar"```, added for symmetry.

Configuration of views is done with ```~>```. The idea is to provide as many convenient and common transforms as possible.
It is easy to make one yourself:
```scala
def id[A <: View](id: Int): ViewMutator[A] = x ⇒ x.setId(id)
```

#### Noteworthy predefined transforms

* ```wire``` assigns the view to the ```var``` you provide (see example above).
* ```hide```, ```show```.
* ```layoutParams``` or ```lp```:
  Automatically uses ```LayoutParams``` constructor from the parent layout or its superclass.
  ```scala
  import ViewGroup.LayoutParams._ // to get WRAP_CONTENT and MATCH_PARENT
  import org.macroid.Transforms._ // to get layoutParams or lp
  
  l[MyShinyLayout](
      w[Button] ~> layoutParams(WRAP_CONTENT, MATCH_PARENT) ~> text("Click me")
  )
  ```
* You can setup almost any ```View``` event listener with the following syntax:
  ```scala
  import org.scaloid.common._ // to get toast
  import org.macroid.Transforms._ // to get most of the stuff

  l[LinearLayout](
      w[Button] ~>
        text("Click me") ~>
        On.click(toast("Howdy?")) ~> // using a lazy block
        On.longClick { toast("Splash!"); true }, // using a lazy block that needs to return a value
      w[Button] ~>
        text("Don’t click me") ~>
        On.click { v: View ⇒ toast(v.getText) } // using function of the same type as OnClickListener.onClick
  ) ~> vertical
  ```

#### Searching for views and fragments

*Macroid* offers three utils:
* ```def findView[A <: View](id: Int): A```
* ```def findView[A <: View](root: View, id: Int): A```
* ```def findFrag[A <: Fragment](tag: String): A```

They come in two flavors, for ```Activities``` and ```Fragments``` respectively: ```trait ActivityViewSearch``` and ```trait FragmentViewSearch```.

#### Concurrency

*Macroid* provides a bunch of great ways to run things on UI thread.

Using scala ```Future```s:
```scala
import org.macroid.Concurrency._
future {
  ...
} onSuccessUi { case x ⇒
  ...
} onFailureUi { case t ⇒
  ...
}
```

Using akka dataflow:
```scala
import akka.dataflow._
import org.macroid.Concurrency._
flow {
  val a = await(someFuture)
  val b = await(otherFuture)
  switchToUiThread()
  findView[Button](Id.myButton) ~> text(a + b)
} onFailureUi { case t ⇒
  t.printStackTrace()
  findView[TextView](Id.error) ~> text("Oops...")
}
```

The usual ```runOnUiThread``` has been pimped, so that now it returns a ```Future``` as well. Everything you put inside
is wrapped in a ```Try```, so you don’t have to worry about your layout being destroyed.

### Installation

If you plan to use dataflow:
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
  "Macroid snapshots" at "http://stanch.github.com/macroid/snapshots/",
  Resolver.sonatypeRepo("snapshots")
)

libraryDependencies += ("org.macroid" %% "macroid" % "1.0-SNAPSHOT") exclude ("org.scala-lang.macro-paradise", "scala-library")
```
For explanation of the weirdness see https://github.com/scalamacros/sbt-example-paradise210/issues/1#issuecomment-21445002.

In your activity:
```scala
import org.macroid._
class MyActivity extends Activity with ActivityViewSearch with LayoutDsl {
  ...
}
```

In your fragment:
```scala
import org.macroid._
class MyFragment extends Fragment with FragmentViewSearch with LayoutDsl {
  ...
}
```
