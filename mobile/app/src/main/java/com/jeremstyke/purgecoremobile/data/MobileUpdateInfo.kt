package com.jeremstyke.purgecoremobile.data

data class MobileUpdateInfo(
    val isUpdateAvailable: Boolean,
    val latestVersion: String?,
    val downloadUrl: String?,
) {
    companion object {
        val NoUpdate = MobileUpdateInfo(isUpdateAvailable = false, latestVersion = null, downloadUrl = null)
    }
}
