package com.anidb

import com.lagradost.cloudstream3.utils.AppUtils.parseJson

fun parseAnimeData(jsonString: String): MetaAnimeData? {
    return try {
        parseJson<MetaAnimeData>(jsonString)
    } catch (_: Exception) {
        null
    }
}

data class ImageData(
    val coverType: String? = null,
    val url: String? = null
)

data class MetaEpisode(
    val episode: String? = null,
    val airdate: String? = null,
    val airDateUtc: String? = null,
    val length: Int? = null,
    val runtime: Int? = null,
    val image: String? = null,
    val title: Map<String, String?>? = null,
    val overview: String? = null,
    val rating: String? = null,
    val finaleType: String? = null
)

data class MetaAnimeData(
    val titles: Map<String, String?>? = null,
    val images: List<ImageData>? = null,
    val episodes: Map<String, MetaEpisode>? = null,
)
