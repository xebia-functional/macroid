# Transformers

Transformers utilize Scala pattern matching to alter nested layouts in a flexible way.

## What are they

A transformer is defined by a partial function `PartialFunction[View, Ui[Any]]`. It traverses
the layout recursively and runs the respective [UI action](UiActions.html) for every widget
it encounters, if the function is defined for that widget.

## Defining

You can use the `Transformer` companion object to define Transformers:

```scala
import macroid.Transformer

val imagesOnly = Transformer {
  case i: ImageView ⇒ i <~ show
  case x: View ⇒ x <~ hide
}
```

There is also a useful `Layout` extractor:

```scala
import macroid.Transformer.Layout

val foobar = Transformer {
  case Layout(x: TextView, y: TextView) ⇒ Ui.sequence(
    x <~ text("foo"),
    y <~ text("bar")
  )
}
```

## Transforming

Transformers can be applied with the tweaking operator (`<~`):

```scala
linearLayout <~ Transformer {
  case t: TextView ⇒ t <~ text("foo")
  case i: ImageView ⇒ i <~ hide
}
```