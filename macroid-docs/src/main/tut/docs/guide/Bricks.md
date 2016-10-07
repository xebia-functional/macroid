---
layout: docs
title: Bricks
section: guide
---

# Bricks

*Macroid* bricks offer a concise and composable way of creating GUI layouts.

## What are they

* `layout[LayoutClass](child1, ...)` (aliased as `l` for short) represents a layout, such as `LinearLayout`, `FrameLayout`, `ScrollView`, etc (in fact, any `ViewGroup`);
* `widget[WidgetClass]` (aliased as `w` for short) represents a widget, such as `Button`, `TextView`, `ImageView`, etc (in fact, any `View`).

## Usage

Import:

```scala
import macroid.FullDsl._
// or just
// import macroid.LayoutBuilding._
```

Stack together:

```scala
l[LinearLayout](
  w[Button],
  w[TextView]
)
```

It is possible to provide arguments to widget constructors (omitting `Context`) like this:

```scala
// same as new ProgressBar(ctx, null, android.R.attr.progressBarStyleLarge)
w[ProgressBar](null, android.R.attr.progressBarStyleLarge)
```

Layouts however can only take a number of widgets (or other layouts) as their arguments. To
create an empty layout, use one of these options:

```scala
l[LinearLayout]() // mind the empty brackets
// or
w[LinearLayout] // yep, layouts are widgets too
```

You can add children to an empty layout later by doing

```scala
layout <~ addViews(button, textView, ...)
```

Finally, note that bricks return a [UI action](UiActions.html), therefore
if you need to use them e.g. in `setContentView`,
`Ui.get` is required. *This also means that `w[Widget]` will
return a new (different) instance of `Widget` each time you call `Ui.get(w[Widget])` or `w[Widget].get`*.

```scala
override def onCreate(savedInstanceState: Bundle) = {
  super.onCreate(savedInstanceState)

  setContentView {
    Ui.get {
      l[LinearLayout](
        w[Button],
        w[TextView]
      )
    }
  }
}
```






