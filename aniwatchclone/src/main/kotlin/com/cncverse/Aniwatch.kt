package com.cncverse

import android.util.Base64
import com.lagradost.api.Log
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import org.jsoup.nodes.Element

class Aniwatch : MainAPI() {
    override var mainUrl = "https://aniwatch.co.at"
    override var name = "Aniwatch"
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
        private const val TAG = "Aniwatch"
    }

    override val mainPage = mainPageOf(
        "$mainUrl/recently-updated/?page=" to "Recently Updated",
        "$mainUrl/top-airing/?page=" to "Top Airing",
        "$mainUrl/most-popular-anime/?page=" to "Most Popular",
        "$mainUrl/new-anime/?page=" to "New On Aniwatch",
        "$mainUrl/latest-completed-anime/?page=" to "Completed",
    )

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val url = "${request.data}$page"
        val document = app.get(url).document
        val home = document.select("div.flw-item").mapNotNull { it.toSearchResult() }
        return newHomePageResponse(request.name, home, hasNext = home.isNotEmpty())
    }

    private fun Element.toSearchResult(): SearchResponse? {
        val titleEl = this.selectFirst("h3.film-name a") ?: return null
        val title = titleEl.text().trim().ifBlank { return null }
        val href = fixUrl(titleEl.attr("href"))

        val posterUrl = this.selectFirst("img.film-poster-img")?.attr("data-src")
            ?: this.selectFirst("img.film-poster-img")?.attr("src")
            ?: this.selectFirst("img")?.attr("data-src")

        val subText = this.selectFirst("div.tick-sub")?.text()?.trim()?.toIntOrNull()
        val dubText = this.selectFirst("div.tick-dub")?.text()?.trim()?.toIntOrNull()

        return newAnimeSearchResponse(title, href, TvType.Anime) {
            this.posterUrl = posterUrl
            addDubStatus(
                dubExist = dubText != null && dubText > 0,
                subExist = subText != null && subText > 0,
                dubEpisodes = dubText,
                subEpisodes = subText
            )
        }
    }

    override suspend fun search(query: String): List<SearchResponse> {
        val url = "$mainUrl/?s=$query"
        val document = app.get(url).document
        return document.select("div.flw-item").mapNotNull { it.toSearchResult() }
    }

    override suspend fun load(url: String): LoadResponse {
        val document = app.get(url).document

        // Anime detail page: /anime/slug/
        val title = document.selectFirst("h2.film-name")?.text()?.trim()
            ?: document.selectFirst("h1")?.text()?.trim()
            ?: "Unknown"

        val poster = document.selectFirst("img.film-poster-img")?.attr("data-src")
            ?: document.selectFirst("img.film-poster-img")?.attr("src")

        val description = document.selectFirst("div.film-description div.text")?.text()?.trim()
            ?: document.selectFirst("div.description")?.text()?.trim()

        // Extract metadata
        var year: Int? = null
        var status: ShowStatus? = null
        val tags = mutableListOf<String>()

        document.select("div.anisc-info div.item, div.spe span").forEach { item ->
            val text = item.text()
            when {
                text.contains("Aired:") || text.contains("Premiered:") -> {
                    year = Regex("(\\d{4})").find(text)?.groupValues?.get(1)?.toIntOrNull()
                }
                text.contains("Status:") -> {
                    status = when {
                        text.contains("Airing", true) -> ShowStatus.Ongoing
                        text.contains("Finished", true) || text.contains("Completed", true) -> ShowStatus.Completed
                        else -> null
                    }
                }
            }
        }
        document.select("div.genres a, a[href*=genre]").forEach {
            val genre = it.text().trim()
            if (genre.isNotBlank() && genre.length < 30) tags.add(genre)
        }

        // Get episodes - the detail page links to episode pages
        // Episode URLs follow pattern: /slug-episode-N-english-sub/
        val episodes = mutableListOf<Episode>()
        document.select("ul.ep-range li a, div.ep-range a, a[href*=episode]").forEach { epLink ->
            val epHref = epLink.attr("href")
            if (epHref.contains("episode") && epHref.startsWith("http")) {
                val epNum = Regex("episode-(\\d+)").find(epHref)?.groupValues?.get(1)?.toIntOrNull()
                episodes.add(newEpisode(epHref) {
                    this.name = "Episode $epNum"
                    this.episode = epNum
                })
            }
        }

        // If no episodes found on detail page, this might be an episode page itself
        if (episodes.isEmpty() && url.contains("episode")) {
            val epNum = Regex("episode-(\\d+)").find(url)?.groupValues?.get(1)?.toIntOrNull()
            episodes.add(newEpisode(url) {
                this.name = "Episode $epNum"
                this.episode = epNum
            })
        }

        return newAnimeLoadResponse(title, url, TvType.Anime) {
            engName = title
            this.posterUrl = poster
            this.year = year
            this.showStatus = status
            this.plot = description
            this.tags = tags.distinct().ifEmpty { null }
            addEpisodes(DubStatus.Subbed, episodes)
        }
    }

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        // data is the episode page URL
        val document = app.get(data).document

        // Find all server items with data-hash (base64 encoded URLs)
        document.select("div.server-item[data-hash]").forEach { server ->
            val hash = server.attr("data-hash")
            val serverName = server.attr("data-server-name")
            val serverType = server.attr("data-type") // sub or dub

            if (hash.isBlank()) return@forEach

            try {
                val decoded = String(Base64.decode(hash, Base64.DEFAULT)).trim()
                Log.d(TAG, "Server: $serverName ($serverType), Decoded: $decoded")

                when {
                    // Direct MP4 player (my.1anime.site)
                    decoded.contains("1anime.site/index.php") -> {
                        val fileParam = Regex("[?&]file=([^&]+)").find(decoded)?.groupValues?.get(1)
                        if (fileParam != null) {
                            val mp4Url = "https://my.1anime.site/videos/$fileParam"
                            callback.invoke(
                                newExtractorLink(
                                    "Aniwatch $serverName [$serverType]",
                                    "Aniwatch $serverName [$serverType]",
                                    mp4Url,
                                    ExtractorLinkType.VIDEO
                                ) {
                                    this.referer = "https://my.1anime.site/"
                                    this.quality = Qualities.P1080.value
                                }
                            )
                        }
                    }
                    // MegaPlay-style streams (1anime.site/megaplay/stream/...)
                    decoded.contains("1anime.site/megaplay") || decoded.contains("megaplay") -> {
                        OneAnimeExtractor().getUrl(decoded, mainUrl, subtitleCallback, callback)
                    }
                    // Vidup iframe
                    decoded.contains("vidup.site") -> {
                        val iframeSrc = Regex("""src="([^"]+)"""").find(decoded)?.groupValues?.get(1)
                        if (iframeSrc != null) {
                            loadExtractor(iframeSrc, mainUrl, subtitleCallback, callback)
                        }
                    }
                    // Generic URL
                    decoded.startsWith("http") -> {
                        loadExtractor(decoded, mainUrl, subtitleCallback, callback)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing server $serverName: ${e.message}")
            }
        }

        return true
    }
}
