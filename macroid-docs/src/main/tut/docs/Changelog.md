---
layout: docs
title: Changelog
section: docs
---

# Changelog

Breaking changes are in bold.

## Version `2.0.0-M4`

Detailed diff can be found [on GitHub](https://github.com/macroid/macroid/compare/v2.0.0-M3...v2.0.0-M4).

### All modules

All *Macroid* modules now reside in the same repository and take advantage of a unified build setup.
Note that commit history has been rewritten to include references to the relevant modules in the commit messages.

### Core

* **`mapUi`, `flatMapUi`, etc now receive UI actions**
* **IDs inside fragments are now hidden from outer search [#31](https://github.com/macroid/macroid/issues/31)**
* **Fixed wrongfully eager evaluation of `RichOption#|`, `Ui#~` and dialog event handlers**
* `FragmentApi` is now contravariant in `A` [#33](https://github.com/macroid/macroid/issues/33)
* Fixed unused dialog `theme` parameter
* Not just a `List`, but any `TraversableOnce` is an `Effector` [#47](https://github.com/macroid/macroid/issues/47) (thanks [Fernando](https://github.com/Governa))
* Generalized typeface tweak [#53](https://github.com/macroid/macroid/issues/53) (thanks [Sean Griffin](https://github.com/sgrif))
* `toast` now supports resource IDs besides plain strings [#51](https://github.com/macroid/macroid/issues/51) (thanks [Sean Griffin](https://github.com/sgrif))
* New tweaks for `TextView#setHint` [#49](https://github.com/macroid/macroid/issues/49) (thanks [Sean Griffin](https://github.com/sgrif))
* Added aliases for `l`, `w` and `f` [#32](https://github.com/macroid/macroid/issues/32)
* Added side-based media queries [#29](https://github.com/macroid/macroid/issues/29)
* New contrib tweaks

### Akka

* **Package changed to `macroid.akka`, artifact changed to `macroid-akka`**

### Viewable

* Added `Listable#addFillView`

## Version `2.0.0-M3`

Detailed diff can be found [on GitHub](https://github.com/macroid/macroid/compare/v2.0.0-M2...v2.0.0-M3).

* **Macroid is now packaged in AAR**
* **ExtraTweaks were refactored into `TextTweaks`, `ImageTweaks`, etc**
* **`macroid.util.Ui` is now `macroid.Ui` for consistency with `macroid.Tweak`, `macroid.Snail`, etc**
* When applying `Future[Tweak[View]]`, completed futures are now handled in-place (inspired by [scala/async#73](https://github.com/scala/async/issues/73))
* Added `RuleRelativeLayout` to work with `RelativeLayout` [#24](https://github.com/macroid/macroid/issues/24)
* `TextTweaks.medium` and `TextTweaks.large` no longer affect style/color of the text
* Many new tweaks were added to `macroid.contrib`

## Version `2.0.0-M2`

Detailed diff can be found [on GitHub](https://github.com/macroid/macroid/compare/v2.0.0-M1...v2.0.0-M2).

* **Operator overhaul [#23](https://github.com/macroid/macroid/issues/23)**
* **Remove `showProgress` tweaks**
* **`Layout` extractor moved to `Transformer.Layout`**
* Added `dialog(items)(handler)` constructor [#21](https://github.com/macroid/macroid/issues/21)
* Added `AutoLogTag` trait [#20](https://github.com/macroid/macroid/issues/20)
* Improved `implicitNotFound` annotations