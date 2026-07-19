package com.anikotov2

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.api.Log
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.newSubtitleFile
import com.lagradost.cloudstream3.utils.*

open class MegaPlayExtractor : ExtractorApi() {
    override val name = "MegaPlay"
    override val mainUrl = "https://megaplay.buzz"
    override val requiresReferer = true

    companion object {
        private const val TAG = "MegaPlayExtractor"

        private val PLAYBACK_HEADERS = mapOf(
            "User-Agent" to "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"
        )
    }

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        extractMegaPlay(url, referer, mainUrl, name, subtitleCallback, callback)
    }

    suspend fun extractMegaPlay(
        url: String,
        referer: String?,
        host: String,
        serverName: String,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        try {
            val embedHeaders = mapOf(
                "Referer" to (referer ?: "https://anikototv.to/"),
                "User-Agent" to PLAYBACK_HEADERS["User-Agent"]!!
            )

            val document = app.get(url, headers = embedHeaders).document
            val streamId = document.selectFirst("#megaplay-player")?.attr("data-id")

            if (streamId.isNullOrBlank()) {
                Log.e(TAG, "No megaplay-player data-id at: $url")
                return
            }

            Log.d(TAG, "Found data-id: $streamId")

            val ajaxHeaders = mapOf(
                "Accept" to "*/*",
                "X-Requested-With" to "XMLHttpRequest",
                "Referer" to host
            )

            val sourcesText = app.get(
                "$host/stream/getSources?id=$streamId&id=$streamId",
                headers = ajaxHeaders
            ).text

            Log.d(TAG, "getSources: $sourcesText")

            val response = AppUtils.parseJson<MegaPlayResponse>(sourcesText)
            val m3u8 = response.sources?.file

            if (m3u8.isNullOrBlank()) {
                Log.e(TAG, "No m3u8 found in response")
                return
            }

            val streamHeaders = mapOf(
                "Referer" to "$host/",
                "Origin" to host,
                "User-Agent" to PLAYBACK_HEADERS["User-Agent"]!!
            )

            callback.invoke(
                newExtractorLink(serverName, serverName, m3u8, ExtractorLinkType.M3U8) {
                    this.referer = "$host/"
                    this.quality = Qualities.Unknown.value
                    this.headers = streamHeaders
                }
            )

            // Subtitles
            response.tracks?.forEach { track ->
                val file = track.file ?: return@forEach
                val kind = track.kind ?: return@forEach
                if (kind == "captions" || kind == "subtitles") {
                    subtitleCallback(newSubtitleFile(track.label ?: "Unknown", file))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "extractMegaPlay failed for $serverName: ${e.message}")
        }
    }

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

class VidwishExtractor : MegaPlayExtractor() {
    override val name = "Vidwish"
    override val mainUrl = "https://vidwish.live"
}

class VidtubeExtractor : MegaPlayExtractor() {
    override val name = "Vidtube"
    override val mainUrl = "https://vidtube.site"
}
