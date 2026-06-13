@file:Suppress("UnstableApiUsage")

import java.util.Properties

version = 1

// Load secrets from local.properties or env
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
fun secret(key: String) = localProps.getProperty(key) ?: System.getenv(key) ?: ""

android {
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
    defaultConfig {
        android.buildFeatures.buildConfig = true
        buildConfigField("String", "TMDB_API",                   "\"${secret("TMDB_API")}\"")
        buildConfigField("String", "ZSHOW_API",                  "\"${secret("ZSHOW_API")}\"")
        buildConfigField("String", "ANICHI_API",                 "\"${secret("ANICHI_API")}\"")
        buildConfigField("String", "ANICHI_APP",                 "\"${secret("ANICHI_APP")}\"")
        buildConfigField("String", "KissKh",                     "\"${secret("KissKh")}\"")
        buildConfigField("String", "KisskhSub",                  "\"${secret("KisskhSub")}\"")
        buildConfigField("String", "SUPERSTREAM_THIRD_API",      "\"${secret("SUPERSTREAM_THIRD_API")}\"")
        buildConfigField("String", "SUPERSTREAM_FOURTH_API",     "\"${secret("SUPERSTREAM_FOURTH_API")}\"")
        buildConfigField("String", "SUPERSTREAM_FIRST_API",      "\"${secret("SUPERSTREAM_FIRST_API")}\"")
        buildConfigField("String", "PROXYAPI",                   "\"${secret("PROXYAPI")}\"")
        buildConfigField("String", "KAISVA",                     "\"${secret("KAISVA")}\"")
        buildConfigField("String", "MOVIEBOX_SECRET_KEY_ALT",    "\"${secret("MOVIEBOX_SECRET_KEY_ALT")}\"")
        buildConfigField("String", "MOVIEBOX_SECRET_KEY_DEFAULT","\"${secret("MOVIEBOX_SECRET_KEY_DEFAULT")}\"")
        buildConfigField("String", "KAIMEG",                     "\"${secret("KAIMEG")}\"")
        buildConfigField("String", "KAIDEC",                     "\"${secret("KAIDEC")}\"")
        buildConfigField("String", "KAIENC",                     "\"${secret("KAIENC")}\"")
        buildConfigField("String", "VideasyDEC",                 "\"${secret("VideasyDEC")}\"")
        buildConfigField("String", "YFXENC",                     "\"${secret("YFXENC")}\"")
        buildConfigField("String", "YFXDEC",                     "\"${secret("YFXDEC")}\"")
        buildConfigField("String", "NuvFeb",                     "\"${secret("NuvFeb")}\"")
    }
}

dependencies {
    implementation("com.google.android.material:material:1.14.0")
    implementation("androidx.browser:browser:1.10.0")
}

cloudstream {
    language = "en"
    description = "StreamPlay - Multi-source streaming (Movies, TV, Anime) by Phisher98"
    authors = listOf("Phisher98", "Hexated", "sevouz")
    status = 1
    tvTypes = listOf(
        "AsianDrama",
        "TvSeries",
        "Anime",
        "Movie",
        "Cartoon",
        "AnimeMovie"
    )
    iconUrl = "https://i3.wp.com/yt3.googleusercontent.com/ytc/AIdro_nCBArSmvOc6o-k2hTYpLtQMPrKqGtAw_nC20rxm70akA=s900-c-k-c0x00ffffff-no-rj?ssl=1"
    requiresResources = true
    isCrossPlatform = false
}
