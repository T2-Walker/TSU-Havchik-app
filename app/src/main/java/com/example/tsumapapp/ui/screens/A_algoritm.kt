package com.example.tsumapapp.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.background

@Composable
fun AzvezdochkaScreen(modifier: Modifier = Modifier){

    //шапка на вкладке азвездочка
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0072BC))
            .padding(vertical = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "ТОМСКИЙ ГОСУДАРСТВЕННЫЙ УНИВЕРСИТЕТ",
            fontSize = 14.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Карта студенческих кафе",
            fontSize = 12.sp,
            color = Color.White
        )
    }
}

