package com.netmirror

import android.util.Base64
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.utils.AppUtils.tryParseJson
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.UUID

// Ordered list of known NetMirror domains — tried in order until one works
private val NETMIRROR_DOMAINS = listOf(
    "https://net52.cc",
    "https://net27.cc",
    "https://net22.cc"
)

@Volatile var MAIN_URL = "https://net52.cc"
    private set

@Volatile private var domainResolved = false
private val domainMutex = Mutex()

suspend fun resolveMainUrl(): String {
    if (domainResolved) return MAIN_URL

    return domainMutex.withLock {
        if (domainResolved) return@withLock MAIN_URL

        for (domain in NETMIRROR_DOMAINS) {
            try {
                val resp = app.get(
                    "$domain/mobile/home?app=1",
                    headers = BROWSER_HEADERS
                )
                if (resp.code in 200..399) {
                    MAIN_URL = domain
                    domainResolved = true
                    return@withLock domain
                }
            } catch (_: Exception) {}
        }
        // Fallback to first domain if none respond
        MAIN_URL = NETMIRROR_DOMAINS.first()
        domainResolved = true
        MAIN_URL
    }
}

val BROWSER_HEADERS = mapOf(
    "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8",
    "Accept-Language" to "en-IN,en-US;q=0.9,en;q=0.8",
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

data class BypassResult(val cookie: String, val addhash: String, val usertoken: String, val dataTime: String)

@Volatile var cachedBypass: BypassResult? = null
@Volatile var cachedBypassTime: Long = 0L

private val bypassMutex = Mutex()

suspend fun ensureBypass(): BypassResult {
    // Resolve working domain first
    resolveMainUrl()

    val cached = cachedBypass
    if (cached != null && cached.cookie.isNotEmpty() && System.currentTimeMillis() - cachedBypassTime < 86_400_000) {
        return cached
    }

    // Use mutex to prevent multiple concurrent bypass attempts
    return bypassMutex.withLock {
        // Double-check after acquiring lock (another coroutine may have completed it)
        val rechecked = cachedBypass
        if (rechecked != null && rechecked.cookie.isNotEmpty() && System.currentTimeMillis() - cachedBypassTime < 86_400_000) {
            return@withLock rechecked
        }
        doBypass()
    }
}

/** Invalidate cached bypass and resolved domain — call when requests start failing */
fun invalidateCache() {
    cachedBypass = null
    cachedBypassTime = 0L
    domainResolved = false
    resolvedApiUrl = ""
}

private suspend fun doBypass(): BypassResult {

    // Step 1: GET homepage to get cookie and check for ad wall
    val homeResp = app.get(
        "$MAIN_URL/mobile/home?app=1",
        headers = BROWSER_HEADERS,
        referer = "$MAIN_URL/mobile/home?app=1"
    )
    var cookie = ""
    homeResp.okhttpResponse.headers("Set-Cookie").forEach { h ->
        if (h.contains("t_hash_t=")) {
            cookie = h.substringAfter("t_hash_t=").substringBefore(";")
        }
    }
    if (cookie.isEmpty()) {
        cookie = homeResp.cookies["t_hash_t"] ?: ""
    }

    // Also try verify.php if homepage didn't give cookie
    if (cookie.isEmpty()) {
        try {
            val client = OkHttpClient.Builder()
                .followRedirects(false)
                .followSslRedirects(false)
                .build()
            val formBody = FormBody.Builder()
                .add("g-recaptcha-response", UUID.randomUUID().toString())
                .build()
            val request = Request.Builder()
                .url("$MAIN_URL/verify.php")
                .post(formBody)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/147.0.0.0 Safari/537.36")
                .header("Referer", "$MAIN_URL/verify2")
                .header("Origin", MAIN_URL)
                .build()
            val response = client.newCall(request).execute()
            response.headers("Set-Cookie").forEach { h ->
                if (h.contains("t_hash_t=")) {
                    cookie = h.substringAfter("t_hash_t=").substringBefore(";")
                }
            }
            response.close()
        } catch (_: Exception) {}
    }

    if (cookie.isEmpty()) return BypassResult("", "", "", "")

    val doc = homeResp.document
    val html = doc.html()

    // Check if there's NO ad wall
    if (!html.contains("We Need Support") || !html.contains("open-support")) {
        val dataTime = doc.selectFirst("body")?.attr("data-time") ?: ""
        val result = BypassResult(cookie, "", "", dataTime)
        cachedBypass = result
        cachedBypassTime = System.currentTimeMillis()
        BypassStorage.save(result)
        return result
    }

    // Ad wall exists - extract addhash
    val addhash = doc.selectFirst("body")?.attr("data-addhash") ?: ""
    val dataTime = doc.selectFirst("body")?.attr("data-time") ?: ""

    if (addhash.isBlank()) {
        val result = BypassResult(cookie, "", "", dataTime)
        cachedBypass = result
        cachedBypassTime = System.currentTimeMillis()
        BypassStorage.save(result)
        return result
    }

    // Extract Qury and Vsite2 from page JavaScript
    val qury = Regex("""Qury\s*=\s*"([^"]+)"""").find(html)?.groupValues?.get(1) ?: "ffr455"
    val vsite = Regex("""Vsite2\s*=\s*"([^"]+)"""").find(html)?.groupValues?.get(1) ?: "userver"

    // Simulate ad click
    val mainDomain = MAIN_URL.substringAfter("://") // e.g. "net52.cc"
    val adClickUrl = "https://$vsite.$mainDomain/?$qury=$addhash&a=y&t=${Math.random()}"
    try {
        app.get(adClickUrl, headers = BROWSER_HEADERS, referer = "$MAIN_URL/mobile/home?app=1")
    } catch (_: Exception) {}

    // Wait for ad to "complete" — 10 seconds (minimum needed for server to accept)
    kotlinx.coroutines.delay(10000)

    // Step 4: POST to verify2.php with addhash to confirm ad was watched
    // Retry up to 6 times with 1.5-second delays (was 10x2s = max 20s, now 6x1.5s = max 9s)
    var usertoken = ""
    var finalCookie = cookie
    for (attempt in 1..6) {
        kotlinx.coroutines.delay(1500)
        try {
            val verifyResp = app.post(
                "$MAIN_URL/mobile/verify2.php",
                data = mapOf("verify" to addhash),
                headers = BROWSER_HEADERS,
                referer = "$MAIN_URL/mobile/home?app=1"
            )
            // Extract updated cookie
            val newCookie = verifyResp.cookies["t_hash_t"]
            if (!newCookie.isNullOrBlank()) finalCookie = newCookie

            val body = verifyResp.text
            val json = tryParseJson<Map<String, String>>(body)
            if (json != null) {
                val status = json["statusup"] ?: ""
                if (status.equals("All Done", ignoreCase = true)) {
                    // Extract usertoken
                    usertoken = json["usertoken"] ?: json["token"] ?: json["utoken"] ?: json["user_token"] ?: ""
                    break
                }
            }
        } catch (_: Exception) {}
    }

    val result = BypassResult(finalCookie, addhash, usertoken, dataTime)
    cachedBypass = result
    cachedBypassTime = System.currentTimeMillis()
    BypassStorage.save(result)
    return result
}

suspend fun getPlaylistLink(id: String, ott: String, playlistPath: String): PlaylistResult? {
    val bypass = ensureBypass()
    if (bypass.cookie.isEmpty()) return null

    val cookies = mutableMapOf(
        "t_hash_t" to bypass.cookie,
        "hd" to "on",
        "ott" to ott
    )
    if (bypass.addhash.isNotEmpty()) cookies["addhash"] = bypass.addhash
    if (bypass.usertoken.isNotEmpty()) cookies["usertoken"] = bypass.usertoken

    // Try current domain first, then retry with fresh bypass if it fails
    for (attempt in 1..2) {
        val baseUrl = MAIN_URL
        val response = try {
            app.get(
                "$baseUrl/mobile/$playlistPath?id=$id",
                headers = BROWSER_HEADERS,
                referer = "$baseUrl/home",
                cookies = cookies
            ).text
        } catch (e: Exception) {
            if (attempt == 1) {
                // First failure — invalidate and retry
                invalidateCache()
                resolveMainUrl()
                val newBypass = ensureBypass()
                if (newBypass.cookie.isEmpty()) return null
                cookies["t_hash_t"] = newBypass.cookie
                if (newBypass.addhash.isNotEmpty()) cookies["addhash"] = newBypass.addhash
                if (newBypass.usertoken.isNotEmpty()) cookies["usertoken"] = newBypass.usertoken
                continue
            }
            return null
        }

        // Try JSON array format: [{"sources":[...],"tracks":[...]}]
        try {
            val playlist = tryParseJson<List<PlayListItem>>(response)
            val item = playlist?.firstOrNull()
            if (item != null && !item.sources.isNullOrEmpty()) {
                return PlaylistResult(item.sources, item.tracks)
            }
        } catch (_: Exception) {}

        // Try single object
        try {
            val item = tryParseJson<PlayListItem>(response)
            if (item != null && !item.sources.isNullOrEmpty()) {
                return PlaylistResult(item.sources, item.tracks)
            }
        } catch (_: Exception) {}

        // Regex fallback for m3u8
        val m3u8 = Regex("""(/mobile/hls/[^\s"']+\.m3u8[^\s"']*)""").find(response)?.groupValues?.get(1)
        if (!m3u8.isNullOrBlank()) {
            return PlaylistResult(listOf(Source("$baseUrl$m3u8", "Auto", "m3u8")), null)
        }

        val fullUrl = Regex("""(https?://[^\s"'<>\}\]\\]+\.m3u8[^\s"'<>\}\]\\]*)""").find(response)?.groupValues?.get(1)
        if (!fullUrl.isNullOrBlank()) {
            return PlaylistResult(listOf(Source(fullUrl, "Auto", "m3u8")), null)
        }

        // If response was empty/invalid on first attempt, retry with fresh bypass
        if (attempt == 1 && (response.isBlank() || response.contains("verify") || response.contains("blocked"))) {
            invalidateCache()
            resolveMainUrl()
            val newBypass = ensureBypass()
            if (newBypass.cookie.isEmpty()) return null
            cookies["t_hash_t"] = newBypass.cookie
            if (newBypass.addhash.isNotEmpty()) cookies["addhash"] = newBypass.addhash
            if (newBypass.usertoken.isNotEmpty()) cookies["usertoken"] = newBypass.usertoken
            continue
        }

        break
    }

    return null
}

data class PlaylistResult(val sources: List<Source>, val tracks: List<Tracks>?)

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

private fun b64(s: String): String = String(Base64.decode(s, Base64.DEFAULT))

@Volatile private var resolvedApiUrl: String = ""

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
        } catch (_: Exception) {}
    }
    return ""
}

suspend fun getNewTvLink(id: String, ott: String): String? {
    // Try up to 2 times — once with cached API URL, once after re-resolving
    for (attempt in 1..2) {
        val apiBase = if (attempt == 1) resolveNewTvApi() else {
            resolvedApiUrl = "" // Clear cache to force re-resolution
            resolveNewTvApi()
        }
        if (apiBase.isEmpty()) continue

        val headers = NEWTV_HEADERS.toMutableMap().apply {
            put("Ott", ott)
            put("Usertoken", "")
        }
        try {
            val text = app.get("$apiBase/newtv/player.php?id=$id", headers = headers).text
            val response = tryParseJson<PlayerResponse>(text)
            if (!response?.video_link.isNullOrBlank()) {
                return response!!.video_link
            }
        } catch (_: Exception) {
            if (attempt == 1) continue // Retry with fresh resolution
        }
    }
    return null
}

data class TokenResponse(val token_hash: String? = null)
data class PlayerResponse(val status: String? = null, val video_link: String? = null, val referer: String? = null)
data class PlayListItem(val sources: List<Source>? = null, val tracks: List<Tracks>? = null, val title: String? = null, val image: String? = null)
data class Source(val file: String? = null, val label: String? = null, val type: String? = null)
data class Tracks(val file: String? = null, val kind: String? = null, val label: String? = null)

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
