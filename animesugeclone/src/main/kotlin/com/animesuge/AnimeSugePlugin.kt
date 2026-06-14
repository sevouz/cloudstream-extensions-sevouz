package com.animesuge

import android.content.Context
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin

@CloudstreamPlugin
class AnimeSugePlugin : Plugin() {
    override fun load(context: Context) {
        registerMainAPI(AnimeSuge())
    }
}
