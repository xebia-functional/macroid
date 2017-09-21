---
layout: docs
title: Handling events
section: tutorial
---

# Handling events

Setting up event listeners is done with a dedicated tweak:

```scala
button <~ On.click {
  ...
}
```

Before we setup our button to show the greeting, we need to [wire](../guide/Searching.html#slots-and-wires) the
greeting textbox to a variable:

```scala
// create a slot
var greeting = slot[TextView]
...
// in the layout
w[TextView] <~ wire(greeting) <~ OurTweaks.greeting("Hello!")
```

Now we can complete our layout:

```scala
l[LinearLayout](
  w[Button] <~
    text("Click me") <~
    On.click {
      greeting <~ show
    },
  w[TextView] <~
    wire(greeting) <~
    OurTweaks.greeting("Hello!")
) <~ vertical
```

But what if we want to adapt our layout to the screen orientation? Read on.