package macroid

import org.scalatest.FlatSpec
import android.widget.{ LinearLayout, TextView, Button }
import LayoutDsl._
import Tweaks._
import contrib._

class TweakingSpec extends FlatSpec {
  implicit val ctx: ContextWrapper = null

  "Tweaking" should "work with widgets and tweaks" in {
    def foo = {
      val action = Ui(println("Hmm..."))
      w[Button] <~ On.click(action) <~ text("Hi") <~ id(2) <~ text("Hey") <~ On.click(action)
    }
  }

  it should "work with effectors on the left" in {
    def foo = {
      Option(List(w[Button], w[TextView])) <~ show
    }
  }

  it should "work with effectors on the right" in {
    def foo = {
      w[Button] <~ Option(List(show, text("doge")))
    }
  }

  it should "work with effectors on both sides" in {
    def foo = {
      Option(List(w[Button], w[TextView])) <~ Option(List(show, text("doge")))
    }
  }

  it should "use provided widget type" in {
    def foo = {
      w[Button] <~ Tweak[Button] { _.setText("test") }
      Option(w[Button]) <~ Tweak[Button] { _.setText("test") }
    }
  }

  it should "allow setting method handlers" in {
    def foo = {
      w[Button] <~ On.click(Ui(println("duh")))
      w[Button] <~ On.editorAction[Button] { for (_ ← Ui(println("duhduh"))) yield true }
    }
  }

  it should "use provided layout type" in {
    def foo = {
      l[LinearLayout](
        w[TextView] <~ lp[LinearLayout](0, 0, 1.0f)
      )
      val z = lp[LinearLayout](0, 0, 1.0f)
    }
  }

  "TextTweaks.allCaps" should "be invokeable without parentheses" in {
    def foo = {
      w[TextView] <~ TextTweaks.allCaps
    }
  }

  it should "allow an optional value" in {
    def foo = {
      w[TextView] <~ TextTweaks.allCaps(false)
    }
  }
}
