version = 454

android {
    buildFeatures {
        buildConfig = true
    }
}

cloudstream {
    language = "en"
    description = "One stop solution for Movies, Series, Anime, AsianDrama and Torrents"
    authors = listOf("megix")
    status = 1
    tvTypes = listOf(
        "TvSeries",
        "Movie",
        "AsianDrama",
        "Anime",
        "Torrent"
    )

    iconUrl = "https://github.com/SaurabhKaperwan/CSX/raw/refs/heads/master/CineStream/icon.png"
}
