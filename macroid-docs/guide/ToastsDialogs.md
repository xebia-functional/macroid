# Toasts and dialogs

Being an important part of Android interfaces, these two things are covered in *Macroid* as well.

## Toasts

The toast API is similar to [tweaking](Tweaks.html#tweaking) and consists of `toast` constructors and `loafs`:

* `toast(text: CharSequence)` produces a toast with text
* `toast(view: Ui[View])` produces a toast with some layout

The loafs are as follows:

```scala
// `fry` shows the toast
toast("Foo") <~ fry

// `long` makes the toast long
toast("Foooo") <~ long <~ fry

// `gravity` should be self-evident
toast("XY") <~ gravity(Gravity.CENTER, xOffset = 3 dp)
```

Note that all the lines above return [UI actions](UiAction.html).

## Dialogs

The dialog API is also similar to [tweaking](Tweaks.html#tweaking) and consists of `dialog` constructors and `phrases`:

* `dialog(message: CharSequence)`
* `dialog(view: Ui[View])`
* `dialog(adapter: ListAdapter)`
* `dialog(items: Array[CharSequence])(handler: OnClickListener)`

It is also possible to provide a dialog theme:

```scala
dialog(themeId)("I’m a message")
```

The phrases can be discovered via the [scaladoc](../api/macroid/Phrases$.html), here are some examples:

```scala
// `speak` shows the dialog
dialog("Please fasten your seat belts") <~ title("Warning") <~ speak
```

Finally, two implicit conversions to `Dialog.OnClickListener` are awailable:

* `Ui[Any]` sets the specified UI Action as the listener
* `(DialogInterface, Int) ⇒ Ui[Any]` is the same, but with arguments

Example:

```scala
dialog("Are you sure") <~
  positiveYes(textView <~ show) <~
  negativeNo(textView <~ hide) <~
  speak
```