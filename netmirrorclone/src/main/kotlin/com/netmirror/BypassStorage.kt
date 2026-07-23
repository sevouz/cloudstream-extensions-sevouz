package com.netmirror

import android.content.Context
import android.content.SharedPreferences

object BypassStorage {
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        try {
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

            // Load NewTV OTP / cf_clearance / usertokens (valid 12h)
            val now = System.currentTimeMillis()
            val otpTs = prefs?.getLong("newtv_otp_ts", 0L) ?: 0L
            if (now - otpTs < 43_200_000) {
                NewTvStore.otp = prefs?.getString("newtv_otp", "") ?: ""
            }
            val cfTs = prefs?.getLong("cf_clearance_ts", 0L) ?: 0L
            if (now - cfTs < 43_200_000) {
                cachedCfClearance = prefs?.getString("cf_clearance", "") ?: ""
            }
            val tokTs = prefs?.getLong("newtv_tokens_ts", 0L) ?: 0L
            if (now - tokTs < 43_200_000) {
                val raw = prefs?.getString("newtv_tokens", "") ?: ""
                raw.split("|").forEach { entry ->
                    val parts = entry.split("::")
                    if (parts.size == 2 && parts[0].isNotEmpty() && parts[1].isNotEmpty()) {
                        NewTvStore.tokens[parts[0]] = parts[1] to tokTs
                    }
                }
            }
        } catch (_: Throwable) {}
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

    fun saveOtp(otp: String) {
        prefs?.edit()?.apply {
            putString("newtv_otp", otp)
            putLong("newtv_otp_ts", System.currentTimeMillis())
            apply()
        }
    }

    fun saveCfClearance(cf: String) {
        prefs?.edit()?.apply {
            putString("cf_clearance", cf)
            putLong("cf_clearance_ts", System.currentTimeMillis())
            apply()
        }
    }

    fun saveTokens(tokens: Map<String, Pair<String, Long>>) {
        val serialized = tokens.entries.joinToString("|") { "${it.key}::${it.value.first}" }
        prefs?.edit()?.apply {
            putString("newtv_tokens", serialized)
            putLong("newtv_tokens_ts", System.currentTimeMillis())
            apply()
        }
    }
}
