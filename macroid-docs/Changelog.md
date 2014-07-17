# Changelog

Breaking changes are in bold.

## Version `2.0.0-M3`

Detailed diff can be found [on GitHub](https://github.com/macroid/macroid/compare/v2.0.0-M2...v2.0.0-M3).

* **Macroid is now packaged in AAR**
* **ExtraTweaks were refactored into `TextTweaks`, `ImageTweaks`, etc**
* **`macroid.util.Ui` is now `macroid.Ui` for consistency with `macroid.Tweak`, `macroid.Snail`, etc**
* When applying `Future[Tweak[View]]`, completed futures are now handled in-place (inspired by [scala/async#73](https://github.com/scala/async/issues/73))
* Added `RuleRelativeLayout` to work with `RelativeLayout` [#24](https://github.com/macroid/macroid/issues/24)
* Many new tweaks were added to `macroid.contrib`

## Version `2.0.0-M2`

Detailed diff can be found [on GitHub](https://github.com/macroid/macroid/compare/v2.0.0-M1...v2.0.0-M2).

* **Operator overhaul [#23](https://github.com/macroid/macroid/issues/23)**
* **Remove `showProgress` tweaks**
* **`Layout` extractor moved to `Transformer.Layout`**
* Added `dialog(items)(handler)` constructor [#21](https://github.com/macroid/macroid/issues/21)
* Added `AutoLogTag` trait [#20](https://github.com/macroid/macroid/issues/20)
* Improved `implicitNotFound` annotations