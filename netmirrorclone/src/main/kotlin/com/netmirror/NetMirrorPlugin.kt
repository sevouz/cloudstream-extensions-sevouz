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
        try { BypassStorage.init(context) } catch (_: Throwable) {}

        val netflix = NetflixProvider()
        val prime = PrimeVideoProvider()
        val hotstar = HotstarProvider()

        registerMainAPI(netflix)
        registerMainAPI(prime)
        registerMainAPI(hotstar)

        // Pre-warm bypass + all 3 home pages in parallel so first open is instant
        CoroutineScope(Dispatchers.IO).launch {
            try { ensureBypass() } catch (_: Throwable) {}
            // Fire all 3 home page fetches simultaneously after bypass is ready
            launch { try { netflix.prewarmHome() } catch (_: Throwable) {} }
            launch { try { prime.prewarmHome() } catch (_: Throwable) {} }
            launch { try { hotstar.prewarmHome() } catch (_: Throwable) {} }
        }
    }
}
