package com.example.tsumapapp.ui.screens


import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.util.BoundingBox
import java.io.File
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize

@Composable
fun OSMDMap(    /* настройки для карты OpenStreetMap */


    modifier: Modifier = Modifier,
    latitude: Double = 56.469807,
    longitude: Double = 84.947217,
    zoom: Double = 18.7
) {
    val fixedBounds = BoundingBox(56.473638, 84.950547, 56.463845, 84.93758)
    var currentBounds by remember { mutableStateOf(fixedBounds) }
    var screenWidth by remember { mutableStateOf(0f) }
    var screenHeight by remember { mutableStateOf(0f) }

    Box(modifier = modifier) { //накладывает элементы друг на друга

        AndroidView(    /* OSMD не робит с jetpack compose поэтому используем старый AndroidView для карт */
            factory = { context ->  /* конструктор из AndroidView для создания карты один раз при заупске приложения, context - все что телефон предоставляет приложению */
                val config = Configuration.getInstance()
                config.load(   /* config хранит глобальные настройки всех карт в приложении, getInstance возвращает единственный Configuration, чтобы он был один на все приложение */
                    context,
                    context.getSharedPreferences(
                        "osmdroid",
                        Context.MODE_PRIVATE
                    )  /* создание папки для хранения context */
                )
                config.osmdroidBasePath =
                    context.filesDir /* путь для хранения кэша при создании карты */
                config.osmdroidTileCache = File(context.filesDir, "osmdroid_tiles")


                MapView(context).apply {
                    setUseDataConnection(true)
                    setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)

                    controller.setZoom(zoom)
                    controller.setCenter(GeoPoint(latitude, longitude))

                    setMultiTouchControls(true) /* чтобы двумя пальцами можно было управлять картой */

                    val bounds = BoundingBox(
                        56.473638,
                        84.950547,
                        56.463845,
                        84.93758,
                    )
                    setScrollableAreaLimitDouble(bounds)    /* задаем границы нашей карты */
                    minZoomLevel = 18.2
                    maxZoomLevel = 21.0

                    // Обновляем границы после движения карты
                    setOnTouchListener { _, event ->
                        when (event.action) {
                            android.view.MotionEvent.ACTION_UP,
                            android.view.MotionEvent.ACTION_CANCEL -> {
                                currentBounds = boundingBox
                            }
                        }
                        false
                    }
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { size ->
                    screenWidth = size.width.toFloat()
                    screenHeight = size.height.toFloat()
                }
        )
        GridOveralay(
            north = currentBounds.latNorth,
            south = currentBounds.latSouth,
            east = currentBounds.lonEast,
            west = currentBounds.lonWest,
            screenWidth = screenWidth,
            screenHeight = screenHeight
        )
    }
}
