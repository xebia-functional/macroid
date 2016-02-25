package macroid.viewable

import android.os.Build.VERSION_CODES._
import android.widget.{ ListView, TextView }
import macroid.{ Ui, ActivityContext, ContextWrapper }
import macroid.FullDsl._
import org.robolectric.shadows.ShadowListView
import org.robolectric.{ Shadows, Robolectric, RuntimeEnvironment }
import org.robolectric.annotation.Config
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ FlatSpec, RobolectricSuite }

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.ExecutionContext.Implicits.global

/** Created by Alexey Afanasev on 25.02.16.
  */
@Config(sdk = Array(LOLLIPOP))
class ListableSpec extends FlatSpec with ScalaFutures with RobolectricSuite with PageableList {
  implicit val config = patienceConfig
  implicit val ctx: ContextWrapper = ContextWrapper(RuntimeEnvironment.application)

  case class User(name: String)
  def usersStream(n: Int = 1): Stream[User] = Stream.cons(User(s"user$n"), usersStream(n + 1))
  val users: Seq[User] = usersStream() take 50

  val listable: Listable[User, TextView] = Listable[User] {
    w[TextView]
  } { view ⇒ data ⇒
    view <~ text(data.name)
  }

  it should "work with adapters" in {
    def ui = w[ListView] <~ listable.listAdapterTweak(users)
    val eventualView: ListView = ui.run.futureValue

    assert(eventualView.getAdapter.getCount === 50)
    assert(eventualView.getAdapter.getItem(0).isInstanceOf[User])
    assert(eventualView.getAdapter.getItem(0).asInstanceOf[User].name == "user1")
  }

  it should "work with pageable adapter" in {
    def loadMore(position: Long)(implicit ec: ExecutionContext): Future[(Seq[User], Long)] = Future {
      println(s"loadMore $position")
      val p: Int = position.toInt
      (users.slice(p, p + 20), users.length.toLong)
    }

    def ui: Ui[ListView] = w[ListView] <~ pagingAdapterTweak(loadMore, listable)
    val eventualView: ListView = ui.run.futureValue
    val shadowListView: ShadowListView = Shadows.shadowOf(eventualView)

    assert(eventualView.getAdapter.getCount === 20)
    shadowListView.getOnScrollListener.onScroll(eventualView, 10, 10, 20)

  }

}
