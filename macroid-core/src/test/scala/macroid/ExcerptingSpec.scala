package macroid

import android.widget.Button
import org.scalatest.FlatSpec

import macroid.FullDsl._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ExcerptingSpec extends FlatSpec {
  implicit val ctx: ContextWrapper = null

  "Excerpting" should "work with widgets and excerpts" in {
    def foo: Ui[CharSequence] =
      w[Button] ~> getText
  }

  it should "work with futures of excerpts" in {
    def foo: Ui[Future[CharSequence]] =
      w[Button] ~> Future(getText)
  }

  it should "work with options of widgets" in {
    def foo: Ui[Option[CharSequence]] =
      slot[Button] ~> getText
  }

  it should "work with ui of widgets" in {
    def foo: Ui[CharSequence] =
      (w[Button] <~ text("bar")) ~> getText
  }

  it should "support tupled excerpts" in {
    def foo: Ui[(Int, CharSequence)] =
      w[Button] ~> (getId + getText)
  }
}
