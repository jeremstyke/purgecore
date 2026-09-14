package com.jeremstyke.purgecoremobile.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeremstyke.purgecoremobile.ui.theme.Accent

/**
 * A ring gauge showing storage used as a percentage in the center, built on
 * Compose's own CircularProgressIndicator rather than a hand-drawn Canvas,
 * so the geometry is handled by tested library code.
 */
@Composable
fun StorageRing(usedFraction: Float, centerLabel: String, ringSize: Dp = 180.dp) {
    val animatedFraction by animateFloatAsState(
        targetValue = usedFraction.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 700),
        label = "storageRing"
    )

    Box(
        modifier = Modifier.size(ringSize),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.size(ringSize),
            color = MaterialTheme.colorScheme.surfaceVariant,
            strokeWidth = 16.dp,
            strokeCap = StrokeCap.Round,
        )
        CircularProgressIndicator(
            progress = { animatedFraction },
            modifier = Modifier.size(ringSize),
            color = Accent,
            strokeWidth = 16.dp,
            strokeCap = StrokeCap.Round,
        )
        Text(
            text = centerLabel,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
