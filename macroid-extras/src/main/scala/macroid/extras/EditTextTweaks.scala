package macroid.extras

import android.content.Context
import android.text.{ Editable, TextWatcher }
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import macroid.{ ContextWrapper, Tweak }

object EditTextTweaks {
  type W = EditText

  def etAddTextChangedListener(onChanged: (String, Int, Int, Int) ⇒ Unit) = Tweak[W] { view ⇒
    view.addTextChangedListener(new TextWatcher {
      override def beforeTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int): Unit = {}

      override def onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int): Unit =
        onChanged(charSequence.toString, start, before, count)

      override def afterTextChanged(editable: Editable): Unit = {}
    })
  }

  def etHideKeyboard(implicit contextWrapper: ContextWrapper) = Tweak[W] { editText ⇒
    Option(contextWrapper.application.getSystemService(Context.INPUT_METHOD_SERVICE)) foreach {
      case imm: InputMethodManager ⇒ imm.hideSoftInputFromWindow(editText.getWindowToken, 0)
      case _ ⇒
    }
  }

  def etShowKeyboard(implicit contextWrapper: ContextWrapper) = Tweak[W] { editText ⇒
    editText.requestFocus()
    Option(contextWrapper.bestAvailable.getSystemService(Context.INPUT_METHOD_SERVICE)) foreach {
      case imm: InputMethodManager ⇒ imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
      case _ ⇒
    }
  }

}
