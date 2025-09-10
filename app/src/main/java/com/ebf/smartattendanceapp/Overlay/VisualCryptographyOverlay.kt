package com.ebf.smartattendanceapp.Overlay

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke


import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Composable
fun VisualCryptographyOverlay() {
    Canvas(modifier = Modifier.size(300.dp).clip(RoundedCornerShape(24.dp))) {
        // This simulates the visual crypto share by drawing random dots
        repeat(2000) {
            drawCircle(
                color = Color.White.copy(alpha = 0.6f),
                radius = Random.nextFloat() * 3f,
                center = Offset(x = Random.nextFloat() * size.width, y = Random.nextFloat() * size.height)
            )
        }
        drawRect(color = Color.White, style = Stroke(
            width = 8f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 20f), 0f)
        ), size = size)
    }
}