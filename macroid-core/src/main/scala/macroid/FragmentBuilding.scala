package macroid

import scala.language.experimental.macros
import scala.language.implicitConversions
import scala.language.postfixOps
import scala.reflect.macros.{ Context ⇒ MacroContext }

import android.os.Bundle
import macroid.contrib.Layouts.RootFrameLayout
import macroid.support.Fragment

/** A fragment builder proxy */
case class FragmentBuilder[F](constructor: Ui[F], arguments: Bundle)(implicit ctx: ContextWrapper, fragment: Fragment[F]) {
  import Searching._
  import Bundles._

  /** Pass arguments in a Bundle */
  def pass(bundle: Bundle) = FragmentBuilder(constructor, arguments + bundle)
  /** Pass arguments, which will be put into a Bundle */
  def pass(arguments: (String, Any)*): FragmentBuilder[F] = macro FragmentBuildingMacros.passImpl[F]

  /** Fragment factory. In contrast to `constructor`, `factory` passes arguments to the fragment */
  def factory = constructor map { f ⇒ fragment.setArguments(f, arguments); f }

  /** Fragment wrapped in FrameLayout to be added to layout */
  def framed[M](id: Int, tag: String)(implicit managerCtx: FragmentManagerContext[F, M]) = {
    import managerCtx.fragmentApi
    managerCtx.manager.findFrag[F](tag) map { f ⇒
      f.getOrElse {
        managerCtx.fragmentApi.addFragment(managerCtx.get, id, tag, factory.get)
      }
      new RootFrameLayout(ctx.getOriginal) {
        setId(id)
      }
    }
  }
}

private[macroid] trait FragmentBuilding extends Bundles {
  import FragmentBuildingMacros._

  /** Fragment builder. To create a fragment, newInstance() is called, and if that fails, class constructor is used.
    */
  def fragment[F](implicit ctx: ContextWrapper, fragment: Fragment[F]): FragmentBuilder[F] = macro fragmentImpl[F]

  /** Fragment builder. To create a fragment, newInstance() is called, and if that fails, class constructor is used.
    * (This is an alias for `fragment`.)
    */
  def f[F](implicit ctx: ContextWrapper, fragment: Fragment[F]): FragmentBuilder[F] = macro fragmentImpl[F]

  /** Fragment builder. `newInstanceArgs` are passed to newInstance, if any.
    * Without arguments, newInstance() is called, and if that fails, class constructor is used.
    */
  def fragment[F](newInstanceArgs: Any*)(implicit ctx: ContextWrapper, fragment: Fragment[F]): FragmentBuilder[F] = macro fragmentArgImpl[F]

  /** Fragment builder. `newInstanceArgs` are passed to newInstance, if any.
    * Without arguments, newInstance() is called, and if that fails, class constructor is used.
    * (This is an alias for `fragment`.)
    */
  def f[F](newInstanceArgs: Any*)(implicit ctx: ContextWrapper, fragment: Fragment[F]): FragmentBuilder[F] = macro fragmentArgImpl[F]
}

object FragmentBuilding extends FragmentBuilding

object FragmentBuildingMacros {
  def instFrag[F: c.WeakTypeTag](c: MacroContext)(args: Seq[c.Expr[Any]], ctx: c.Expr[ContextWrapper]) = {
    import c.universe._

    scala.util.Try {
      // try to use newInstance(args)
      c.typeCheck(q"${weakTypeOf[F].typeSymbol.companionSymbol}.newInstance(..$args)")
    } orElse scala.util.Try {
      // use class constructor
      assert(args.isEmpty)
      q"new ${weakTypeOf[F]}"
    } getOrElse {
      c.abort(c.enclosingPosition, s"Could not find ${weakTypeOf[F]}.newInstance() method that accepts $args")
    }
  }

  def fragmentImpl[F: c.WeakTypeTag](c: MacroContext)(ctx: c.Expr[ContextWrapper], fragment: c.Expr[Fragment[F]]) = {
    import c.universe._
    val constructor = instFrag(c)(Seq(), ctx)
    c.Expr[FragmentBuilder[F]](q"_root_.macroid.FragmentBuilder(_root_.macroid.Ui($constructor), new _root_.android.os.Bundle)($ctx, $fragment)")
  }

  def fragmentArgImpl[F: c.WeakTypeTag](c: MacroContext)(newInstanceArgs: c.Expr[Any]*)(ctx: c.Expr[ContextWrapper], fragment: c.Expr[Fragment[F]]) = {
    import c.universe._
    val constructor = instFrag(c)(newInstanceArgs, ctx)
    c.Expr[FragmentBuilder[F]](q"_root_.macroid.FragmentBuilder(_root_.macroid.Ui($constructor), new _root_.android.os.Bundle)($ctx, $fragment)")
  }

  def passImpl[F: c.WeakTypeTag](c: MacroContext)(arguments: c.Expr[(String, Any)]*) = {
    import c.universe._
    val Apply(Apply(_, List(constructor, args)), List(ctx, fragment)) = c.prefix.tree
    c.Expr[FragmentBuilder[F]](q"_root_.macroid.FragmentBuilder($constructor, $args + bundle(..$arguments))($ctx, $fragment)")
  }
}
