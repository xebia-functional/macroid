package macroid

import android.widget.{ TextView, Button }
import org.scalatest.FlatSpec

import macroid.FullDsl._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class SnailingSpec extends FlatSpec {
  implicit val ctx: ContextWrapper = null

  "Snailing" should "work with widgets and snails" in {
    def foo: Ui[Future[Button]] = {
      w[Button] <~~ delay(400)
    }
  }

  it should "work with futures of tweaks" in {
    def foo: Ui[Future[Button]] = {
      w[Button] <~~ Future(text("bar"))
    }
  }

  it should "work with traversables and options of widgets" in {
    def foo = {
      Option(List(w[Button], w[TextView])) <~~ delay(500) <~~ delay(600)
    }
  }

  it should "work with traversables and options of snails" in {
    def foo: Ui[Future[TextView]] = {
      w[Button] <~~ Option(List(delay(500), delay(600)))
    }
  }
}
