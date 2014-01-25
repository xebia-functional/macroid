package org.macroid.viewable

import android.app.Activity

import org.macroid.FullDsl._
import org.macroid.viewable.Viewing._
import org.macroid.viewable.Dummifying._
import org.macroid.contrib.Layouts.VerticalLinearLayout
import android.widget.Button
import org.macroid.{ AppContext, ActivityContext }

trait LayoutTestingActivity extends Activity {
  def testLayout[A](implicit ctx: ActivityContext, appCtx: AppContext, viewable: Viewable[A], dummifiable: Dummifiable[A]): Unit = setContentView {
    l[VerticalLinearLayout](
      dummy[A].layout,
      w[Button] ~> text("Reload") ~> On.click(testLayout[A])
    )
  }
}
