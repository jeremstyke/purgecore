package com.jeremstyke.purgecoremobile.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jeremstyke.purgecoremobile.R
import com.jeremstyke.purgecoremobile.data.StorageInfo
import com.jeremstyke.purgecoremobile.data.StorageRepository
import com.jeremstyke.purgecoremobile.ui.components.BrandHeader
import com.jeremstyke.purgecoremobile.ui.components.DonateButton
import com.jeremstyke.purgecoremobile.ui.components.StorageRing
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
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(Unit) {
        storageInfo = StorageRepository.readStorageInfo()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        BrandHeader(title = stringResource(R.string.storage_title))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            storageInfo?.let { info ->
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(
                    modifier = Modifier.padding(20.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    StorageRing(
                        usedFraction = info.usedFraction,
                        centerLabel = "${(info.usedFraction * 100).toInt()}%"
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "${formatBytes(info.usedBytes)} used of ${formatBytes(info.totalBytes)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "${formatBytes(info.freeBytes)} free",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } ?: run {
            Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
            Spacer(Modifier.weight(1f))
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
