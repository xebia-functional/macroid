package macroid

import org.scalatest.FlatSpec
import macroid.FullDsl._

class FragmentApiSpec extends FlatSpec {
  "FragmentApi" should "work with legacy fragments in activity" in {
    import android.support.v4.app.{ FragmentActivity, Fragment }
    def foo = {
      class MyActivity extends FragmentActivity with Contexts[FragmentActivity] {
        f[Fragment].framed(1, "1")
        this.findFrag[Fragment]("1")
      }
    }
  }

  it should "work with modern fragments in activity" in {
    import android.app.{ Activity, Fragment }
    def foo = {
      class MyActivity extends Activity with Contexts[Activity] {
        f[Fragment].framed(1, "1")
        this.findFrag[Fragment]("1")
      }
    }
  }

  it should "work with legacy fragments in fragment" in {
    import android.support.v4.app.Fragment
    def foo = {
      class MyFragment extends Fragment with Contexts[Fragment] {
        f[Fragment].framed(1, "1")
        this.findFrag[Fragment]("1")
      }
    }
  }

  it should "work with modern fragments in fragment" in {
    import android.app.Fragment
    def foo = {
      class MyFragment extends Fragment with Contexts[Fragment] {
        f[Fragment].framed(1, "1")
        this.findFrag[Fragment]("1")
      }
    }
  }
}
