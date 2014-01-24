package org.macroid.viewable

import android.app.Activity
import android.os.Bundle
import org.macroid.viewable.Viewable._
import org.macroid.viewable.Dummifiable._
import org.macroid.Contexts

abstract class LayoutTestingActivity[A: Viewable: Dummifiable] extends Activity with Contexts[Activity] {
  override def onCreate(savedInstanceState: Bundle) = {
    super.onCreate(savedInstanceState)
    setContentView(dummy[A].layout)
  }
}
