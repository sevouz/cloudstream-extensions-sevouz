package com.netmirror

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.network.CloudflareKiller
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.utils.AppUtils.parseJson
import com.lagradost.cloudstream3.utils.AppUtils.toJson
import com.lagradost.cloudstream3.utils.AppUtils.tryParseJson
import okhttp3.Interceptor
import okhttp3.Response
import org.jsoup.nodes.Element
import com.lagradost.cloudstream3.APIHolder.unixTime

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
        // Try with quick (non-blocking) cookies first
        var doc = app.get(
            "$mainUrl/mobile/home?app=1",
            cookies = quickCookies(),
            headers = BROWSER_HEADERS,
            referer = "$mainUrl/mobile/home?app=1"
        ).document
        var items = doc.select(".tray-container, #top10").mapNotNull { section ->
            val name = section.select("h2, span").text()
            val list = section.select("article, .top10-post").mapNotNull { it.toResult() }
            if (list.isEmpty()) null else HomePageList(name, list, isHorizontalImages = false)
        }

        // If quick cookies didn't work (empty results), fallback to full bypass
        if (items.isEmpty()) {
            doc = app.get(
                "$mainUrl/mobile/home?app=1",
                cookies = cookies(),
                headers = BROWSER_HEADERS,
                referer = "$mainUrl/mobile/home?app=1"
            ).document
            items = doc.select(".tray-container, #top10").mapNotNull { section ->
                val name = section.select("h2, span").text()
                val list = section.select("article, .top10-post").mapNotNull { it.toResult() }
                if (list.isEmpty()) null else HomePageList(name, list, isHorizontalImages = false)
            }
        }

        return newHomePageResponse(items, false)
    }

    private fun Element.toResult(): SearchResponse? {
        val id = selectFirst("a")?.attr("data-post") ?: attr("data-post")
        if (id.isBlank()) return null
        return newAnimeSearchResponse("", Id(id).toJson()) {
            posterUrl = "https://imgcdn.kim/$imgPrefix/v/$id.jpg"
            posterHeaders = mapOf("Referer" to "$mainUrl/home")
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
                posterUrl = "https://imgcdn.kim/$imgPrefix/v/${it.id}.jpg"
                posterHeaders = mapOf("Referer" to "$mainUrl/home")
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
                posterUrl = "https://imgcdn.kim/$imgPrefix/v/${it.id}.jpg"
                posterHeaders = mapOf("Referer" to "$mainUrl/home")
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
            posterUrl = "https://imgcdn.kim/$imgPrefix/v/$id.jpg"
            backgroundPosterUrl = "https://imgcdn.kim/$imgPrefix/h/$id.jpg"
            posterHeaders = mapOf("Referer" to "$mainUrl/home")
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

        // Try NewTV API first (ad-free streams)
        val newTvM3u8 = try { getNewTvLink(ld.id, ott) } catch (_: Exception) { null }
        if (!newTvM3u8.isNullOrBlank()) {
            callback.invoke(
                newExtractorLink(name, "$name NewTV", newTvM3u8, type = ExtractorLinkType.M3U8) {
                    this.referer = MAIN_URL
                }
            )
        }

        // Also try playlist API (may have subtitles)
        val result = try {
            getPlaylistLink(ld.id, ott, playlistPath)
        } catch (_: Exception) { null }

        if (result != null) {
            val source = result.sources.firstOrNull { !it.file.isNullOrBlank() }
            if (source != null) {
                val url = source.file!!
                val fullUrl = if (url.startsWith("http")) url else "$MAIN_URL$url"
                callback.invoke(
                    newExtractorLink(name, name, fullUrl, type = ExtractorLinkType.M3U8) {
                        this.referer = MAIN_URL
                    }
                )
            }

            // Add subtitles
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

        return !newTvM3u8.isNullOrBlank() || result != null
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

    private val cloudflareKiller by lazy { CloudflareKiller() }

    override fun getVideoInterceptor(extractorLink: ExtractorLink): Interceptor {
        return Interceptor { chain ->
            val req = chain.request()
            val bypass = cachedBypass
            if (bypass != null && bypass.cookie.isNotEmpty()) {
                val cookieParts = mutableListOf("t_hash_t=${bypass.cookie}", "hd=on", "ott=$ott")
                if (bypass.addhash.isNotEmpty()) cookieParts.add("addhash=${bypass.addhash}")
                if (bypass.usertoken.isNotEmpty()) cookieParts.add("usertoken=${bypass.usertoken}")
                val newReq = req.newBuilder()
                    .header("Cookie", cookieParts.joinToString("; "))
                    .build()
                chain.proceed(newReq)
            } else {
                chain.proceed(req)
            }
        }
    }

    data class Id(val id: String)
    data class LoadData(val title: String, val id: String)
}
