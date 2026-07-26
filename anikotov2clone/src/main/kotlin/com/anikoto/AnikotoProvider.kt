package com.anikoto

import android.content.Context
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.DubStatus
import com.lagradost.cloudstream3.HomePageResponse
import com.lagradost.cloudstream3.LoadResponse
import com.lagradost.cloudstream3.MainAPI
import com.lagradost.cloudstream3.MainPageRequest
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.addDubStatus
import com.lagradost.cloudstream3.addEpisodes
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.fixUrl
import com.lagradost.cloudstream3.mainPageOf
import com.lagradost.cloudstream3.newAnimeLoadResponse
import com.lagradost.cloudstream3.newAnimeSearchResponse
import com.lagradost.cloudstream3.newEpisode
import com.lagradost.cloudstream3.newHomePageResponse
import com.lagradost.cloudstream3.utils.AppUtils.parseJson
import com.lagradost.cloudstream3.utils.AppUtils.tryParseJson
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.loadExtractor
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.net.URLDecoder
import java.net.URLEncoder

class AnikotoProvider : MainAPI() {
    override var mainUrl = "https://anikototv.to"
    override var name = "AniKoto"
    override var lang = "en"
    override val hasMainPage = true
    override val supportedTypes = setOf(TvType.Anime, TvType.AnimeMovie, TvType.OVA)

    companion object {
        var context: Context? = null
    }

    private val browserHeaders = mapOf(
        "User-Agent" to "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36",
        "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
        "Accept-Language" to "en-US,en;q=0.5"
    )

    private fun ajaxHeaders(referer: String) = mapOf(
        "User-Agent" to "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36",
        "X-Requested-With" to "XMLHttpRequest",
        "Accept" to "application/json, text/javascript, */*; q=0.01",
        "Referer" to referer
    )

    override val mainPage = mainPageOf(
        "$mainUrl/latest-updated" to "Latest Updated",
        "$mainUrl/most-viewed" to "Most Popular",
        "$mainUrl/status/currently-airing" to "Ongoing",
        "$mainUrl/type/movie" to "Movies",
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val doc = app.get("${request.data}?page=$page", headers = browserHeaders).document
        val items = doc.select("div.item, div.flw-item").mapNotNull { toSearchResult(it) }
        return newHomePageResponse(request.name, items)
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val encodedQuery = URLEncoder.encode(query, "UTF-8")
        val doc = app.get("$mainUrl/filter?keyword=$encodedQuery", headers = browserHeaders).document
        return doc.select("div.item, div.flw-item").mapNotNull { toSearchResult(it) }
    }

    private fun toSearchResult(element: Element): SearchResponse? {
        val titleEl = element.selectFirst("a.name.d-title")
            ?: element.selectFirst("a[title]")
            ?: element.selectFirst("a[href*='/watch/']")
            ?: return null

        var href = titleEl.attr("href")
        if (href.isBlank()) {
            href = element.selectFirst("div.poster a, a")?.attr("href") ?: ""
        }

        var title = titleEl.text().trim()
        if (title.isBlank()) title = titleEl.attr("title").trim()

        if (href.isBlank() || title.isBlank()) return null

        val cleanHref = fixUrl(Regex("/ep-\\d+$").replace(href, ""))

        val posterEl = element.selectFirst("div.poster img, img")
        val poster = posterEl?.attr("data-src")?.takeIf { it.isNotBlank() }
            ?: posterEl?.attr("src")

        val typeText = element.selectFirst(".fd-infor .tick-item.tick-type, .item-type, .tick-type")?.text()
            ?: element.selectFirst(".type")?.ownText()?.trim()
        val type = if (typeText != null && typeText.contains("Movie", true)) TvType.AnimeMovie else TvType.Anime

        val metaText = element.select(".meta, .info, .type, .right").text()
        val hasDub = element.selectFirst(".dub, i.dub, .fa-microphone") != null ||
                metaText.contains("Dub", true)
        val hasSub = element.selectFirst(".sub, i.sub, .fa-closed-captioning") != null ||
                metaText.contains("Sub", true) || !hasDub

        return newAnimeSearchResponse(title, cleanHref, type) {
            this.posterUrl = poster?.let { fixUrl(it) }
            addDubStatus(hasDub, hasSub)
        }
    }

    override suspend fun load(url: String): LoadResponse? {
        val doc = app.get(url, headers = browserHeaders).document

        val title = doc.selectFirst("#w-info h1.title, h1[itemprop=name], .title[itemprop=name]")
            ?.text()?.trim()?.takeIf { it.isNotBlank() }
            ?: doc.selectFirst("h1.title")?.text()?.trim()
            ?: return null

        val posterEl = doc.selectFirst("#w-info .poster img, img[itemprop=image], .poster img")
        val poster = posterEl?.attr("data-src")?.takeIf { it.isNotBlank() }
            ?: posterEl?.attr("src")

        val description = doc.selectFirst("#w-info .synopsis .content, #w-info .synopsis, .synopsis .content")?.text()
        val genres = doc.select("#w-info a[href*='/genre/'], .meta a[href*='/genre/']").map { it.text().trim() }
        val isMovie = doc.selectFirst("#w-info a[href*='/type/movie']") != null

        val animeId = doc.selectFirst("#watch-main")?.attr("data-id")?.takeIf { it.isNotBlank() }
            ?: doc.selectFirst("[data-id]")?.attr("data-id")?.takeIf { it.isNotBlank() }
            ?: Regex("data-id=[\"'](\\d+)[\"']").find(doc.html())?.groupValues?.getOrNull(1)

        val subEpisodes = mutableListOf<com.lagradost.cloudstream3.Episode>()
        val dubEpisodes = mutableListOf<com.lagradost.cloudstream3.Episode>()

        if (animeId != null) {
            runCatching {
                val json = app.get(
                    "$mainUrl/ajax/episode/list/$animeId",
                    headers = ajaxHeaders(url),
                    referer = url
                ).text
                val html = jsonResultString(json)

                Jsoup.parse(html).select("a[data-ids]").forEach { el ->
                    val serverIds = el.attr("data-ids")
                    if (serverIds.isBlank()) return@forEach

                    val episodeNumber = el.attr("data-num").toIntOrNull()
                    val hasSub = el.attr("data-sub") == "1"
                    val hasDub = el.attr("data-dub") == "1"

                    val episodeName = el.selectFirst(".d-title")?.text()?.takeIf { it.isNotBlank() }
                        ?: el.attr("data-jp").takeIf { it.isNotBlank() }
                        ?: "Episode ${episodeNumber ?: ""}"

                    if (hasSub || !hasDub) {
                        subEpisodes.add(
                            newEpisode("anikoto|$url|$serverIds|sub") {
                                this.episode = episodeNumber
                                this.name = episodeName
                            }
                        )
                    }
                    if (hasDub) {
                        dubEpisodes.add(
                            newEpisode("anikoto|$url|$serverIds|dub") {
                                this.episode = episodeNumber
                                this.name = episodeName
                            }
                        )
                    }
                }
            }
        }

        val type = if (isMovie) TvType.AnimeMovie else TvType.Anime

        return newAnimeLoadResponse(title, url, type) {
            this.posterUrl = poster?.let { fixUrl(it) }
            this.plot = description
            this.tags = genres
            if (subEpisodes.isNotEmpty()) addEpisodes(DubStatus.Subbed, subEpisodes)
            if (dubEpisodes.isNotEmpty()) addEpisodes(DubStatus.Dubbed, dubEpisodes)
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        var ep = try {
            URLDecoder.decode(data, "UTF-8")
        } catch (_: Exception) {
            data
        }
        if (ep.startsWith("$mainUrl/")) ep = ep.removePrefix("$mainUrl/")

        return when {
            ep.startsWith("anikoto|") -> {
                val parts = ep.split("|")
                if (parts.size < 4) return false
                val referer = parts[1]
                val serverIds = parts[2]
                val audioType = parts[3].ifBlank { "sub" }
                if (serverIds.isBlank()) return false
                resolveServers(serverIds, referer, audioType, subtitleCallback, callback)
            }

            ep.startsWith("anikoto-direct|") -> {
                resolveFromWatchPage(ep.removePrefix("anikoto-direct|"), subtitleCallback, callback)
            }

            else -> resolveFromWatchPage(ep, subtitleCallback, callback)
        }
    }

    private suspend fun resolveServers(
        serverIds: String,
        referer: String,
        audioType: String,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val serverListJson = app.get(
            "$mainUrl/ajax/server/list?servers=$serverIds",
            headers = ajaxHeaders(referer)
        ).text
        val serverListHtml = jsonResultString(serverListJson)
        if (serverListHtml.isBlank()) return false

        val serverDoc = Jsoup.parse(serverListHtml)

        val typeSelectors = if (audioType == "dub") {
            listOf("div.type[data-type=dub]")
        } else {
            listOf("div.type[data-type=sub]", "div.type[data-type=hsub]")
        }

        var preferredServers = typeSelectors.flatMap { sel ->
            serverDoc.select("$sel li[data-link-id]")
        }
        if (preferredServers.isEmpty()) {
            preferredServers = serverDoc.select("li[data-link-id]")
        }

        val linkIds = preferredServers
            .map { it.attr("data-link-id") }
            .filter { it.isNotBlank() }
            .distinct()
        if (linkIds.isEmpty()) return false

        for (linkId in linkIds) {
            runCatching {
                val serverJson = app.get(
                    "$mainUrl/ajax/server?get=$linkId",
                    headers = ajaxHeaders(referer)
                ).text
                val embedUrl = jsonResultUrl(serverJson)
                if (!embedUrl.isNullOrBlank()) {
                    resolveEmbedInline(embedUrl, referer, audioType, subtitleCallback, callback)
                }
            }
        }
        return true
    }

    private suspend fun resolveFromWatchPage(
        episodeUrl: String,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val fullUrl = if (episodeUrl.startsWith("http")) episodeUrl else fixUrl(episodeUrl)
        val doc = app.get(fullUrl, headers = browserHeaders).document

        val animeId = doc.selectFirst("#watch-main")?.attr("data-id")?.takeIf { it.isNotBlank() }
            ?: doc.selectFirst("[data-id]")?.attr("data-id")?.takeIf { it.isNotBlank() }
            ?: Regex("data-id=[\"'](\\d+)[\"']").find(doc.html())?.groupValues?.getOrNull(1)
            ?: return false

        val targetEp = Regex("/ep-(\\d+)").find(fullUrl)?.groupValues?.getOrNull(1)?.toIntOrNull()

        val json = app.get(
            "$mainUrl/ajax/episode/list/$animeId",
            headers = ajaxHeaders(fullUrl),
            referer = fullUrl
        ).text
        val html = jsonResultString(json)

        val episodes = Jsoup.parse(html).select("a[data-ids]")
        val target = episodes.firstOrNull {
            targetEp != null && it.attr("data-num").toIntOrNull() == targetEp
        } ?: episodes.firstOrNull() ?: return false

        val serverIds = target.attr("data-ids")
        if (serverIds.isBlank()) return false

        val audioType = if (target.attr("data-dub") == "1" && target.attr("data-sub") != "1") "dub" else "sub"
        return resolveServers(serverIds, fullUrl, audioType, subtitleCallback, callback)
    }

    private suspend fun resolveEmbedInline(
        url: String,
        referer: String,
        audioType: String,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val normalizedUrl = when {
            url.startsWith("//") -> "https:$url"
            url.startsWith("/") -> "$mainUrl$url"
            else -> url
        }

        val domain = Regex("https?://([^/]+)").find(normalizedUrl)?.groupValues?.getOrNull(1) ?: ""

        return try {
            when {
                domain.contains("megaplay", true) ||
                        domain.contains("vidwish", true) ||
                        domain.contains("vidtube", true) -> {
                    val host = "https://$domain"
                    val serverName = when {
                        domain.contains("megaplay", true) -> "MegaPlay"
                        domain.contains("vidwish", true) -> "Vidwish"
                        else -> "Vidtube"
                    }
                    MegaPlay.extractMegaPlayUrl(
                        normalizedUrl, referer, host, serverName,
                        subtitleCallback, callback, audioType
                    )
                    true
                }

                else -> loadExtractor(normalizedUrl, referer, subtitleCallback, callback)
            }
        } catch (_: Exception) {
            false
        }
    }

    private fun jsonResultString(json: String): String {
        return try {
            val response = tryParseJson<AjaxResponse>(json)
            if (response?.status == 200) {
                (response.result as? String) ?: ""
            } else ""
        } catch (_: Exception) {
            ""
        }
    }

    private fun jsonResultUrl(json: String): String? {
        return try {
            val response = tryParseJson<AjaxResponse>(json)
            if (response?.status == 200) {
                val map = response.result as? Map<*, *>
                map?.get("url") as? String
            } else null
        } catch (_: Exception) {
            null
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class AjaxResponse(
        @JsonProperty("status") val status: Int? = null,
        @JsonProperty("result") val result: Any? = null
    )
}
