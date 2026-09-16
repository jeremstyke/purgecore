package com.jeremstyke.purgecoremobile.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jeremstyke.purgecoremobile.BuildConfig
import com.jeremstyke.purgecoremobile.R
import com.jeremstyke.purgecoremobile.data.MobileUpdateChecker
import com.jeremstyke.purgecoremobile.data.MobileUpdateInfo
import com.jeremstyke.purgecoremobile.data.MediaBreakdown
import com.jeremstyke.purgecoremobile.data.StorageInfo
import com.jeremstyke.purgecoremobile.data.StorageRepository
import com.jeremstyke.purgecoremobile.ui.components.BrandHeader
import com.jeremstyke.purgecoremobile.ui.components.DonateButton
import com.jeremstyke.purgecoremobile.ui.components.RingSegment
import com.jeremstyke.purgecoremobile.ui.components.StorageRing
import com.jeremstyke.purgecoremobile.ui.components.UpdateBanner
import com.jeremstyke.purgecoremobile.ui.theme.Accent
import com.jeremstyke.purgecoremobile.ui.theme.Amber
import com.jeremstyke.purgecoremobile.ui.theme.Teal
import com.jeremstyke.purgecoremobile.ui.theme.Violet
import java.text.CharacterIterator
import java.text.StringCharacterIterator

/** Same human-readable byte formatting approach as the Windows app. */
fun formatBytes(bytes: Long): String {
    var value = bytes
    if (-1000 < value && value < 1000) return "$value B"
    val ci: CharacterIterator = StringCharacterIterator("kMGTPE")
    while (value <= -999_950 || value >= 999_950) {
        value /= 1000
        ci.next()
    }
    return String.format("%.1f %cB", value / 1000.0, ci.current())
}

@Composable
fun StorageScreen() {
    var storageInfo by remember { mutableStateOf<StorageInfo?>(null) }
    var mediaBreakdown by remember { mutableStateOf<MediaBreakdown?>(null) }
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    var updateInfo by remember { mutableStateOf<MobileUpdateInfo?>(null) }

    LaunchedEffect(Unit) {
        val info = StorageRepository.readStorageInfo()
        storageInfo = info
        mediaBreakdown = StorageRepository.readMediaBreakdown(context, info.usedBytes)
    }

    LaunchedEffect(Unit) {
        updateInfo = MobileUpdateChecker.checkForUpdate(BuildConfig.VERSION_NAME)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        updateInfo?.let { info ->
            if (info.isUpdateAvailable && info.latestVersion != null && info.downloadUrl != null) {
                UpdateBanner(latestVersion = info.latestVersion, downloadUrl = info.downloadUrl)
            }
        }
        BrandHeader(title = stringResource(R.string.storage_title))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            storageInfo?.let { info ->
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    StorageRing(
                        segments = mediaBreakdown?.let { breakdown ->
                            listOf(
                                RingSegment(breakdown.photosBytes, Teal),
                                RingSegment(breakdown.videosBytes, Violet),
                                RingSegment(breakdown.otherBytes, Amber),
                            )
                        } ?: listOf(RingSegment(info.usedBytes, Accent)),
                        totalBytes = info.totalBytes,
                        centerLabel = "${(info.usedFraction * 100).toInt()}%"
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        String.format(
                            stringResource(R.string.storage_used_of_total),
                            formatBytes(info.usedBytes),
                            formatBytes(info.totalBytes)
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Teal,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        String.format(stringResource(R.string.storage_free), formatBytes(info.freeBytes)),
                        style = MaterialTheme.typography.bodySmall,
                        color = Violet
                    )
                }
            }
        } ?: run {
            Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
            mediaBreakdown?.let { breakdown ->
                Spacer(Modifier.height(16.dp))
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(20.dp)) {
                        BreakdownRow(
                            color = Teal,
                            label = stringResource(R.string.storage_breakdown_photos),
                            bytes = breakdown.photosBytes
                        )
                        Spacer(Modifier.height(12.dp))
                        BreakdownRow(
                            color = Violet,
                            label = stringResource(R.string.storage_breakdown_videos),
                            bytes = breakdown.videosBytes
                        )
                        Spacer(Modifier.height(12.dp))
                        BreakdownRow(
                            color = Amber,
                            label = stringResource(R.string.storage_breakdown_other),
                            bytes = breakdown.otherBytes
                        )
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.report_bug),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clickable {
                        uriHandler.openUri("mailto:juryjeremy@gmail.com?subject=PurgeCore%20Mobile%20-%20Bug%20report")
                    },
            )
            Text(
                text = stringResource(R.string.visit_website),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp)
                    .clickable { uriHandler.openUri("https://jeremstyke.github.io/purgecore/mobile.html") },
            )
            DonateButton(modifier = Modifier.padding(bottom = 8.dp))
        }
    }
}

@Composable
private fun BreakdownRow(color: androidx.compose.ui.graphics.Color, label: String, bytes: Long) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = androidx.compose.foundation.shape.CircleShape)
        )
        Spacer(Modifier.width(12.dp))
        Text(text = label, modifier = Modifier.weight(1f))
        Text(text = formatBytes(bytes), fontWeight = FontWeight.SemiBold)
    }
}
