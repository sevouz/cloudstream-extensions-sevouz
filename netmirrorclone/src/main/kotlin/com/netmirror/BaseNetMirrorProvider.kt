package com.netmirror

import com.lagradost.cloudstream3.*
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
    abstract val searchPath: String
    abstract val postPath: String
    abstract val episodesPath: String

    private var cookieValue = ""

    private suspend fun getCookie(): String {
        if (cookieValue.isEmpty()) cookieValue = getBypassCookie()
        return cookieValue
    }

    private fun buildCookies(cookie: String): Map<String, String> = mapOf(
        "t_hash_t" to cookie, "ott" to ott, "hd" to "on"
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val cookie = getCookie()
        val doc = app.get(
            "$mainUrl/mobile/home?app=1",
            cookies = buildCookies(cookie),
            headers = BROWSER_HEADERS,
            referer = "$mainUrl/mobile/home?app=1"
        ).document
        val items = doc.select(".tray-container, #top10").map { it.toHomePageList() }
        return newHomePageResponse(items, false)
    }

    private fun Element.toHomePageList(): HomePageList {
        val name = select("h2, span").text()
        val items = select("article, .top10-post").mapNotNull { it.toSearchResult() }
        return HomePageList(name, items, isHorizontalImages = false)
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val id = selectFirst("a")?.attr("data-post") ?: attr("data-post")
        if (id.isBlank()) return null
        return newAnimeSearchResponse("", Id(id).toJson()) {
            posterUrl = "https://imgcdn.kim/$imgPrefix/v/$id.jpg"
            posterHeaders = mapOf("Referer" to "$mainUrl/home")
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val cookie = getCookie()
        val url = "$mainUrl/mobile/$searchPath?s=$query&t=$unixTime"
        val text = app.get(url, referer = "$mainUrl/home", cookies = buildCookies(cookie)).text
        val data = tryParseJson<SearchData>(text) ?: return emptyList()
        return data.searchResult.map {
            newAnimeSearchResponse(it.t, Id(it.id).toJson()) {
                posterUrl = "https://imgcdn.kim/$imgPrefix/v/${it.id}.jpg"
                posterHeaders = mapOf("Referer" to "$mainUrl/home")
            }
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        val cookie = getCookie()
        val id = parseJson<Id>(url).id
        val text = app.get(
            "$mainUrl/mobile/$postPath?id=$id&t=$unixTime",
            headers = BROWSER_HEADERS,
            referer = "$mainUrl/home",
            cookies = buildCookies(cookie)
        ).text
        val data = tryParseJson<PostData>(text) ?: return null

        val episodes = arrayListOf<Episode>()
        val title = data.title
        val castList = data.cast?.split(",")?.map { it.trim() } ?: emptyList()
        val cast = castList.map { ActorData(Actor(it)) }
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
                }
            }
            if (data.nextPageShow == 1 && data.nextPageSeason != null) {
                episodes.addAll(fetchEpisodes(title, url, data.nextPageSeason, 2, cookie))
            }
            data.season?.dropLast(1)?.forEach {
                episodes.addAll(fetchEpisodes(title, url, it.id, 1, cookie))
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

    private suspend fun fetchEpisodes(title: String, eid: String, sid: String, page: Int, cookie: String): List<Episode> {
        val episodes = arrayListOf<Episode>()
        var pg = page
        while (true) {
            val text = app.get(
                "$mainUrl/mobile/$episodesPath?s=$sid&series=$eid&t=$unixTime&page=$pg",
                headers = BROWSER_HEADERS,
                referer = "$mainUrl/home",
                cookies = buildCookies(cookie)
            ).text
            val data = tryParseJson<EpisodesData>(text) ?: break
            data.episodes?.mapTo(episodes) {
                newEpisode(LoadData(title, it.id)) {
                    name = it.t
                    episode = it.ep.replace("E", "").toIntOrNull()
                    season = it.s.replace("S", "").toIntOrNull()
                }
            }
            if (data.nextPageShow == 0) break
            pg++
        }
        return episodes
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val loadData = parseJson<LoadData>(data)
        val response = getVideoLink(loadData.id, ott) ?: return false
        if (response.video_link.isNullOrBlank()) return false

        val apiBase = resolveNewTvApi()
        callback.invoke(
            newExtractorLink(name, name, response.video_link, type = ExtractorLinkType.M3U8) {
                this.referer = response.referer ?: apiBase
            }
        )
        return true
    }

    @Suppress("ObjectLiteralToLambda")
    override fun getVideoInterceptor(extractorLink: ExtractorLink): Interceptor {
        return object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val request = chain.request()
                if (request.url.toString().contains(".m3u8")) {
                    return chain.proceed(
                        request.newBuilder().header("Cookie", "hd=on").build()
                    )
                }
                return chain.proceed(request)
            }
        }
    }

    data class Id(val id: String)
    data class LoadData(val title: String, val id: String)
}
