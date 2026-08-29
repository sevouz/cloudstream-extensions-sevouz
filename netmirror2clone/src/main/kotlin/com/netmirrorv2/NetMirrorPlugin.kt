package com.netmirrorv2

import android.content.Context
import com.lagradost.cloudstream3.plugins.CloudstreamPlugin
import com.lagradost.cloudstream3.plugins.Plugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@CloudstreamPlugin
class NetMirrorPlugin : Plugin() {
    override fun load(context: Context) {
        try { BypassStorage.init(context) } catch (_: Throwable) {}
        // Pre-warm everything in parallel so content and posters load instantly on first open:
        //  • bypass session cookie  (needed for home page / search / load)
        //  • NewTV API base URL     (needed for loadLinks)
        //  • NewTV user tokens for all three OTTs (pre-caches tokens so first play is instant)
        CoroutineScope(Dispatchers.IO).launch {
            val bypassDeferred = async {
                try { ensureBypass() } catch (_: Throwable) { null }
            }
            val apiBaseDeferred = async {
                try { resolveNewTvApi() } catch (_: Throwable) { "" }
            }
            // Wait for both, then pre-fetch tokens for all three OTTs in parallel
            bypassDeferred.await()
            val apiBase = apiBaseDeferred.await()
            if (apiBase.isNotEmpty()) {
                listOf("nf", "pv", "hs").map { ott ->
                    async {
                        try { getNewTvUserToken(apiBase, ott, false) } catch (_: Throwable) {}
                    }
                }.forEach { it.await() }
            }
        }
        registerMainAPI(NetflixProvider())
        registerMainAPI(PrimeVideoProvider())
        registerMainAPI(HotstarProvider())
    }
}

