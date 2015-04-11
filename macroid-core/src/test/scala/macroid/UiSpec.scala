package macroid

import org.scalatest.FlatSpec
import android.widget.TextView
import LayoutDsl._
import Tweaks._
import Snails._

class UiSpec extends FlatSpec {
  implicit val ctx: ContextWrapper = null

  "UI actions" should "be composable" in {
    def foo = {
      (w[TextView] <~ text("foo")) ~
        (w[TextView] <~ text("bar"))
    }
  }

  they should "be asynchronously composable" in {
    import scala.concurrent.ExecutionContext.Implicits.global
    def foo = {
      (w[TextView] <~~ fadeIn(100)) ~~
        (w[TextView] <~~ fadeOut(100))
    }
  }
}
