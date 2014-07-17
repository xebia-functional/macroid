# Tweaks

The idea behind tweaks is that widget styles and behaviors should be as composable as widget layouts themselves.

## What are they

A tweak is something that changes a property of a widget. More precisely,
it is defined by a function `WidgetClass ⇒ Unit`.

## Defining

You can use the `Tweak` companion object to define tweaks:

```scala
import macroid.Tweak

val bringToFront = Tweak[View] { view ⇒
  view.bringToFront()
}

def text(str: String) = Tweak[TextView](_.setText(str))
```

## Composing

Tweaks can be composed simply by using the `+` operator. For example:

```scala
def textAndBringToFront(str: String) =
  text(str) + bringToFront
```

The execution order is the same as the composition order, so in this case
first the caption will be set and then `bringToFront` will be called.

## Tweaking

Tweaking is one of *Macroid*’s most powerful operations ([here’s why](Advanced.html)). We’ve already seen this example:

```scala
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

A number of standard tweaks are included. Please refer to the [scaladoc](../api/macroid/Tweaks$.html).
Note that all these tweaks can be imported with `import macroid.FullDsl._` or just `import macroid.Tweaks._`.

## Extra tweaks

Some extra tweaks are included in the `macroid.contrib` package. Please refer to the [scaladoc](../api/macroid/contrib/package.html).