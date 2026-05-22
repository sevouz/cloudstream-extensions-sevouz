package com.cncverse

import com.lagradost.api.Log
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.newSubtitleFile
import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.ExtractorLinkType
import com.lagradost.cloudstream3.utils.M3u8Helper
import com.lagradost.cloudstream3.utils.Qualities
import com.lagradost.cloudstream3.utils.newExtractorLink
import com.fasterxml.jackson.annotation.JsonProperty

class OneAnimeExtractor : ExtractorApi() {
    override val name = "1Anime"
    override val mainUrl = "https://1anime.site"
    override val requiresReferer = false

    companion object {
        private const val TAG = "OneAnimeExtractor"
    }

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        val headers = mapOf(
            "Referer" to (referer ?: "https://aniwatch.co.at/"),
            "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36"
        )

        try {
            // URL is like: https://1anime.site/megaplay/stream/s-2/170548/sub
            // This is a megaplay-style page, similar to megaplay.buzz
            val pageResponse = app.get(url, headers = headers)
            val document = pageResponse.document

            // Look for megaplay-player data-id
            val dataId = document.selectFirst("#megaplay-player")?.attr("data-id")

            if (!dataId.isNullOrBlank()) {
                Log.d(TAG, "Found megaplay data-id: $dataId")

                // Call getSources API
                val sourcesUrl = "$mainUrl/megaplay/stream/getSources?id=$dataId&id=$dataId"
                val sourcesHeaders = mapOf(
                    "Accept" to "*/*",
                    "X-Requested-With" to "XMLHttpRequest",
                    "Referer" to url
                )

                val sourcesResponse = app.get(sourcesUrl, headers = sourcesHeaders).text
                Log.d(TAG, "Sources response: $sourcesResponse")

                val parsed = com.lagradost.cloudstream3.utils.AppUtils.parseJson<MegaPlayResponse>(sourcesResponse)
                val m3u8 = parsed.sources?.file

                if (!m3u8.isNullOrBlank()) {
                    val streamHeaders = mapOf(
                        "Referer" to "$mainUrl/",
                        "Origin" to mainUrl
                    )
                    M3u8Helper.generateM3u8(name, m3u8, mainUrl, headers = streamHeaders).forEach(callback)
                }

                // Subtitles
                parsed.tracks?.forEach { track ->
                    if (track.kind == "captions" || track.kind == "subtitles") {
                        subtitleCallback(newSubtitleFile(track.label, track.file))
                    }
                }
            } else {
                // Fallback: maybe it's a direct video page
                Log.d(TAG, "No megaplay-player found, trying direct video extraction")
                val videoSrc = document.selectFirst("video source")?.attr("src")
                    ?: document.selectFirst("video")?.attr("src")

                if (!videoSrc.isNullOrBlank()) {
                    callback.invoke(
                        newExtractorLink(
                            name,
                            name,
                            videoSrc,
                            ExtractorLinkType.VIDEO
                        ) {
                            this.referer = url
                            this.quality = Qualities.P1080.value
                        }
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting from $url: ${e.message}")
        }
    }

    data class MegaPlayResponse(
        @JsonProperty("sources") val sources: Sources?,
        @JsonProperty("tracks") val tracks: List<Track>?,
    )

    data class Sources(
        @JsonProperty("file") val file: String?,
    )

    data class Track(
        @JsonProperty("file") val file: String,
        @JsonProperty("label") val label: String,
        @JsonProperty("kind") val kind: String,
    )
}
