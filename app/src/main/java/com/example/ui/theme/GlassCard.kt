package com.example.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            // Subtle glass shadow
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(cornerRadius),
                clip = false,
                ambientColor = Color(0xFFB12E33).copy(alpha = 0.08f),
                spotColor = Color(0xFFB12E33).copy(alpha = 0.12f)
            )
            .clip(RoundedCornerShape(cornerRadius))
            // Frosted glass background: translucent white with 45% to 55% opacity
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.55f),
                        Color.White.copy(alpha = 0.40f)
                    )
                )
            )
            // Frosted glass border: 1.dp white with 50% opacity
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.60f),
                        Color.White.copy(alpha = 0.25f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
    ) {
        // Frosted glass blur effect layer beneath the content
        Box(
            modifier = Modifier
                .matchParentSize()
                .blur(16.dp)
                .background(Color.White.copy(alpha = 0.15f))
        )
        // Inner padding of the card container
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}
