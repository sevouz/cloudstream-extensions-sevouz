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

        // Restore persisted home pages from disk immediately — runs in <5ms.
        // This means even the very first open after an app kill is instant.
        try { netflix.loadPersistedHome() } catch (_: Throwable) {}
        try { prime.loadPersistedHome() } catch (_: Throwable) {}
        try { hotstar.loadPersistedHome() } catch (_: Throwable) {}

        registerMainAPI(netflix)
        registerMainAPI(prime)
        registerMainAPI(hotstar)

        // Pre-warm bypass + refresh all 3 home pages in parallel in the background
        CoroutineScope(Dispatchers.IO).launch {
            try { ensureBypass() } catch (_: Throwable) {}
            launch { try { netflix.prewarmHome() } catch (_: Throwable) {} }
            launch { try { prime.prewarmHome() } catch (_: Throwable) {} }
            launch { try { hotstar.prewarmHome() } catch (_: Throwable) {} }
        }
    }
}
