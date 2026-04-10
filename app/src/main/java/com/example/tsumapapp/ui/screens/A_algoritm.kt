package com.example.tsumapapp.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.util.GeoPoint
import androidx.compose.runtime.MutableState
import androidx.core.content.ContextCompat
import com.example.tsumapapp.R
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.painterResource
import com.example.tsumapapp.enveloup.geoPointToMatrix
import com.example.tsumapapp.enveloup.Matrix

object Azvezdochka_algoritm {
    private var isInitialized = false
    private var currentOverlay: MapEventsOverlay? = null

    fun enableMarkers(mapViewRef: MutableState<MapView?>, onMarkerAdded: (GeoPoint) -> Unit = {}) { // второй параметр: тип параметра -> возвращаемый тип = значение по умолчанию
        val map = mapViewRef.value ?: return

        if (isInitialized) return   //здесь проверка чтобы функция не запускалась много раз
        isInitialized = true

        if (currentOverlay != null && map.overlays.contains(currentOverlay)) return      // если оверлей уже есть, ничего не делаем

        val receiver = object : MapEventsReceiver { //создаем листенер еще раз если до этого переключались между экранами и снова нажали на экран А*
            override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                p?.let { geoPoint ->
                    addMarker(map, geoPoint)
                }
                return true
            }
            override fun longPressHelper(p: GeoPoint?): Boolean = false
        }

        val eventsOverlay = MapEventsOverlay(receiver)
        currentOverlay = eventsOverlay
        map.overlays.add(0, eventsOverlay)
        map.invalidate()
    }

    fun disableMarkers(mapViewRef: MutableState<MapView?>) {    //отключение оверлея с маркерами
        val map = mapViewRef.value ?: return
        currentOverlay?.let {
            map.overlays.remove(it)
            currentOverlay = null
            map.invalidate()
        }
    }

    fun clearAllMarkers(mapViewRef: MutableState<MapView?>) {
        val map = mapViewRef.value ?: return
        val markers = map.overlays.filterIsInstance<Marker>()
        map.overlays.removeAll(markers)
        map.invalidate()
    }


    fun addMarker(map: MapView, geoPoint: GeoPoint) {   //добавляем маркер на карту и накачиваем его свойствами
        val context = map.context
        val marker = Marker(map)
        val icon = ContextCompat.getDrawable(context, R.drawable.ic_home)   //🚜 надо иконку для метки на мапе
        marker.position = geoPoint
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.icon = icon
        marker.title = "(${geoPoint.latitude}, ${geoPoint.longitude})"    // надпись при наведении
        map.overlays.add(marker)
        map.invalidate()
    }
}

@Composable
fun AzvezdochkaScreen(modifier: Modifier = Modifier, mapViewRef: MutableState<MapView?>){

    LaunchedEffect(Unit) {
        Azvezdochka_algoritm.enableMarkers(mapViewRef)
    }

    // Выключаем при уходе с экрана
    DisposableEffect(Unit) {
        onDispose {
            Azvezdochka_algoritm.disableMarkers(mapViewRef)
        }
    }

    Column {
        Button(onClick = {
            Azvezdochka_algoritm.clearAllMarkers(mapViewRef)
        }) {
            Icon(
                painter = painterResource(
                    R.drawable.ic_favorite //🚜 иконка для кнопки очистки меток на мапе
                ),
                contentDescription = null
            )
        }
    }

}
