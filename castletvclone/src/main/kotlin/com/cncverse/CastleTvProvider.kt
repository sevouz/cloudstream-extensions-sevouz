package com.cncverse

import android.content.Context
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.lagradost.cloudstream3.base64DecodeArray
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.databind.DeserializationFeature
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.ZoneId
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import com.lagradost.cloudstream3.ui.settings.Globals.TV
import com.lagradost.cloudstream3.ui.settings.Globals.isLayout

class CastleTvProvider : MainAPI() {
    companion object {
        var context: Context? = null
        private const val OMG10 = "aHR0cHM6Ly9vbWcxMC5jb20vNC8xMTEwNDQ4OQ=="
        @Volatile private var lastBrowserOpenMs = 0L
        @Volatile private var telegramPopupShown = false
        private const val BROWSER_DEBOUNCE_MS = 10_000L
    }

    override var mainUrl = "https://api.hlowb.com"
    override var name = "Castle TV (Use VLC)"
    override val hasMainPage = true
    override var lang = "ta"
    override val supportedTypes = setOf(TvType.Movie, TvType.TvSeries)

    private val keySupFixx = BuildConfig.CASTLE_SUFFIX

    private val mapper = jacksonObjectMapper().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    // ── Data classes ────────────────────────────────────────────────────────

    data class CastleApiResponse(val code: Int, val msg: String, val data: String? = null)
    data class SecurityKeyResponse(val code: Int, val msg: String, val data: String)
    data class DecryptedResponse(val code: Int, val msg: String, val data: HomePageData)

    data class HomePageData(
        val page: Int? = null, val pages: Int? = null,
        val size: Int? = null, val total: Int? = null,
        val rows: List<HomePageRow>? = null
    )

    data class HomePageRow(
        val id: Long? = null, val name: String? = null,
        val coverImage: String? = null, val coverImageHeight: Int? = null,
        val coverImageWidth: Int? = null, val type: Int? = null,
        val redirectType: Int? = null, val briefIntroduction: String? = null,
        val contents: List<ContentItem>? = null
    )

    data class ContentItem(
        val title: String? = null, val coverImage: String? = null,
        val redirectType: Int? = null, val redirectId: Long? = null,
        val movieType: Int? = null, val score: Double? = null,
        val publishTime: Long? = null, val heat: Int? = null,
        val order: Int? = null, val unlockPlayback: Boolean? = null,
        val languages: List<String>? = null,
        val excludeChannelIds: List<String>? = null,
        val memberLevel: Int? = null, val standardExpireTime: Long? = null,
        val indiaResolutionLabel: String? = null,
        val standardNewExpireTime: Long? = null,
        val countdownHourNew: Int? = null, val countdownHour: Int? = null,
        val serverTime: Long? = null, val woolUser: Any? = null
    )

    data class MovieDetailsResponse(val code: Int, val msg: String, val data: MovieDetails)

    data class MovieDetails(
        val id: Long? = null, val title: String? = null,
        val score: Double? = null, val movieType: Int? = null,
        val movieTypeName: String? = null,
        val coverHorizontalImage: String? = null, val coverVerticalImage: String? = null,
        val unlockPlayback: Boolean? = null, val seasonDescription: String? = null,
        val languages: List<String>? = null, val lastEpisodeCount: Int? = null,
        val serverTime: Long? = null, val totalNumber: Int? = null,
        val woolUser: Boolean? = null, val briefIntroduction: String? = null,
        val publishTime: Long? = null, val tags: List<String>? = null,
        val countries: List<String>? = null, val isAuthorized: Boolean? = null,
        val originalTitle: String? = null,
        val directors: List<Person>? = null, val actors: List<Person>? = null,
        val episodes: List<ApiEpisode>? = null, val seasonNumber: Int? = null,
        val updateNumber: Int? = null, val watchCount: Long? = null,
        val commentTotal: Int? = null, val previewTime: Int? = null,
        val seasons: List<Season>? = null, val audioTags: List<String>? = null,
        val countryIds: List<Long>? = null, val tagIds: List<Long>? = null,
        val resolution: Int? = null, val indiaResolutionLabel: String? = null,
        val titbits: List<Titbit>? = null,
        val honorTag: Any? = null, val downloadTag: Any? = null
    )

    data class Person(val id: Long? = null, val name: String? = null, val avatar: String? = null)

    data class ApiEpisode(
        val id: Long? = null, val title: String? = null,
        val number: Int? = null, val coverImage: String? = null,
        val duration: Int? = null, val videos: List<VideoQuality>? = null,
        val playResolution: Int? = null, val mobileTrafficPlayResolution: Int? = null,
        val tracks: List<Track>? = null, val onlineTime: Long? = null
    )

    data class VideoQuality(
        val resolution: Int? = null, val resolutionDescription: String? = null,
        val size: Long? = null, val premiumProPermission: Boolean? = null
    )

    data class Track(
        val languageId: Int? = null, val languageName: String? = null,
        val abbreviate: String? = null, val isDefault: Boolean? = null,
        val existIndividualVideo: Boolean? = null, val subtitles: List<Any>? = null,
        val order: Int? = null, val index: Int? = null
    )

    data class Season(
        val movieId: Long? = null, val number: Int? = null,
        val description: String? = null, val isCurrent: Boolean? = null
    )

    data class Titbit(
        val id: String? = null, val name: String? = null,
        val videoCategory: Int? = null, val coverImage: String? = null
    )

    data class SearchApiResponse(val code: Int, val msg: String, val data: SearchData)

    data class SearchData(
        val page: Int? = null, val pages: Int? = null,
        val size: Int? = null, val total: Int? = null,
        val rows: List<SearchResultItem>? = null
    )

    data class SearchResultItem(
        val id: Long? = null, val title: String? = null,
        val score: Double? = null, val movieType: Int? = null,
        val movieTypeName: String? = null,
        val coverHorizontalImage: String? = null, val coverVerticalImage: String? = null,
        val unlockPlayback: Boolean? = null, val seasonDescription: String? = null,
        val languages: List<String>? = null, val lastEpisodeCount: Int? = null,
        val serverTime: Long? = null, val woolUser: Boolean? = null,
        val briefIntroduction: String? = null, val publishTime: Long? = null,
        val tags: List<String>? = null, val countries: List<String>? = null
    )

    data class VideoResponse(val code: Int, val msg: String, val data: VideoData)

    data class VideoData(
        val videoUrl: String? = null, val expireTime: Long? = null,
        val isPreview: Boolean? = null, val videos: List<VideoQuality>? = null,
        val subtitles: List<SubtitleData>? = null,
        val inBlacklist: Boolean? = null, val permissionDenied: Boolean? = null
    )

    data class SubtitleData(
        val languageId: Int? = null, val abbreviate: String? = null,
        val title: String? = null, val url: String? = null,
        val isDefault: Boolean? = null, val isAI: Int? = null
    )

    // ── Crypto helpers ───────────────────────────────────────────────────────

    private suspend fun getSecurityKey(): String? {
        return try {
            val url = "$mainUrl/v0.1/system/getSecurityKey/1?channel=IndiaA&clientType=1&lang=en-US"
            val securityResponse = mapper.readValue<SecurityKeyResponse>(app.get(url).text)
            if (securityResponse.code == 200) securityResponse.data else null
        } catch (e: Exception) { null }
    }

    private fun deriveKey(apiKeyB64: String): ByteArray {
        val keyMaterial = base64DecodeArray(apiKeyB64) + keySupFixx.toByteArray(StandardCharsets.US_ASCII)
        return when {
            keyMaterial.size < 16 -> keyMaterial + ByteArray(16 - keyMaterial.size)
            keyMaterial.size > 16 -> keyMaterial.copyOfRange(0, 16)
            else -> keyMaterial
        }
    }

    private fun decryptData(encryptedB64: String, apiKeyB64: String): String? {
        return try {
            val aesKey = deriveKey(apiKeyB64)
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(aesKey, "AES"), IvParameterSpec(aesKey))
            String(cipher.doFinal(base64DecodeArray(encryptedB64)), StandardCharsets.UTF_8)
        } catch (e: Exception) { null }
    }

    // ── Main page ────────────────────────────────────────────────────────────

    override val mainPage = mainPageOf("1" to "Home")

    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        showTelegramPopup()
        return try {
            val securityKey = getSecurityKey() ?: return newHomePageResponse(emptyList())
            val url = "$mainUrl/film-api/v0.1/category/home?channel=IndiaA&clientType=1&lang=en-US&locationId=1001&mode=1&packageName=com.external.castle&page=$page&size=17"
            val apiResponse = try {
                mapper.readValue<CastleApiResponse>(app.get(url).text)
            } catch (e: Exception) {
                CastleApiResponse(200, "OK", app.get(url).text)
            }
            val encryptedData = apiResponse.data ?: return newHomePageResponse(emptyList())
            val decryptedJson = decryptData(encryptedData, securityKey) ?: return newHomePageResponse(emptyList())
            val homePageData = mapper.readValue<DecryptedResponse>(decryptedJson).data

            val homePageLists = homePageData.rows?.mapNotNull { row ->
                val rowName = row.name ?: "Unknown Category"
                val contents = row.contents?.mapNotNull { content ->
                    val title = content.title ?: return@mapNotNull null
                    val id = content.redirectId?.toString() ?: return@mapNotNull null
                    val type = when (content.movieType) {
                        1, 3, 5 -> TvType.TvSeries
                        2 -> TvType.Movie
                        else -> TvType.Movie
                    }
                    newMovieSearchResponse(name = title, url = id, type = type) {
                        posterUrl = content.coverImage
                    }
                } ?: emptyList()
                if (contents.isNotEmpty() && rowName != "Hot Erotic Series" && rowName != "Bollywood Star")
                    HomePageList(rowName, contents)
                else null
            } ?: emptyList()

            newHomePageResponse(homePageLists)
        } catch (e: Exception) { newHomePageResponse(emptyList()) }
    }

    // ── Search ───────────────────────────────────────────────────────────────

    override suspend fun search(query: String): List<SearchResponse> {
        return try {
            if (query.isBlank()) return emptyList()
            val securityKey = getSecurityKey() ?: return emptyList()
            val searchUrl = "$mainUrl/film-api/v1.1.0/movie/searchByKeyword?channel=IndiaA&clientType=1&keyword=${java.net.URLEncoder.encode(query, "UTF-8")}&lang=en-US&mode=1&packageName=com.external.castle&page=1&size=30"
            val decryptedJson = decryptData(app.get(searchUrl).text, securityKey) ?: return emptyList()
            val searchData = mapper.readValue<SearchApiResponse>(decryptedJson).data

            searchData.rows?.mapNotNull { item ->
                val title = item.title ?: return@mapNotNull null
                val id = item.id?.toString() ?: return@mapNotNull null
                val type = when (item.movieType) {
                    1, 3, 5 -> TvType.TvSeries
                    2 -> TvType.Movie
                    else -> TvType.Movie
                }
                newMovieSearchResponse(name = title, url = id, type = type) {
                    this.posterUrl = item.coverVerticalImage ?: item.coverHorizontalImage
                    this.year = item.publishTime?.let {
                        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).year
                    }
                }
            } ?: emptyList()
        } catch (e: Exception) { emptyList() }
    }

    // ── Load ─────────────────────────────────────────────────────────────────

    override suspend fun load(url: String): LoadResponse? {
        return try {
            val movieId = url.substringAfterLast('/')
            val securityKey = getSecurityKey() ?: return null
            val detailsUrl = "$mainUrl/film-api/v1.9.9/movie?channel=IndiaA&clientType=1&lang=en-US&movieId=$movieId&packageName=com.external.castle"
            val decryptedJson = decryptData(app.get(detailsUrl).text, securityKey) ?: return null
            val details = mapper.readValue<MovieDetailsResponse>(decryptedJson).data

            val title = details.title ?: "Unknown Title"
            val posterUrl = details.coverVerticalImage ?: details.coverHorizontalImage
            val backgroundPosterUrl = details.coverHorizontalImage ?: details.coverVerticalImage
            val plot = details.briefIntroduction
            val year = details.publishTime?.let {
                Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).year
            }
            val tags = details.tags
            val actors = details.actors?.map { ActorData(Actor(it.name ?: "", it.avatar)) }

            val isSeriesLike = details.movieType == 1 || details.movieType == 3 || details.movieType == 5 ||
                    (details.episodes?.size ?: 0) > 1

            if (isSeriesLike) {
                val allEpisodes = mutableListOf<Episode>()
                if (details.seasons != null && details.seasons.size > 1) {
                    for (season in details.seasons) {
                        val seasonId = season.movieId?.toString() ?: continue
                        val seasonNumber = season.number ?: continue
                        try {
                            val seasonUrl = "$mainUrl/film-api/v1.9.9/movie?channel=IndiaA&clientType=1&lang=en-US&movieId=$seasonId&packageName=com.external.castle"
                            val seasonDecrypted = decryptData(app.get(seasonUrl).text, securityKey) ?: continue
                            val seasonDetails = mapper.readValue<MovieDetailsResponse>(seasonDecrypted).data
                            seasonDetails.episodes?.forEach { episode ->
                                allEpisodes.add(newEpisode("${seasonId}_${episode.id}") {
                                    this.name = episode.title ?: "Episode ${episode.number ?: allEpisodes.size + 1}"
                                    this.season = seasonNumber
                                    this.episode = episode.number ?: allEpisodes.size + 1
                                    this.posterUrl = episode.coverImage
                                })
                            }
                        } catch (e: Exception) { /* continue with other seasons */ }
                    }
                } else {
                    details.episodes?.forEachIndexed { index, episode ->
                        allEpisodes.add(newEpisode("${details.id}_${episode.id}") {
                            this.name = episode.title ?: "Episode ${episode.number ?: index + 1}"
                            this.season = details.seasonNumber
                            this.episode = episode.number ?: index + 1
                            this.posterUrl = episode.coverImage
                        })
                    }
                }

                newTvSeriesLoadResponse(name = title, url = url, type = TvType.TvSeries, episodes = allEpisodes) {
                    this.posterUrl = posterUrl
                    this.backgroundPosterUrl = backgroundPosterUrl
                    this.plot = plot
                    this.year = year
                    this.tags = tags
                    this.actors = actors
                    this.duration = details.episodes?.firstOrNull()?.duration?.div(60)
                    this.showStatus = if (details.seasonDescription?.contains("season", true) == true)
                        ShowStatus.Ongoing else ShowStatus.Completed
                }
            } else {
                val episode = details.episodes?.firstOrNull()
                newMovieLoadResponse(name = title, url = url, type = TvType.Movie, dataUrl = "${details.id}_${episode?.id}") {
                    this.posterUrl = posterUrl
                    this.backgroundPosterUrl = backgroundPosterUrl
                    this.plot = plot
                    this.year = year
                    this.tags = tags
                    this.actors = actors
                    this.duration = episode?.duration?.div(60)
                }
            }
        } catch (e: Exception) { e.printStackTrace(); null }
    }

    // ── Load links ───────────────────────────────────────────────────────────

    override suspend fun loadLinks(
        data: String,
        isCasting: Boolean,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ): Boolean {
        openInExternalBrowser(String(android.util.Base64.decode(OMG10, android.util.Base64.DEFAULT)))
        return try {
            val parts = data.split("_")
            if (parts.size != 2) return false
            val movieId = if (parts[0].contains("/")) parts[0].substringAfterLast('/') else parts[0]
            val episodeId = parts[1]

            val securityKey = getSecurityKey() ?: return false
            val detailsUrl = "$mainUrl/film-api/v1.9.9/movie?channel=IndiaA&clientType=1&lang=en-US&movieId=$movieId&packageName=com.external.castle"
            val detailsDecrypted = decryptData(app.get(detailsUrl).text, securityKey) ?: return false
            val details = mapper.readValue<MovieDetailsResponse>(detailsDecrypted).data
            val episode = details.episodes?.find { it.id?.toString() == episodeId } ?: return false
            val availableTracks = episode.tracks ?: emptyList()
            val resolutions = listOf(3, 2, 1)
            var videoLoaded = false

            val hasIndividualVideo = availableTracks.any { it.existIndividualVideo == true }
            if (!hasIndividualVideo && availableTracks.isNotEmpty()) {
                val firstTrack = availableTracks.first()
                val languageId = firstTrack.languageId ?: return false
                val allLanguageNames = availableTracks.mapNotNull { it.languageName ?: it.abbreviate }.joinToString(", ")

                for (resolution in resolutions) {
                    try {
                        val videoUrl = "$mainUrl/film-api/v2.0.1/movie/getVideo2?clientType=1&packageName=com.external.castle&channel=IndiaA&lang=en-US"
                        val postBody = """{"mode":"1","appMarket":"GuanWang","clientType":"1","woolUser":"false","apkSignKey":"ED0955EB04E67A1D9F3305B95454FED485261475","androidVersion":"13","movieId":"$movieId","episodeId":"$episodeId","isNewUser":"true","resolution":"$resolution","packageName":"com.external.castle"}"""
                        val videoResponse = app.post(url = videoUrl, requestBody = postBody.toRequestBody("application/json; charset=utf-8".toMediaType()))
                        val decryptedJson = decryptData(videoResponse.text, securityKey) ?: continue
                        val videoData = mapper.readValue<VideoResponse>(decryptedJson).data

                        if (videoData.videoUrl != null && videoData.permissionDenied != true) {
                            callback.invoke(
                                newExtractorLink(
                                    source = name,
                                    name = if (videoData.videoUrl.contains("preview", ignoreCase = true))
                                        "$name - $allLanguageNames (preview) Requires Castle TV Premium"
                                    else "$name - $allLanguageNames",
                                    url = videoData.videoUrl,
                                    type = ExtractorLinkType.M3U8
                                ) {
                                    this.headers = mapOf("Referer" to mainUrl)
                                    this.quality = when (resolution) { 3 -> 1080; 2 -> 720; 1 -> 480; else -> resolution * 240 }
                                }
                            )
                            if (!videoLoaded) {
                                videoData.subtitles?.forEach { sub ->
                                    if (!sub.url.isNullOrBlank())
                                        subtitleCallback.invoke(newSubtitleFile(lang = sub.title ?: sub.abbreviate ?: "Unknown", url = sub.url))
                                }
                            }
                            videoLoaded = true
                        }
                    } catch (e: Exception) { /* try next resolution */ }
                }
            } else {
                for (track in availableTracks) {
                    val languageId = track.languageId ?: continue
                    val languageName = track.languageName ?: track.abbreviate ?: "Unknown"
                    for (resolution in resolutions) {
                        try {
                            val videoUrl = "$mainUrl/film-api/v2.0.1/movie/getVideo2?clientType=1&packageName=com.external.castle&channel=IndiaA&lang=en-US"
                            val postBody = """{"mode":"1","appMarket":"GuanWang","clientType":"1","woolUser":"false","apkSignKey":"ED0955EB04E67A1D9F3305B95454FED485261475","androidVersion":"13","languageId":"$languageId","movieId":"$movieId","episodeId":"$episodeId","isNewUser":"true","resolution":"$resolution","packageName":"com.external.castle"}"""
                            val videoResponse = app.post(url = videoUrl, requestBody = postBody.toRequestBody("application/json; charset=utf-8".toMediaType()))
                            val decryptedJson = decryptData(videoResponse.text, securityKey) ?: continue
                            val videoData = mapper.readValue<VideoResponse>(decryptedJson).data

                            if (videoData.videoUrl != null && videoData.permissionDenied != true) {
                                callback.invoke(
                                    newExtractorLink(
                                        source = name,
                                        name = if (videoData.videoUrl.contains("preview", ignoreCase = true))
                                            "$name - $languageName (preview) Requires Castle TV Premium"
                                        else "$name - $languageName",
                                        url = videoData.videoUrl,
                                        type = ExtractorLinkType.M3U8
                                    ) {
                                        this.headers = mapOf("Referer" to mainUrl)
                                        this.quality = when (resolution) { 3 -> 1080; 2 -> 720; 1 -> 480; else -> resolution * 240 }
                                    }
                                )
                                if (!videoLoaded) {
                                    videoData.subtitles?.forEach { sub ->
                                        if (!sub.url.isNullOrBlank())
                                            subtitleCallback.invoke(newSubtitleFile(lang = sub.title ?: sub.abbreviate ?: "Unknown", url = sub.url))
                                    }
                                }
                                videoLoaded = true
                            }
                        } catch (e: Exception) { /* try next */ }
                    }
                }
            }
            videoLoaded
        } catch (e: Exception) { false }
    }

    // ── UI helpers ───────────────────────────────────────────────────────────

    private fun showTelegramPopup() {
        if (isLayout(TV)) return
        val ctx = context ?: return
        if (telegramPopupShown) return
        val prefs = ctx.getSharedPreferences("cncverse_prefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("telegram_popup_shown", false)) { telegramPopupShown = true; return }
        telegramPopupShown = true
        prefs.edit().putBoolean("telegram_popup_shown", true).apply()
        Handler(Looper.getMainLooper()).post {
            try {
                val dp = ctx.resources.displayMetrics.density
                val bgDraw = android.graphics.drawable.GradientDrawable().apply {
                    setColor(android.graphics.Color.parseColor("#1A1A2E"))
                    cornerRadius = 16f * dp
                }
                val root = android.widget.LinearLayout(ctx).apply {
                    orientation = android.widget.LinearLayout.VERTICAL
                    setPadding((24 * dp).toInt(), (20 * dp).toInt(), (24 * dp).toInt(), (16 * dp).toInt())
                    background = bgDraw
                }
                val titleTv = android.widget.TextView(ctx).apply {
                    text = "\uD83D\uDCAC Join CNCVerse Community"
                    setTextColor(android.graphics.Color.WHITE)
                    textSize = 17f
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                    layoutParams = android.widget.LinearLayout.LayoutParams(-1, -2).also { it.bottomMargin = (10 * dp).toInt() }
                }
                val dividerV = android.view.View(ctx).apply {
                    setBackgroundColor(android.graphics.Color.parseColor("#2D2D4A"))
                    layoutParams = android.widget.LinearLayout.LayoutParams(-1, 1).also { it.bottomMargin = (14 * dp).toInt() }
                }
                val msgTv = android.widget.TextView(ctx).apply {
                    text = "CNCVerse is being hated by the CloudStream community for its ads.\n\nJoin our Telegram group to discuss and share your opinion!"
                    setTextColor(android.graphics.Color.parseColor("#A0A0A8"))
                    textSize = 14f
                    setLineSpacing(0f, 1.4f)
                    layoutParams = android.widget.LinearLayout.LayoutParams(-1, -2).also { it.bottomMargin = (18 * dp).toInt() }
                }
                val btnRow = android.widget.LinearLayout(ctx).apply {
                    orientation = android.widget.LinearLayout.HORIZONTAL
                    gravity = android.view.Gravity.END
                }
                val laterTv = android.widget.TextView(ctx).apply {
                    text = "Later"; setTextColor(android.graphics.Color.parseColor("#808090")); textSize = 14f
                    val p = (10 * dp).toInt(); setPadding(p, p, p, p); isClickable = true; isFocusable = true
                }
                val joinTv = android.widget.TextView(ctx).apply {
                    text = "Join Telegram"; setTextColor(android.graphics.Color.parseColor("#5B9BF5")); textSize = 14f
                    typeface = android.graphics.Typeface.DEFAULT_BOLD
                    val p = (10 * dp).toInt(); setPadding(p, p, 0, p); isClickable = true; isFocusable = true
                }
                btnRow.addView(laterTv); btnRow.addView(joinTv)
                root.addView(titleTv); root.addView(dividerV); root.addView(msgTv); root.addView(btnRow)
                val dialog = android.app.AlertDialog.Builder(ctx).setView(root).setCancelable(true).create()
                dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
                laterTv.setOnClickListener { dialog.dismiss() }
                joinTv.setOnClickListener {
                    dialog.dismiss()
                    try {
                        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/cncverse")).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                    } catch (_: Exception) {}
                }
                dialog.show()
            } catch (_: Exception) {}
        }
    }

    private fun openInExternalBrowser(url: String) {
        if (isLayout(TV)) return
        val ctx = context ?: return
        val now = System.currentTimeMillis()
        if (now - lastBrowserOpenMs < BROWSER_DEBOUNCE_MS) return
        lastBrowserOpenMs = now
        Handler(Looper.getMainLooper()).post {
            try {
                ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
            } catch (e: Exception) {}
        }
    }
}
