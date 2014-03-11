package org.macroid

import org.scalatest.FlatSpec
import android.widget.{ LinearLayout, TextView, Button }
import android.app.Activity

class TweakingSpec extends FlatSpec with LayoutDsl with Tweaks {
  implicit val ctx = ActivityContext(null: Activity)

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

  it should "use provided widget type" in {
    def foo = {
      w[Button] ~> Tweak[Button] { _.setText("test") }
      Option(w[Button]) ~> Tweak[Button] { _.setText("test") }
    }
  }

  it should "allow setting method handlers" in {
    def foo = {
      w[Button] ~> On.click(println("duh"))
      w[Button] ~> On.editorAction[Button] { println("duhduh"); true }
    }
  }

  it should "use provided layout type" in {
    def foo = {
      l[LinearLayout](
        w[TextView] ~> lp[LinearLayout](0, 0, 1.0f)
      )
      val z = lp[LinearLayout](0, 0, 1.0f)
    }
  }
}
