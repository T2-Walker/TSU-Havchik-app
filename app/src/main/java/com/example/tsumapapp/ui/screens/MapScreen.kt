package com.example.tsumapapp.ui.screens

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import java.io.File

@Composable
fun OSMDMap(    /* настройки для карты OpenStreetMap */
    modifier: Modifier = Modifier,
    latitude: Double = 56.469807,
    longitude: Double = 84.947217,
    zoom: Double = 18.7
){  AndroidView(    /* OSMD не робит с jetpack compose поэтому используем старый AndroidView для карт */
        factory = { context ->  /* конструктор из AndroidView для создания карты один раз при заупске приложения, context - все что телефон предоставляет приложению */
            val config = Configuration.getInstance()
            config.load(   /* config хранит глобальные настройки всех карт в приложении, getInstance возвращает единственный Configuration, чтобы он был один на все приложение */
                context,
                context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)  /* создание папки для хранения context */
            )
            config.osmdroidBasePath = context.filesDir /* путь для хранения кэша при создании карты */
            config.osmdroidTileCache = File(context.filesDir, "osmdroid_tiles")


            MapView(context).apply {
                setUseDataConnection(true)
                setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE)

                controller.setZoom(zoom)
                controller.setCenter(GeoPoint(latitude, longitude))

                setMultiTouchControls(true) /* чтобы двумя пальцами можно было управлять картой */
            }
        },
        modifier = modifier
    )

}

