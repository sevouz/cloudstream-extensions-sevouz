package com.netmirror

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.lagradost.cloudstream3.CommonActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

const val NETMIRROR_TV_URL = "https://netmirror.gg/tv"

// Cached cf_clearance cookie
@Volatile var cachedCfClearance: String = ""

data class CfResult(val html: String, val cfClearance: String)

fun cfClearanceFrom(url: String): String {
    return try {
        val cookies = CookieManager.getInstance().getCookie(url) ?: ""
        Regex("""cf_clearance=([^;]+)""").find(cookies)?.groupValues?.get(1) ?: ""
    } catch (_: Exception) {
        ""
    }
}

/**
 * Opens an interactive WebView (in an AlertDialog) so the user can solve the Cloudflare
 * challenge on netmirror.gg/tv. Returns the cf_clearance cookie once solved.
 * On TV, shows a movable cursor controlled by the D-pad.
 */
@SuppressLint("SetJavaScriptEnabled")
suspend fun solveCloudflareInWebView(url: String = NETMIRROR_TV_URL): CfResult? {
    val activity = CommonActivity.activity ?: return null

    return withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { cont ->
            var resolved = false

            fun finish(result: CfResult?) {
                if (resolved) return
                resolved = true
                if (cont.isActive) cont.resume(result)
            }

            try {
                val cookieManager = CookieManager.getInstance()
                cookieManager.setAcceptCookie(true)

                val wv = WebView(activity)
                cookieManager.setAcceptThirdPartyCookies(wv, true)
                wv.settings.javaScriptEnabled = true
                wv.settings.domStorageEnabled = true
                @Suppress("DEPRECATION")
                wv.settings.mixedContentMode = 0
                wv.settings.userAgentString =
                    "Mozilla/5.0 (Linux; Android 13; Pixel 5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
                wv.settings.mediaPlaybackRequiresUserGesture = false
                wv.webChromeClient = WebChromeClient()

                fun extractAndFinish(dialog: Dialog?) {
                    if (resolved) return
                    // Read the page HTML from the WebView itself (real browser context).
                    wv.evaluateJavascript(
                        "(function(){return document.documentElement.outerHTML;})();"
                    ) { raw ->
                        if (resolved || raw == null) return@evaluateJavascript
                        val html = raw
                            .replace("\\u003C", "<")
                            .replace("\\u003E", ">")
                            .replace("\\\"", "\"")
                            .replace("\\n", "\n")
                            .replace("\\/", "/")
                        val isChallenge = html.contains("Just a moment", true) ||
                            html.contains("Checking if the site connection is secure", true) ||
                            html.contains("cf-browser-verification", true)
                        val hasOtp = Regex("""const\s+otp\s*=""").containsMatchIn(html)
                        val cf = cfClearanceFrom(url)
                        // Finish only when the real /tv page loaded (has const otp), past the challenge
                        if (hasOtp || (cf.isNotEmpty() && !isChallenge && html.length > 2000)) {
                            if (cf.isNotEmpty()) cachedCfClearance = cf
                            try { wv.destroy() } catch (_: Exception) {}
                            try { dialog?.dismiss() } catch (_: Exception) {}
                            finish(CfResult(html, cf))
                        }
                    }
                }

                val dp = activity.resources.displayMetrics.density
                val wm = activity.getSystemService(android.content.Context.WINDOW_SERVICE) as WindowManager
                val metrics = Point()
                @Suppress("DEPRECATION")
                wm.defaultDisplay.getSize(metrics)

                val wrapper = LinearLayout(activity).apply {
                    orientation = LinearLayout.VERTICAL
                    minimumWidth = (metrics.x * 0.95f).toInt()
                    minimumHeight = (metrics.y * 0.9f).toInt()
                }

                val info = TextView(activity).apply {
                    text = "🔐 Solve the Cloudflare captcha — use D-pad to move cursor, OK to click."
                    setTextColor(Color.WHITE)
                    setBackgroundColor(Color.parseColor("#1A1A2E"))
                    textSize = 13f
                    val p = (10 * dp).toInt()
                    setPadding(p, p, p, p)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                }

                // TV cursor
                val isTv = try {
                    com.lagradost.cloudstream3.ui.settings.Globals.isLayout(2)
                } catch (_: Throwable) { false }

                val cursorSize = (22 * dp).toInt()
                val cursorView: View? = if (isTv) {
                    View(activity).apply {
                        layoutParams = FrameLayout.LayoutParams(cursorSize, cursorSize)
                        background = GradientDrawable().apply {
                            shape = GradientDrawable.OVAL
                            setColor(Color.argb(160, 255, 50, 50))
                            setStroke((2 * dp).toInt(), Color.WHITE)
                        }
                        elevation = 999f
                    }
                } else null

                val container = FrameLayout(activity).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    ).apply { weight = 1f }
                    isFocusable = true
                    isFocusableInTouchMode = true
                }
                wv.layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                container.addView(wv)

                var cursorX = (metrics.x * 0.95f) / 2f
                var cursorY = (metrics.y * 0.9f) / 2f

                if (cursorView != null) {
                    container.addView(cursorView)
                    container.viewTreeObserver.addOnGlobalLayoutListener(object :
                        ViewTreeObserver.OnGlobalLayoutListener {
                        override fun onGlobalLayout() {
                            container.viewTreeObserver.removeOnGlobalLayoutListener(this)
                            cursorX = container.width / 2f
                            cursorY = container.height / 2f
                            cursorView.translationX = cursorX - cursorSize / 2f
                            cursorView.translationY = cursorY - cursorSize / 2f
                        }
                    })
                    container.setOnKeyListener { _, keyCode, event ->
                        if (event.action != KeyEvent.ACTION_DOWN) return@setOnKeyListener false
                        val step = dp * 10f
                        fun move(dx: Float, dy: Float) {
                            cursorX = (cursorX + dx).coerceIn(0f, container.width.toFloat())
                            cursorY = (cursorY + dy).coerceIn(0f, container.height.toFloat())
                            cursorView.translationX = cursorX - cursorSize / 2f
                            cursorView.translationY = cursorY - cursorSize / 2f
                        }
                        when (keyCode) {
                            KeyEvent.KEYCODE_DPAD_UP -> { move(0f, -step); true }
                            KeyEvent.KEYCODE_DPAD_DOWN -> { move(0f, step); true }
                            KeyEvent.KEYCODE_DPAD_LEFT -> { move(-step, 0f); true }
                            KeyEvent.KEYCODE_DPAD_RIGHT -> { move(step, 0f); true }
                            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                                val t = SystemClock.uptimeMillis()
                                val down = MotionEvent.obtain(t, t, MotionEvent.ACTION_DOWN, cursorX, cursorY, 0)
                                val up = MotionEvent.obtain(t, t + 120, MotionEvent.ACTION_UP, cursorX, cursorY, 0)
                                wv.dispatchTouchEvent(down)
                                wv.dispatchTouchEvent(up)
                                down.recycle(); up.recycle()
                                true
                            }
                            else -> false
                        }
                    }
                }

                wrapper.addView(info)
                wrapper.addView(container)

                val dialog = AlertDialog.Builder(activity)
                    .setView(wrapper)
                    .setCancelable(false)
                    .create()
                dialog.window?.apply {
                    setBackgroundDrawable(ColorDrawable(0))
                    setLayout((metrics.x * 0.95f).toInt(), (metrics.y * 0.9f).toInt())
                }

                wv.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, pageUrl: String?) {
                        super.onPageFinished(view, pageUrl)
                        extractAndFinish(dialog)
                        if (!resolved) {
                            val handler = Handler(Looper.getMainLooper())
                            handler.postDelayed(object : Runnable {
                                override fun run() {
                                    if (!resolved) {
                                        extractAndFinish(dialog)
                                        if (!resolved) handler.postDelayed(this, 1000L)
                                    }
                                }
                            }, 1000L)
                        }
                    }
                }

                dialog.setOnDismissListener {
                    if (!resolved) {
                        try { wv.destroy() } catch (_: Exception) {}
                        finish(null)
                    }
                }

                // 120s timeout
                Handler(Looper.getMainLooper()).postDelayed({
                    if (!resolved) {
                        try { wv.destroy() } catch (_: Exception) {}
                        try { dialog.dismiss() } catch (_: Exception) {}
                        finish(null)
                    }
                }, 120_000L)

                dialog.show()
                wv.loadUrl(url)

                cont.invokeOnCancellation {
                    try { wv.destroy() } catch (_: Exception) {}
                    try { dialog.dismiss() } catch (_: Exception) {}
                }
            } catch (e: Exception) {
                finish(null)
            }
        }
    }
}
