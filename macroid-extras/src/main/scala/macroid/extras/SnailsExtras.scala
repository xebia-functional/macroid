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

import android.animation.{ Animator, AnimatorListenerAdapter }
import android.view.View._
import android.view.{ View, ViewAnimationUtils }
import macroid.Snail

import scala.concurrent.Promise
import scala.util.Success

object RevealSnails {

  val showCircularReveal: Snail[View] = Snail[View] {
    view ⇒
      val animPromise = Promise[Unit]()
      val x = view.getWidth / 2
      val y = view.getHeight / 2
      val radius = SnailsUtils.calculateRadius(x, y, view.getWidth, view.getHeight)
      val anim: Animator = ViewAnimationUtils.createCircularReveal(view, x, y, 0, radius)
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

  val hideCircularReveal: Snail[View] = Snail[View] {
    view ⇒
      val animPromise = Promise[Unit]()
      val x = view.getWidth / 2
      val y = view.getHeight / 2
      val radius = SnailsUtils.calculateRadius(x, y, view.getWidth, view.getHeight)
      val anim: Animator = ViewAnimationUtils.createCircularReveal(view, x, y, radius, 0)
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

  def move(maybeToView: Option[View]): Snail[View] = Snail[View] {
    view ⇒
      val animPromise = Promise[Unit]()

      maybeToView foreach {
        toView ⇒
          val finalX: Int = (toView.getX + (toView.getWidth / 2) - ((view.getWidth / 2) + view.getX)).toInt
          val finalY: Int = (toView.getY + (toView.getHeight / 2) - ((view.getHeight / 2) + view.getY)).toInt

          view.animate.translationX(finalX).translationY(finalY).setListener(new AnimatorListenerAdapter {
            override def onAnimationEnd(animation: Animator) = {
              super.onAnimationEnd(animation)
              animPromise.complete(Success(()))
            }
          }).start()

      }
      animPromise.future
  }

  def moveBy(maybeToView: Option[View]): Snail[View] = Snail[View] {
    view ⇒
      val animPromise = Promise[Unit]()

      maybeToView foreach {
        toView ⇒
          val finalX: Int = (toView.getX + (toView.getWidth / 2) - ((view.getWidth / 2) + view.getX)).toInt
          val finalY: Int = (toView.getY + (toView.getHeight / 2) - ((view.getHeight / 2) + view.getY)).toInt

          view.animate.translationXBy(finalX).translationYBy(finalY).setListener(new AnimatorListenerAdapter {
            override def onAnimationEnd(animation: Animator) = {
              super.onAnimationEnd(animation)
              animPromise.complete(Success(()))
            }
          }).start()

      }
      animPromise.future
  }

}

object SnailsUtils {

  def calculateRadius(x: Int = 0, y: Int = 0, width: Int = 0, height: Int = 0): Int = {
    val catheti1: Int = if (x < width / 2) width - x else x
    val catheti2: Int = if (y < height / 2) height - y else y
    Math.sqrt((catheti1 * catheti1) + (catheti2 * catheti2)).asInstanceOf[Int]
  }

}

