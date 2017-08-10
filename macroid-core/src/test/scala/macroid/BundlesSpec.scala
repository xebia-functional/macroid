package macroid

import org.scalatest.{Matchers, FlatSpec, RobolectricSuite}
import macroid.FullDsl._
import android.os.Bundle

class BundlesSpec extends FlatSpec with RobolectricSuite with Matchers {

  "Bundles" should "allow two bundles to be combined" in {
    val result = bundle("a" → 1) + bundle("b" → 2)
    result should ===(bundle("a" → 1, "b" → 2))
  }

}
