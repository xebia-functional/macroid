package macroid.extras

import android.webkit.{WebView, WebViewClient}
import macroid.Tweak

object WebViewTweaks {
  type W = WebView

  def wvLoadUrl(url: String): Tweak[W] = Tweak[W](_.loadUrl(url))

  def wvClient(webViewClient: WebViewClient): Tweak[W] =
    Tweak[W](_.setWebViewClient(webViewClient))

}
