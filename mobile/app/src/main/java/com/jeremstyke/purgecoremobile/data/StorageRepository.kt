package com.jeremstyke.purgecoremobile.data

import android.content.Context
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore

data class StorageInfo(
    val totalBytes: Long,
    val freeBytes: Long,
) {
    val usedBytes: Long get() = totalBytes - freeBytes
    val usedFraction: Float get() = if (totalBytes == 0L) 0f else usedBytes.toFloat() / totalBytes.toFloat()
}

data class MediaBreakdown(
    val photosBytes: Long,
    val videosBytes: Long,
    val otherBytes: Long,
)

object StorageRepository {
    /** Reads total and free space on the primary storage volume. */
    fun readStorageInfo(): StorageInfo {
        val stat = StatFs(Environment.getDataDirectory().path)
        val total = stat.blockCountLong * stat.blockSizeLong
        val free = stat.availableBlocksLong * stat.blockSizeLong
        return StorageInfo(totalBytes = total, freeBytes = free)
    }

    /**
     * How much of that space is photos and videos specifically, using the
     * same media permission already requested for the duplicate finder,
     * nothing new to ask for. The rest (apps, system files, documents,
     * anything else) is reported as a single "other" bucket rather than
     * broken down further, Android only exposes a precise per-app
     * breakdown behind a special permission that would complicate
     * installing this app and reviewing it for the Play Store, for very
     * little gained here.
     */
    fun readMediaBreakdown(context: Context, usedBytes: Long): MediaBreakdown {
        val photos = sumMediaSize(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        val videos = sumMediaSize(context, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        val other = (usedBytes - photos - videos).coerceAtLeast(0L)
        return MediaBreakdown(photosBytes = photos, videosBytes = videos, otherBytes = other)
    }

    private fun sumMediaSize(context: Context, collection: android.net.Uri): Long {
        var total = 0L
        val projection = arrayOf(MediaStore.MediaColumns.SIZE)
        context.contentResolver.query(collection, projection, null, null, null)?.use { cursor ->
            val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
            while (cursor.moveToNext()) {
                total += cursor.getLong(sizeCol)
            }
        }
        return total
    }
}
