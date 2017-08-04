---
layout: docs
title: Fragments
section: guide
---

# Fragments

To quote the Android documentation,

> A Fragment represents a behavior or a portion of user interface in an Activity. You can combine multiple fragments in a single activity to build a multi-pane UI and reuse a fragment in multiple activities.

Macroid allows you to deal with fragments, both `android.app.Fragment` and `android.support.v4.app.Fragment`.

## Contexts

For the below features to work, you need to include the right [contexts](Contexts.html):

```scala
import macroid.Contexts

class MyActivity extends Activity with Contexts[Activity] {
  ...
}
```

If you use fragments from the support library:

```scala
class MyActivity extends FragmentActivity with Contexts[FragmentActivity] {
  ...
}
```

Finally, in a fragment (either `android.app.Fragment` or `android.support.v4.app.Fragment`):

```scala
class MyFragment extends Fragment with Contexts[Fragment] {
  ...
}
```

## The `f` word

Macroid provides an `fragment` (alised as `f` for short) brick, which is somewhat similar to the [`l` and `w` bricks](Bricks.html):

```scala
f[MapFragment]
```

The fragment is created with its primary constructor, or with a `newInstance` method in its companion
(or static method in Java fragments), if it exists. Like with `w` bricks, you can provide arguments
to the `newInstance` method:

```scala
// same as FragmentClass.newInstance(arg1, arg2, ...)
f[FragmentClass](arg1, arg2, ...)
```

You can also put arguments into a `Bundle` and pass via the `setArguments` method:

```scala
// same as
// val b = new Bundle
// b.putXXX("arg1", arg1)
// b.putXXX("arg2", arg2)
// (new FragmentClass).setArguments(b)
f[FragmentClass].pass("arg1" → arg1, "arg2" → arg2, ...)
```

The `factory` method allows to obtain a fragment factory (`Ui[FragmentClass]`), which is
very useful for creating fragment pagers:

```scala
f[FragmentClass].pass(...).factory
```

Finally, here is a way to insert the fragment directly into the layout (it will be assigned a tag
and wrapped into a `FrameLayout`). Note that for this to work, you need to [create an id generator](Searching.html#id-and-tag-generation)
into your activity or parent fragment.

```scala
f[FragmentClass].pass(...).framed(Id.someId, Tag.someTag)
```

A more elaborate example:

```scala
l[LinearLayout](
  w[Button],
  w[TextView],
  f[MapFragment].framed(Id.map, Tag.map)
)
```