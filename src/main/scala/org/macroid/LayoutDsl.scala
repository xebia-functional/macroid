package org.macroid

import scala.language.experimental.macros
import android.support.v4.app.Fragment
import scala.reflect.macros.{ Context ⇒ MacroContext }
import android.widget.FrameLayout
import android.view.{ ViewGroup, View }
import android.content.Context
import scalaz.Monoid
import io.dylemma.frp.{ Observer, EventStream }
import org.macroid.Util.Thunk

trait LayoutDsl {
  import LayoutDslMacros._

  /** Define a widget */
  def w[A <: View](implicit ctx: Context) = macro widgetImpl[A]
  /** Define a widget, supplying additional arguments */
  def w[A <: View](args: Any*)(implicit ctx: Context) = macro widgetArgImpl[A]

  /** Define a layout */
  def l[A <: ViewGroup](children: View*)(implicit ctx: Context) = macro layoutImpl[A]

  type Tweak[-A <: View] = Function[A, Unit]
  implicit def tweakMonoid[A <: View] = new Monoid[Tweak[A]] {
    def zero = { x ⇒ () }
    def append(t1: Tweak[A], t2: ⇒ Tweak[A]) = t1 + t2
  }

  implicit class RichView[A <: View](v: A) {
    /** Tweak this view */
    def ~>(t: Tweak[A]) = { t(v); v }
    /** React to tweaking events */
    def ~>(s: EventStream[Tweak[A]])(implicit observer: Observer) = {
      s.foreach(t ⇒ Concurrency.runOnUiThread(t(v)))
      v
    }

    /** Unicode alias for ~> */
    def ⇝(t: Tweak[A]) = { t(v); v }
    /** Unicode alias for ~> */
    def ⇝(s: EventStream[Tweak[A]])(implicit observer: Observer) = {
      s.foreach(t ⇒ Concurrency.runOnUiThread(t(v)))
      v
    }
  }

  implicit class RichTweak[A <: View](t: Tweak[A]) {
    /** Combine (sequence) with another tweak */
    def +[B <: A](other: Tweak[B]): Tweak[B] = { x ⇒ t(x); other(x) }
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
      c.typeCheck(q"new ${weakTypeOf[A]} { setArguments(org.macroid.Util.map2bundle(Map(..$args))) }")
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
    c.Expr[Thunk[A]](q"org.macroid.Util.Thunk($frag)")
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
    val additions = children.map(ch ⇒ c.resetAllAttrs(q"this.addView($ch)"))
    c.Expr[A](q"new ${weakTypeOf[A]}($ctx) { ..$additions }")
  }
}
