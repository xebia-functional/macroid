---
layout: docs
title: Media queries
section: guide
---

# Media queries

Media queries are inspired by the CSS feature of the same name. They provide a way to design adaptive layouts.

## What are they

Media queries are in fact not much more than `Boolean`s with a few convenient operators:

```scala
import macroid.FullDsl._

widerThan(100 dp) ? text("foobar") | text("bar")
```

The key aspect here is that the `?` operator returns an `Option`, which
is defined only if the query condition holds:
`widerThan(100 dp) ? text("foobar")` will contain either `Some(text("foobar"))` or
`None`. We then use the `|` operator to provide an alternative.

## Units

Media quries come with display units:

* `3 px` is 3 pixels
* `3 dp` is 3 device points
* `3 sp` is 3 scalable points

Their usage however is not limited to media queries:

```scala
import macroid.FullDsl._

// set top padding
button <~ padding(top = 3 dp)
```

## Usage

As you may have figured out, media queries can be used to define adaptive tweaks:

```scala
def orient(implicit ctx: ContextWrapper) =
  landscape ? horizontal | vertical
```

It is not necessary to provide an alternative, since tweaking will handle an `Option[Tweak[...]]`
[just fine](Advanced.html).

```scala
onlySeenInHD <~ (hdpi ? show)
```

On the other hand, several queries can be chained:

```scala
def adaptiveCaption(implicit ctx: ContextWrapper) =
  widerThan(200 dp) ? text("fooobaaar") |
  widerThan(100 dp) ? text("foobar") |
  text("foo")
```

## Standard media queries

A number of standard media queries are included. Please refer to the [scaladoc](../api/core/macroid/MediaQueries$.html).
Note that all these media queries can be imported with `import macroid.FullDsl._`.