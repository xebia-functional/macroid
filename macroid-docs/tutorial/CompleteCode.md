# Complete code

This pretty much wraps the tutorial up. Below you will find the complete code for our activity;
in the mean time you can proceed to the [detailed guide](../Guide.html), which discusses each separate
concept and feature more deeply. In particular, sections on [UI actions](../guide/UiActions.html) and
[Contexts](../guide/Contexts.html) are a must-read, since these topics were not touched in this tutorial.

```scala
import macroid._
import macroid.contrib._
import macroid.FullDsl._

object OurTweaks {
  def greeting(greeting: String)(implicit ctx: ContextWrapper) =
    TextTweaks.large +
    text(greeting) +
    hide

  def orient(implicit ctx: ContextWrapper) =
    landscape ? horizontal | vertical
}

class GreetingActivity extends Activity with Contexts[Activity] {
  var greeting = slot[TextView]

  override def onCreate(savedInstanceState: Bundle) = {
    super.onCreate(savedInstanceState)

    setContentView {
      Ui.get {
        l[LinearLayout](
          w[Button] <~
            text("Click me") <~
            On.click {
              greeting <~ show
            },
          w[TextView] <~
            wire(greeting) <~
            OurTweaks.greeting("Hello!")
        ) <~ OurTweaks.orient
      }
    }
  }
}
```