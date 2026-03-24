package com.example.tsumapapp.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.min


@Composable
fun GridOveralay(
    north: Double,
    south: Double,
    east: Double,
    west: Double,
    screenWidth: Float,
    screenHeight: Float
)
{
    Canvas(Modifier.fillMaxSize())
    {
        //клетки
        val cellSizeDegrees = 0.0001

        val latRange = north - south
        val lonRange = east - west

        val theoreticalCellsX = (lonRange / cellSizeDegrees).toInt()
        val theoreticalCellsY = (latRange / cellSizeDegrees).toInt()


        if (theoreticalCellsX < 1 || theoreticalCellsY < 1) return@Canvas

        val cellSizePx = min(
            screenWidth / theoreticalCellsX,
            screenHeight / theoreticalCellsY
        )

        val cellsX = (screenWidth / cellSizePx).toInt()
        val cellsY = (screenHeight / cellSizePx).toInt()

        for(i in 0..cellsX)
        {
            val xPos = i * cellSizePx
            drawLine(
                color = Color.LightGray,
                start = Offset(x = xPos, y = 0f),
                end = Offset(x = xPos, y = screenHeight),
                strokeWidth = 1.dp.toPx()
            )
        }
        for(i in 0..cellsY)
        {
            val yPos = i * cellSizePx
            drawLine(
                color = Color.LightGray,
                start = Offset(x = 0f, y = yPos),
                end = Offset(x = screenWidth, y = yPos),
                strokeWidth = 1.dp.toPx()
            )
        }
    }
}