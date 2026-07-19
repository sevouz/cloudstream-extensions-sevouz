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
    override val supportedTypes = setOf(TvType.Anime, TvType.AnimeMovie, TvType.OVA)

    companion object {
        private const val TAG = "AniKotoV2"
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
        val url = if (page > 1) "${request.data}?page=$page" else request.data
        val document = app.get(url, headers = browserHeaders).document
        val home = document.select("div#list-items div.item").mapNotNull { it.toSearchResult() }
        return newHomePageResponse(request.name, home, hasNext = home.isNotEmpty())
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val nameTag = selectFirst("a.name.d-title") ?: return null
        val title = nameTag.text().trim().ifBlank { return null }
        val href = fixUrl(nameTag.attr("href")).replace(Regex("/ep-\\d+$"), "")
        val posterUrl = selectFirst("div.ani.poster img")?.attr("src")
        val subCount = selectFirst("span.ep-status.sub span")?.text()?.trim()?.toIntOrNull()
        val dubCount = selectFirst("span.ep-status.dub span")?.text()?.trim()?.toIntOrNull()

        return newAnimeSearchResponse(title, href, TvType.Anime) {
            this.posterUrl = posterUrl
            addDubStatus(dubExist = dubCount != null && dubCount > 0, subExist = subCount != null && subCount > 0, dubEpisodes = dubCount, subEpisodes = subCount)
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val document = app.get("$mainUrl/filter?keyword=$query", headers = browserHeaders).document
        return document.select("div#list-items div.item").mapNotNull { it.toSearchResult() }
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url, headers = browserHeaders).document
        val watchMain = document.selectFirst("div#watch-main")
        val animeId = watchMain?.attr("data-id") ?: ""

        val title = document.selectFirst("div#w-info h1.title")?.text()?.trim() ?: "Unknown"
        val poster = document.selectFirst("div#w-info div.poster img")?.attr("src")
        val description = document.selectFirst("div#w-info div.synopsis div.content")?.text()?.trim()
        val japName = document.selectFirst("h1.title.d-title")?.attr("data-jp")

        val subEpisodes = mutableListOf<Episode>()
        val dubEpisodes = mutableListOf<Episode>()

        if (animeId.isNotBlank()) {
            try {
                val epResponse = app.get("$mainUrl/ajax/episode/list/$animeId", headers = ajaxHeaders(url)).text
                val epResult = parseJson<AjaxResponse>(epResponse)
                val epDoc = Jsoup.parse(epResult.result ?: "")

                epDoc.select("ul.ep-range li a").forEach { epLink ->
                    val epNum = epLink.attr("data-num").toIntOrNull() ?: return@forEach
                    val epTitle = epLink.selectFirst("span.d-title")?.text() ?: "Episode $epNum"
                    val epHref = epLink.attr("href")
                    val dataIds = epLink.attr("data-ids")
                    val hasSub = epLink.attr("data-sub") == "1"
                    val hasDub = epLink.attr("data-dub") == "1"

                    // Store episode URL path for resolveFromWatchPage approach
                    val episodeUrl = if (epHref.isNotBlank()) fixUrl(epHref) else "$url/ep-$epNum"

                    if (hasSub) {
                        subEpisodes.add(newEpisode("$episodeUrl|||$dataIds|sub") {
                            this.name = epTitle
                            this.episode = epNum
                        })
                    }
                    if (hasDub) {
                        dubEpisodes.add(newEpisode("$episodeUrl|||$dataIds|dub") {
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

        document.select("div#w-info div.bmeta div.meta div").forEach { item ->
            val text = item.text()
            when {
                text.startsWith("Premiered:") || text.startsWith("Aired:") ->
                    year = Regex("(\\d{4})").find(text)?.groupValues?.get(1)?.toIntOrNull()
                text.startsWith("Status:") -> status = when {
                    text.contains("Airing", true) -> ShowStatus.Ongoing
                    text.contains("Finished", true) -> ShowStatus.Completed
                    else -> null
                }
                text.startsWith("Genres:") || text.startsWith("Genre:") ->
                    tags.addAll(item.select("a").map { it.text().trim() })
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
            if (dubEpisodes.isNotEmpty()) addEpisodes(DubStatus.Dubbed, dubEpisodes)
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
        // data format: episodeUrl|||serverIds|audioType
        val parts = data.split("|||")
        val ep = parts[0]  // full episode URL
        val serverParts = parts.getOrNull(1)?.split("|") ?: listOf()
        val serverIds = serverParts.getOrNull(0) ?: ""
        val audioType = serverParts.getOrNull(1) ?: "sub"

        val referer = ep

        // Try direct server resolution first
        if (serverIds.isNotBlank()) {
            val found = resolveServers(serverIds, audioType, referer, subtitleCallback, callback)
            if (found) return true
        }

        // Fallback: load the watch page and resolve from there
        return resolveFromWatchPage(ep, subtitleCallback, callback)
    }

    private suspend fun resolveServers(
        serverIds: String,
        audioType: String,
        referer: String,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        try {
            val serverResponse = app.get(
                "$mainUrl/ajax/server/list?servers=$serverIds",
                headers = ajaxHeaders(referer)
            ).text
            val serverResult = parseJson<AjaxResponse>(serverResponse)
            val serverDoc = Jsoup.parse(serverResult.result ?: return false)

            // Select servers based on audio type preference
            val typeSelector = if (audioType == "dub") "dub" else "sub"
            val preferredServers = serverDoc.select("div.type[data-type=$typeSelector] li[data-link-id]")
            val allServers = if (preferredServers.isEmpty()) serverDoc.select("li[data-link-id]") else preferredServers

            var found = false
            for (server in allServers) {
                val linkId = server.attr("data-link-id").ifBlank { continue }
                try {
                    val embedJson = app.get("$mainUrl/ajax/server?get=$linkId", headers = ajaxHeaders(referer)).text
                    val embedResult = parseJson<ServerGetResponse>(embedJson)
                    val embedUrl = embedResult.result?.url ?: continue

                    found = resolveEmbedInline(embedUrl, referer, audioType, subtitleCallback, callback) || found
                } catch (e: Exception) {
                    Log.e(TAG, "Server $linkId error: ${e.message}")
                }
            }

            // Also try the other audio type
            val otherType = if (audioType == "dub") "sub" else "dub"
            val otherServers = serverDoc.select("div.type[data-type=$otherType] li[data-link-id]")
            for (server in otherServers) {
                val linkId = server.attr("data-link-id").ifBlank { continue }
                try {
                    val embedJson = app.get("$mainUrl/ajax/server?get=$linkId", headers = ajaxHeaders(referer)).text
                    val embedResult = parseJson<ServerGetResponse>(embedJson)
                    val embedUrl = embedResult.result?.url ?: continue

                    found = resolveEmbedInline(embedUrl, referer, otherType, subtitleCallback, callback) || found
                } catch (e: Exception) {
                    Log.e(TAG, "Server $linkId error: ${e.message}")
                }
            }

            return found
        } catch (e: Exception) {
            Log.e(TAG, "resolveServers failed: ${e.message}")
            return false
        }
    }

    private suspend fun resolveFromWatchPage(
        episodeUrl: String,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        try {
            val doc = app.get(episodeUrl, headers = browserHeaders).document
            val watchMain = doc.selectFirst("div#watch-main") ?: return false
            val animeId = watchMain.attr("data-id").ifBlank { return false }

            // Get episode number from URL
            val epNum = Regex("/ep-(\\d+)").find(episodeUrl)?.groupValues?.get(1)

            // Get episode list
            val epListJson = app.get("$mainUrl/ajax/episode/list/$animeId", headers = ajaxHeaders(episodeUrl)).text
            val epListResult = parseJson<AjaxResponse>(epListJson)
            val epDoc = Jsoup.parse(epListResult.result ?: return false)

            // Find the target episode
            val targetEp = if (epNum != null) {
                epDoc.select("ul.ep-range li a").firstOrNull { it.attr("data-num") == epNum }
            } else {
                epDoc.select("ul.ep-range li a").lastOrNull()
            } ?: return false

            val serverIds = targetEp.attr("data-ids")
            val audioType = if (targetEp.attr("data-dub") == "1") "dub" else "sub"

            if (serverIds.isBlank()) return false
            return resolveServers(serverIds, audioType, episodeUrl, subtitleCallback, callback)
        } catch (e: Exception) {
            Log.e(TAG, "resolveFromWatchPage failed: ${e.message}")
            return false
        }
    }

    private suspend fun resolveEmbedInline(
        url: String,
        referer: String,
        audioType: String,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val normalizedUrl = url.replace("http://", "https://")
        val domain = Regex("https?://([^/]+)").find(normalizedUrl)?.groupValues?.get(1) ?: return false

        // Check if it's a MegaPlay-family domain
        val isMegaPlayDomain = domain.contains("megaplay") || domain.contains("vidwish") || domain.contains("vidtube")

        return if (isMegaPlayDomain) {
            resolveMegaPlayInline(normalizedUrl, referer, domain, audioType, subtitleCallback, callback)
        } else {
            // Use loadExtractor for unknown domains
            var found = false
            val collected = mutableListOf<ExtractorLink>()
            loadExtractor(normalizedUrl, referer, subtitleCallback) { link ->
                collected.add(link)
            }
            for (link in collected) {
                val displayName = if (audioType.isNotBlank()) "${link.source} (${audioType.replaceFirstChar { it.uppercase() }})" else link.source
                callback.invoke(
                    newExtractorLink(displayName, displayName, link.url, link.type) {
                        this.referer = link.referer
                        this.quality = link.quality
                        this.headers = link.headers
                    }
                )
                found = true
            }
            found
        }
    }

    private suspend fun resolveMegaPlayInline(
        url: String,
        referer: String,
        domain: String,
        audioType: String,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        val host = "https://$domain"
        val serverName = when {
            domain.contains("vidwish") -> "Vidwish"
            domain.contains("vidtube") -> "Vidtube"
            else -> "MegaPlay"
        }
        val type = if (audioType.isNotBlank()) "$serverName (${audioType.replaceFirstChar { it.uppercase() }})" else serverName

        val pageHeaders = mapOf(
            "Referer" to referer,
            "User-Agent" to browserHeaders["User-Agent"]!!
        )
        val playbackHeaders = mapOf(
            "Referer" to "$host/",
            "Origin" to host,
            "User-Agent" to browserHeaders["User-Agent"]!!
        )

        try {
            val doc = app.get(url, headers = pageHeaders).document
            val playerEl = doc.selectFirst("#megaplay-player")
            val streamId = playerEl?.attr("data-id")

            if (streamId.isNullOrBlank()) {
                Log.e(TAG, "No data-id at $url")
                return false
            }

            val ajaxH = mapOf(
                "Accept" to "*/*",
                "X-Requested-With" to "XMLHttpRequest",
                "Referer" to host
            )

            val jsonText = app.get("$host/stream/getSources?id=$streamId&id=$streamId", headers = ajaxH).text
            val root = parseJson<MegaPlayResponse>(jsonText)
            val m3u8 = root.sources?.file

            if (m3u8.isNullOrBlank()) {
                Log.e(TAG, "No m3u8 for $serverName")
                return false
            }

            callback.invoke(
                newExtractorLink(type, type, m3u8, ExtractorLinkType.M3U8) {
                    this.referer = "$host/"
                    this.quality = Qualities.Unknown.value
                    this.headers = playbackHeaders
                }
            )

            // Subtitles
            root.tracks?.forEach { track ->
                val file = track.file ?: return@forEach
                val kind = track.kind ?: return@forEach
                if (kind == "captions" || kind == "subtitles") {
                    val label = track.label ?: "Unknown"
                    subtitleCallback(newSubtitleFile(label, file))
                }
            }

            return true
        } catch (e: Exception) {
            Log.e(TAG, "resolveMegaPlayInline failed for $serverName: ${e.message}")
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
