package macroid.extras

import android.animation._
import android.annotation._
import android.content.res._
import android.graphics.PorterDuff._
import android.graphics.drawable._
import android.graphics._
import android.support.design.widget._
import android.support.v4.view._
import android.support.v7.widget._
import android.support.v7.widget.PopupMenu._
import android.view.View._
import android.view.ViewGroup.LayoutParams._
import android.view.ViewTreeObserver._
import android.view.animation._
import android.view._
import android.widget.AdapterView._
import android.widget.{AdapterView, ArrayAdapter}
import macroid.FullDsl._
import macroid.{ContextWrapper, Tweak, Ui}
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

  def vResize(size: Int): Tweak[View] = vResize(size, size)

  def vResize(width: Int, height: Int): Tweak[W] = Tweak[W] { view ⇒
    val params = view.getLayoutParams
    params.height = width
    params.width = height
    view.requestLayout()
  }

  def vMargins(margin: Int): Tweak[W] = Tweak[W] { view ⇒
    view.getLayoutParams
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
    view.getLayoutParams
      .asInstanceOf[ViewGroup.MarginLayoutParams]
      .setMargins(marginLeft, marginTop, marginRight, marginBottom)
    view.requestLayout()
  }

  def vPaddings(padding: Int): Tweak[W] =
    Tweak[W](_.setPadding(padding, padding, padding, padding))

  def vPaddings(
      paddingLeftRight: Int = 0,
      paddingTopBottom: Int = 0
  ): Tweak[W] =
    Tweak[W](
      _.setPadding(paddingLeftRight,
                   paddingTopBottom,
                   paddingLeftRight,
                   paddingTopBottom))

  def vPadding(
      paddingLeft: Int = 0,
      paddingTop: Int = 0,
      paddingRight: Int = 0,
      paddingBottom: Int = 0
  ): Tweak[W] =
    Tweak[W](_.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom))

  def vDisableHapticFeedback: Tweak[W] =
    Tweak[W](_.setHapticFeedbackEnabled(false))

  def vActivated(activated: Boolean): Tweak[W] =
    Tweak[W](_.setActivated(activated))

  def vBackground(res: Int): Tweak[W] = Tweak[W](_.setBackgroundResource(res))

  def vBackgroundColor(color: Int): Tweak[W] =
    Tweak[W](_.setBackgroundColor(color))

  @TargetApi(23)
  def vBackgroundColorResources(color: Int)(
      implicit context: ContextWrapper): Tweak[W] =
    Tweak[W](
      _.setBackgroundColor(
        context.application.getResources.getColor(color, null)))

  @deprecated(message = "", since = "API 23")
  def vBackgroundColorResource(color: Int)(
      implicit context: ContextWrapper): Tweak[W] =
    Tweak[W](
      _.setBackgroundColor(context.application.getResources.getColor(color)))

  def vBackground(drawable: Drawable): Tweak[W] =
    Tweak[W](view ⇒ view.setBackground(drawable))

  val vBlankBackground = Tweak[W](view ⇒ view.setBackground(null))

  def vTag[T](tag: T) = Tweak[W](_.setTag(tag))

  def vTag[T](id: Int, tag: T) = Tweak[W](_.setTag(id, tag))

  def vTransformation(x: Int = 0, y: Int = 0): Tweak[W] = Tweak[W] { view ⇒
    view.setTranslationX(x.toFloat)
    view.setTranslationY(y.toFloat)
  }

  val vGone: Tweak[W] = Tweak[W](_.setVisibility(View.GONE))

  val vVisible: Tweak[W] = Tweak[W](_.setVisibility(View.VISIBLE))

  val vInvisible: Tweak[W] = Tweak[W](_.setVisibility(View.INVISIBLE))

  def vScrollBarStyle(style: Int): Tweak[W] =
    Tweak[W](_.setScrollBarStyle(style))

  def vAlpha(alpha: Float): Tweak[W] = Tweak[W](_.setAlpha(alpha))

  def vX(x: Float): Tweak[W] = Tweak[W](_.setX(x))

  def vY(y: Float): Tweak[W] = Tweak[W](_.setY(y))

  def vPivotX(x: Float): Tweak[W] = Tweak[W](_.setPivotX(x))

  def vPivotY(y: Float): Tweak[W] = Tweak[W](_.setPivotY(y))

  def vScaleX(x: Float): Tweak[W] = Tweak[W](_.setScaleX(x))

  def vScaleY(y: Float): Tweak[W] = Tweak[W](_.setScaleY(y))

  def vTranslationX(x: Float): Tweak[W] = Tweak[W](_.setTranslationX(x.toFloat))

  def vTranslationY(y: Float): Tweak[W] = Tweak[W](_.setTranslationY(y.toFloat))

  def vTranslationZ(z: Float): Tweak[W] = Tweak[W](_.setTranslationZ(z.toFloat))

  def vRotation(rotation: Float) = Tweak[W](_.setRotation(rotation))

  def vSelectableItemBackground(implicit contextWrapper: ContextWrapper) =
    Tweak[W] { view ⇒
      val typedArray = contextWrapper.bestAvailable.obtainStyledAttributes(
        Seq(android.R.attr.selectableItemBackground).toArray)
      view.setBackgroundResource(typedArray.getResourceId(0, 0))
      typedArray.recycle()
    }

  def vFocusable(focusable: Boolean) = Tweak[W](_.setFocusable(focusable))

  def vBackgroundColorFilterResource(res: Int, mode: Mode = Mode.MULTIPLY)(
      implicit context: ContextWrapper): Tweak[W] =
    Tweak[W](
      _.getBackground.setColorFilter(
        new PorterDuffColorFilter(resGetColor(res), mode)))

  def vBackgroundColorFilter(color: Int, mode: Mode = Mode.MULTIPLY): Tweak[W] =
    Tweak[W](
      _.getBackground.setColorFilter(new PorterDuffColorFilter(color, mode)))

  def vBackgroundTransition(durationMillis: Int,
                            reverse: Boolean = false): Tweak[W] = Tweak[W] {
    view ⇒
      val transitionBackground =
        view.getBackground.asInstanceOf[TransitionDrawable]
      if (reverse) transitionBackground.reverseTransition(durationMillis)
      else transitionBackground.startTransition(durationMillis)
  }

  def vCircleOutlineProvider(padding: Int = 0): Tweak[W] = Tweak[W] { view ⇒
    view.setOutlineProvider(new ViewOutlineProvider() {
      override def getOutline(view: ViewTweaks.W, outline: Outline): Unit = {
        outline.setOval(padding,
                        padding,
                        view.getWidth - padding,
                        view.getHeight - padding)
      }
    })
    view.setClipToOutline(true)
  }

  def vOutlineProvider(viewOutlineProvider: ViewOutlineProvider): Tweak[W] =
    Tweak[W] { view ⇒
      view.setOutlineProvider(viewOutlineProvider)
      view.setClipToOutline(true)
    }

  def vFitsSystemWindows(fits: Boolean): Tweak[W] = Tweak[W] { view ⇒
    view.setFitsSystemWindows(fits)
  }

  def vElevation(elevation: Float): Tweak[W] =
    Tweak[W](_.setElevation(elevation))

  val vBringToFront: Tweak[W] = Tweak[W](_.bringToFront())

  val vClearAnimation: Tweak[W] = Tweak[W](_.clearAnimation())

  def vAnimation(animation: Animation): Tweak[W] =
    Tweak[W](_.setAnimation(animation))

  def vStartAnimation(animation: Animation): Tweak[W] =
    Tweak[W](_.startAnimation(animation))

  def vStateListAnimator(animation: Int)(
      implicit context: ContextWrapper): Tweak[W] =
    Tweak[W](
      _.setStateListAnimator(
        AnimatorInflater.loadStateListAnimator(context.application, animation)))

  def vLayerType(layerType: Int, paint: Paint = null): Tweak[W] =
    Tweak[W](_.setLayerType(layerType, paint))

  def vLayerTypeHardware(paint: Paint = null): Tweak[W] =
    Tweak[W](_.setLayerType(View.LAYER_TYPE_HARDWARE, paint))

  def vLayerTypeSoftware(paint: Paint = null): Tweak[W] =
    Tweak[W](_.setLayerType(View.LAYER_TYPE_SOFTWARE, paint))

  def vLayerTypeNone(paint: Paint = null): Tweak[W] =
    Tweak[W](_.setLayerType(View.LAYER_TYPE_NONE, paint))

  def vGlobalLayoutListener(globalLayoutListener: View ⇒ Ui[_]): Tweak[W] =
    Tweak[W] { view ⇒
      view.getViewTreeObserver.addOnGlobalLayoutListener(
        new OnGlobalLayoutListener() {
          override def onGlobalLayout(): Unit = {
            view.getViewTreeObserver.removeOnGlobalLayoutListener(this)
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

  def vClipBackground(radius: Int,
                      verticalPadding: Int = 0,
                      horizontalPadding: Int = 0): Tweak[W] = Tweak[W] { view ⇒
    view.setOutlineProvider(new ViewOutlineProvider() {
      override def getOutline(view: View, outline: Outline): Unit =
        outline.setRoundRect(
          horizontalPadding,
          verticalPadding,
          view.getWidth - horizontalPadding,
          view.getHeight - verticalPadding,
          radius.toFloat
        )
    })
    view.setClipToOutline(true)
  }

  def vClearFocus = Tweak[W](_.clearFocus())

  def vRequestFocus = Tweak[W](_.requestFocus())

  def vEnabled(enabled: Boolean) = Tweak[W](_.setEnabled(enabled))

  def vBackgroundTint(color: Int) = Tweak[W] {
    case t: TintableBackgroundView ⇒
      t.setSupportBackgroundTintList(ColorStateList.valueOf(color))
    case _ ⇒
  }

  def vSelected(selected: Boolean) = Tweak[W](_.setSelected(selected))

  def vClickable(clickable: Boolean): Tweak[W] =
    Tweak[W](_.setClickable(clickable))

  def vClearClick: Tweak[W] = Tweak[W](_.setOnClickListener(null))

  def vInvalidate: Tweak[W] = Tweak[W](_.invalidate())

  def vPopupMenuShow(menu: Int, onMenuItemClickListener: (MenuItem) ⇒ Boolean)(
      implicit contextWrapper: ContextWrapper) =
    Tweak[W] { view ⇒
      val popupMenu = new PopupMenu(contextWrapper.bestAvailable, view)
      popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener {
        override def onMenuItemClick(item: MenuItem): Boolean =
          onMenuItemClickListener(item)
      })
      popupMenu.inflate(menu)
      popupMenu.show()
    }

  def vPopupMenuShow(menu: Seq[String],
                     onMenuItemClickListener: (MenuItem) ⇒ Boolean)(
      implicit contextWrapper: ContextWrapper) =
    Tweak[W] { view ⇒
      val popupMenu = new PopupMenu(contextWrapper.bestAvailable, view)
      popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener {
        override def onMenuItemClick(item: MenuItem): Boolean =
          onMenuItemClickListener(item)
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
      listPopupWindow.setAdapter(
        new ArrayAdapter(contextWrapper.bestAvailable, layout, menu.toArray))
      listPopupWindow.setAnchorView(view)
      width foreach listPopupWindow.setWidth
      height foreach listPopupWindow.setHeight
      listPopupWindow.setModal(true)
      listPopupWindow.setOnItemClickListener(new OnItemClickListener {
        override def onItemClick(parent: AdapterView[_],
                                 view: View,
                                 position: Int,
                                 id: Long): Unit = {
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

  def vSnackbarIndefiniteAction(res: Int, buttonText: Int, f: () ⇒ Unit) =
    Tweak[W] { view ⇒
      Ui(
        Snackbar
          .make(view, res, Snackbar.LENGTH_INDEFINITE)
          .setAction(buttonText, new OnClickListener {
            override def onClick(v: View): Unit = f()
          })
          .show()).run
    }

  def vSnackbarLongAction(res: Int, buttonText: Int, f: () ⇒ Unit) = Tweak[W] {
    view ⇒
      Ui(
        Snackbar
          .make(view, res, Snackbar.LENGTH_LONG)
          .setAction(buttonText, new OnClickListener {
            override def onClick(v: View): Unit = f()
          })
          .show()).run
  }

  def vSnackbarShortAction(res: Int, buttonText: Int, f: () ⇒ Unit) = Tweak[W] {
    view ⇒
      Ui(
        Snackbar
          .make(view, res, Snackbar.LENGTH_SHORT)
          .setAction(buttonText, new OnClickListener {
            override def onClick(v: View): Unit = f()
          })
          .show()).run
  }

}

object ViewCompatTweaks {
  type W = View

  def vcElevation(elevation: Float): Tweak[W] =
    Tweak[W](ViewCompat.setElevation(_, elevation))

}
