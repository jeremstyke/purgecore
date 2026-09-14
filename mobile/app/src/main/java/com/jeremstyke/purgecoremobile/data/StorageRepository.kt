package com.jeremstyke.purgecoremobile.data

import android.os.Environment
import android.os.StatFs

data class StorageInfo(
    val totalBytes: Long,
    val freeBytes: Long,
) {
    val usedBytes: Long get() = totalBytes - freeBytes
    val usedFraction: Float get() = if (totalBytes == 0L) 0f else usedBytes.toFloat() / totalBytes.toFloat()
}

object StorageRepository {
    /** Reads total and free space on the primary storage volume. */
    fun readStorageInfo(): StorageInfo {
        val stat = StatFs(Environment.getDataDirectory().path)
        val total = stat.blockCountLong * stat.blockSizeLong
        val free = stat.availableBlocksLong * stat.blockSizeLong
        return StorageInfo(totalBytes = total, freeBytes = free)
    }
}
