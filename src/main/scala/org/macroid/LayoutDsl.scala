package org.macroid

import scala.language.experimental.macros
import scala.language.higherKinds
import android.content.Context
import android.view.{ ViewGroup, View }
import org.macroid.util.{ Effectors, Effector }
import scala.concurrent.{ Promise, ExecutionContext, Future }
import scala.reflect.macros.{ Context ⇒ MacroContext }

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
trait Tweaking extends Effectors {
  /** A tweak is a function that mutates a View */
  type Tweak[-A <: View] = Function[A, Unit]

  object Tweak {
    /** A tweak that does nothing */
    def blank[A <: View]: Tweak[A] = { x ⇒ () }
  }

  /** Combining tweaks */
  implicit class TweakAddition[A <: View](t: Tweak[A]) {
    /** Combine (sequence) with another tweak */
    def +[B <: A](other: Tweak[B]): Tweak[B] = { x ⇒ t(x); other(x) }
  }

  // TODO: fundeps

  /** Applying tweaks to views */
  implicit class ViewTweaking[A <: View](v: A) {
    /** Tweak `v`. Always runs on UI thread */
    def ~>(t: Tweak[A]): A = { Concurrency.fireUi(t(v)); v }
    /** Apply tweak(s) in `f` to `v`. Always runs on UI thread */
    def ~>[F[+_]: Effector](f: F[Tweak[A]]): A = { implicitly[Effector[F]].foreach(f)(t ⇒ v ~> t); v }
  }

  /** Applying tweaks to effectors */
  implicit class EffectorTweaking[A <: View, F[_]: Effector](f: F[A]) {
    /** Tweak view(s) in `f`. Always runs on UI thread */
    def ~>(t: Tweak[A]): F[A] = { implicitly[Effector[F]].foreach(f)(v ⇒ v ~> t); f }
    /** Apply tweak(s) in `g` to view(s) in `f`. Always runs on UI thread */
    def ~>[G[+_]: Effector](g: G[Tweak[A]]): F[A] = {
      val F = implicitly[Effector[F]]
      val G = implicitly[Effector[G]]
      F.foreach(f)(v ⇒ G.foreach(g)(t ⇒ v ~> t)); f
    }
  }

  /** Applying tweaks to futures of options */
  implicit class FutureOptionTweaking[A <: View](f: Future[Option[A]]) {
    /** Tweak view in `f`. Always runs on UI thread */
    def ~>(t: Tweak[A])(implicit ec: ExecutionContext): Future[Option[A]] = f map {
      case Some(v) ⇒
        v ~> t; Some(v)
      case None ⇒ None
    }
    /** Apply tweak(s) in `g` to view in `f`. Always runs on UI thread */
    def ~>[G[+_]](g: G[Tweak[A]])(implicit ec: ExecutionContext, ev: Effector[G]): Future[Option[A]] = f map {
      case Some(v) ⇒
        v ~> g; Some(v)
      case None ⇒ None
    }
  }
}

object Tweaking extends Tweaking

/** This trait defines snails, snailing operator (~@>) and its generalizations */
trait Snailing extends Tweaking {
  /** A snail mutates the view slowly (e.g. animation) */
  type Snail[-A <: View] = Function[A, Future[Unit]]

  object Snail {
    /** A snail that does nothing */
    def blank[A <: View]: Snail[A] = { x ⇒ Future.successful(()) }
  }

  /** Combining tweaks with snails */
  implicit class TweakSnailAddition[A <: View](t: Tweak[A]) {
    /** Combine (sequence) with a snail */
    def +@[B <: A](other: Snail[B]): Snail[B] = { x ⇒ t(x); other(x) }
  }

  /** Combining snails */
  implicit class SnailAddition[A <: View](s: Snail[A]) {
    /** Combine (sequence) with a tweak */
    def @+[B <: A](other: Tweak[B])(implicit ec: ExecutionContext): Snail[B] = { x ⇒
      s(x).map(_ ⇒ Concurrency.Ui(other(x)))
    }
    /** Combine (sequence) with another snail */
    def @+@[B <: A](other: Snail[B])(implicit ec: ExecutionContext): Snail[B] = { x ⇒
      s(x).flatMap(_ ⇒ Concurrency.Ui(other(x)))
    }
  }

  /* TODO: can this be shortened with fundeps and/or monad laws? */

  /** Applying snails to views */
  implicit class ViewSnailing[A <: View](v: A) {
    /** Apply a snail to `v`. Always runs on UI thread */
    def ~@>(s: Snail[A])(implicit ec: ExecutionContext): Future[A] = {
      val snailPromise = Promise[Unit]()
      Concurrency.fireUi(snailPromise.completeWith(s(v)))
      snailPromise.future.map(_ ⇒ v)
    }
    /** Apply a future snail to `v`. Always runs on UI thread */
    def ~@>(g: Future[Snail[A]])(implicit ec: ExecutionContext): Future[A] = g.flatMap(s ⇒ v ~@> s)
  }

  /** Applying snails to options */
  implicit class OptionSnailing[A <: View](f: Option[A]) {
    /** Apply a snail to the view inside `f`. Always runs on UI thread */
    def ~@>(s: Snail[A])(implicit ec: ExecutionContext): Future[Option[A]] = f match {
      case None ⇒ Future.successful(None)
      case Some(v) ⇒
        val snailPromise = Promise[Unit]()
        Concurrency.fireUi(snailPromise.completeWith(s(v)))
        snailPromise.future.map(_ ⇒ Some(v))
    }
    /** Apply a future snail to view inside `f`. Always runs on UI thread */
    def ~@>(g: Future[Snail[A]])(implicit ec: ExecutionContext): Future[Option[A]] = g.flatMap(s ⇒ f ~@> s)
  }

  /** Applying snails to futures */
  implicit class FutureSnailing[A <: View](f: Future[A]) {
    /** Apply a snail to view inside `f`. Always runs on UI thread */
    def ~@>(s: Snail[A])(implicit ec: ExecutionContext): Future[A] = f.flatMap(v ⇒ v ~@> s)
    /** Apply a future snail to view inside `f`. Always runs on UI thread */
    def ~@>(g: Future[Snail[A]])(implicit ec: ExecutionContext): Future[A] = g.flatMap(s ⇒ f ~@> s)
  }

  /** Applying snails to futures of options */
  implicit class FutureOptionSnailing[A <: View](f: Future[Option[A]]) {
    /** Apply a snail to the view inside `f`. Always runs on UI thread */
    def ~@>(s: Snail[A])(implicit ec: ExecutionContext): Future[Option[A]] = f.flatMap(v ⇒ v ~@> s)
    /** Apply a future snail to the view inside `f`. Always runs on UI thread */
    def ~@>(g: Future[Snail[A]])(implicit ec: ExecutionContext): Future[Option[A]] = g.flatMap(s ⇒ f ~@> s)
  }
}

object Snailing extends Snailing

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

trait LayoutDsl extends LayoutBuilding with Tweaking with Snailing with LayoutTransforming

object LayoutDsl extends LayoutDsl