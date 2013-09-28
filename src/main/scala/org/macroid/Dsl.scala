package org.macroid

import scala.language.experimental.macros
import scala.language.higherKinds
import scala.language.implicitConversions
import android.support.v4.app.Fragment
import scala.reflect.macros.{ Context ⇒ MacroContext }
import android.widget.FrameLayout
import android.view.{ ViewGroup, View }
import android.content.Context
import scalaz.{ Functor, Monoid }
import org.macroid.util.{ Functors, Thunk }
import scala.concurrent.{ ExecutionContext, Promise, Future }

/** This trait contains basic building blocks used to define layouts: w, l and slot */
trait LayoutBuilding {
  import LayoutBuildingMacros._

  /** Define a widget */
  def w[A <: View](implicit ctx: Context) = macro widgetImpl[A]
  /** Define a widget, supplying additional arguments */
  def w[A <: View](args: Any*)(implicit ctx: Context) = macro widgetArgImpl[A]

  /** Define a layout */
  def l[A <: ViewGroup](children: View*)(implicit ctx: Context) = macro layoutImpl[A]

  /** Define a slot */
  def slot[A <: View]: Option[A] = None
}
object LayoutBuilding extends LayoutBuilding

object LayoutBuildingMacros {
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

/** This trait defines tweaks, tweaking operator (~>) and its generalized counterparts for Functors */
trait Tweaking {
  /** A tweak is a function that mutates a View */
  type Tweak[-A <: View] = Function[A, Unit]
  /** A slow tweak mutates the view slowly (e.g. animation) */
  type SlowTweak[-A <: View] = Function[A, Future[Unit]]

  // a monoid instance
  implicit def tweakMonoid[A <: View] = new Monoid[Tweak[A]] {
    def zero = { x ⇒ () }
    def append(t1: Tweak[A], t2: ⇒ Tweak[A]) = t1 + t2
  }

  // combining tweaks
  implicit class RichTweak[A <: View](t: Tweak[A]) {
    /** Combine (sequence) with another tweak */
    def +[B <: A](other: Tweak[B]): Tweak[B] = { x ⇒ t(x); other(x) }
    def +@[B <: A](other: SlowTweak[B]): SlowTweak[B] = { x ⇒ t(x); other(x) }
  }

  // combining slow tweaks
  implicit class RichSlowTweak[A <: View](t: SlowTweak[A]) {
    /** Combine (sequence) with a tweak */
    def @+[B <: A](other: Tweak[B])(implicit ec: ExecutionContext): SlowTweak[B] = { x ⇒
      t(x).map(_ ⇒ Concurrency.Ui(other(x)))
    }
    def @+@[B <: A](other: SlowTweak[B])(implicit ec: ExecutionContext): SlowTweak[B] = { x ⇒
      t(x).flatMap(_ ⇒ Concurrency.Ui(other(x)))
    }
  }

  // applying tweaks to views
  implicit class RichView[A <: View](v: A) {
    /** Tweak `v`. Always runs on UI thread */
    def ~>(t: Tweak[A]): A = { Concurrency.fireForget(t(v)); v }
    /** Apply tweak(s) in `f` to `v`. Always runs on UI thread */
    def ~>[F[_]: Functor](f: F[Tweak[A]]): A = { implicitly[Functor[F]].map(f)(t ⇒ Concurrency.fireForget(t(v))); v }
    /** Tweak `v` slowly. Always runs on UI thread */
    def ~@>(t: SlowTweak[A])(implicit ec: ExecutionContext): Future[A] = {
      val slowPromise = Promise[Unit]()
      Concurrency.fireForget(slowPromise.completeWith(t(v)))
      slowPromise.future.map(_ ⇒ v)
    }
  }

  // applying tweaks to functors
  implicit class RichFunctor[A <: View, F[_]: Functor](f: F[A]) {
    /** Tweak view(s) in `f`. Always runs on UI thread */
    def ~>(t: Tweak[A]): F[A] = { implicitly[Functor[F]].map(f)(v ⇒ Concurrency.fireForget(t(v))); f }
    /** Apply tweak(s) in `g` to view(s) in `f`. Always runs on UI thread */
    def ~>[G[_]: Functor](g: G[Tweak[A]]): F[A] = {
      val F = implicitly[Functor[F]]
      val G = implicitly[Functor[G]]
      F.map(f)(v ⇒ G.map(g)(t ⇒ Concurrency.fireForget(t(v)))); f
    }
  }

  // applying slow tweaks to options
  implicit class RichSlowOption[A <: View](f: Option[A]) {
    /** Tweak view inside `f` slowly. Always runs on UI thread */
    def ~@>(t: SlowTweak[A])(implicit ec: ExecutionContext): Future[Option[A]] = f match {
      case None ⇒ Future.successful(None)
      case Some(v) ⇒
        val slowPromise = Promise[Unit]()
        Concurrency.fireForget(slowPromise.completeWith(t(v)))
        slowPromise.future.map(_ ⇒ Some(v))
    }
  }

  // applying slow tweaks to futures
  implicit class RichSlowFuture[A <: View](f: Future[A]) {
    /** Tweak view inside `f` slowly. Always runs on UI thread */
    def ~@>(t: SlowTweak[A])(implicit ec: ExecutionContext): Future[A] = f.flatMap(v ⇒ v ~@> t)
  }

  // applying slow tweaks to futures of options
  implicit class RichSlowFutureOption[A <: View](f: Future[Option[A]]) {
    /** Tweak view inside `f` slowly. Always runs on UI thread */
    def ~@>(t: SlowTweak[A])(implicit ec: ExecutionContext): Future[Option[A]] = f.flatMap(v ⇒ v ~@> t)
  }
}
object Tweaking extends Tweaking

/** This trait defines transformers and transforming operator (~~>) */
trait LayoutTransforming {
  /** A transformer is a partial mutating function that can be recursively applied to a layout */
  type Transformer = PartialFunction[View, Unit]

  // transforming layouts
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
  object LayoutTransforming extends LayoutTransforming

  // layout extractor
  object Layout {
    def unapplySeq(v: View): Option[Seq[View]] = v match {
      case g: ViewGroup ⇒ Some((0 until g.getChildCount).map(i ⇒ g.getChildAt(i)))
      case _ ⇒ None
    }
  }
}

trait LayoutDsl extends LayoutBuilding with Tweaking with LayoutTransforming with Functors
object LayoutDsl extends LayoutDsl

trait FragmentBuilding extends FragmentApi { self: ViewSearch ⇒
  import FragmentBuildingMacros._

  /** Define a fragment, which is wrapped in FrameLayout to be added to the layout */
  def f[A <: Fragment](id: Int, tag: String, args: Any*)(implicit ctx: Context) = macro fragmentImpl[A]
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
}

trait FragmentFactories extends FragmentApi { self: ViewSearch ⇒
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

trait FragmentDsl extends FragmentApi with FragmentBuilding with FragmentFactories { self: ViewSearch ⇒ }
