package com.example.tsumapapp.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GridOveralay()
{

    Canvas(Modifier.fillMaxSize())
    {
        //клетки
        val cellsX = 10
        val cellsY = 10

        val cellWidth = size.width / cellsX
        val cellHeight = size.height / cellsY

        for(i in 0..cellsX)
        {
            val x = i * cellWidth
            drawLine(
                color = Color.Gray,
                start = Offset(x = x, y = 0f),
                end = Offset(x = x, y = size.height),
                strokeWidth = 1.dp.toPx()
            )
        }
        for(i in 0..cellsY)
        {
            val y = i * cellHeight
            drawLine(
                color = Color.Gray,
                start = Offset(x = 0f, y = y),
                end = Offset(x = size.width, y = y),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}