package org.macroid

import scala.language.experimental.macros
import android.support.v4.app.Fragment
import scala.reflect.macros.{ Context ⇒ MacroContext }
import android.widget.FrameLayout
import android.view.{ ViewGroup, View }
import android.content.Context
import scalaz.Monoid

trait LayoutDsl {
  import LayoutDslMacros._

  /** Define a widget */
  def w[A <: View](implicit ctx: Context) = macro widgetImpl[A]
  /** Define a widget, supplying additional arguments */
  def w[A <: View](args: Any*)(implicit ctx: Context) = macro widgetArgImpl[A]

  /** Define a layout */
  def l[A <: ViewGroup](children: View*)(implicit ctx: Context) = macro layoutImpl[A]

  type Tweak[-A] = Function[A, Unit]
  implicit def tweakMonoid[A] = new Monoid[Tweak[A]] {
    def zero = { x ⇒ () }
    def append(t1: Tweak[A], t2: ⇒ Tweak[A]) = t1 + t2
  }

  implicit class RichView[A <: View](v: A) {
    /** Tweak this view */
    def ~>(t: Tweak[A]) = { t(v); v }
    /** Unicode alias for ~> */
    def ⇝(t: Tweak[A]) = { t(v); v }
  }

  implicit class RichViewMutator[A](t: Tweak[A]) {
    /** Combine (sequence) with another tweak */
    def +(other: Tweak[A]): Tweak[A] = { x ⇒ t(x); other(x) }
  }
}

object LayoutDsl extends LayoutDsl

trait FragmentDsl extends FragmentApi { self: ViewSearch ⇒
  import LayoutDslMacros._

  /** Define a fragment, which is wrapped in FrameLayout to be added to the layout */
  def f[A <: Fragment](id: Int, tag: String, args: Any*)(implicit ctx: Context) = macro fragmentImpl[A]

  /** Returns a fragment factory ( () ⇒ A ) */
  def fragmentFactory[A <: Fragment](args: Any*) = macro fragmentFactoryImpl[A]
  /** Same as fragmentFactory */
  def ff[A <: Fragment](args: Any*) = macro fragmentFactoryImpl[A]
}

object LayoutDslMacros {
  class Helper[CTX <: MacroContext](val c: CTX) {
    import c.universe._

    def instantiateFragment(fragTpe: Type, args: Seq[c.Expr[Any]]) = q"""
      val frag = new $fragTpe
      val bundle = org.macroid.Util.map2bundle(Map(..$args))
      frag.setArguments(bundle)
      frag
    """

    def instantiateFragmentNewInstance(fragTpe: Type, args: Seq[c.Expr[Any]]) = q"""
      ${fragTpe.typeSymbol.companionSymbol}.newInstance(..$args)
    """

    def makeFactory(tree: Tree) = q"() ⇒ { $tree }"

    def wrapFragment(frag: Tree, id: c.Expr[Int], tag: c.Expr[String], ctx: c.Expr[Context]) = q"""
      fragment($frag, $id, $tag)($ctx)
    """

    def instantiateWidget(widgetTpe: Type, args: Seq[c.Expr[Any]])(ctx: c.Expr[Context]) = q"""
      new $widgetTpe($ctx, ..$args)
    """

    def populateLayout(lay: Tree, children: Seq[c.Expr[Any]]) = {
      val additions = children.map(c ⇒ q"l.addView($c)")
      q"val l = $lay; ..$additions; l"
    }
  }

  def instFrag[A <: Fragment: c.WeakTypeTag](c: MacroContext)(helper: Helper[c.type], args: Seq[c.Expr[Any]]) = scala.util.Try {
    c.typeCheck(helper.instantiateFragmentNewInstance(c.weakTypeOf[A], args))
  } orElse scala.util.Try {
    assert(args.forall(_.actualType <:< c.typeOf[(String, Any)]))
    c.typeCheck(helper.instantiateFragment(c.weakTypeOf[A], args))
  } getOrElse {
    c.abort(c.enclosingPosition, s"Args should either be supported by ${c.weakTypeOf[A]}.newInstance() or be a sequence of (String, Any)")
  }

  def fragmentImpl[A <: Fragment: c.WeakTypeTag](c: MacroContext)(id: c.Expr[Int], tag: c.Expr[String], args: c.Expr[Any]*)(ctx: c.Expr[Context]): c.Expr[FrameLayout] = {
    val helper = new Helper[c.type](c)
    val frag = instFrag[A](c)(helper, args)
    val wrap = helper.wrapFragment(frag, id, tag, ctx)
    c.Expr[FrameLayout](wrap)
  }

  def fragmentFactoryImpl[A <: Fragment: c.WeakTypeTag](c: MacroContext)(args: c.Expr[Any]*): c.Expr[A] = {
    val helper = new Helper[c.type](c)
    val frag = instFrag[A](c)(helper, args)
    c.Expr[A](frag)
  }

  def widgetImpl[A <: View: c.WeakTypeTag](c: MacroContext)(ctx: c.Expr[Context]): c.Expr[A] = {
    val helper = new Helper[c.type](c)
    val widget = helper.instantiateWidget(c.weakTypeOf[A], Seq())(ctx)
    c.Expr[A](widget)
  }

  def widgetArgImpl[A <: View: c.WeakTypeTag](c: MacroContext)(args: c.Expr[Any]*)(ctx: c.Expr[Context]): c.Expr[A] = {
    val helper = new Helper[c.type](c)
    val widget = helper.instantiateWidget(c.weakTypeOf[A], args)(ctx)
    c.Expr[A](widget)
  }

  def layoutImpl[A <: View: c.WeakTypeTag](c: MacroContext)(children: c.Expr[View]*)(ctx: c.Expr[Context]): c.Expr[A] = {
    val helper = new Helper[c.type](c)
    val layout = helper.instantiateWidget(c.weakTypeOf[A], Seq())(ctx)
    val populated = helper.populateLayout(layout, children)
    c.Expr[A](populated)
  }
}
