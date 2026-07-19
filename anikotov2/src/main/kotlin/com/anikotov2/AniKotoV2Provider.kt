package com.anikotov2

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.api.Log
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addAniListId
import com.lagradost.cloudstream3.LoadResponse.Companion.addMalId
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.utils.AppUtils.parseJson
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class AniKotoV2Provider : MainAPI() {
    override var mainUrl = "https://anikototv.to"
    override var name = "AniKoto V2"
    override val hasMainPage = true
    override var lang = "en"
    override val hasDownloadSupport = true
    override val hasQuickSearch = false
    override val supportedTypes = setOf(
        TvType.Anime,
        TvType.AnimeMovie,
        TvType.OVA,
    )

    companion object {
        private const val TAG = "AniKotoV2"

        private val BROWSER_HEADERS = mapOf(
            "User-Agent" to "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36",
            "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
            "Accept-Language" to "en-US,en;q=0.5"
        )
    }

    private fun ajaxHeaders(referer: String): Map<String, String> = mapOf(
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
        val url = if (page > 1) "${request.data}?page=$page" else request.data
        val document = app.get(url, headers = BROWSER_HEADERS).document
        val home = document.select("div#list-items div.item, div.film_list-wrap div.flw-item").mapNotNull {
            it.toSearchResult()
        }
        return newHomePageResponse(request.name, home, hasNext = home.isNotEmpty())
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val nameTag = selectFirst("a.name.d-title, h3.film-name a, a.dynamic-name") ?: return null
        val title = nameTag.text().trim().ifBlank { return null }
        val href = fixUrl(nameTag.attr("href"))
        val cleanHref = href.replace(Regex("/ep-\\d+$"), "")

        val posterUrl = selectFirst("div.ani.poster img, img.film-poster-img")?.let {
            it.attr("data-src").ifBlank { it.attr("src") }
        }

        val subCount = selectFirst("span.ep-status.sub span, div.tick-sub")?.text()?.trim()?.toIntOrNull()
        val dubCount = selectFirst("span.ep-status.dub span, div.tick-dub")?.text()?.trim()?.toIntOrNull()

        return newAnimeSearchResponse(title, cleanHref, TvType.Anime) {
            this.posterUrl = posterUrl
            addDubStatus(
                dubExist = dubCount != null && dubCount > 0,
                subExist = subCount != null && subCount > 0,
                dubEpisodes = dubCount,
                subEpisodes = subCount
            )
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/filter?keyword=$query"
        val document = app.get(url, headers = BROWSER_HEADERS).document
        return document.select("div#list-items div.item, div.film_list-wrap div.flw-item").mapNotNull {
            it.toSearchResult()
        }
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url, headers = BROWSER_HEADERS).document

        val watchMain = document.selectFirst("div#watch-main")
        val animeId = watchMain?.attr("data-id") ?: ""

        val title = document.selectFirst("div#w-info h1.title, h2.film-name")?.text()?.trim()
            ?: document.selectFirst("meta[property=og:title]")?.attr("content")
                ?.replace(Regex("Watch|Online|Free|-|AniKoto|Anime"), "")?.trim()
            ?: "Unknown"

        val poster = document.selectFirst("div#w-info div.poster img, div.film-poster img")?.let {
            it.attr("data-src").ifBlank { it.attr("src") }
        }

        val description = document.selectFirst("div#w-info div.synopsis div.content, div.film-description")?.text()?.trim()
        val japName = document.selectFirst("h1.title.d-title")?.attr("data-jp")

        val subEpisodes = mutableListOf<Episode>()
        val dubEpisodes = mutableListOf<Episode>()

        if (animeId.isNotBlank()) {
            try {
                val epResponse = app.get(
                    "$mainUrl/ajax/episode/list/$animeId",
                    headers = ajaxHeaders(url)
                ).text
                val epResult = parseJson<AjaxResponse>(epResponse)
                val epDoc = Jsoup.parse(epResult.result ?: "")

                epDoc.select("ul.ep-range li a, a.ep-item").forEach { epLink ->
                    val epNum = (epLink.attr("data-num").ifBlank { epLink.attr("data-number") })
                        .toIntOrNull() ?: return@forEach
                    val epTitle = epLink.selectFirst("span.d-title")?.text() ?: "Episode $epNum"
                    val dataIds = epLink.attr("data-ids").ifBlank { epLink.attr("data-id") }
                    val hasSub = epLink.attr("data-sub") == "1"
                    val hasDub = epLink.attr("data-dub") == "1"

                    val episodeData = "$animeId|$dataIds|$epNum"

                    if (hasSub) {
                        subEpisodes.add(newEpisode(episodeData) {
                            this.name = epTitle
                            this.episode = epNum
                        })
                    }
                    if (hasDub) {
                        dubEpisodes.add(newEpisode("$episodeData|dub") {
                            this.name = epTitle
                            this.episode = epNum
                        })
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load episodes: ${e.message}")
            }
        }

        var year: Int? = null
        var malId: Int? = null
        var aniListId: Int? = null
        var status: ShowStatus? = null
        val tags = mutableListOf<String>()

        document.select("div#w-info div.bmeta div.meta div, div.elements div.row div").forEach { item ->
            val text = item.text()
            when {
                text.startsWith("Premiered:") || text.startsWith("Aired:") -> {
                    year = Regex("(\\d{4})").find(text)?.groupValues?.get(1)?.toIntOrNull()
                }
                text.startsWith("Status:") -> {
                    status = when {
                        text.contains("Airing", true) -> ShowStatus.Ongoing
                        text.contains("Finished", true) -> ShowStatus.Completed
                        else -> null
                    }
                }
                text.startsWith("Genres:") || text.startsWith("Genre:") -> {
                    tags.addAll(item.select("a").map { it.text().trim() })
                }
            }
        }

        document.select("a[href*=myanimelist.net]").firstOrNull()?.let {
            malId = it.attr("href").substringAfterLast("/").toIntOrNull()
        }
        document.select("a[href*=anilist.co]").firstOrNull()?.let {
            aniListId = it.attr("href").substringAfterLast("/").toIntOrNull()
        }

        return newAnimeLoadResponse(title, url, TvType.Anime) {
            engName = title
            this.japName = japName
            this.posterUrl = poster
            this.year = year
            this.showStatus = status
            this.plot = description
            this.tags = tags.ifEmpty { null }
            addEpisodes(DubStatus.Subbed, subEpisodes)
            if (dubEpisodes.isNotEmpty()) {
                addEpisodes(DubStatus.Dubbed, dubEpisodes)
            }
            addMalId(malId)
            addAniListId(aniListId)
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val parts = data.split("|")
        if (parts.size < 3) return false

        val dataIds = parts[1]
        val isDub = parts.size > 3 && parts[3] == "dub"

        // Step 1: Get server list
        val serverResponse = app.get(
            "$mainUrl/ajax/server/list?servers=$dataIds",
            headers = ajaxHeaders(mainUrl)
        ).text
        val serverResult = parseJson<AjaxResponse>(serverResponse)
        val serverDoc = Jsoup.parse(serverResult.result ?: return false)

        // Step 2: Collect servers by type
        val typeLabels = mutableListOf<Pair<String, List<Element>>>()

        val subServers = serverDoc.select("div.type[data-type=sub] li[data-link-id]")
        val dubServers = serverDoc.select("div.type[data-type=dub] li[data-link-id]")

        if (isDub) {
            if (dubServers.isNotEmpty()) typeLabels.add("Dub" to dubServers.toList())
            if (subServers.isNotEmpty()) typeLabels.add("Sub" to subServers.toList())
        } else {
            if (subServers.isNotEmpty()) typeLabels.add("Sub" to subServers.toList())
            if (dubServers.isNotEmpty()) typeLabels.add("Dub" to dubServers.toList())
        }

        if (typeLabels.isEmpty()) {
            val allServers = serverDoc.select("li[data-link-id]")
            if (allServers.isNotEmpty()) typeLabels.add("" to allServers.toList())
        }

        // Step 3: Resolve each server
        for ((audioType, serverItems) in typeLabels) {
            for (server in serverItems) {
                val linkId = server.attr("data-link-id")
                val serverName = server.text().trim()
                if (linkId.isBlank()) continue

                try {
                    val embedResponse = app.get(
                        "$mainUrl/ajax/server?get=$linkId",
                        headers = ajaxHeaders(mainUrl)
                    ).text
                    val embedResult = parseJson<ServerGetResponse>(embedResponse)
                    val embedUrl = embedResult.result?.url ?: continue

                    val sourceName = if (audioType.isNotBlank()) "$serverName ($audioType)" else serverName
                    Log.d(TAG, "Server: $sourceName, Embed: $embedUrl")

                    // Try inline MegaPlay extraction first (faster, no WebView)
                    val resolved = resolveMegaPlayInline(embedUrl, audioType, subtitleCallback, callback)
                    if (!resolved) {
                        // Fallback to loadExtractor
                        val collectedLinks = mutableListOf<ExtractorLink>()
                        loadExtractor(embedUrl, mainUrl, subtitleCallback) { link ->
                            collectedLinks.add(link)
                        }
                        for (link in collectedLinks) {
                            val displayName = if (audioType.isNotBlank()) "${link.source} ($audioType)" else link.source
                            callback.invoke(
                                newExtractorLink(displayName, sourceName, link.url, link.type) {
                                    this.referer = link.referer
                                    this.quality = link.quality
                                    this.headers = link.headers
                                }
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error with server $serverName: ${e.message}")
                }
            }
        }

        return true
    }

    private suspend fun resolveMegaPlayInline(
        embedUrl: String,
        audioType: String,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        // Normalize URL
        val normalizedUrl = embedUrl.replace("http://", "https://")
        val domain = Regex("https?://([^/]+)").find(normalizedUrl)?.groupValues?.get(1) ?: return false

        // Check if it's a MegaPlay-compatible domain
        val megaPlayDomains = listOf("megaplay.buzz", "vidwish.live", "vidtube.site")
        if (megaPlayDomains.none { domain.contains(it) }) return false

        val host = "https://$domain"
        val serverName = when {
            domain.contains("vidwish") -> "Vidwish"
            domain.contains("vidtube") -> "Vidtube"
            else -> "MegaPlay"
        }
        val displayType = if (audioType.isNotBlank()) "$serverName ($audioType)" else serverName

        try {
            val pageHeaders = mapOf(
                "Referer" to "$mainUrl/",
                "User-Agent" to BROWSER_HEADERS["User-Agent"]!!
            )
            val doc = app.get(normalizedUrl, headers = pageHeaders).document

            val streamId = doc.selectFirst("#megaplay-player")?.attr("data-id")
            if (streamId.isNullOrBlank()) {
                Log.e(TAG, "No megaplay-player data-id found at: $normalizedUrl")
                return false
            }

            val ajaxH = mapOf(
                "Accept" to "*/*",
                "X-Requested-With" to "XMLHttpRequest",
                "Referer" to host
            )

            val sourcesText = app.get(
                "$host/stream/getSources?id=$streamId&id=$streamId",
                headers = ajaxH
            ).text

            Log.d(TAG, "getSources response: $sourcesText")

            val root = parseJson<MegaPlayResponse>(sourcesText)
            val m3u8 = root.sources?.file

            if (m3u8.isNullOrBlank()) {
                Log.e(TAG, "No m3u8 found in getSources response")
                return false
            }

            val playbackHeaders = mapOf(
                "Referer" to "$host/",
                "Origin" to host,
                "User-Agent" to BROWSER_HEADERS["User-Agent"]!!
            )

            callback.invoke(
                newExtractorLink(displayType, displayType, m3u8, ExtractorLinkType.M3U8) {
                    this.referer = "$host/"
                    this.quality = Qualities.Unknown.value
                    this.headers = playbackHeaders
                }
            )

            // Handle subtitles
            root.tracks?.forEach { track ->
                val file = track.file ?: return@forEach
                val kind = track.kind ?: return@forEach
                if (kind == "captions" || kind == "subtitles") {
                    subtitleCallback(newSubtitleFile(track.label ?: "Unknown", file))
                }
            }

            return true
        } catch (e: Exception) {
            Log.e(TAG, "resolveMegaPlayInline failed: ${e.message}")
            return false
        }
    }

    // Data classes
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class AjaxResponse(
        @JsonProperty("status") val status: Int? = null,
        @JsonProperty("result") val result: String? = null,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ServerGetResponse(
        @JsonProperty("status") val status: Int? = null,
        @JsonProperty("result") val result: ServerResult? = null,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ServerResult(
        @JsonProperty("url") val url: String? = null,
        @JsonProperty("skip_data") val skipData: Any? = null,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class MegaPlayResponse(
        @JsonProperty("sources") val sources: Sources? = null,
        @JsonProperty("tracks") val tracks: List<Track>? = null,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Sources(
        @JsonProperty("file") val file: String? = null,
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Track(
        @JsonProperty("file") val file: String? = null,
        @JsonProperty("label") val label: String? = null,
        @JsonProperty("kind") val kind: String? = null,
    )
}
