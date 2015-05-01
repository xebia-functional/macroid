# Tweaking widgets

In *Macroid* tweaking is done with something called... [Tweaks](../guide/Tweaks.html)!
A tweak changes some property of a widget, for example:

```scala
textView <~ text("Hello!")
```

Here `text` is one of the standard tweaks which are included when you import `macroid.FullDsl._`
or available separately in `macroid.Tweaks._`.
Using these things, we can improve our layout as follows:

```scala
l[LinearLayout](
  // set button caption
  w[Button] <~ text("Click me"),

  // set text and hide for the time being
  w[TextView] <~ text("Hello!") <~ hide

  // set layout orientation
) <~ vertical
```

Of course, we can do much fancier than that! Remember the composability principle? Tweaks are composable as well:

```scala
// ContextWrapper is a safer Context wrapper
import macroid.ContextWrapper
// More tweaks
import macroid.contrib.TextTweaks

// A module with custom tweaks
object OurTweaks {
  def greeting(greeting: String)(implicit ctx: ContextWrapper) =
    TextTweaks.large +
    text(greeting) +
    hide
}
```

Now, we can rewrite our layout more nicely:

```scala
l[LinearLayout](
  w[Button] <~ text("Click me"),
  w[TextView] <~ OurTweaks.greeting("Hello!")
) <~ vertical
```

But something is still missing! We need to handle button clicks.
