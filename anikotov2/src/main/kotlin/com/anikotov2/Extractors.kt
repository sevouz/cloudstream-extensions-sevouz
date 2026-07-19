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
    }

    override suspend fun getUrl(
        url: String,
        referer: String?,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        try {
            val embedHeaders = mapOf(
                "Referer" to (referer ?: "https://anikototv.to/"),
                "User-Agent" to "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"
            )

            val document = app.get(url, headers = embedHeaders).document
            val streamId = document.selectFirst("#megaplay-player")?.attr("data-id")

            if (streamId.isNullOrBlank()) {
                Log.e(TAG, "No megaplay-player data-id at: $url, trying WebView fallback")
                fallbackWebView(url, subtitleCallback, callback)
                return
            }

            Log.d(TAG, "Found data-id: $streamId for $name")

            val ajaxHeaders = mapOf(
                "Accept" to "*/*",
                "X-Requested-With" to "XMLHttpRequest",
                "Referer" to mainUrl
            )

            val sourcesText = app.get(
                "$mainUrl/stream/getSources?id=$streamId&id=$streamId",
                headers = ajaxHeaders
            ).text

            Log.d(TAG, "getSources ($name): $sourcesText")

            val response = parseJson<MegaPlayResponse>(sourcesText)
            val m3u8 = response.sources?.file

            if (m3u8.isNullOrBlank()) {
                Log.e(TAG, "No m3u8 in response for $name, trying WebView fallback")
                fallbackWebView(url, subtitleCallback, callback)
                return
            }

            val streamHeaders = mapOf(
                "Referer" to "$mainUrl/",
                "Origin" to mainUrl,
                "User-Agent" to "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"
            )

            callback.invoke(
                newExtractorLink(name, name, m3u8, ExtractorLinkType.M3U8) {
                    this.referer = "$mainUrl/"
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
            Log.e(TAG, "$name primary failed: ${e.message}, trying WebView")
            fallbackWebView(url, subtitleCallback, callback)
        }
    }

    private suspend fun fallbackWebView(
        url: String,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        Log.d(TAG, "WebView fallback for $name: $url")

        val jsToClickPlay = """
            (() => {
                const btn = document.querySelector('.jw-icon-display.jw-button-color.jw-reset');
                if (btn) { btn.click(); return "clicked"; }
                return "button not found";
            })();
        """.trimIndent()

        val m3u8Resolver = WebViewResolver(
            interceptUrl = Regex("""master\.m3u8|index\.m3u8|playlist\.m3u8|\.m3u8"""),
            additionalUrls = listOf(Regex("""\.m3u8""")),
            script = jsToClickPlay,
            scriptCallback = { result -> Log.d(TAG, "JS Result: $result") },
            useOkhttp = false,
            timeout = 15_000L
        )

        try {
            val fallbackM3u8 = app.get(url = url, referer = mainUrl, interceptor = m3u8Resolver).url

            callback.invoke(
                newExtractorLink(name, name, fallbackM3u8, ExtractorLinkType.M3U8) {
                    this.referer = "$mainUrl/"
                    this.quality = Qualities.Unknown.value
                    this.headers = mapOf(
                        "Referer" to "$mainUrl/",
                        "Origin" to mainUrl
                    )
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "$name WebView fallback also failed: ${e.message}")
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
