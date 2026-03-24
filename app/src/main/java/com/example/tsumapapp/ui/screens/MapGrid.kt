package com.example.tsumapapp.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GridOveralay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawLine(
            color = Color.Red,
            start = Offset(x = 100f, y = 0f),
            end = Offset(x = 100f, y = size.height),
            strokeWidth = 2.dp.toPx()
        )
    }
}