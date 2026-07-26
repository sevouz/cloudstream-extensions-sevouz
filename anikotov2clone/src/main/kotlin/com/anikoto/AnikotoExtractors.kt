package com.anikoto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.newSubtitleFile
import com.lagradost.cloudstream3.utils.AppUtils.tryParseJson
import com.lagradost.cloudstream3.utils.ExtractorApi
import com.lagradost.cloudstream3.utils.ExtractorLink
import com.lagradost.cloudstream3.utils.M3u8Helper.Companion.generateM3u8

open class MegaPlay : ExtractorApi() {
    override val name = "MegaPlay"
    override val mainUrl = "https://megaplay.buzz"
    override val requiresReferer = true

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        extractMegaPlayUrl(url, referer, mainUrl, name, subtitleCallback, callback)
    }

    companion object {
        suspend fun extractMegaPlayUrl(
            url: String,
            referer: String?,
            host: String,
            serverName: String,
            subtitleCallback: (SubtitleFile) -> Unit,
            callback: (ExtractorLink) -> Unit,
            audioType: String? = null
        ) {
            try {
                val pageHeaders = mapOf(
                    "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36",
                    "Referer" to (referer ?: "https://anikototv.to/")
                )

                val doc = app.get(url, headers = pageHeaders).document

                val player = doc.selectFirst("#megaplay-player")
                val id = player?.attr("data-id")?.takeIf { it.isNotBlank() }
                    ?: player?.attr("data-realid")?.takeIf { it.isNotBlank() }
                    ?: Regex("/stream/s-\\d+/(\\d+)/").find(url)?.groupValues?.getOrNull(1)
                    ?: return

                val type = if (url.contains("/dub", ignoreCase = true) ||
                    audioType.equals("dub", ignoreCase = true)
                ) "dub" else "sub"

                val ajaxHeaders = mapOf(
                    "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36",
                    "Accept" to "*/*",
                    "X-Requested-With" to "XMLHttpRequest",
                    "Origin" to host,
                    "Referer" to url
                )

                val res = app.get(
                    "$host/stream/getSources?id=$id&type=$type",
                    headers = ajaxHeaders,
                    referer = url
                ).parsedSafe<MegaPlayResponse>() ?: return

                val m3u8 = res.sources?.file ?: return

                val playbackHeaders = mapOf(
                    "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36",
                    "Accept" to "*/*",
                    "Origin" to host,
                    "Referer" to "$host/"
                )

                generateM3u8(
                    "$serverName [${type.uppercase()}]",
                    m3u8,
                    host,
                    headers = playbackHeaders
                ).forEach(callback)

                res.tracks.forEach { track ->
                    if (track.kind == "captions" || track.kind == "subtitles") {
                        val file = track.file ?: return@forEach
                        subtitleCallback(
                            newSubtitleFile(track.label ?: "English", file) {
                                this.headers = playbackHeaders
                            }
                        )
                    }
                }
            } catch (_: Exception) {
                // ignore extractor failure
            }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class MegaPlayResponse(
        @JsonProperty("sources") val sources: Sources? = null,
        @JsonProperty("tracks") val tracks: List<Track> = emptyList()
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Sources(
        @JsonProperty("file") val file: String? = null
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Track(
        @JsonProperty("file") val file: String? = null,
        @JsonProperty("label") val label: String? = null,
        @JsonProperty("kind") val kind: String? = null
    )
}

class Vidwish : MegaPlay() {
    override val name = "Vidwish"
    override val mainUrl = "https://vidwish.live"
}

class Vidtube : MegaPlay() {
    override val name = "Vidtube"
    override val mainUrl = "https://vidtube.site"
}
