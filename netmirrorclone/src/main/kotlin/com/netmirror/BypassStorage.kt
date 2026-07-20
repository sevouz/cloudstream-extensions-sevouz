package com.netmirror

import android.content.Context
import android.content.SharedPreferences

object BypassStorage {
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.getSharedPreferences("NetMirrorBypassPrefs", Context.MODE_PRIVATE)
        // Load cached bypass on init
        val cookie = prefs?.getString("cookie", "") ?: ""
        val addhash = prefs?.getString("addhash", "") ?: ""
        val usertoken = prefs?.getString("usertoken", "") ?: ""
        val dataTime = prefs?.getString("dataTime", "") ?: ""
        val timestamp = prefs?.getLong("timestamp", 0L) ?: 0L
        if (cookie.isNotEmpty() && System.currentTimeMillis() - timestamp < 86_400_000) {
            cachedBypass = BypassResult(cookie, addhash, usertoken, dataTime)
            cachedBypassTime = timestamp
        }
    }

    fun save(result: BypassResult) {
        prefs?.edit()?.apply {
            putString("cookie", result.cookie)
            putString("addhash", result.addhash)
            putString("usertoken", result.usertoken)
            putString("dataTime", result.dataTime)
            putLong("timestamp", System.currentTimeMillis())
            apply()
        }
    }
}
