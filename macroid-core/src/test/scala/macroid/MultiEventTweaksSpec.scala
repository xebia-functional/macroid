package macroid

import android.app.Activity
import android.view.View
import android.widget.{SeekBar, _}
import macroid.LayoutDsl._
import macroid.Tweaks._
import org.scalatest.FlatSpec

class MultiEventTweaksSpec extends FlatSpec {
  implicit val ctx = ActivityContext(null: Activity)

  "Tweaking with UnitOn" should "work with widgets and tweaks" in {
    def foo = {
      val action = Ui(println("Hmm..."))
      w[Button] <~ UnitOn.click(action) <~ text("Hi") <~ id(2) <~ text("Hey") <~ UnitOn.click(action)
    }
  }

  it should "work with several lines handlers" in {
    def foo = {
      w[TextView] <~ UnitOn.click({
        val base = 1
        val res = base * base
        Ui {
          res
        }
      })
    }
  }

  "Tweaking with UnitFuncOn" should "work with widgets and tweaks" in {
    def foo = {
      val action = (v: View) => Ui(println("Hmm..."))
      w[Button] <~ UnitFuncOn.click(action) <~ text("Hi") <~ id(2) <~ text("Hey") <~ UnitFuncOn.click(action)
    }
  }

  it should "work with more complex handlers" in {
    def foo = {
      w[TextView] <~ UnitFuncOn.click({
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
      w[Button] <~ UnitOn.longClick(action) <~ text("Hi") <~ id(2) <~ text("Hey") <~ UnitOn.longClick(action)
    }
  }

  it should "work with more complex handlers returning true" in {
    def foo = {
      w[TextView] <~ UnitOn.longClick({
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
      w[TextView] <~ UnitOn.longClick({
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
            (onClick: UnitHandler) => action
          ) <~
          MultiOn.click[View](
            (onClick: FuncHandler) => {
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
        (onClick: UnitHandler) => action) <~ MultiOn.longClick[View](
        (onLongClick: UnitHandler) => {
          action ~ Ui(true)
        })
    }
  }

  it should "work with multi-listeners with default implementations" in {
    def foo = {
      w[SeekBar] <~ MultiOn.SeekBarChange[SeekBar](
        (onProgressChanged: FuncHandler) =>
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
        (onStartTrackingTouch: FuncHandler) =>
          (seekBar: SeekBar) => {
            val current = seekBar.getProgress

            Ui(println(s"Current Progress is $current"))
          },
        (onStopTrackingTouch: FuncHandler) =>
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
        (onProgressChanged: FuncHandler) =>
          (view: SeekBar, progress: Int, fromUser: Boolean) => {
            val current = progress + 1

            Ui(println(s"Current Progress is $current"))
          },
        (onStartTrackingTouch: FuncHandler) =>
          (seekBar: SeekBar) => {
            val current = seekBar.getProgress

            Ui(println(s"Current Progress is $current"))
          },
        (onStopTrackingTouch: FuncHandler) =>
          (seekBar: SeekBar) => {
            val current = seekBar.getProgress

            Ui(println(s"Current Progress is $current"))
          }
      )
    }
  }
}