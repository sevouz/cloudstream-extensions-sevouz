package com.cncversev2

import android.content.Context
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin

@CloudstreamPlugin
class CastleTvV2Plugin : Plugin() {
    override fun load(context: Context) {
        CastleTvV2Provider.context = context
        registerMainAPI(CastleTvV2Provider())
    }
}
