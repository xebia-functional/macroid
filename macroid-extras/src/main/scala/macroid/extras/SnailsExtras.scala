package macroid.extras

import android.animation.{Animator, AnimatorListenerAdapter}
import android.view.View._
import android.view.{View, ViewAnimationUtils}
import macroid.Snail

import scala.concurrent.Promise
import scala.util.Success

object RevealSnails {

  val showCircularReveal: Snail[View] = Snail[View] { view ⇒
    val animPromise = Promise[Unit]()
    val x = view.getWidth / 2
    val y = view.getHeight / 2
    val radius =
      SnailsUtils.calculateRadius(x, y, view.getWidth, view.getHeight)
    val anim: Animator =
      ViewAnimationUtils.createCircularReveal(view, x, y, 0f, radius)
    anim.addListener(new AnimatorListenerAdapter {
      override def onAnimationEnd(animation: Animator) = {
        super.onAnimationEnd(animation)
        animPromise.complete(Success(()))
      }
    })
    view.setVisibility(VISIBLE)
    anim.start()
    animPromise.future
  }

  val hideCircularReveal: Snail[View] = Snail[View] { view ⇒
    val animPromise = Promise[Unit]()
    val x = view.getWidth / 2
    val y = view.getHeight / 2
    val radius =
      SnailsUtils.calculateRadius(x, y, view.getWidth, view.getHeight)
    val anim: Animator =
      ViewAnimationUtils.createCircularReveal(view, x, y, radius, 0f)
    anim.addListener(new AnimatorListenerAdapter {
      override def onAnimationEnd(animation: Animator) = {
        super.onAnimationEnd(animation)
        view.setVisibility(INVISIBLE)
        animPromise.complete(Success(()))
      }
    })
    anim.start()
    animPromise.future
  }

}

object MoveSnails {

  def move(maybeToView: Option[View]): Snail[View] = Snail[View] { view ⇒
    val animPromise = Promise[Unit]()

    maybeToView foreach { toView ⇒
      val finalX
        : Float = toView.getX + (toView.getWidth / 2) - ((view.getWidth / 2) + view.getX)
      val finalY
        : Float = toView.getY + (toView.getHeight / 2) - ((view.getHeight / 2) + view.getY)

      view.animate
        .translationX(finalX)
        .translationY(finalY)
        .setListener(new AnimatorListenerAdapter {
          override def onAnimationEnd(animation: Animator) = {
            super.onAnimationEnd(animation)
            animPromise.complete(Success(()))
          }
        })
        .start()

    }
    animPromise.future
  }

  def moveBy(maybeToView: Option[View]): Snail[View] = Snail[View] { view ⇒
    val animPromise = Promise[Unit]()

    maybeToView foreach { toView ⇒
      val finalX
        : Float = toView.getX + (toView.getWidth / 2) - ((view.getWidth / 2) + view.getX)
      val finalY
        : Float = toView.getY + (toView.getHeight / 2) - ((view.getHeight / 2) + view.getY)

      view.animate
        .translationXBy(finalX)
        .translationYBy(finalY)
        .setListener(new AnimatorListenerAdapter {
          override def onAnimationEnd(animation: Animator) = {
            super.onAnimationEnd(animation)
            animPromise.complete(Success(()))
          }
        })
        .start()

    }
    animPromise.future
  }

}

object SnailsUtils {

  def calculateRadius(x: Int = 0,
                      y: Int = 0,
                      width: Int = 0,
                      height: Int = 0): Float = {
    val catheti1: Int = if (x < width / 2) width - x else x
    val catheti2: Int = if (y < height / 2) height - y else y
    Math.sqrt((catheti1 * catheti1) + (catheti2 * catheti2)).toFloat
  }

}
