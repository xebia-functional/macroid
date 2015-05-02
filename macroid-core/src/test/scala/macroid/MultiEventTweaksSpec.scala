package macroid

import android.view.View
import android.widget.{SeekBar, _}
import macroid.LayoutDsl._
import macroid.Tweaks._
import org.scalatest.FlatSpec

class MultiEventTweaksSpec extends FlatSpec {
  implicit val ctx: ContextWrapper = null

  "Tweaking with On" should "work with widgets and tweaks" in {
    def foo = {
      val action = Ui(println("Hmm..."))
      w[Button] <~ On.click(action) <~ text("Hi") <~ id(2) <~ text("Hey") <~ On.click(action)
    }
  }

  it should "work with several lines handlers" in {
    def foo = {
      w[TextView] <~ On.click({
        val base = 1
        val res = base * base
        Ui {
          res
        }
      })
    }
  }

  "Tweaking with FuncOn" should "work with widgets and tweaks" in {
    def foo = {
      val action = (v: View) => Ui(println("Hmm..."))
      w[Button] <~ FuncOn.click(action) <~ text("Hi") <~ id(2) <~ text("Hey") <~ FuncOn.click(action)
    }
  }

  it should "work with more complex handlers" in {
    def foo = {
      w[TextView] <~ FuncOn.click({
        (v: View) =>
          val base = 1
          val res = base * base
          Ui {
            res
          }
      })
    }
  }

  "Tweaking with Handlers which return a Boolean" should "work fine as well" in {
    def foo = {
      val action = Ui(true)
      w[Button] <~ On.longClick(action) <~ text("Hi") <~ id(2) <~ text("Hey") <~ On.longClick(action)
    }
  }

  it should "work with more complex handlers returning true" in {
    def foo = {
      w[TextView] <~ On.longClick({
        val base = 1
        val res = base * base
        Ui {
          res
        } ~ Ui(true)
      })
    }
  }

  it should "work with more complex handlers returning false" in {
    def foo = {
      w[TextView] <~ On.longClick({
        val base = 1
        val res = base * base
        Ui {
          res
        } ~ Ui(false)
      })
    }
  }

  "Tweaking with MultiOn" should "work widgets and tweaks for single event listeners" in {
    def foo = {
      val action = Ui(println("Hmm..."))
      w[Button] <~ text("Hi") <~ id(2) <~ text("Hey") <~
          MultiOn.click[View](
            (onClick: UnitListener) => action
          ) <~
          MultiOn.click[View](
            (onClick: FuncListener) => {
              (view: View) => action
            }
          )
    }
  }

  it should "work with more complex handlers" in {
    def foo = {
      val action = {
        val base = 1
        val res = base * base
        Ui {
          res
        }
      }
      w[TextView] <~ MultiOn.click[View](
        (onClick: UnitListener) => action) <~ MultiOn.longClick[View](
        (onLongClick: UnitListener) => {
          action ~ Ui(true)
        })
    }
  }

  it should "work with multi-listeners with default implementations" in {
    def foo = {
      w[SeekBar] <~ MultiOn.SeekBarChange[SeekBar](
        (onProgressChanged: FuncListener) =>
          (view: SeekBar, progress: Int, fromUser: Boolean) => {
            val current = progress + 1

            Ui(println(s"Current Progress is $current"))
          }
      )
    }
  }

  it should "work with multi-listeners with default implementations. Another example." in {
    def foo = {
      w[SeekBar] <~ MultiOn.SeekBarChange[SeekBar](
        (onStartTrackingTouch: FuncListener) =>
          (seekBar: SeekBar) => {
            val current = seekBar.getProgress

            Ui(println(s"Current Progress is $current"))
          },
        (onStopTrackingTouch: FuncListener) =>
          (seekBar: SeekBar) => {
            val current = seekBar.getProgress

            Ui(println(s"Current Progress is $current"))
          }
      )
    }
  }

  it should "work with multi-listeners without default implementations" in {
    def foo = {
      w[SeekBar] <~ MultiOn.SeekBarChange[SeekBar](
        (onProgressChanged: FuncListener) =>
          (view: SeekBar, progress: Int, fromUser: Boolean) => {
            val current = progress + 1

            Ui(println(s"Current Progress is $current"))
          },
        (onStartTrackingTouch: FuncListener) =>
          (seekBar: SeekBar) => {
            val current = seekBar.getProgress

            Ui(println(s"Current Progress is $current"))
          },
        (onStopTrackingTouch: FuncListener) =>
          (seekBar: SeekBar) => {
            val current = seekBar.getProgress

            Ui(println(s"Current Progress is $current"))
          }
      )
    }
  }
}