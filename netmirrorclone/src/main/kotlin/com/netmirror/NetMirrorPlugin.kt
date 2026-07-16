package com.netmirror

import android.content.Context
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin

@CloudstreamPlugin
class NetMirrorPlugin : Plugin() {
    override fun load(context: Context) {
        BypassStorage.init(context)
        registerMainAPI(NetflixProvider())
        registerMainAPI(PrimeVideoProvider())
        registerMainAPI(HotstarProvider())
    }
}
