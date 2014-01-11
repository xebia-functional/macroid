package org.macroid

import org.scalatest.FlatSpec
import android.widget.{ LinearLayout, TextView, Button }
import android.view.{ KeyEvent, View }
import android.widget.TextView.OnEditorActionListener

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

  it should "infer widget type" in {
    def foo = {
      Option(w[Button]) ~> (tweak doing (_.setText("test")))
    }
  }

  it should "allow setting method handlers" in {
    def foo = {
      w[Button] ~> On.click(println("duh"))
      w[Button] ~> On.editorAction { println("duhduh"); true }
    }
  }

  it should "infer parent layout type" in {
    def foo = {
      l[LinearLayout](
        w[TextView] ~> lp(0, 0, 1.0f)
      )
    }
  }
}
