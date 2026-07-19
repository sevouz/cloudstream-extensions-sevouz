package com.anikotov2

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.lagradost.api.Log
import com.lagradost.cloudstream3.SubtitleFile
import com.lagradost.cloudstream3.app
import com.lagradost.cloudstream3.network.WebViewResolver
import com.lagradost.cloudstream3.newSubtitleFile
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.utils.AppUtils.parseJson

open class MegaPlayExtractor : ExtractorApi() {
    override val name = "MegaPlay"
    override val mainUrl = "https://megaplay.buzz"
    override val requiresReferer = true

    companion object {
        private const val TAG = "MegaPlayV2"

        suspend fun extractMegaPlayUrl(
            url: String,
            referer: String?,
            host: String,
            serverName: String,
            subtitleCallback: (SubtitleFile) -> Unit,
            callback: (ExtractorLink) -> Unit
        ) {
            val playbackHeaders = mapOf(
                "Referer" to "$host/",
                "Origin" to host,
                "User-Agent" to "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"
            )
            val pageHeaders = mapOf(
                "Referer" to (referer ?: "https://anikototv.to/"),
                "User-Agent" to "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"
            )

            try {
                val doc = app.get(url, headers = pageHeaders).document
                val playerEl = doc.selectFirst("#megaplay-player")
                val streamId = playerEl?.attr("data-id")

                if (streamId.isNullOrBlank()) {
                    Log.e(TAG, "No data-id at $url for $serverName, trying WebView")
                    fallbackWebView(url, host, serverName, playbackHeaders, callback)
                    return
                }

                val type = playerEl.attr("data-type").ifBlank { "m3u8" }
                Log.d(TAG, "[$serverName] streamId=$streamId type=$type")

                val ajaxHeaders = mapOf(
                    "Accept" to "*/*",
                    "X-Requested-With" to "XMLHttpRequest",
                    "Referer" to host
                )

                val jsonText = app.get("$host/stream/getSources?id=$streamId&id=$streamId", headers = ajaxHeaders).text
                Log.d(TAG, "[$serverName] getSources: $jsonText")

                val root = parseJson<MegaPlayResponse>(jsonText)
                val m3u8 = root.sources?.file

                if (m3u8.isNullOrBlank()) {
                    Log.e(TAG, "[$serverName] no m3u8, trying WebView")
                    fallbackWebView(url, host, serverName, playbackHeaders, callback)
                    return
                }

                callback.invoke(
                    newExtractorLink(serverName, serverName, m3u8, ExtractorLinkType.M3U8) {
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
            } catch (e: Exception) {
                Log.e(TAG, "[$serverName] extractMegaPlayUrl failed: ${e.message}")
                fallbackWebView(url, host, serverName, playbackHeaders, callback)
            }
        }

        private suspend fun fallbackWebView(
            url: String,
            host: String,
            serverName: String,
            playbackHeaders: Map<String, String>,
            callback: (ExtractorLink) -> Unit
        ) {
            try {
                val jsToClickPlay = """
                    (() => {
                        const btn = document.querySelector('.jw-icon-display.jw-button-color.jw-reset');
                        if (btn) { btn.click(); return "clicked"; }
                        return "not found";
                    })();
                """.trimIndent()

                val m3u8Resolver = WebViewResolver(
                    interceptUrl = Regex("""master\.m3u8|index\.m3u8|playlist\.m3u8|\.m3u8"""),
                    additionalUrls = listOf(Regex("""\.m3u8""")),
                    script = jsToClickPlay,
                    useOkhttp = false,
                    timeout = 15_000L
                )

                val fallbackM3u8 = app.get(url = url, referer = host, interceptor = m3u8Resolver).url

                callback.invoke(
                    newExtractorLink(serverName, serverName, fallbackM3u8, ExtractorLinkType.M3U8) {
                        this.referer = "$host/"
                        this.quality = Qualities.Unknown.value
                        this.headers = playbackHeaders
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "[$serverName] WebView fallback failed: ${e.message}")
            }
        }
    }

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        extractMegaPlayUrl(url, referer, mainUrl, name, subtitleCallback, callback)
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
