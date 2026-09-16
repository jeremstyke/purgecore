package com.jeremstyke.purgecoremobile.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

/**
 * Checks GitHub Releases for a newer PurgeCore Mobile version, the same
 * repository the Windows app checks, but filtered to tags starting with
 * "mobile-v" (the Windows app filters those same tags out). Uses only
 * org.json and HttpURLConnection, both already part of the Android SDK, no
 * extra dependency needed for a single small API call.
 */
object MobileUpdateChecker {

    private const val RELEASES_URL = "https://api.github.com/repos/jeremstyke/purgecore/releases"

    suspend fun checkForUpdate(currentVersion: String): MobileUpdateInfo = withContext(Dispatchers.IO) {
        try {
            val connection = URL(RELEASES_URL).openConnection() as HttpURLConnection
            connection.setRequestProperty("Accept", "application/vnd.github+json")
            connection.connectTimeout = 8000
            connection.readTimeout = 8000

            if (connection.responseCode != 200) return@withContext MobileUpdateInfo.NoUpdate

            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val releases = JSONArray(body)

            for (i in 0 until releases.length()) {
                val release = releases.getJSONObject(i)
                val tag = release.optString("tag_name", "")
                if (!tag.startsWith("mobile-v")) continue

                val latestVersion = tag.removePrefix("mobile-v")
                if (latestVersion == currentVersion) return@withContext MobileUpdateInfo.NoUpdate

                val assets = release.optJSONArray("assets") ?: return@withContext MobileUpdateInfo.NoUpdate
                var apkUrl: String? = null
                for (j in 0 until assets.length()) {
                    val asset = assets.getJSONObject(j)
                    val name = asset.optString("name", "")
                    if (name.endsWith(".apk")) {
                        apkUrl = asset.optString("browser_download_url")
                        break
                    }
                }
                if (apkUrl == null) return@withContext MobileUpdateInfo.NoUpdate

                return@withContext MobileUpdateInfo(
                    isUpdateAvailable = true,
                    latestVersion = latestVersion,
                    downloadUrl = apkUrl
                )
            }
            MobileUpdateInfo.NoUpdate
        } catch (e: Exception) {
            // Offline, rate-limited, or any other network hiccup: just
            // don't show an update banner rather than crash or retry loop.
            MobileUpdateInfo.NoUpdate
        }
    }
}
