/*
 *
 *   Copyright (C) 2015 47 Degrees, LLC http://47deg.com hello@47deg.com
 *
 *   Licensed under the Apache License, Version 2.0 (the "License"); you may
 *   not use this file except in compliance with the License. You may obtain
 *   a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

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
}
