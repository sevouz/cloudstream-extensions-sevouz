package com.animesuge

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.LoadResponse.Companion.addAniListId
import com.lagradost.cloudstream3.LoadResponse.Companion.addMalId
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.utils.AppUtils.parseJson
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

class AnimeSuge : MainAPI() {
    override var mainUrl = "https://animesuge.cz"
    override var name = "AnimeSuge"
    override val hasMainPage = true
    override var lang = "en"
    override val hasDownloadSupport = true
    override val supportedTypes = setOf(
        TvType.Anime,
        TvType.AnimeMovie,
        TvType.OVA,
    )

    override val mainPage = mainPageOf(
        "$mainUrl/filter?page=" to "Latest",
        "$mainUrl/status/ongoing?page=" to "Ongoing",
        "$mainUrl/most-viewed?page=" to "Most Viewed",
        "$mainUrl/status/completed?page=" to "Completed",
        "$mainUrl/new-release?page=" to "New Releases",
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val url = "${request.data}$page"
        val document = app.get(url).document
        val home = document.select("div.item").mapNotNull { it.toSearchResult() }
        return newHomePageResponse(request.name, home, hasNext = home.isNotEmpty())
    }

    private fun Element.toSearchResult(): SearchResponse? {
        // Title from div.name a
        val nameTag = this.selectFirst("div.name a") ?: return null
        val title = nameTag.text().trim().ifBlank { return null }

        // URL from the poster anchor (or name anchor), strip /ep-X suffix
        val href = fixUrl(
            this.selectFirst("a.poster")?.attr("href")
                ?: nameTag.attr("href")
        ).replace(Regex("/ep-\\d+$"), "")

        // Poster uses data-src (lazy loaded)
        val posterUrl = this.selectFirst("a.poster img")?.let {
            it.attr("data-src").ifBlank { it.attr("src") }
        }

        // Sub/dub counts from span.sub and span.dub text (contains icon + number)
        val subText = this.selectFirst("span.sub")?.text()?.trim()
        val dubText = this.selectFirst("span.dub")?.text()?.trim()
        val subCount = subText?.filter { it.isDigit() }?.toIntOrNull()
        val dubCount = dubText?.filter { it.isDigit() }?.toIntOrNull()

        return newAnimeSearchResponse(title, href, TvType.Anime) {
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
        val document = app.get("$mainUrl/filter?keyword=$query").document
        return document.select("div.item").mapNotNull { it.toSearchResult() }
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document
        val animeId = document.selectFirst("[data-id]")?.attr("data-id") ?: ""

        // Title from og:title meta, strip site suffix
        val title = document.selectFirst("meta[property=og:title]")?.attr("content")
            ?.replace(Regex("\\s*[–-].*AnimeSuge.*", RegexOption.IGNORE_CASE), "")
            ?.trim()
            ?: document.title().replace(Regex("\\s*[–-].*", RegexOption.IGNORE_CASE), "").trim()
            ?: "Unknown"

        // Poster from og:image
        val poster = document.selectFirst("meta[property=og:image]")?.attr("content")

        // Description from short description block
        val description = document.selectFirst("div.description div.short div")?.text()?.trim()
            ?.replace("more+", "")?.trim()
            ?: document.selectFirst("meta[name=description]")?.attr("content")

        // Japanese name
        val japName = document.selectFirst("h1.title.d-title")?.attr("data-jp")
            ?: document.selectFirst("h2.title.d-title")?.attr("data-jp")

        val ajaxHeaders = mapOf("X-Requested-With" to "XMLHttpRequest", "Referer" to url)
        val subEpisodes = mutableListOf<Episode>()
        val dubEpisodes = mutableListOf<Episode>()

        if (animeId.isNotBlank()) {
            try {
                val epResponse = app.get("$mainUrl/ajax/episode/list/$animeId", headers = ajaxHeaders).text
                val epResult = parseJson<AjaxResponse>(epResponse)
                val epDoc = Jsoup.parse(epResult.result ?: "")

                epDoc.select("div.range a[data-num]").forEach { epLink ->
                    val epNum = epLink.text().trim().toIntOrNull() ?: return@forEach
                    val epTitle = epLink.attr("title").ifBlank { "Episode $epNum" }
                    val dataIds = epLink.attr("data-ids")
                    val hasSub = epLink.attr("data-sub") == "1"
                    val hasDub = epLink.attr("data-dub") == "1"
                    val episodeData = "$animeId|$dataIds|$epNum"

                    if (hasSub) subEpisodes.add(newEpisode(episodeData) {
                        this.name = epTitle; this.episode = epNum
                    })
                    if (hasDub) dubEpisodes.add(newEpisode("$episodeData|dub") {
                        this.name = epTitle; this.episode = epNum
                    })
                }
            } catch (_: Exception) {}
        }

        var year: Int? = null
        var malId: Int? = null
        var aniListId: Int? = null
        var status: ShowStatus? = null
        val tags = mutableListOf<String>()

        // Info from div.meta rows
        document.select("div.meta > div").forEach { item ->
            val label = item.selectFirst("div")?.text()?.trim() ?: ""
            val value = item.selectFirst("span")?.text()?.trim() ?: ""
            when {
                label.startsWith("Premiered") || label.startsWith("Aired") ->
                    year = Regex("(\\d{4})").find(value)?.groupValues?.get(1)?.toIntOrNull()
                label.startsWith("Status") -> status = when {
                    value.contains("Airing", true) -> ShowStatus.Ongoing
                    value.contains("Finished", true) || value.contains("Completed", true) -> ShowStatus.Completed
                    else -> null
                }
                label.startsWith("Genre") ->
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
        val parts = data.split("|")
        if (parts.size < 3) return false
        val dataIds = parts[1]
        val isDub = parts.size > 3 && parts[3] == "dub"
        val ajaxHeaders = mapOf("X-Requested-With" to "XMLHttpRequest", "Referer" to mainUrl)

        val serverResponse = app.get("$mainUrl/ajax/server/list?servers=$dataIds", headers = ajaxHeaders).text
        val serverResult = parseJson<AjaxResponse>(serverResponse)
        val serverDoc = Jsoup.parse(serverResult.result ?: return false)

        val subServers = serverDoc.select("div.server-type[data-type=sub] div.server[data-link-id]")
        val dubServers = serverDoc.select("div.server-type[data-type=dub] div.server[data-link-id]")
        val typeLabels = mutableListOf<Pair<String, org.jsoup.select.Elements>>()

        if (isDub) {
            if (dubServers.isNotEmpty()) typeLabels.add("Dub" to dubServers)
            if (subServers.isNotEmpty()) typeLabels.add("Sub" to subServers)
        } else {
            if (subServers.isNotEmpty()) typeLabels.add("Sub" to subServers)
            if (dubServers.isNotEmpty()) typeLabels.add("Dub" to dubServers)
        }
        if (typeLabels.isEmpty()) {
            val all = serverDoc.select("div.server[data-link-id]")
            if (all.isNotEmpty()) typeLabels.add("" to all)
        }

        for ((audioType, serverItems) in typeLabels) {
            for (server in serverItems) {
                val linkId = server.attr("data-link-id").ifBlank { continue }
                val serverName = server.text().trim()
                try {
                    val embedResponse = app.get("$mainUrl/ajax/server?get=$linkId", headers = ajaxHeaders).text
                    val embedResult = parseJson<ServerGetResponse>(embedResponse)
                    val embedUrl = embedResult.result?.url ?: continue
                    val sourceName = if (audioType.isNotBlank()) "$serverName ($audioType)" else serverName

                    val collected = mutableListOf<ExtractorLink>()
                    loadExtractor(embedUrl, mainUrl, subtitleCallback) { collected.add(it) }

                    for (link in collected) {
                        callback.invoke(newExtractorLink(
                            if (audioType.isNotBlank()) "${link.source} ($audioType)" else link.source,
                            sourceName, link.url, link.type
                        ) {
                            this.referer = link.referer
                            this.quality = link.quality
                            this.headers = link.headers
                        })
                    }
                } catch (_: Exception) {}
            }
        }
        return true
    }

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
}
