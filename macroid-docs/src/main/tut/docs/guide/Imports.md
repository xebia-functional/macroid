---
layout: docs
title: Imports
section: guide
---

# Imports

Normally most *Macroid* features are imported via

```scala
import macroid._
import macroid.FullDsl._
```

However you can also import them one by one (click list items to jump to docs):

* `macroid` package object includes
  * [`macroid.UiFuture` implicit conversion](UiActions.html)
  * [`macroid.Tweaking` operators](Tweaks.html#tweaking) (standard tweaks should be imported separately from `macroid.Tweaks`)
  * [`macroid.Snailing` operators](Snails.html#-snailing-) (standard snails should be imported separately from `macroid.Snails`)
* `macroid.LayoutDsl` includes
  * [`macroid.Searching`](Searching.html)
  * [`macroid.LayoutBuilding`](Bricks.html)
  * [`macroid.FragmentBuilding`](Fragments.html)
* [`macroid.DialogDsl`](ToastsDialogs.html#dialogs) (standard phrases should be imported separately from `macroid.Phrases`)
* [`macroid.ToastDsl`](ToastsDialogs.html#toasts) (standard loafs should be imported separately from `macroid.Loafs`)
* `macroid.FullDsl` includes
  * `macroid.LayoutDsl`, `macroid.Tweaks`, `macroid.Snails`
  * `macroid.ToastDsl`, `macroid.Loafs`
  * `macroid.DialogDsl`, `macroid.Phrases`
  * [`macroid.MediaQueries`](MediaQueries.html)

There are also some things ouside these modules:

* [`macroid.Ui`](UiActions.html), [`macroid.Tweak`](Tweaks.html),
  [`macroid.Snail`](Snails.html), [`macroid.Transformer`](Transformers.html)
* [`macroid.{ ContextWrapper, Contexts }`](Contexts.html)