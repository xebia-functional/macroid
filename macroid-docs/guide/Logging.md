# Logging

*Macroid* provides logging string interpolators, which can be imported either
via `import macroid.FullDsl._` or via `import macroid.Logging._`. They are all of the form `logX`,
where `X` can be `V`, `I`, `D`, `W`, `E` or `Wtf`. Here is an example:

```scala
// log with Verbose level
// and empty log tag
val message = "bar"
logV"foo, $message"()
```

You can also provide a log tag explicitly:

```scala
logV"foo, $message"("TAG")
```

Or implicitly:

```scala
implicit val logTag = LogTag("TAG")
logV"foo, $message"()
```

Finally, the tag can be derived from the class name:

```scala
import macroid.AutoLogTag

class MyActivity extends ... with AutoLogTag {
  // there is now an implicit LogTag("MyActivity")
}
```