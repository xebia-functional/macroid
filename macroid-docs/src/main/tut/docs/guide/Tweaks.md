---
layout: docs
title: Tweaks
section: guide
---

# Tweaks

The idea behind tweaks is that widget styles and behaviors should be as composable as widget layouts themselves.

## What are they

A tweak is something that changes a property of a widget. More precisely,
it is defined by a function `WidgetClass ⇒ Unit`.

## Defining

You can use the `Tweak` companion object to define tweaks:

```tut:book:silent
import android.view.View
import android.widget.TextView
import macroid._
import macroid.FullDsl._

val bringToFront = Tweak[View] { view ⇒
  view.bringToFront()
}

def text(str: String) = Tweak[TextView](_.setText(str))
```

## Composing

Tweaks can be composed simply by using the `+` operator. For example:

```tut:book:silent
def textAndBringToFront(str: String) =
  text(str) + bringToFront
```

The execution order is the same as the composition order, so in this case
first the caption will be set and then `bringToFront` will be called.

## Tweaking

Tweaking is one of *Macroid*’s most powerful operations ([here’s why](Advanced.html)). We’ve already seen this example:

```scala
val textView = slot[TextView]
textView <~ text("Hi")
```

In fact, we can chain a few tweaks in one operation:

```scala
textView <~ text("Hi") <~ show
```

As you may already know, tweaking returns a [UI action](UiActions.html):

```scala
val textView2: Ui[TextView] =
  textView <~ text("Hi") <~ show
```

Note that tweaking does not require the widgets to be created with *Macroid* bricks.

## Standard tweaks

A number of standard tweaks are included. You can always find them in the [source in github](https://github.com/47deg/macroid/blob/master/macroid-core/src/main/scala/macroid/Tweaks.scala).
Note that all these tweaks can be imported with `import macroid.FullDsl._` or just `import macroid.Tweaks._`.

A quick rundown:

* Basic tweaks
  * `id` — set the id
  * `wire` — wire to a slot (see [Searching](Searching.html#slots-and-wires))
* Visibility
  * `hide`
  * `show`
* Enabling/disabling
  * `enable`
  * `disable`
* Padding
  * `padding` — supports named arguments. Example:

    ```scala
    w[TextView] <~ padding(top = 8 dp, bottom = 8 dp)
    ```
* Layout
  * `layoutParams` / `lp` — works with any layout. Example:

    ```scala
    w[Button] <~ lp[LinearLayout](MATCH_PARENT, WRAP_CONTENT, 1.0f)
    ```
  * `vertical`
  * `horizontal`
  * `addViews` — add views to a ViewGroup. Example:

    ```scala
    val views = List("foo", "bar").map(s ⇒ w[TextView] <~ text(s))
    linearLayout <~ addViews(views, removeOld = true)
    ```
* Text/captions
  * `text`
  * `hint`
* Handling events
  * `On` — works with (almost) any events. Expects a [UI action](UiActions.html) that takes no arguments and returns the same type as the associated listener. Examples:

    ```scala
    w[Button] <~ On.click(textView <~ hide)

    w[Button] <~ On.longClick((textView <~ show) ~ Ui(true))

    l[SwipeRefreshLayout](
      ...
    ) <~ On.refresh[SwipeRefreshLayout](Ui(...))
    ```
  * `FuncOn` — the same as `On`, but takes arguments, as specified by the listener. Example:

    ```scala
    w[StaggeredGridView] <~
      FuncOn.itemClick[StaggeredGridView] { (_: AdapterView[_], _: View, index: Int, _: Long) ⇒
        ...
      }
    ```

## Extra tweaks

Some extra tweaks are included in the `macroid.contrib` package. You can always find them in the [source in github](https://github.com/47deg/macroid/blob/master/macroid-core/src/main/scala/macroid/contrib/ExtraTweaks.scala).

*Please feel free to file a pull request with any extra tweaks you find useful. The naming strategy can be observed below.*

A quick rundown:

* `LpTweaks`
  * `matchParent` — same as `lp[ViewGroup](MATCH_PARENT, MATCH_PARENT)`
  * `wrapContent` — same as `lp[ViewGroup](WRAP_CONTENT, WRAP_CONTENT)`
  * `matchWidth` — same as `lp[ViewGroup](MATCH_PARENT, WRAP_CONTENT)`
  * `matchHeight` — same as `lp[ViewGroup](WRAP_CONTENT, MATCH_PARENT)`
* `BgTweaks`
  * `res` — sets a background from resources
  * `color` — sets background to a color
* `TextTweaks` (for `TextView`)
  * `color`
  * `bold`, `italic`, `boldItalic`
  * `serif`, `sans`, `mono`, `typeface`
  * `medium`, `large`, `size`
* `ImageTweaks` (for `ImageView`)
  * `res` — shows a drawable from resources
  * `bitmap`
  * `adjustBounds` — sets `adjustViewBounds` to `true`
* `ListTweaks` (for `ListView`)
  * `noDivider` — hides the divider
  * `adapter` — sets list adapter (works with any `AbsListView`)
* `PagerTweaks` (for `ViewPager`)
  * `page` — sets current page
  * `adapter` — sets pager adapter
* `SeekTweaks` (for `SeekBar`)
  * `seek` — sets the cue
