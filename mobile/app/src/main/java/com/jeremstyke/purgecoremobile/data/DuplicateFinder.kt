package com.jeremstyke.purgecoremobile.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest

data class MediaItem(
    val uri: Uri,
    val displayName: String,
    val sizeBytes: Long,
)

data class DuplicateGroup(
    val items: List<MediaItem>,
    val sizeBytes: Long,
) {
    /** Freeing all but one copy frees this many bytes. */
    val reclaimableBytes: Long get() = sizeBytes * (items.size - 1)
}

/**
 * Finds photos and videos with identical content, the same approach as
 * PurgeCore Windows' duplicate finder: group by file size first (cheap),
 * then confirm with a real content hash only within same-size groups, so a
 * same-size-different-content file is never wrongly flagged.
 */
object DuplicateFinder {

    // Same safety cap in spirit as the Windows app: a hard ceiling on how
    // many files get hashed in one pass, so a huge media library can't turn
    // a scan into a multi-minute freeze.
    private const val MAX_FILES_TO_HASH = 20_000

    suspend fun findDuplicates(context: Context): List<DuplicateGroup> = withContext(Dispatchers.IO) {
        val allMedia = queryAllMedia(context)

        val bySize = allMedia.groupBy { it.sizeBytes }
            .filter { (size, items) -> size > 0 && items.size > 1 }

        val groups = mutableListOf<DuplicateGroup>()
        var hashedSoFar = 0

        for ((size, candidates) in bySize) {
            if (hashedSoFar >= MAX_FILES_TO_HASH) break

            val byHash = mutableMapOf<String, MutableList<MediaItem>>()
            for (item in candidates) {
                if (hashedSoFar >= MAX_FILES_TO_HASH) break
                val hash = hashContent(context, item.uri) ?: continue
                hashedSoFar++
                byHash.getOrPut(hash) { mutableListOf() }.add(item)
            }

            byHash.values.filter { it.size > 1 }.forEach { dupItems ->
                groups.add(DuplicateGroup(items = dupItems, sizeBytes = size))
            }
        }

        groups.sortedByDescending { it.reclaimableBytes }
    }

    private fun queryAllMedia(context: Context): List<MediaItem> {
        val result = mutableListOf<MediaItem>()
        val collections = listOf(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        )

        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.SIZE,
        )

        for (collection in collections) {
            context.contentResolver.query(collection, projection, null, null, null)?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val name = cursor.getString(nameCol) ?: continue
                    val size = cursor.getLong(sizeCol)
                    val uri = ContentUris.withAppendedId(collection, id)
                    result.add(MediaItem(uri = uri, displayName = name, sizeBytes = size))
                }
            }
        }
        return result
    }

    /** MD5 is fine here, this is duplicate detection, not security. */
    private fun hashContent(context: Context, uri: Uri): String? {
        return try {
            val digest = MessageDigest.getInstance("MD5")
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (stream.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            } ?: return null
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            null
        }
    }
}
