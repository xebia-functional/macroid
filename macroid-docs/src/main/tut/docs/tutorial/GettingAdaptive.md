---
layout: docs
title: Getting adaptive
section: tutorial
---

# Getting adaptive

As Android devices vary greatly in sizes and shapes, we often want to adapt out layouts accordingly.
Say, we want to make our `LinearLayout` horizontal when the screen is in the landscape orientation.
In *Macroid* this can be achieved with so-called [media queries](../guide/MediaQueries.html):

```scala
layout <~ (landscape ? horizontal | vertical)
```

Here `landscape` is one of the standard media queries available in `macroid.FullDsl` or `macroid.MediaQueries`.
Examples of other media queries are `hdpi`, `ldpi`, `widerThan(200 dp)`, `maxHeight(300 dp)` and so on.

We are going to incorporate this into our layout more elegantly by defining a helper tweak:

```scala
object OurTweaks {
  ...
  // the new helper
  def orient(implicit ctx: ContextWrapper) =
    landscape ? horizontal | vertical
}
...
// in layout
l[LinearLayout](
  ...
) <~ OurTweaks.orient
```

As you can see, this approach is much more flexible than Android’s multiple resource folders,
because we are able to define custom adaptive pieces and compose them together.