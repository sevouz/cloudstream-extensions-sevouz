package com.cncverse

import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import android.content.Context

@CloudstreamPlugin
class CastleTvProviderPlugin : Plugin() {
    override fun load(context: Context) {
        CastleTvProvider.context = context
        registerMainAPI(CastleTvProvider())
    }
}
