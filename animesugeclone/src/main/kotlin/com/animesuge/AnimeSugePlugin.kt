package com.animesuge

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.BasePlugin

@CloudstreamPlugin
class AnimeSugePlugin : BasePlugin() {
    override fun load() {
        registerMainAPI(AnimeSuge())
    }
}
