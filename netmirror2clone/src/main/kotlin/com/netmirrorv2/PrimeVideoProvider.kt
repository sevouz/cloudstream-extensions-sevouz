package com.netmirrorv2

import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.AppUtils.toJson
import com.lagradost.cloudstream3.utils.AppUtils.tryParseJson

class PrimeVideoProvider : BaseNetMirrorProvider() {
    override var name = "Prime Video V2"
    override val ott = "pv"
    override val imgPrefix = "pv"
    override val epImgPrefix = "pvepimg/150"
    override val searchPath = "pv/search.php"
    override val postPath = "pv/post.php"
    override val episodesPath = "pv/episodes.php"
    override val playlistPath = "pv/playlist.php"

    // Prime Video has a dedicated JSON homepage: { post:[{cate, ids}] }
    // instead of the HTML tray page used by Netflix/Hotstar
    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val text = try {
            app.get(
                "$mainUrl/mobile/pv/homepage.php",
                cookies = quickCookies(),
                headers = BROWSER_HEADERS,
                referer = "$mainUrl/home"
            ).text
        } catch (_: Exception) { "" }

        var root = tryParseJson<PrimeHomeData>(text)

        // Fallback to full bypass if quick cookies returned nothing
        if (root == null || root.post.isNullOrEmpty()) {
            val text2 = try {
                app.get(
                    "$mainUrl/mobile/pv/homepage.php",
                    cookies = cookies(),
                    headers = BROWSER_HEADERS,
                    referer = "$mainUrl/home"
                ).text
            } catch (_: Exception) { "" }
            root = tryParseJson<PrimeHomeData>(text2)
        }

        val items = root?.post?.mapNotNull { group ->
            val cateName = group.cate?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val ids = group.ids?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
            if (ids.isNullOrEmpty()) return@mapNotNull null
            val list = ids.map { id ->
                newAnimeSearchResponse("", Id(id).toJson()) {
                    posterUrl = "https://imgcdn.kim/$imgPrefix/v/$id.jpg"
                    posterHeaders = mapOf("Referer" to "$mainUrl/home")
                }
            }
            HomePageList(cateName, list, isHorizontalImages = false)
        } ?: emptyList()

        return newHomePageResponse(items, false)
    }
}

