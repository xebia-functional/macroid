package org.macroid

import scala.language.experimental.macros
import android.support.v4.app.Fragment
import scala.reflect.macros.{ Context ⇒ MacroContext }
import android.widget.FrameLayout
import android.view.{ ViewGroup, View }
import android.content.Context
import scalaz.{ Functor, Monoid }
import org.macroid.util.Thunk

trait LayoutDsl {
  import LayoutDslMacros._

  /** Define a widget */
  def w[A <: View](implicit ctx: Context) = macro widgetImpl[A]
  /** Define a widget, supplying additional arguments */
  def w[A <: View](args: Any*)(implicit ctx: Context) = macro widgetArgImpl[A]

  /** Define a layout */
  def l[A <: ViewGroup](children: View*)(implicit ctx: Context) = macro layoutImpl[A]

  // Tweaks
  type Tweak[-A <: View] = Function[A, Unit]
  implicit def tweakMonoid[A <: View] = new Monoid[Tweak[A]] {
    def zero = { x ⇒ () }
    def append(t1: Tweak[A], t2: ⇒ Tweak[A]) = t1 + t2
  }
  implicit class RichTweak[A <: View](t: Tweak[A]) {
    /** Combine (sequence) with another tweak */
    def +[B <: A](other: Tweak[B]): Tweak[B] = { x ⇒ t(x); other(x) }
  }

  // Transformers
  type Transformer = PartialFunction[View, Unit]

  implicit class RichView[A <: View](v: A) {
    /** Tweak this view. Always runs on UI thread */
    def ~>(t: Tweak[A]) = { Concurrency.runOnUiThread(t(v)); v }
    /** Functor tweak. Always runs on UI thread */
    def ~>[F[_]: Functor](f: F[Tweak[A]]) = { implicitly[Functor[F]].map(f)(t ⇒ Concurrency.runOnUiThread(t(v))); v }

    /** Tweak this view. Always runs on UI thread */
    def ⇝(t: Tweak[A]): A = { Concurrency.runOnUiThread(t(v)); v }
    /** Functor tweak. Always runs on UI thread */
    def ⇝[F[_]: Functor](f: F[Tweak[A]]) = { implicitly[Functor[F]].map(f)(t ⇒ Concurrency.runOnUiThread(t(v))); v }
  }

  implicit class RichViewGroup[A <: ViewGroup](v: A) {
    /** Apply transformer. Always runs on UI thread */
    def ~~>(t: Transformer) = {
      def applyTo(v: View) {
        if (t.isDefinedAt(v)) t(v)
        v match {
          case Layout(children @ _*) ⇒ children.foreach(applyTo)
          case _ ⇒ ()
        }
      }
      Concurrency.runOnUiThread(applyTo(v))
      v
    }
  }
  object Layout {
    def unapplySeq(v: View): Option[Seq[View]] = v match {
      case g: ViewGroup ⇒ Some((0 until g.getChildCount).map(i ⇒ g.getChildAt(i)))
      case _ ⇒ None
    }
  }
}

object LayoutDsl extends LayoutDsl

trait FragmentDsl extends FragmentApi { self: ViewSearch ⇒
  import LayoutDslMacros._

  /** Define a fragment, which is wrapped in FrameLayout to be added to the layout */
  def f[A <: Fragment](id: Int, tag: String, args: Any*)(implicit ctx: Context) = macro fragmentImpl[A]

  /** Returns a fragment factory (Thunk[A]) */
  def fragmentFactory[A <: Fragment](args: Any*) = macro fragmentFactoryImpl[A]
  /** Same as fragmentFactory */
  def ff[A <: Fragment](args: Any*) = macro fragmentFactoryImpl[A]
}

object LayoutDslMacros {
  def instFrag[A <: Fragment: c.WeakTypeTag](c: MacroContext)(args: Seq[c.Expr[Any]]) = {
    import c.universe._
    scala.util.Try {
      // try to use newInstance(args)
      c.typeCheck(q"${weakTypeOf[A].typeSymbol.companionSymbol}.newInstance(..$args)")
    } orElse scala.util.Try {
      // try to put args in a map, convert to a Bundle and use setArguments
      assert(args.forall(_.actualType <:< typeOf[(String, Any)]))
      c.typeCheck(q"new ${weakTypeOf[A]} { setArguments(org.macroid.util.Map2Bundle(Map(..$args))) }")
    } getOrElse {
      c.abort(c.enclosingPosition, s"Args should either be supported by ${weakTypeOf[A]}.newInstance() or be a sequence of (String, Any)")
    }
  }

  def fragmentImpl[A <: Fragment: c.WeakTypeTag](c: MacroContext)(id: c.Expr[Int], tag: c.Expr[String], args: c.Expr[Any]*)(ctx: c.Expr[Context]): c.Expr[FrameLayout] = {
    import c.universe._
    val frag = instFrag[A](c)(args)
    c.Expr[FrameLayout](q"fragment($frag, $id, $tag)($ctx)")
  }

  def fragmentFactoryImpl[A <: Fragment: c.WeakTypeTag](c: MacroContext)(args: c.Expr[Any]*): c.Expr[Thunk[A]] = {
    import c.universe._
    val frag = instFrag[A](c)(args)
    c.Expr[Thunk[A]](q"org.macroid.util.Thunk($frag)")
  }

  def widgetImpl[A <: View: c.WeakTypeTag](c: MacroContext)(ctx: c.Expr[Context]): c.Expr[A] = {
    import c.universe._
    c.Expr[A](q"new ${weakTypeOf[A]}($ctx)")
  }

  def widgetArgImpl[A <: View: c.WeakTypeTag](c: MacroContext)(args: c.Expr[Any]*)(ctx: c.Expr[Context]): c.Expr[A] = {
    import c.universe._
    c.Expr[A](q"new ${weakTypeOf[A]}($ctx, ..$args)")
  }

  def layoutImpl[A <: View: c.WeakTypeTag](c: MacroContext)(children: c.Expr[View]*)(ctx: c.Expr[Context]): c.Expr[A] = {
    import c.universe._
    val additions = children.map(ch ⇒ c.resetLocalAttrs(q"this.addView($ch)"))
    c.Expr[A](q"new ${weakTypeOf[A]}($ctx) { ..$additions }")
  }
}
