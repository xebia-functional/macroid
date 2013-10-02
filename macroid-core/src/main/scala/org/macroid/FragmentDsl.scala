package org.macroid

import scala.language.experimental.macros
import android.content.Context
import android.support.v4.app.Fragment
import android.widget.FrameLayout
import org.macroid.util.Thunk
import scala.reflect.macros.{ Context ⇒ MacroContext }

trait FragmentBuilding { self: ViewSearch ⇒
  import FragmentBuildingMacros._

  /** Define a fragment, which is wrapped in FrameLayout to be added to the layout */
  def f[A <: Fragment](id: Int, tag: String, args: Any*)(implicit ctx: Context) = macro fragmentImpl[A]

  /** Create a fragment from factory, wrap in a FrameLayout and return */
  def fragment(frag: ⇒ Fragment, id: Int, tag: String)(implicit ctx: Context): FrameLayout = {
    findFrag[Fragment](tag) getOrElse {
      fragmentManager.beginTransaction().add(id, frag, tag).commit()
    }
    new FrameLayout(ctx) { setId(id) }
  }
}

object FragmentBuildingMacros {
  def instFrag[A <: Fragment: c.WeakTypeTag](c: MacroContext)(args: Seq[c.Expr[Any]]) = {
    import c.universe._
    scala.util.Try {
      // try to use newInstance(args)
      c.typeCheck(q"${weakTypeOf[A].typeSymbol.companionSymbol}.newInstance(..$args)")
    } orElse scala.util.Try {
      // try to put args in a map, convert to a Bundle and use setArguments
      assert(args.forall(_.actualType <:< typeOf[(String, Any)]))
      c.typeCheck(q"val x = new ${weakTypeOf[A]}; x.setArguments(org.macroid.util.Map2Bundle(Map(..$args))); x")
    } getOrElse {
      c.abort(c.enclosingPosition, s"Args should either be supported by ${weakTypeOf[A]}.newInstance() or be a sequence of (String, Any)")
    }
  }

  def fragmentImpl[A <: Fragment: c.WeakTypeTag](c: MacroContext)(id: c.Expr[Int], tag: c.Expr[String], args: c.Expr[Any]*)(ctx: c.Expr[Context]): c.Expr[FrameLayout] = {
    import c.universe._
    val frag = instFrag[A](c)(args)
    c.Expr[FrameLayout](q"fragment($frag, $id, $tag)($ctx)")
  }
}

trait FragmentFactories { self: ViewSearch ⇒
  import FragmentFactoryMacros._

  /** Returns a fragment factory (Thunk[A]) */
  def fragmentFactory[A <: Fragment](args: Any*) = macro fragmentFactoryImpl[A]
  /** Same as fragmentFactory */
  def ff[A <: Fragment](args: Any*) = macro fragmentFactoryImpl[A]
}

object FragmentFactoryMacros {
  import FragmentBuildingMacros._

  def fragmentFactoryImpl[A <: Fragment: c.WeakTypeTag](c: MacroContext)(args: c.Expr[Any]*): c.Expr[Thunk[A]] = {
    import c.universe._
    val frag = instFrag[A](c)(args)
    c.Expr[Thunk[A]](q"org.macroid.util.Thunk($frag)")
  }
}

trait FragmentDsl extends FragmentBuilding with FragmentFactories { self: ViewSearch ⇒ }