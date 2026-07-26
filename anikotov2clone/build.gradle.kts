version = 7

android {
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation("com.google.code.gson:gson:2.11.0")
}

cloudstream {
    language = "en"

    description = "Watch Anime Online Free in HD (SUB/DUB)"
    authors = listOf("NivinCNC")

    /**
     * Status int as the following:
     * 0: Down
     * 1: Ok
     * 2: Slow
     * 3: Beta only
     * */
    status = 1
    tvTypes = listOf(
        "AnimeMovie",
        "Anime",
        "OVA",
    )

    iconUrl = "https://www.google.com/s2/favicons?domain=anikototv.to&sz=%size%"

    isCrossPlatform = false
}

// Override the display name that appears inside CloudStream
tasks.withType<com.lagradost.cloudstream3.gradle.tasks.GenerateManifestTask> {
    pluginName.set("AniKoto V2")
}
