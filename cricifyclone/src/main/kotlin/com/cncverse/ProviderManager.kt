package com.cncverse

import com.lagradost.cloudstream3.utils.AppUtils.parseJson
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

data class ProviderData(val id: Int, val title: String, val image: String, val catLink: String?)

data class LiveEventData(
    val id: Int, val title: String, val image: String?, val slug: String,
    val cat: String?, val eventInfo: LiveEventInfo?, val publish: Int,
    val formats: List<LiveEventFormat>?
)

data class LiveEventInfo(
    val teamA: String?, val teamB: String?, val teamAFlag: String?, val teamBFlag: String?,
    val eventCat: String?, val eventName: String?, val eventLogo: String?,
    val isHot: String?, val eventType: String?, val startTime: String?, val endTime: String?
)

data class LiveEventFormat(val title: String?, val webLink: String?)

object ProviderManager {
    private const val DEFAULT_BASE_URL = "https://cfymarkscanjiostar80.top"
    private var cachedBaseUrl: String? = null

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val fallbackProviders = listOf(
        mapOf("id" to 13, "title" to "TATA PLAY", "image" to "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQz_qYe3Y4S5bXXVlPtXQnqtAkLw1-no57QHhPyMgWE0SQmxujzHxZKiDs&s=10", "catLink" to "https://hotstarlive.delta-cloud.workers.dev/?token=240bb9-374e2e-3c13f0-4a7xz5"),
        mapOf("id" to 14, "title" to "HOTSTAR", "image" to "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcRWwYjMvB58DMLsL9Ii2fhvw6NBYvD1iVCjOMU8TXBLJt0eibLGOjoRkLJP&s=10", "catLink" to "https://hotstar-live-event.alpha-circuit.workers.dev/?token=a13d9c-4b782a-6c90fd-9a1b84"),
        mapOf("id" to 22, "title" to "JIO IND", "image" to "https://uxwing.com/wp-content/themes/uxwing/download/brands-and-social-media/jio-logo-icon.png", "catLink" to "https://jiotv.byte-vault.workers.dev/?token=42e4f5-2d873b-3c37d8-7f3f50"),
        mapOf("id" to 104, "title" to "ZEE5", "image" to "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQS0OT2NFe9Jb4ofg_DrXx42EKLgyGnSGwoLg&usqp=CAU", "catLink" to "https://zee5.cloud-hatchh.workers.dev/?token=42e4f5-2d413b-3c37d8-7f3f35"),
        mapOf("id" to 130, "title" to "JIO CINEMA IND", "image" to "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQc3qZ1WgzPyFRX4cWIBJF0MSjWW3gZcLFycg&usqp=CAU", "catLink" to "https://raw.githubusercontent.com/alex8875/m3u/refs/heads/main/jcinema.m3u")
    )

    suspend fun getBaseUrl(): String {
        cachedBaseUrl?.let { return it }
        val firebaseUrl = FirebaseRemoteConfigFetcher.getProviderApiUrl()
        if (!firebaseUrl.isNullOrBlank()) {
            cachedBaseUrl = firebaseUrl.trimEnd('/')
            return cachedBaseUrl!!
        }
        cachedBaseUrl = DEFAULT_BASE_URL
        return DEFAULT_BASE_URL
    }

    private suspend fun getProvidersUrl(): String = "${getBaseUrl()}/cats.txt"
    private suspend fun getLiveEventsUrl(): String = "${getBaseUrl()}/categories/live-events.txt"

    suspend fun fetchProviders(): List<Map<String, Any>> {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(getProvidersUrl())
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .build()
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val encryptedData = response.body.string()
                    if (!encryptedData.isNullOrBlank()) {
                        val decryptedData = CryptoUtils.decryptData(encryptedData.trim())
                        if (!decryptedData.isNullOrBlank()) {
                            val providers = parseJson<List<ProviderData>>(decryptedData)
                            return@withContext providers.filter { !it.catLink.isNullOrBlank() }
                                .map { mapOf("id" to it.id, "title" to it.title, "image" to it.image, "catLink" to it.catLink!!) }
                        }
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
            fallbackProviders
        }
    }

    suspend fun fetchLiveEvents(): List<LiveEventData> {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(getLiveEventsUrl())
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .build()
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val encryptedData = response.body.string()
                    if (!encryptedData.isNullOrBlank()) {
                        val decryptedData = CryptoUtils.decryptData(encryptedData.trim())
                        if (!decryptedData.isNullOrBlank()) {
                            return@withContext parseJson<List<LiveEventData>>(decryptedData).filter { it.publish == 1 }
                        }
                    }
                }
            } catch (e: Exception) { e.printStackTrace() }
            emptyList()
        }
    }
}
