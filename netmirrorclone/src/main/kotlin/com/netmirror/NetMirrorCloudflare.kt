package com.netmirror

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.TextView
import com.lagradost.api.Log
import com.lagradost.cloudstream3.CommonActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

const val NETMIRROR_TV_URL = "https://netmirror.gg/tv"

data class CfResult(val html: String, val cfClearance: String)

// Cache the cf_clearance + otp for reuse
@Volatile var cachedCfClearance: String = ""
@Volatile var cachedOtp: String = ""
@Volatile var cachedCfTime: Long = 0L

fun getCfClearanceFromManager(url: String): String {
    return try {
        val cookie = CookieManager.getInstance().getCookie(url) ?: return ""
        Regex("""cf_clearance=([^;]+)""").find(cookie)?.groupValues?.get(1) ?: ""
    } catch (_: Exception) {
        ""
    }
}

/**
 * Opens an interactive WebView so the user can solve the Cloudflare challenge on netmirror.gg/tv.
 * Captures the cf_clearance cookie and the resulting page HTML (which contains the OTP).
 */
@SuppressLint("SetJavaScriptEnabled")
suspend fun solveCloudflareInWebView(timeoutMs: Long = 120_000L): CfResult? {
    val activity = CommonActivity.activity ?: return null

    return withTimeoutOrNull(timeoutMs) {
        suspendCancellableCoroutine { cont ->
            activity.runOnUiThread {
                var webView: WebView? = null
                var overlay: FrameLayout? = null
                var resumed = false

                fun finish(result: CfResult?) {
                    if (resumed) return
                    resumed = true
                    try {
                        webView?.stopLoading()
                        overlay?.let { (it.parent as? ViewGroup)?.removeView(it) }
                        webView?.destroy()
                    } catch (_: Exception) {}
                    if (cont.isActive) cont.resume(result)
                }

                try {
                    val root = activity.window.decorView as? ViewGroup

                    overlay = FrameLayout(activity).apply {
                        setBackgroundColor(Color.BLACK)
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }

                    val infoBar = TextView(activity).apply {
                        text = "🔐 Solve the Cloudflare captcha to verify you are human"
                        setTextColor(Color.WHITE)
                        setBackgroundColor(Color.parseColor("#222244"))
                        setPadding(24, 24, 24, 24)
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).apply { gravity = Gravity.TOP }
                    }

                    webView = WebView(activity).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        ).apply { topMargin = 120 }
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.databaseEnabled = true
                        settings.userAgentString =
                            "Mozilla/5.0 (Linux; Android 13; Pixel 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"

                        CookieManager.getInstance().setAcceptCookie(true)
                        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                val clearance = getCfClearanceFromManager(NETMIRROR_TV_URL)
                                if (clearance.isNotEmpty()) {
                                    // Challenge passed — extract page HTML
                                    view?.evaluateJavascript(
                                        "(function(){return document.documentElement.outerHTML;})();"
                                    ) { rawHtml ->
                                        val html = rawHtml
                                            ?.replace("\\u003C", "<")
                                            ?.replace("\\\"", "\"")
                                            ?.replace("\\n", "\n")
                                            ?.replace("\\/", "/")
                                            ?: ""
                                        // Only finish if the page is no longer a CF challenge
                                        if (!html.contains("Just a moment", true) &&
                                            !html.contains("Checking your browser", true) &&
                                            html.length > 500
                                        ) {
                                            finish(CfResult(html, clearance))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    overlay!!.addView(webView)
                    overlay!!.addView(infoBar)
                    root?.addView(overlay)

                    webView!!.loadUrl(NETMIRROR_TV_URL)
                } catch (e: Exception) {
                    Log.e("NetMirrorCF", "WebView error: ${e.message}")
                    finish(null)
                }

                cont.invokeOnCancellation { finish(null) }
            }
        }
    }
}

/** Extract the OTP token from the netmirror.gg/tv page HTML */
fun extractOtp(html: String): String {
    // const otp = [ ... ]  or  const otp = "..."
    Regex("""const\s+otp\s*=\s*\[([^\]]*)]""").find(html)?.let { m ->
        val inner = m.groupValues[1]
        val digits = Regex("""\d+""").findAll(inner).map { it.value }.joinToString("")
        if (digits.isNotEmpty()) return digits
    }
    Regex("""const\s+otp\s*=\s*["']([^"']+)["']""").find(html)?.let { m ->
        return m.groupValues[1]
    }
    Regex("""["']?otp["']?\s*[:=]\s*["']?(\d{4,})["']?""").find(html)?.let { m ->
        return m.groupValues[1]
    }
    return ""
}

/**
 * Ensures we have a valid cf_clearance + OTP. Solves Cloudflare via WebView if needed.
 * Cached for 12 hours.
 */
suspend fun ensureCfAndOtp(): Pair<String, String>? {
    val now = System.currentTimeMillis()
    if (cachedCfClearance.isNotEmpty() && cachedOtp.isNotEmpty() && now - cachedCfTime < 43_200_000) {
        return cachedCfClearance to cachedOtp
    }

    val result = solveCloudflareInWebView() ?: return null
    val otp = extractOtp(result.html)
    cachedCfClearance = result.cfClearance
    cachedOtp = otp
    cachedCfTime = System.currentTimeMillis()
    return result.cfClearance to otp
}
