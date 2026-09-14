package com.jeremstyke.purgecoremobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jeremstyke.purgecoremobile.R
import com.jeremstyke.purgecoremobile.data.StorageInfo
import com.jeremstyke.purgecoremobile.data.StorageRepository
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

    LaunchedEffect(Unit) {
        storageInfo = StorageRepository.readStorageInfo()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(R.string.storage_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(20.dp))

        storageInfo?.let { info ->
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(20.dp)) {
                    LinearProgressIndicator(
                        progress = { info.usedFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(999.dp)),
                    )
                    Spacer(Modifier.height(12.dp))
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
    }
}
