package com.jeremstyke.purgecoremobile.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jeremstyke.purgecoremobile.R
import com.jeremstyke.purgecoremobile.data.StorageInfo
import com.jeremstyke.purgecoremobile.data.StorageRepository
import com.jeremstyke.purgecoremobile.ui.components.BrandHeader
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

    LaunchedEffect(Unit) {
        storageInfo = StorageRepository.readStorageInfo()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        BrandHeader(
            title = stringResource(R.string.storage_title),
            subtitle = "PurgeCore Mobile"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            storageInfo?.let { info ->
                Spacer(Modifier.height(12.dp))
                StorageRing(
                    usedFraction = info.usedFraction,
                    centerLabel = "${(info.usedFraction * 100).toInt()}%"
                )
                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = "Used",
                        value = formatBytes(info.usedBytes)
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        label = "Free",
                        value = formatBytes(info.freeBytes)
                    )
                }
            } ?: run {
                Box(Modifier.fillMaxWidth().padding(60.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun StatCard(modifier: Modifier = Modifier, label: String, value: String) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}
