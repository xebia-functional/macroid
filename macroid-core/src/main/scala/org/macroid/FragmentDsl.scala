package org.macroid

import scala.language.experimental.macros
import scala.language.implicitConversions
import scala.language.postfixOps
import android.content.Context
import android.support.v4.app.Fragment
import android.widget.FrameLayout
import org.macroid.util.Thunk
import scala.reflect.macros.{ Context ⇒ MacroContext }
import android.os.Bundle

trait FragmentBuilding extends Bundles { self: ViewSearch ⇒
  import FragmentBuildingMacros._

  /**
   * A fragment builder proxy that handles both inserting fragments into layout
   * (via `framed`) and creating fragment factories (via `factory`)
   */
  case class FragmentBuilder[A <: Fragment](constructor: Thunk[A], arguments: Bundle)(implicit ctx: ActivityContext) {
    /** Pass arguments in a Bundle */
    def pass(bundle: Bundle) = FragmentBuilder(constructor, arguments + bundle)
    /** Pass arguments, which will be put into a Bundle */
    def pass(arguments: (String, Any)*) = macro passImpl[A]

    /** Fragment factory. In contrast to `constructor`, `factory` passes arguments to the fragment */
    def factory = constructor map { f ⇒ f.setArguments(arguments); f }

    /** Fragment wrapped in FrameLayout to be added to layout */
    def framed(id: Int, tag: String) = {
      findFrag[Fragment](tag) getOrElse {
        fragmentManager.beginTransaction().add(id, factory(), tag).commit()
      }
      new FrameLayout(ctx.get) { setId(id) }
    }
  }

  def f[A <: Fragment](implicit ctx: ActivityContext) = macro fragmentImpl[A]

  /**
   * Fragment builder. `newInstanceArgs` are passed to newInstance, if any.
   * Without arguments, newInstance() is called, and if that fails, class constructor is used.
   */
  def f[A <: Fragment](newInstanceArgs: Any*)(implicit ctx: ActivityContext) = macro fragmentArgImpl[A]
}

object FragmentBuildingMacros {
  def instFrag[A <: Fragment: c.WeakTypeTag](c: MacroContext)(args: Seq[c.Expr[Any]], ctx: c.Expr[ActivityContext]) = {
    import c.universe._

    scala.util.Try {
      // try to use newInstance(args)
      c.typeCheck(q"${weakTypeOf[A].typeSymbol.companionSymbol}.newInstance(..$args)")
    } orElse scala.util.Try {
      // use class constructor
      assert(args.isEmpty)
      q"new ${weakTypeOf[A]}"
    } getOrElse {
      c.abort(c.enclosingPosition, s"Could not find ${weakTypeOf[A]}.newInstance() method that accepts $args")
    }
  }

  def fragmentImpl[A <: Fragment: c.WeakTypeTag](c: MacroContext)(ctx: c.Expr[ActivityContext]) = {
    import c.universe._
    val constructor = instFrag(c)(Seq(), ctx)
    c.Expr[FragmentBuilding#FragmentBuilder[A]](q"FragmentBuilder(org.macroid.util.Thunk($constructor), new android.os.Bundle)($ctx)")
  }

  def fragmentArgImpl[A <: Fragment: c.WeakTypeTag](c: MacroContext)(newInstanceArgs: c.Expr[Any]*)(ctx: c.Expr[ActivityContext]) = {
    import c.universe._
    val constructor = instFrag(c)(newInstanceArgs, ctx)
    c.Expr[FragmentBuilding#FragmentBuilder[A]](q"FragmentBuilder(org.macroid.util.Thunk($constructor), new android.os.Bundle)($ctx)")
  }

  def passImpl[A <: Fragment: c.WeakTypeTag](c: MacroContext)(arguments: c.Expr[(String, Any)]*) = {
    import c.universe._
    val Apply(Apply(_, List(constructor, args)), List(ctx)) = c.prefix.tree
    c.Expr[FragmentBuilding#FragmentBuilder[A]](q"FragmentBuilder($constructor, $args + bundle(..$arguments))($ctx)")
  }
}

trait FragmentDsl extends FragmentBuilding { self: ViewSearch ⇒ }