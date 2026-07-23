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

// Primary domain for API calls — behind Cloudflare, triggers verification
@Volatile var MAIN_URL = "https://netmirror.gg"
    private set

// Cloudflare-protected entry point (used for cf_clearance cookie)
const val CF_VERIFY_URL = "https://netmirror.gg"

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

/** Invalidate cached bypass — call when requests start failing */
fun invalidateCache() {
    cachedBypass = null
    cachedBypassTime = 0L
    resolvedApiUrl = ""
}

// Hardcoded user token — this tells the server "ad was watched"
private const val USER_TOKEN = "233123f803cf02184bf6c67e149cdd50"

private suspend fun doBypass(): BypassResult {
    // Simple bypass: POST to verify.php to get t_hash_t cookie
    var cookie = ""
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
            .header("sec-ch-ua", "\"Google Chrome\";v=\"147\", \"Not.A/Brand\";v=\"8\", \"Chromium\";v=\"147\"")
            .header("sec-ch-ua-mobile", "?0")
            .header("sec-ch-ua-platform", "\"Windows\"")
            .header("Sec-Fetch-Dest", "document")
            .header("Sec-Fetch-Mode", "navigate")
            .header("Sec-Fetch-Site", "same-origin")
            .header("Sec-Fetch-User", "?1")
            .header("Upgrade-Insecure-Requests", "1")
            .build()
        val response = client.newCall(request).execute()
        response.headers("Set-Cookie").forEach { h ->
            if (h.contains("t_hash_t=")) {
                cookie = h.substringAfter("t_hash_t=").substringBefore(";")
            }
        }
        response.close()
    } catch (_: Exception) {}

    // Fallback: try GET homepage for cookie
    if (cookie.isEmpty()) {
        try {
            val homeResp = app.get(
                "$MAIN_URL/mobile/home?app=1",
                headers = BROWSER_HEADERS,
                referer = "$MAIN_URL/mobile/home?app=1"
            )
            homeResp.okhttpResponse.headers("Set-Cookie").forEach { h ->
                if (h.contains("t_hash_t=")) {
                    cookie = h.substringAfter("t_hash_t=").substringBefore(";")
                }
            }
            if (cookie.isEmpty()) {
                cookie = homeResp.cookies["t_hash_t"] ?: ""
            }
        } catch (_: Exception) {}
    }

    if (cookie.isEmpty()) return BypassResult("", "", USER_TOKEN, "")

    val result = BypassResult(cookie, "", USER_TOKEN, "")
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
        "ott" to ott,
        "user_token" to USER_TOKEN
    )
    if (bypass.addhash.isNotEmpty()) cookies["addhash"] = bypass.addhash

    val baseUrl = MAIN_URL
    val response = try {
        app.get(
            "$baseUrl/mobile/$playlistPath?id=$id",
            headers = BROWSER_HEADERS,
            referer = "$baseUrl/home",
            cookies = cookies
        ).text
    } catch (_: Exception) {
        return null
    }

    // Detect rate-limit/ad penalty responses — reject them
    if (response.contains("too many requests", ignoreCase = true) ||
        response.contains("stop abuse", ignoreCase = true) ||
        response.contains("rate limit", ignoreCase = true)) {
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
@Volatile private var apiResolvedTime: Long = 0L
private val apiMutex = Mutex()

suspend fun resolveNewTvApi(): String {
    // Cache for 12 hours
    if (resolvedApiUrl.isNotBlank() && System.currentTimeMillis() - apiResolvedTime < 43_200_000) {
        return resolvedApiUrl
    }

    return apiMutex.withLock {
        // Double-check after lock
        if (resolvedApiUrl.isNotBlank() && System.currentTimeMillis() - apiResolvedTime < 43_200_000) {
            return@withLock resolvedApiUrl
        }

        for (encoded in NEWTV_DOMAINS) {
            val base = b64(encoded).trimEnd('/')
            try {
                val text = app.get("$base/checknewtv.php", headers = NEWTV_HEADERS).text
                val hash = tryParseJson<TokenResponse>(text)?.token_hash
                if (!hash.isNullOrBlank()) {
                    resolvedApiUrl = b64(hash).trimEnd('/')
                    apiResolvedTime = System.currentTimeMillis()
                    return@withLock resolvedApiUrl
                }
            } catch (_: Exception) {}
        }
        ""
    }
}

suspend fun getNewTvLink(id: String, ott: String): String? {
    val apiBase = resolveNewTvApi()
    if (apiBase.isEmpty()) return null

    val bypass = cachedBypass
    val headers = NEWTV_HEADERS.toMutableMap().apply {
        put("Ott", ott)
        put("Usertoken", bypass?.usertoken ?: "")
    }
    if (bypass != null && bypass.cookie.isNotEmpty()) {
        val cookieParts = mutableListOf("t_hash_t=${bypass.cookie}", "hd=on", "ott=$ott")
        if (bypass.addhash.isNotEmpty()) cookieParts.add("addhash=${bypass.addhash}")
        if (bypass.usertoken.isNotEmpty()) cookieParts.add("usertoken=${bypass.usertoken}")
        headers["Cookie"] = cookieParts.joinToString("; ")
    }

    try {
        val text = app.get("$apiBase/newtv/player.php?id=$id", headers = headers).text
        val response = tryParseJson<PlayerResponse>(text)
        if (!response?.video_link.isNullOrBlank()) {
            return response!!.video_link
        }
    } catch (_: Exception) {}
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
