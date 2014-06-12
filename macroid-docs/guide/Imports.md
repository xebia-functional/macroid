# Imports

Normally most *Macroid* features are imported via

```scala
import macroid.FullDsl._
```

However you can also import them one by one (click list items to jump to docs):

* `macroid.LayoutDsl` includes
  * [`macroid.Searching`](Searching.html)
  * [`macroid.LayoutBuilding`](Bricks.html)
  * [`macroid.FragmentBuilding`](Fragments.html)
  * [`macroid.Tweaking`](Tweaks.html#tweaking) (standard tweaks should be imported separately from `macroid.Tweaks`)
  * [`macroid.Snailing`](Snails.html#-snailing-) (standard snails should be imported separately from `macroid.Snails`)
  * [`macroid.Transforming`](Transformers.html)
* [`macroid.DialogDsl`](ToastsDialogs.html#dialogs) (standard phrases should be imported separately from `macroid.Phrases`)
* [`macroid.ToastDsl`](ToastsDialogs.html#toasts)
* `macroid.FullDsl` includes
  * [`macroid.UiThreading`](UiActions.html)
  * `macroid.LayoutDsl`, `macroid.Tweaks`, `macroid.Snails`
  * `macroid.ToastDsl`
  * `macroid.DialogDsl`, `macroid.Phrases`
  * [`macroid.MediaQueries`](MediaQueries.html)
  * [`macroid.Logging`](Logging.html)

There are also some things ouside these modules:

* [`macroid.utils.Ui`](UiActions.html)
* [`macroid.{ AppContext, ActivityContext, Contexts }`](Contexts.html)
* [`macroid.IdGeneration`](Searching.html#id-and-tag-generation)
* [`macroid.AutoLogTag`](Logging.html)