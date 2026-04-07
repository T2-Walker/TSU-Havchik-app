package com.example.tsumapapp.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.util.GeoPoint
import androidx.compose.runtime.MutableState

object Azvezdochka_algoritm {
    private var isInitialized = false

    fun MapTouchsetup(
        mapViewRef: MutableState<MapView?>,
        onMarkerAdded: (GeoPoint) -> Unit = {}
    ) {
        val map = mapViewRef.value ?: return    // если null - выходит из функции return

        if (isInitialized) return   //здесь проверка чтобы функция не запускалась много раз
        isInitialized = true

        val receiver = object : MapEventsReceiver { //на основе уже существующего в osmd листенера событий делаем свой с обработкой нажати й
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {  // override дополняет или перезаписывает уже существующую функцию
                p?.let { geoPoint ->
                    addMarker(map, geoPoint)
                    onMarkerAdded(geoPoint)
                }
                return true
            }

            override fun longPressHelper(p: GeoPoint?): Boolean = false
        }

        val eventsOverlay = MapEventsOverlay(map.context, receiver)
        map.overlays.add(eventsOverlay)
        map.invalidate()
    }

    fun addMarker(map: MapView, geoPoint: GeoPoint) {
        val marker = Marker(map)
        marker.position = geoPoint
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = "Метка"
        marker.snippet = "(${geoPoint.latitude}, ${geoPoint.longitude})"    // надпись под title при наведении
        map.overlays.add(marker)
        map.invalidate()
    }
}

@Composable
fun AzvezdochkaScreen(modifier: Modifier = Modifier){


}
