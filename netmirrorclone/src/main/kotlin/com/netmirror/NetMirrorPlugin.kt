package com.netmirror

import android.content.Context
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@CloudstreamPlugin
class NetMirrorPlugin : Plugin() {
    override fun load(context: Context) {
        BypassStorage.init(context)
        // Pre-fetch bypass in background so content loads instantly
        CoroutineScope(Dispatchers.IO).launch {
            try { ensureBypass() } catch (_: Exception) {}
        }
        registerMainAPI(NetflixProvider())
        registerMainAPI(PrimeVideoProvider())
        registerMainAPI(HotstarProvider())
    }
}
