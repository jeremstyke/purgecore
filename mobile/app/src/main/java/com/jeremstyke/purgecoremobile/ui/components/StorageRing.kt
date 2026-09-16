package com.jeremstyke.purgecoremobile.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** One colored slice of the ring, sized by its share of totalBytes. */
data class RingSegment(val bytes: Long, val color: Color)

/**
 * A ring made of colored arcs, one per category, plus a gray remainder for
 * free space, rather than a single-color used/free split. Hand-drawn via
 * Canvas since neither Compose's CircularProgressIndicator nor a stack of
 * them can show more than one color per ring cleanly.
 */
@Composable
fun StorageRing(segments: List<RingSegment>, totalBytes: Long, centerLabel: String) {
    val ringSize = 180.dp
    val strokeWidthDp = 16.dp
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = Modifier.size(ringSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(ringSize)) {
            val strokeWidthPx = strokeWidthDp.toPx()
            val stroke = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt)
            val diameter = size.minDimension - strokeWidthPx
            val arcSize = Size(diameter, diameter)
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)

            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke
            )

            if (totalBytes > 0) {
                var startAngle = -90f
                for (segment in segments) {
                    if (segment.bytes <= 0) continue
                    val sweep = 360f * (segment.bytes.toFloat() / totalBytes.toFloat())
                    drawArc(
                        color = segment.color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = stroke
                    )
                    startAngle += sweep
                }
            }
        }
        Text(
            text = centerLabel,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
