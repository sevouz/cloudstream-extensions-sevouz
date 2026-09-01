package com.netmirror

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.network.CloudflareKiller
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.utils.AppUtils.parseJson
import com.lagradost.cloudstream3.utils.AppUtils.toJson
import com.lagradost.cloudstream3.utils.AppUtils.tryParseJson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.Response
import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.APIHolder.unixTime
import java.util.concurrent.ConcurrentHashMap

// Simple persistence model: one entry per home page section.
private data class CachedSection(val name: String, val items: List<CachedItem>)
private data class CachedItem(val id: String, val title: String)

abstract class BaseNetMirrorProvider : MainAPI() {
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries, TvType.Anime, TvType.AsianDrama)
    override var lang = "ta"
    override var mainUrl = MAIN_URL
    override val hasMainPage = true

    abstract val ott: String
    abstract val imgPrefix: String
    abstract val epImgPrefix: String
    abstract val searchPath: String
    abstract val postPath: String
    abstract val episodesPath: String
    abstract val playlistPath: String

    // Poster URL cache: id -> full CDN URL. Avoids repeated string construction on re-renders.
    private val posterCache = ConcurrentHashMap<String, String>(256)

    // Home page result cache: holds the last fetched result and when it was fetched.
    // Served instantly on repeat opens; refreshed in the background after 5 minutes.
    @Volatile private var cachedHomeItems: List<HomePageList>? = null
    @Volatile private var cachedHomeTime: Long = 0L
    private val HOME_CACHE_TTL = 5 * 60 * 1000L // 5 minutes

    /** Fetch home page from network and cache the result. Called at plugin load and on TTL expiry. */
    suspend fun prewarmHome() {
        try {
            val fetched = fetchHomeItems() ?: return
            cachedHomeItems = fetched
            cachedHomeTime = System.currentTimeMillis()
            // Persist to disk so the next app launch can show results instantly
            saveToDisk(fetched)
        } catch (_: Throwable) {}
    }

    /**
     * Restore the last-known home page from SharedPreferences into memory.
     * Called synchronously at plugin load — completes in <5ms (disk read only).
     * If the persisted data is still within TTL it will be served immediately on
     * the first getMainPage() call, making even the first open after an app kill instant.
     */
    fun loadPersistedHome() {
        try {
            val json = BypassStorage.loadHomeCache(ott) ?: return
            val sections = tryParseJson<List<CachedSection>>(json) ?: return
            if (sections.isEmpty()) return
            val items = sections.mapNotNull { section ->
                val list = section.items.map { item ->
                    newAnimeSearchResponse(item.title, Id(item.id).toJson()) {
                        posterUrl = posterUrl(item.id)
                    }
                }
                if (list.isEmpty()) null
                else HomePageList(section.name, list, isHorizontalImages = false)
            }
            if (items.isNotEmpty()) {
                cachedHomeItems = items
                cachedHomeTime = System.currentTimeMillis()
            }
        } catch (_: Throwable) {}
    }

    private fun saveToDisk(items: List<HomePageList>) {
        try {
            val sections = items.map { section ->
                CachedSection(
                    name = section.name,
                    items = section.list.map { result ->
                        val id = tryParseJson<Id>(result.url)?.id ?: return@map null
                        CachedItem(id = id, title = result.name)
                    }.filterNotNull()
                )
            }
            BypassStorage.saveHomeCache(ott, sections.toJson())
        } catch (_: Throwable) {}
    }

    private suspend fun fetchHomeItems(): List<HomePageList>? {
        val doc = app.get(
            "$mainUrl/mobile/home?app=1",
            cookies = quickCookies(),
            headers = BROWSER_HEADERS,
            referer = "$mainUrl/mobile/home?app=1"
        ).document
        var sections = doc.select(".tray-container, #top10")
        if (sections.isEmpty()) {
            val doc2 = app.get(
                "$mainUrl/mobile/home?app=1",
                cookies = cookies(),
                headers = BROWSER_HEADERS,
                referer = "$mainUrl/mobile/home?app=1"
            ).document
            sections = doc2.select(".tray-container, #top10")
        }
        if (sections.isEmpty()) return null
        return coroutineScope {
            sections.map { section ->
                async {
                    val name = section.select("h2, span").first()?.text() ?: return@async null
                    val list = section.select("article, .top10-post").mapNotNull { it.toResult() }
                    if (list.isEmpty()) null else HomePageList(name, list, isHorizontalImages = false)
                }
            }.mapNotNull { it.await() }
        }.ifEmpty { null }
    }

    private suspend fun cookies(): Map<String, String> {
        val bypass = ensureBypass()
        val c = mutableMapOf("ott" to ott, "hd" to "on")
        if (bypass.cookie.isNotEmpty()) c["t_hash_t"] = bypass.cookie
        if (bypass.addhash.isNotEmpty()) c["addhash"] = bypass.addhash
        if (bypass.usertoken.isNotEmpty()) c["usertoken"] = bypass.usertoken
        return c
    }

    /** Quick cookies using cached bypass if available, without blocking */
    private fun quickCookies(): Map<String, String> {
        val bypass = cachedBypass
        val c = mutableMapOf("ott" to ott, "hd" to "on")
        if (bypass != null && bypass.cookie.isNotEmpty()) {
            c["t_hash_t"] = bypass.cookie
            if (bypass.addhash.isNotEmpty()) c["addhash"] = bypass.addhash
            if (bypass.usertoken.isNotEmpty()) c["usertoken"] = bypass.usertoken
        }
        return c
    }

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val now = System.currentTimeMillis()
        val cached = cachedHomeItems

        return if (cached != null && cached.isNotEmpty()) {
            // Serve cached result instantly
            if (now - cachedHomeTime > HOME_CACHE_TTL) {
                // Cache stale — refresh in background, don't block the UI
                CoroutineScope(Dispatchers.IO).launch { prewarmHome() }
            }
            newHomePageResponse(cached, false)
        } else {
            // No cache yet — must fetch now (first open after install or app kill)
            val items = fetchHomeItems() ?: emptyList()
            cachedHomeItems = items
            cachedHomeTime = now
            newHomePageResponse(items, false)
        }
    }

    private fun posterUrl(id: String): String =
        posterCache.getOrPut(id) { "https://imgcdn.kim/$imgPrefix/v/$id.jpg" }

    private fun Element.toResult(): SearchResponse? {
        val id = selectFirst("a")?.attr("data-post") ?: attr("data-post")
        if (id.isBlank()) return null
        // Extract title from img alt or h3/p text so cards render immediately without a detail fetch
        val title = selectFirst("img")?.attr("alt")?.trim()
            ?: selectFirst("h3, p, .title")?.text()?.trim()
            ?: ""
        return newAnimeSearchResponse(title, Id(id).toJson()) {
            posterUrl = posterUrl(id)
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val text = app.get(
            "$mainUrl/mobile/$searchPath?s=$query&t=$unixTime",
            referer = "$mainUrl/home",
            cookies = cookies()
        ).text
        val data = tryParseJson<SearchData>(text) ?: return emptyList()
        return data.searchResult.map {
            newAnimeSearchResponse(it.t, Id(it.id).toJson()) {
                posterUrl = posterUrl(it.id)
            }
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        val id = parseJson<Id>(url).id
        val text = app.get(
            "$mainUrl/mobile/$postPath?id=$id&t=$unixTime",
            headers = BROWSER_HEADERS,
            referer = "$mainUrl/home",
            cookies = cookies()
        ).text
        val data = tryParseJson<PostData>(text) ?: return null

        val episodes = arrayListOf<Episode>()
        val title = data.title
        val cast = data.cast?.split(",")?.map { ActorData(Actor(it.trim())) } ?: emptyList()
        val genre = data.genre?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
        val suggest = data.suggest?.map {
            newAnimeSearchResponse("", Id(it.id).toJson()) {
                posterUrl = posterUrl(it.id)
            }
        }

        if (data.episodes.firstOrNull() == null) {
            episodes.add(newEpisode(LoadData(title, id)) { name = title })
        } else {
            data.episodes.filterNotNull().mapTo(episodes) {
                newEpisode(LoadData(title, it.id)) {
                    this.name = it.t
                    this.episode = it.ep.replace("E", "").toIntOrNull()
                    this.season = it.s.replace("S", "").toIntOrNull()
                    this.posterUrl = "https://imgcdn.kim/${epImgPrefix}/${it.id}.jpg"
                    this.runTime = it.time.replace("m", "").toIntOrNull()
                }
            }
            if (data.nextPageShow == 1 && data.nextPageSeason != null) {
                episodes.addAll(fetchEps(title, url, data.nextPageSeason, 2))
            }
            data.season?.dropLast(1)?.forEach {
                episodes.addAll(fetchEps(title, url, it.id, 1))
            }
        }

        val type = if (data.episodes.firstOrNull() == null) TvType.Movie else TvType.TvSeries
        return newTvSeriesLoadResponse(title, url, type, episodes) {
            posterUrl = posterUrl(id)
            backgroundPosterUrl = "https://imgcdn.kim/$imgPrefix/h/$id.jpg"
            plot = data.desc
            year = data.year.toIntOrNull()
            tags = genre
            actors = cast
            this.recommendations = suggest
        }
    }

    private suspend fun fetchEps(title: String, eid: String, sid: String, page: Int): List<Episode> {
        val eps = arrayListOf<Episode>()
        var pg = page
        while (true) {
            val text = app.get(
                "$mainUrl/mobile/$episodesPath?s=$sid&series=$eid&t=$unixTime&page=$pg",
                headers = BROWSER_HEADERS,
                referer = "$mainUrl/home",
                cookies = cookies()
            ).text
            val data = tryParseJson<EpisodesData>(text) ?: break
            data.episodes?.mapTo(eps) {
                newEpisode(LoadData(title, it.id)) {
                    name = it.t
                    episode = it.ep.replace("E", "").toIntOrNull()
                    season = it.s.replace("S", "").toIntOrNull()
                    this.posterUrl = "https://imgcdn.kim/${epImgPrefix}/${it.id}.jpg"
                    this.runTime = it.time.replace("m", "").toIntOrNull()
                }
            }
            if (data.nextPageShow == 0) break
            pg++
        }
        return eps
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val ld = parseJson<LoadData>(data)
        var hasLink = false

        // Primary: NewTV API (ad-free, OTP-authenticated)
        val newTvM3u8 = try { getNewTvLink(ld.id, ott) } catch (_: Exception) { null }
        if (!newTvM3u8.isNullOrBlank()) {
            callback.invoke(
                newExtractorLink(name, "$name NewTV", newTvM3u8, type = ExtractorLinkType.M3U8) {
                    this.referer = MAIN_URL
                }
            )
            hasLink = true
        }

        // Playlist API — used for subtitles always, and as a video fallback only if NewTV failed
        val result = try {
            getPlaylistLink(ld.id, ott, playlistPath)
        } catch (_: Exception) { null }

        if (result != null) {
            if (!hasLink) {
                val source = result.sources.firstOrNull { !it.file.isNullOrBlank() }
                if (source != null) {
                    val url = source.file!!
                    val fullUrl = if (url.startsWith("http")) url else "$MAIN_URL$url"
                    callback.invoke(
                        newExtractorLink(name, name, fullUrl, type = ExtractorLinkType.M3U8) {
                            this.referer = MAIN_URL
                        }
                    )
                    hasLink = true
                }
            }

            // Subtitles (from playlist response)
            result.tracks?.forEach { track ->
                val url = track.file ?: return@forEach
                val label = track.label ?: "Unknown"
                val kind = track.kind ?: ""
                if (kind == "captions" || url.endsWith(".srt") || url.endsWith(".vtt")) {
                    subtitleCallback.invoke(
                        SubtitleFile(label, url)
                    )
                }
            }
        }

        return hasLink
    }

    private fun getQualityFromLabel(label: String): Int {
        return when {
            label.contains("4k", true) || label.contains("2160", true) -> Qualities.P2160.value
            label.contains("1080", true) -> Qualities.P1080.value
            label.contains("720", true) -> Qualities.P720.value
            label.contains("480", true) -> Qualities.P480.value
            label.contains("360", true) -> Qualities.P360.value
            else -> Qualities.Unknown.value
        }
    }

    override fun getVideoInterceptor(extractorLink: ExtractorLink): Interceptor {
        return Interceptor { chain ->
            val req = chain.request()
            if (req.url.toString().contains(".m3u8")) {
                val newReq = req.newBuilder().header("Cookie", "hd=on").build()
                chain.proceed(newReq)
            } else {
                chain.proceed(req)
            }
        }
    }

    data class Id(val id: String)
    data class LoadData(val title: String, val id: String)
}
