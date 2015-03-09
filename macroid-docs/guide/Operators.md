# Understanding operators

*Macroid* operators can appear puzzling at first: what’s the difference between `<~` and `<~~`,
`~` and `~~`, `+` and `++`?
However, there is a very simple logic behind them, which is explained in this section.

*Macroid* supports two different ways of changing widget properties:
* “fire and forget” — the property change is triggered and the world moves on
* “apply and wait” — the property change is triggered, and the world waits until it finishes

For example, when triggering an animation you can choose whether to proceed immediately or not.

This distinction is directly mirrored in the operators: `·` means “fire and forget”, `··` means “apply and wait”:

```scala
// fire the label change
textView <~ text("foo")

// fire the transformer
widget <~ transformer

// fire something, that will backfire in the future
// and continue immediately
textView <~ futureCaption.map(text) <~ show

// fire the snail, without waiting for it to finish
textView <~ fadeIn(300) <~ text("bar")

// add two tweaks
tweak1 + tweak2

// sequence two UI actions
(widget1 <~ tweak1) ~ (widget2 <~ tweak2)


// wait for the future tweak
textView <~ hide <~~ futureCaption.map(text) <~ show

// when applying snail1 or snail2, wait until they finish
widget <~ tweak1 <~~ snail1 <~~ snail2 <~ tweak2

// combine tweak1 with snail1, waiting for snail1 to finish (gives a snail)
tweak1 ++ snail1

// combine two snails (gives a snail)
snail1 ++ snail2

// combine snail with a tweak (no need to wait for tweak2)
snail2 + tweak2

// sequence two UI actions, waiting for the first one to finish
(widget1 <~~ snail1) ~~ (widget2 <~~ snail2)
```