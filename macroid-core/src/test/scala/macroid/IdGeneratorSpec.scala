package macroid

import org.scalatest.FlatSpec
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class IdGeneratorSpec extends FlatSpec with ScalaFutures {
  behavior of "id generator"

  it should "generate the same id for the same identifier" in {
    object Id extends IdGenerator(1000)

    assert(Id.button == Id.button)
  }

  it should "generate different ids for different identifiers" in {
    object Id extends IdGenerator(1000)

    assert(Id.button != Id.helicopter)
  }

  it should "be thread-safe" in {
    object Id extends IdGenerator(1000)

    val twoIds = List(Future(Id.button), Future(Id.button))
    whenReady(Future.sequence(twoIds)) {
      case List(id1, id2) ⇒ assert(id1 == id2)
    }
  }
}
