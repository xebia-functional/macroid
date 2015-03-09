# Contexts

Android Context is normally required to construct widgets, check screen orientation, etc.
However, one has to [differentiate between](http://stackoverflow.com/questions/987072/using-application-context-everywhere?rq=1)
two types of context: one obtained from an activity
and one obtained from the application. The reason is that holding on to the activity context
may cause [memory leaks](http://stackoverflow.com/questions/3346080/android-references-to-a-context-and-memory-leaks).

*Macroid* distinguishes between these two types of Context and passes them implicitly to prevent code bloat.

## What are they

* `macroid.AppContext` holds the application context;
* `macroid.ActivityContext` holds a weak reference to the activity context, so that it’s safe to store it.

## Including

To include the implicit contexts in your activity, inherit `Contexts`:

```scala
import macroid.Contexts

class MyActivity extends Activity with Contexts[Activity] {
  ...
}
```

If you use fragments from the support library
(see the [section on fragments](Fragments.html)):

```scala
class MyActivity extends FragmentActivity with Contexts[FragmentActivity] {
  ...
}
```

Finally, in a fragment (either `android.app.Fragment` or `android.support.v4.app.Fragment`):

```scala
class MyFragment extends Fragment with Contexts[Fragment] {
  ...
}
```

## Usage

Most *Macroid* APIs require one or two of these contexts. If you use them inside an 
activity or a fragment, you are golden. However sometimes you need to pass the contexts
to other methods:

```scala
import macroid.{ AppContext, ActivityContext }
import macroid.FullDsl._
import macroid.contrib._

def customTweak(implicit appCtx: AppContext) =
  // this one requires AppContext
  TextTweaks.large +
  text("foo")

def customLayout(implicit ctx: ActivityContext) =
  // layout bricks require ActivityContext
  l[LinearLayout](
    w[TextView],
    w[Button]
  )
```
