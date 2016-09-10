/*
 * Copyright (C) 2015 47 Degrees, LLC http://47deg.com hello@47deg.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package macroid.extras

import android.animation.AnimatorInflater
import android.content.res.ColorStateList
import android.graphics.PorterDuff.Mode
import android.graphics.drawable.{ Drawable, TransitionDrawable }
import android.graphics.{ Outline, Paint, PorterDuffColorFilter }
import android.support.design.widget.Snackbar
import android.support.v4.view.{ TintableBackgroundView, ViewCompat }
import android.support.v7.widget.{ ListPopupWindow, PopupMenu }
import android.support.v7.widget.PopupMenu.OnMenuItemClickListener
import android.view.View.OnClickListener
import android.view.ViewGroup.LayoutParams._
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.view.animation.Animation
import android.view.{ MenuItem, View, ViewGroup, ViewOutlineProvider }
import android.widget.AdapterView.OnItemClickListener
import android.widget.{ AdapterView, ArrayAdapter }
import macroid.FullDsl._
import macroid.extras.DeviceVersion.{ IceCreamSandwich, JellyBean }
import macroid.{ ContextWrapper, Tweak, Ui }
import macroid.extras.ResourcesExtras._

object ViewTweaks {
  type W = View

  val vMatchParent: Tweak[W] = lp[ViewGroup](MATCH_PARENT, MATCH_PARENT)

  val vWrapContent: Tweak[W] = lp[ViewGroup](WRAP_CONTENT, WRAP_CONTENT)

  val vMatchWidth: Tweak[W] = lp[ViewGroup](MATCH_PARENT, WRAP_CONTENT)

  val vMatchHeight: Tweak[W] = lp[ViewGroup](WRAP_CONTENT, MATCH_PARENT)

  def vContentSizeMatchWidth(h: Int): Tweak[W] = lp[ViewGroup](MATCH_PARENT, h)

  def vContentSizeMatchHeight(w: Int): Tweak[W] = lp[ViewGroup](w, MATCH_PARENT)

  def vMinHeight(height: Int): Tweak[W] = Tweak[W](_.setMinimumHeight(height))

  def vMinWidth(width: Int): Tweak[W] = Tweak[W](_.setMinimumWidth(width))

  def vMargins(margin: Int): Tweak[W] = Tweak[W] { view ⇒
    view
      .getLayoutParams
      .asInstanceOf[ViewGroup.MarginLayoutParams]
      .setMargins(margin, margin, margin, margin)
    view.requestLayout()
  }

  def vMargin(
    marginLeft: Int = 0,
    marginTop: Int = 0,
    marginRight: Int = 0,
    marginBottom: Int = 0
  ): Tweak[W] = Tweak[W] { view ⇒
    view
      .getLayoutParams
      .asInstanceOf[ViewGroup.MarginLayoutParams]
      .setMargins(marginLeft, marginTop, marginRight, marginBottom)
    view.requestLayout()
  }

  def vPaddings(padding: Int): Tweak[W] = Tweak[W](_.setPadding(padding, padding, padding, padding))

  def vPaddings(
    paddingLeftRight: Int = 0,
    paddingTopBottom: Int = 0
  ): Tweak[W] = Tweak[W](_.setPadding(paddingLeftRight, paddingTopBottom, paddingLeftRight, paddingTopBottom))

  def vPadding(
    paddingLeft: Int = 0,
    paddingTop: Int = 0,
    paddingRight: Int = 0,
    paddingBottom: Int = 0
  ): Tweak[W] = Tweak[W](_.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom))

  def vActivated(activated: Boolean): Tweak[W] = Tweak[W](_.setActivated(activated))

  def vBackground(res: Int): Tweak[W] = Tweak[W](_.setBackgroundResource(res))

  def vBackgroundColor(color: Int): Tweak[W] = Tweak[W](_.setBackgroundColor(color))

  def vBackgroundColorResource(color: Int)(implicit context: ContextWrapper): Tweak[W] =
    Tweak[W](_.setBackgroundColor(context.application.getResources.getColor(color)))

  def vBackground(drawable: Drawable): Tweak[W] = Tweak[W](view ⇒
    JellyBean ifSupportedThen view.setBackground(drawable) getOrElse view.setBackgroundDrawable(drawable))

  val vBlankBackground = Tweak[W](view ⇒
    JellyBean ifSupportedThen view.setBackground(null) getOrElse view.setBackgroundDrawable(null))

  def vTag[T](tag: T) = Tweak[W](_.setTag(tag))

  def vTag[T](id: Int, tag: T) = Tweak[W](_.setTag(id, tag))

  def vTransformation(x: Int = 0, y: Int = 0): Tweak[W] = Tweak[W] { view ⇒
    view.setTranslationX(x)
    view.setTranslationY(y)
  }

  val vGone: Tweak[W] = Tweak[W](_.setVisibility(View.GONE))

  val vVisible: Tweak[W] = Tweak[W](_.setVisibility(View.VISIBLE))

  val vInvisible: Tweak[W] = Tweak[W](_.setVisibility(View.INVISIBLE))

  def vScrollBarStyle(style: Int): Tweak[W] = Tweak[W](_.setScrollBarStyle(style))

  def vAlpha(alpha: Float): Tweak[W] = Tweak[W](_.setAlpha(alpha))

  def vX(x: Float): Tweak[W] = Tweak[W](_.setX(x))

  def vY(y: Float): Tweak[W] = Tweak[W](_.setY(y))

  def vPivotX(x: Float): Tweak[W] = Tweak[W](_.setPivotX(x))

  def vPivotY(y: Float): Tweak[W] = Tweak[W](_.setPivotY(y))

  def vScaleX(x: Float): Tweak[W] = Tweak[W](_.setScaleX(x))

  def vScaleY(y: Float): Tweak[W] = Tweak[W](_.setScaleY(y))

  def vTranslationX(x: Float): Tweak[W] = Tweak[W](_.setTranslationX(x))

  def vTranslationY(y: Float): Tweak[W] = Tweak[W](_.setTranslationY(y))

  def vTranslationZ(z: Float): Tweak[W] = Tweak[W](_.setTranslationZ(z))

  def vBackgroundColorFilterResource(res: Int, mode: Mode = Mode.MULTIPLY)(implicit context: ContextWrapper): Tweak[W] =
    Tweak[W](_.getBackground.setColorFilter(new PorterDuffColorFilter(resGetColor(res), mode)))

  def vBackgroundColorFilter(color: Int, mode: Mode = Mode.MULTIPLY): Tweak[W] =
    Tweak[W](_.getBackground.setColorFilter(new PorterDuffColorFilter(color, mode)))

  def vBackgroundTransition(durationMillis: Int, reverse: Boolean = false): Tweak[W] = Tweak[W] { view ⇒
    val transitionBackground = view.getBackground.asInstanceOf[TransitionDrawable]
    if (reverse) transitionBackground.reverseTransition(durationMillis) else transitionBackground.startTransition(durationMillis)
  }

  def vCircleOutlineProvider(padding: Int = 0): Tweak[W] = Tweak[W] { view ⇒
    view.setOutlineProvider(new ViewOutlineProvider() {
      override def getOutline(view: ViewTweaks.W, outline: Outline): Unit = {
        outline.setOval(padding, padding, view.getWidth - padding, view.getHeight - padding)
      }
    })
    view.setClipToOutline(true)
  }

  def vOutlineProvider(viewOutlineProvider: ViewOutlineProvider): Tweak[W] = Tweak[W] { view ⇒
    view.setOutlineProvider(viewOutlineProvider)
    view.setClipToOutline(true)
  }

  def vFitsSystemWindows(fits: Boolean): Tweak[W] = Tweak[W] { view ⇒
    IceCreamSandwich ifSupportedThen view.setFitsSystemWindows(fits) getOrElse Tweak.blank
  }

  def vElevation(elevation: Float): Tweak[W] = Tweak[W](_.setElevation(elevation))

  val vBringToFront: Tweak[W] = Tweak[W](_.bringToFront())

  val vClearAnimation: Tweak[W] = Tweak[W](_.clearAnimation())

  def vAnimation(animation: Animation): Tweak[W] = Tweak[W](_.setAnimation(animation))

  def vStartAnimation(animation: Animation): Tweak[W] = Tweak[W](_.startAnimation(animation))

  def vStateListAnimator(animation: Int)(implicit context: ContextWrapper): Tweak[W] =
    Tweak[W](_.setStateListAnimator(AnimatorInflater.loadStateListAnimator(context.application, animation)))

  def vLayerType(layerType: Int, paint: Paint = null): Tweak[W] = Tweak[W](_.setLayerType(layerType, paint))

  def vLayerTypeHardware(paint: Paint = null): Tweak[W] = Tweak[W](_.setLayerType(View.LAYER_TYPE_HARDWARE, paint))

  def vLayerTypeSoftware(paint: Paint = null): Tweak[W] = Tweak[W](_.setLayerType(View.LAYER_TYPE_SOFTWARE, paint))

  def vLayerTypeNone(paint: Paint = null): Tweak[W] = Tweak[W](_.setLayerType(View.LAYER_TYPE_NONE, paint))

  def vGlobalLayoutListener(globalLayoutListener: View ⇒ Ui[_]): Tweak[W] = Tweak[W] { view ⇒
    view.getViewTreeObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
      override def onGlobalLayout(): Unit = {
        JellyBean ifSupportedThen
          view.getViewTreeObserver.removeOnGlobalLayoutListener(this) getOrElse
          view.getViewTreeObserver.removeGlobalOnLayoutListener(this)
        globalLayoutListener(view).run
      }
    })
  }

  def vOverScrollMode(mode: Int): Tweak[W] = Tweak[W](_.setOverScrollMode(mode))

  def vScrollBy(x: Int, y: Int) = Tweak[W](_.scrollBy(x, y))

  def vScrollTo(x: Int, y: Int) = Tweak[W](_.scrollTo(x, y))

  val vComputeScroll = Tweak[W](_.computeScroll())

  def vScrollX(x: Int) = Tweak[W](_.setScrollX(x))

  def vScrollY(y: Int) = Tweak[W](_.setScrollY(y))

  def vClipBackground(radius: Int, verticalPadding: Int = 0, horizontalPadding: Int = 0): Tweak[W] = Tweak[W] { view ⇒
    view.setOutlineProvider(new ViewOutlineProvider() {
      override def getOutline(view: View, outline: Outline): Unit =
        outline.setRoundRect(
          horizontalPadding,
          verticalPadding,
          view.getWidth - horizontalPadding,
          view.getHeight - verticalPadding,
          radius
        )
    })
    view.setClipToOutline(true)
  }

  def vClearFocus = Tweak[W](_.clearFocus())

  def vRequestFocus = Tweak[W](_.requestFocus())

  def vEnabled(enabled: Boolean) = Tweak[W](_.setEnabled(enabled))

  def vBackgroundTint(color: Int) = Tweak[W] {
    case t: TintableBackgroundView ⇒ t.setSupportBackgroundTintList(ColorStateList.valueOf(color))
    case _ ⇒
  }

  def vSelected(selected: Boolean) = Tweak[W](_.setSelected(selected))

  def vClickable(clickable: Boolean): Tweak[W] = Tweak[W](_.setClickable(clickable))

  def vClearClick: Tweak[W] = Tweak[W](_.setOnClickListener(null))

  def vInvalidate: Tweak[W] = Tweak[W](_.invalidate())

  def vPopupMenuShow(menu: Int, onMenuItemClickListener: (MenuItem) ⇒ Boolean)(implicit contextWrapper: ContextWrapper) =
    Tweak[W] { view ⇒
      val popupMenu = new PopupMenu(contextWrapper.bestAvailable, view)
      popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener {
        override def onMenuItemClick(item: MenuItem): Boolean = onMenuItemClickListener(item)
      })
      popupMenu.inflate(menu)
      popupMenu.show()
    }

  def vPopupMenuShow(menu: Seq[String], onMenuItemClickListener: (MenuItem) ⇒ Boolean)(implicit contextWrapper: ContextWrapper) =
    Tweak[W] { view ⇒
      val popupMenu = new PopupMenu(contextWrapper.bestAvailable, view)
      popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener {
        override def onMenuItemClick(item: MenuItem): Boolean = onMenuItemClickListener(item)
      })
      menu.zipWithIndex foreach {
        case (item, order) ⇒ popupMenu.getMenu.add(0, 0, order, item)
      }
      popupMenu.show()
    }

  def vListPopupWindowShow(
    layout: Int,
    menu: Seq[String],
    onItemClickListener: (Int) ⇒ Unit,
    width: Option[Int] = None,
    height: Option[Int] = None
  )(implicit contextWrapper: ContextWrapper) =
    Tweak[W] { view ⇒
      val listPopupWindow = new ListPopupWindow(contextWrapper.bestAvailable)
      listPopupWindow.setAdapter(new ArrayAdapter(contextWrapper.bestAvailable, layout, menu.toArray))
      listPopupWindow.setAnchorView(view)
      width foreach listPopupWindow.setWidth
      height foreach listPopupWindow.setHeight
      listPopupWindow.setModal(true)
      listPopupWindow.setOnItemClickListener(new OnItemClickListener {
        override def onItemClick(parent: AdapterView[_], view: View, position: Int, id: Long): Unit = {
          onItemClickListener(position)
          listPopupWindow.dismiss()
        }
      })
      listPopupWindow.show()
    }

  def vSnackbarShort(res: Int) = Tweak[W] { view ⇒
    Ui(Snackbar.make(view, res, Snackbar.LENGTH_SHORT).show()).run
  }

  def vSnackbarLong(res: Int) = Tweak[W] { view ⇒
    Ui(Snackbar.make(view, res, Snackbar.LENGTH_LONG).show()).run
  }

  def vSnackbarIndefinite(res: Int) = Tweak[W] { view ⇒
    Ui(Snackbar.make(view, res, Snackbar.LENGTH_INDEFINITE).show()).run
  }

  def vSnackbarShort(message: String) = Tweak[W] { view ⇒
    Ui(Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()).run
  }

  def vSnackbarLong(message: String) = Tweak[W] { view ⇒
    Ui(Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()).run
  }

  def vSnackbarIndefinite(message: String) = Tweak[W] { view ⇒
    Ui(Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE).show()).run
  }

  def vSnackbarIndefiniteAction(res: Int, buttonText: Int, f: () ⇒ Unit) = Tweak[W] { view ⇒
    Ui(Snackbar.make(view, res, Snackbar.LENGTH_INDEFINITE).setAction(buttonText, new OnClickListener {
      override def onClick(v: View): Unit = f()
    }).show()).run
  }

}

object ViewCompatTweaks {
  type W = View

  def vcElevation(elevation: Float): Tweak[W] = Tweak[W](ViewCompat.setElevation(_, elevation))

}