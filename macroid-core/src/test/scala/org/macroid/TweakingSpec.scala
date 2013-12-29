package org.macroid

import org.scalatest.FlatSpec
import android.widget.{ LinearLayout, TextView, Button }

class TweakingSpec extends FlatSpec with LayoutBuilding with Tweaks {
  implicit val ctx = ActivityContext(null)

  "Tweaking" should "work with widgets and tweaks" in {
    def foo = {
      w[Button] ~> On.click(println("Hm...")) ~> text("Hi") ~> id(2) ~> text("Hey") ~> On.click(println("Hurray"))
    }
  }

  it should "work with effectors on the left" in {
    def foo = {
      Option(List(w[Button], w[TextView])) ~> show
    }
  }

  it should "work with effectors on the right" in {
    def foo = {
      w[Button] ~> Option(List(show, text("doge")))
    }
  }

  it should "work with effectors on both sides" in {
    def foo = {
      Option(List(w[Button], w[TextView])) ~> Option(List(show, text("doge")))
    }
  }
}
