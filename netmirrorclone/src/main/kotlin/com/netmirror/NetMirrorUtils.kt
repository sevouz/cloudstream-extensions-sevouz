package com.netmirror

import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.utils.AppUtils.tryParseJson
import okhttp3.FormBody
import okhttp3.Request
import java.util.Base64
import java.util.UUID

const val MAIN_URL = "https://net52.cc"

val BROWSER_HEADERS = mapOf(
    "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8",
    "Accept-Language" to "en-IN,en-US;q=0.9,en;q=0.8",
    "Cache-Control" to "max-age=0",
    "Connection" to "keep-alive",
    "sec-ch-ua" to "\"Not(A:Brand\";v=\"8\", \"Chromium\";v=\"144\", \"Android WebView\";v=\"144\"",
    "sec-ch-ua-mobile" to "?0",
    "sec-ch-ua-platform" to "\"Android\"",
    "Sec-Fetch-Dest" to "document",
    "Sec-Fetch-Mode" to "navigate",
    "Sec-Fetch-Site" to "same-origin",
    "Sec-Fetch-User" to "?1",
    "Upgrade-Insecure-Requests" to "1",
    "User-Agent" to "Mozilla/5.0 (Linux; Android 13; Pixel 5 Build/TQ3A.230901.001; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/144.0.7559.132 Safari/537.36 /OS.Gatu v3.0",
    "X-Requested-With" to "XMLHttpRequest"
)

private val NEWTV_HEADERS = mapOf(
    "Cache-Control" to "no-cache, no-store, must-revalidate",
    "Pragma" to "no-cache",
    "Expires" to "0",
    "X-Requested-With" to "NetmirrorNewTV v1.0",
    "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:136.0) Gecko/20100101 Firefox/136.0 /OS.GatuNewTV v1.0",
    "Accept" to "application/json, text/plain, */*"
)

private val NEWTV_DOMAINS = listOf(
    "aHR0cHM6Ly9tb2JpbGVkZXRlY3RzLmNvbQ==",
    "aHR0cHM6Ly9tb2JpbGVkZXRlY3QuYXBw",
    "aHR0cHM6Ly9tb2JpZGV0ZWN0LmFydA==",
    "aHR0cHM6Ly9tb2JpZGV0ZWN0LmNj",
    "aHR0cHM6Ly9tb2JpZGV0ZWN0LmNsaWNr",
    "aHR0cHM6Ly9tb2JpZGV0ZWN0Lmluaw==",
    "aHR0cHM6Ly9tb2JpZGV0ZWN0LmxpdmU=",
    "aHR0cHM6Ly9tb2JpZGV0ZWN0LnBybw==",
    "aHR0cHM6Ly9tb2JpZGV0ZWN0LnNob3A=",
    "aHR0cHM6Ly9tb2JpZGV0ZWN0LnNpdGU=",
    "aHR0cHM6Ly9tb2JpZGV0ZWN0LnNwYWNl",
    "aHR0cHM6Ly9tb2JpZGV0ZWN0LnN0b3Jl",
    "aHR0cHM6Ly9tb2JpZGV0ZWN0LnZpcA==",
    "aHR0cHM6Ly9tb2JpZGV0ZWN0Lndpa2k=",
    "aHR0cHM6Ly9tb2JpZGV0ZWN0Lnh5eg==",
    "aHR0cHM6Ly9tb2JpZGV0ZWN0cy5hcnQ=",
    "aHR0cHM6Ly9tb2JpZGV0ZWN0cy5jYw==",
    "aHR0cHM6Ly9tb2JpZGV0ZWN0cy5pbmZv",
    "aHR0cHM6Ly9tb2JpZGV0ZWN0cy5pbms=",
    "aHR0cHM6Ly9tb2JpZGV0ZWN0cy5saXZl",
    "aHR0cHM6Ly9tb2JpZGV0ZWN0cy5wcm8=",
    "aHR0cHM6Ly9tb2JpZGV0ZWN0cy5zdG9yZQ==",
    "aHR0cHM6Ly9tb2JpZGV0ZWN0cy50b3A=",
    "aHR0cHM6Ly9tb2JpZGV0ZWN0cy54eXo="
)

// Cached cookie and API URL
@Volatile private var cachedCookie: String = ""
@Volatile private var cachedCookieTime: Long = 0L
@Volatile private var resolvedApiUrl: String = ""

private fun b64(s: String): String = String(Base64.getDecoder().decode(s))

suspend fun getBypassCookie(): String {
    if (cachedCookie.isNotEmpty() && System.currentTimeMillis() - cachedCookieTime < 54_000_000) {
        return cachedCookie
    }
    val client = app.baseClient.newBuilder()
        .followRedirects(false)
        .followSslRedirects(false)
        .build()
    val formBody = FormBody.Builder()
        .add("g-recaptcha-response", UUID.randomUUID().toString())
        .build()
    val request = Request.Builder()
        .url("$MAIN_URL/verify.php")
        .post(formBody)
        .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36")
        .addHeader("Referer", "$MAIN_URL/verify2")
        .addHeader("Origin", MAIN_URL)
        .addHeader("Content-Type", "application/x-www-form-urlencoded")
        .build()
    val cookie = client.newCall(request).execute().use { response ->
        response.headers("Set-Cookie")
            .firstOrNull { it.startsWith("t_hash_t=") }
            ?.substringAfter("t_hash_t=")
            ?.substringBefore(";")
            .orEmpty()
    }
    if (cookie.isNotEmpty()) {
        cachedCookie = cookie
        cachedCookieTime = System.currentTimeMillis()
    }
    return cookie
}

suspend fun resolveNewTvApi(): String {
    if (resolvedApiUrl.isNotBlank()) return resolvedApiUrl
    for (encoded in NEWTV_DOMAINS) {
        val base = b64(encoded).trimEnd('/')
        try {
            val text = app.get("$base/checknewtv.php", headers = NEWTV_HEADERS).text
            val hash = tryParseJson<TokenResponse>(text)?.token_hash
            if (!hash.isNullOrBlank()) {
                resolvedApiUrl = b64(hash).trimEnd('/')
                return resolvedApiUrl
            }
        } catch (_: Exception) { }
    }
    throw Exception("Failed to resolve NewTV API")
}

suspend fun getVideoLink(id: String, ott: String): PlayerResponse? {
    val apiBase = resolveNewTvApi()
    val headers = NEWTV_HEADERS.toMutableMap().apply {
        put("Ott", ott)
        put("Usertoken", "")
    }
    val text = app.get("$apiBase/newtv/player.php?id=$id", headers = headers).text
    return tryParseJson<PlayerResponse>(text)
}

data class TokenResponse(val token_hash: String? = null)
data class PlayerResponse(val status: String? = null, val video_link: String? = null, val referer: String? = null)

// API data models
data class SearchResult(val id: String, val t: String)
data class SearchData(val head: String? = null, val searchResult: List<SearchResult> = emptyList(), val type: Int = 0)
data class Suggest(val id: String)
data class Season(val ep: String, val id: String, val s: String, val sele: String)
data class EpisodeItem(val complate: String? = null, val ep: String, val id: String, val s: String, val t: String, val time: String)
data class PostData(
    val desc: String? = null, val director: String? = null, val ua: String? = null,
    val episodes: List<EpisodeItem?> = emptyList(), val genre: String? = null,
    val nextPage: Int? = null, val nextPageSeason: String? = null, val nextPageShow: Int? = null,
    val season: List<Season>? = null, val title: String = "", val year: String = "",
    val cast: String? = null, val match: String? = null, val runtime: String? = null,
    val suggest: List<Suggest>? = null
)
data class EpisodesData(val episodes: List<EpisodeItem>? = null, val nextPage: Int = 0, val nextPageSeason: String = "", val nextPageShow: Int = 0)
