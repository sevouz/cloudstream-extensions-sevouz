package com.anikotov2

import android.content.Context
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin

@CloudstreamPlugin
class AniKotoV2Plugin : Plugin() {
    override fun load(context: Context) {
        registerMainAPI(AniKotoV2Provider())
        registerExtractorAPI(MegaPlayExtractor())
        registerExtractorAPI(VidwishExtractor())
        registerExtractorAPI(VidtubeExtractor())
    }
}
