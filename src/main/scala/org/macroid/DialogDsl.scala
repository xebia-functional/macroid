package org.macroid

import scala.language.implicitConversions
import android.app.AlertDialog
import android.view.View
import scala.concurrent.{ ExecutionContext, Future }
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import org.macroid.util.Thunk
import android.widget.ListAdapter

case class Phrase(f: AlertDialog.Builder ⇒ Unit) {
  def apply(d: AlertDialog.Builder) = f(d)
}

private[macroid] trait DialogBuilding {
  class Builder[A](theme: Option[Int], f: AlertDialog.Builder ⇒ A) {
    def apply(view: ⇒ View)(implicit ctx: ActivityContext) =
      f(new AlertDialog.Builder(ctx.get).setView(view))

    def apply(message: CharSequence)(implicit ctx: ActivityContext) =
      f(new AlertDialog.Builder(ctx.get).setMessage(message))

    def apply(adapter: ListAdapter)(handler: OnClickListener)(implicit ctx: ActivityContext) =
      f(new AlertDialog.Builder(ctx.get).setAdapter(adapter, handler))
  }

  def dialog = new Builder(None, code ⇒ UiThreading.runOnUiThread(code))
  def dialog(theme: Int) = new Builder(Some(theme), code ⇒ UiThreading.runOnUiThread(code))

  def createDialog = new Builder(None, identity)
  def createDialog(theme: Int) = new Builder(Some(theme), identity)
}

object DialogBuilding extends DialogBuilding

private[macroid] trait DialogImplicits {
  implicit def lazy2OnClickListener(f: ⇒ Any) = new OnClickListener {
    def onClick(dialog: DialogInterface, which: Int): Unit = f
  }

  implicit def thunk2OnClickListener(f: Thunk[Any]) = new OnClickListener {
    def onClick(dialog: DialogInterface, which: Int): Unit = f()
  }

  implicit def func2OnClickListener(f: (DialogInterface, Int) ⇒ Any) = new OnClickListener {
    def onClick(dialog: DialogInterface, which: Int): Unit = f(dialog, which)
  }
}

private[macroid] trait Phrases {
  /** Set title */
  def title(title: CharSequence) = Phrase(_.setTitle(title))

  /** Set positive button */
  def positive(text: CharSequence)(handler: OnClickListener) = Phrase(_.setPositiveButton(text, handler))

  /** Set positive button with text "yes" */
  def positiveYes(handler: OnClickListener) = Phrase(_.setPositiveButton(android.R.string.yes, handler))

  /** Set positive button with text "Ok" */
  def positiveOk(handler: OnClickListener) = Phrase(_.setPositiveButton(android.R.string.ok, handler))

  /** Set negative button */
  def negative(text: CharSequence)(handler: OnClickListener) = Phrase(_.setNegativeButton(text, handler))

  /** Set negative button with text "no" */
  def negativeNo(handler: OnClickListener) = Phrase(_.setNegativeButton(android.R.string.no, handler))

  /** Set negative button with text "cancel" */
  def negativeCancel(handler: OnClickListener) = Phrase(_.setNegativeButton(android.R.string.cancel, handler))

  /** Set neutral button */
  def neutral(text: CharSequence)(handler: OnClickListener) = Phrase(_.setNeutralButton(text, handler))

  /** Show the dialog */
  def speak = Phrase { d ⇒ d.show(); () }
}

object Phrases extends Phrases

private[macroid] trait Phrasing extends DialogImplicits {
  import org.macroid.UiThreading._

  object create

  implicit class FuturePhrasingOps(dialog: Future[AlertDialog.Builder])(implicit ec: ExecutionContext) {
    def ~>(phrase: Phrase) = dialog mapUi { d ⇒ phrase(d); d }
  }

  implicit class PhrasingOps(dialog: AlertDialog.Builder)(implicit ec: ExecutionContext) {
    def ~>(phrase: Phrase) = { phrase(dialog); dialog }
    def ~>(c: create.type) = dialog.create()
  }
}

object Phrasing extends Phrasing
