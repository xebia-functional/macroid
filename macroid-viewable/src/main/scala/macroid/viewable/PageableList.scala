package macroid.viewable

import android.view.View
import android.widget.AbsListView.OnScrollListener
import android.widget.AdapterView.OnItemClickListener
import android.widget.{ AbsListView, AdapterView }
import macroid.{ ContextWrapper, Tweak }

import scala.concurrent.{ ExecutionContext, Future }

/** Created by Alexey Afanasev on 04.03.16.
  */
trait PageableList {

  /** Adapter tweak for infinite list that automatically fetches additional data when scrolled down to the bottom
    *
    * @param loadMoreItems  input data accessor that takes offset position and returns future with result and total items count available
    * @param listable is about creating layout from data
    * @param dataLoaded callback to handle empty tale
    * @tparam A data type param
    * @return
    */
  def pagingAdapterTweak[A](loadMoreItems: Long ⇒ Future[(Seq[A], Long)], listable: Listable[A, _], dataLoaded: Long ⇒ Unit = _ ⇒ ())(implicit cw: ContextWrapper, ec: ExecutionContext): Future[Tweak[AbsListView]] = {
    loadMoreItems(0) map {
      case (firstPage: Seq[A], total: Long) ⇒
        listable.listAdapterTweak(firstPage) + Tweak[AbsListView](l ⇒ {
          var isLoading = false
          var totalOutThere = total

          dataLoaded(total)

          def load(a: ListableListAdapter[A, _], lastVisibleItem: Int): Unit = {
            isLoading = true
            loadMoreItems(lastVisibleItem) map {
              case (nextPage: Seq[A], total: Long) ⇒
                isLoading = false
                totalOutThere = total
                a.addAll(nextPage: _*)
            }
          }

          l.setOnScrollListener(new OnScrollListener {
            override def onScrollStateChanged(absListView: AbsListView, i: Int): Unit = {
            }

            override def onScroll(absListView: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalLoadedItemCount: Int): Unit = {
              val lastVisibleItem = firstVisibleItem + visibleItemCount
              absListView.getAdapter match {
                case a: ListableListAdapter[A, _] if lastVisibleItem == totalLoadedItemCount && !isLoading && totalOutThere > totalLoadedItemCount ⇒
                  load(a, lastVisibleItem)
                case _ ⇒
              }
            }
          })
        })
    }
  }

  def adapterOnClick[A](onclick: A ⇒ Any)(implicit cw: ContextWrapper) = {
    Tweak[AbsListView](l ⇒ {
      l.setOnItemClickListener(new OnItemClickListener {
        override def onItemClick(adapterView: AdapterView[_], view: View, i: Int, l: Long): Unit = {
          adapterView.getAdapter match {
            case a: ListableListAdapter[A, _] ⇒ onclick(a.getItem(i))
            case _ ⇒
          }
        }
      })
    })
  }
}
