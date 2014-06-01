package macroid

import scala.language.implicitConversions
import android.app.AlertDialog
import android.view.View
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import macroid.util.Ui
import android.widget.ListAdapter

case class Phrase(f: AlertDialog.Builder ⇒ Unit) {
  def apply(d: AlertDialog.Builder) = f(d)
}

private[macroid] trait DialogBuilding {
  /** A helper class to provide different ways of building a dialog */
  class DialogBuilder[A](theme: Option[Int]) {
    /** Create a dialog with the specified view */
    def apply(view: Ui[View])(implicit ctx: ActivityContext): Ui[AlertDialog.Builder] =
      view.map(v ⇒ new AlertDialog.Builder(ctx.get).setView(v))

    /** Create a dialog with the specified message */
    def apply(message: CharSequence)(implicit ctx: ActivityContext): Ui[AlertDialog.Builder] =
      Ui(new AlertDialog.Builder(ctx.get).setMessage(message))

    /** Create a dialog with the specified item list and click handler */
    def apply(items: Array[CharSequence])(handler: OnClickListener)(implicit ctx: ActivityContext): Ui[AlertDialog.Builder] =
      Ui(new AlertDialog.Builder(ctx.get).setItems(items, handler))

    /** Create a dialog with the specified ListAdapter and click handler */
    def apply(adapter: ListAdapter)(handler: OnClickListener)(implicit ctx: ActivityContext): Ui[AlertDialog.Builder] =
      Ui(new AlertDialog.Builder(ctx.get).setAdapter(adapter, handler))
  }

  /** Create a dialog with the default theme */
  def dialog = new DialogBuilder(None)

  /** Create a dialog with the specified theme */
  def dialog(theme: Int) = new DialogBuilder(Some(theme))
}

object DialogBuilding extends DialogBuilding

private[macroid] trait DialogImplicits {
  implicit def unit2OnClickListener(f: Ui[Any]) = new OnClickListener {
    def onClick(dialog: DialogInterface, which: Int): Unit = f.get
  }

  implicit def func2OnClickListener(f: (DialogInterface, Int) ⇒ Ui[Any]) = new OnClickListener {
    def onClick(dialog: DialogInterface, which: Int): Unit = f(dialog, which).get
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
  implicit class PhrasingOps(dialog: Ui[AlertDialog.Builder]) {
    def <~(phrase: Phrase) = dialog map { d ⇒ phrase(d); d }
  }
}

object Phrasing extends Phrasing
