package dev.chungjungsoo.gptmobile.presentation.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

/** The two-line menu mark used by the DeepSeek top bars (not a hamburger). */
@Composable
fun DeepSeekMenuIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val stroke = 2.1.dp.toPx()
        drawLine(
            color = tint,
            start = Offset(2.dp.toPx(), 7.dp.toPx()),
            end = Offset(22.dp.toPx(), 7.dp.toPx()),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(2.dp.toPx(), 15.dp.toPx()),
            end = Offset(13.dp.toPx(), 15.dp.toPx()),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

/** Outlined circle + plus, matching the top-right action in the official client. */
@Composable
fun CirclePlusIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(26.dp)) {
        val stroke = 1.8.dp.toPx()
        val radius = size.minDimension / 2f - stroke / 2f
        drawCircle(
            color = tint,
            radius = radius,
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke)
        )
        val half = radius * 0.45f
        drawLine(
            color = tint,
            start = Offset(center.x - half, center.y),
            end = Offset(center.x + half, center.y),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(center.x, center.y - half),
            end = Offset(center.x, center.y + half),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}
