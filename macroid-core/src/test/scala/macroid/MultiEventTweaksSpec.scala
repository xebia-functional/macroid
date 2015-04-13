package macroid

import android.widget.SeekBar
import macroid.MultiEventTweakMacros._
import org.scalatest.{WordSpec, FlatSpec}
import android.widget._
import android.view.View
import android.app.Activity
import LayoutDsl._
import Tweaks._
import contrib._

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

  "Tweaking with BoolOn" should "work with widgets and tweaks" in {
    def foo = {
      val action = Ui(println("Hmm..."))
      w[Button] <~ BoolOn.longClick(action) <~ text("Hi") <~ id(2) <~ text("Hey") <~ BoolOn.LongClick(action)
    }
  }

  it should "work with more complex handlers returning true" in {
    def foo = {
      w[TextView] <~ BoolOn.LongClick({
        val base = 1
        val res = base * base
        Ui {
          res
        }
        true
      })
    }
  }

  it should "work with more complex handlers returning false" in {
    def foo = {
      w[TextView] <~ BoolOn.LongClick({
        val base = 1
        val res = base * base
        Ui {
          res
        }
        false
      })
    }
  }

  it should "work with more complex handlers returning boolean UI at the end" in {
    def foo = {
      w[TextView] <~ BoolOn.LongClick({
        val base = 1
        val res = base * base
        val resultUi = Ui {
          res
        } ~ Ui(true)

        resultUi.get
      })
    }
  }

  "Tweaking with MultiOn" should "work widgets and tweaks for single event listeners" in {
    def foo = {
      val action = Ui(println("Hmm..."))
      w[Button] <~ MultiOn.click[View](
        (onClick: UnitEvent) => action) <~ text("Hi") <~ id(2) <~ text("Hey") <~ MultiOn.click[View](
        (onClick: UnitFuncEvent) => {
          (view: View) => action
        })
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
        (onClick: UnitEvent) => action) <~ MultiOn.LongClick[View](
        (onLongClick: BoolEvent) => {
          val tempVal = action ~ Ui(true)
          tempVal.get
        })
    }
  }

  it should "work with multi-listeners view widget like SeekBar with default implementations " +
      "for onStartTrackingTouch and onStopTrackingTouch" in {
    def foo = {
      w[SeekBar] <~ MultiOn.SeekBarChange[SeekBar](
        (onProgressChanged: UnitFuncEvent) =>
          (view: SeekBar, progress: Int, fromUser: Boolean) => {
            val current = progress + 1

            Ui(println(s"Current Progress is $current"))
          }
      )
    }
  }

  it should "work with multi-listeners view widget like SeekBar with default implementation " +
      "for onProgressChanged" in {
    def foo = {
      w[SeekBar] <~ MultiOn.SeekBarChange[SeekBar](
        (onStartTrackingTouch: UnitFuncEvent) =>
          (seekBar: SeekBar) => {
            val current = seekBar.getProgress

            Ui(println(s"Current Progress is $current"))
          },
        (onStopTrackingTouch: UnitFuncEvent) =>
          (seekBar: SeekBar) => {
            val current = seekBar.getProgress

            Ui(println(s"Current Progress is $current"))
          }
      )
    }
  }

  it should "work with multi-listeners view widget like SeekBar with the three listener overrides" in {
    def foo = {
      w[SeekBar] <~ MultiOn.SeekBarChange[SeekBar](
        (onProgressChanged: UnitFuncEvent) =>
          (view: SeekBar, progress: Int, fromUser: Boolean) => {
            val current = progress + 1

            Ui(println(s"Current Progress is $current"))
          },
        (onStartTrackingTouch: UnitFuncEvent) =>
          (seekBar: SeekBar) => {
            val current = seekBar.getProgress

            Ui(println(s"Current Progress is $current"))
          },
        (onStopTrackingTouch: UnitFuncEvent) =>
          (seekBar: SeekBar) => {
            val current = seekBar.getProgress

            Ui(println(s"Current Progress is $current"))
          }
      )
    }
  }
}