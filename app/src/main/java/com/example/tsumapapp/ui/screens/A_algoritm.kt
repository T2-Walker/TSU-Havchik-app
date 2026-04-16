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
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import com.example.tsumapapp.enveloup.geoPointToMatrix
import com.example.tsumapapp.enveloup.Matrix
import com.example.tsumapapp.enveloup.matrixToGeoPoint
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt
import org.osmdroid.views.overlay.Polyline
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

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
                println("singleTapConfirmedHelper called")
                p?.let { geoPoint ->    //для каждого геопоинта от перехваченного нажатия на экран добавляем метку
                    println("GeoPoint: ${geoPoint.latitude}, ${geoPoint.longitude}")
                    onMarkerAdded(geoPoint)
                }
                return true
            }
            override fun longPressHelper(p: GeoPoint?): Boolean = false
        }

        val eventsOverlay = MapEventsOverlay(receiver)  //создаем оверлей для событий, он не накладывает ничего визуально, а нужен чтобы перехватывать события
        currentOverlay = eventsOverlay
        map.overlays.add(eventsOverlay)
        map.invalidate()
        println("enableMarkers: overlay added, total overlays: ${map.overlays.size}")
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
        println("addMarker called, current overlays: ${map.overlays.size}")
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

    // дальше сам алгоритм
    data class Node(    // узел - клетка на карте
        val row: Int,
        val col: Int,
        var g: Double = Double.MAX_VALUE,  // стоимость от старта до этого узла
        var h: Double = 0.0,               // эвристика до цели
        var f: Double = Double.MAX_VALUE,   // f это общая предполагаемая стоимость пути через узел, g - сколько уже прошли, h - сколько осталось
        var parent: Node? = null           // для восстановления пути
    )

    private val directions = arrayOf(   // нправления движения
        -1 to 0,   // вверх     to создает кортеж пар
        1 to 0,    // вниз
        0 to -1,   // влево
        0 to 1,    // вправо
        -1 to -1,  // вверх-влево
        -1 to 1,   // вверх-вправо
        1 to -1,   // вниз-влево
        1 to 1     // вниз-вправо
    )

    private fun heur(row1: Int, col1: Int, row2: Int, col2: Int): Double {  //эвристика - диагональное расстояние
        return max(abs(row1 - row2), abs(col1 - col2)).toDouble()
    }

    suspend fun findPath(matrix: Array<IntArray>, startRow: Int, startCol: Int, endRow: Int, endCol: Int): List<Pair<Int, Int>>? = withContext(Dispatchers.Default) {  //функция поиска пути
        println("AStar.findPath called")
        println("start: ($startRow, $startCol), end: ($endRow, $endCol)")

        if (matrix[startRow][startCol] == 1 || matrix[endRow][endCol] == 1) {    // Проверка валидности старта и цели
            return@withContext null  // старт или цель на стене
        }

        val rows = matrix.size
        val cols = matrix[0].size

        val openSet = mutableSetOf<Node>()  // открытйц список - для узлов по которым еще не прошлись
        val closedSet = mutableSetOf<Node>()    // закрытый список - для узлов по которым прошли

        val startNode = Node(startRow, startCol, g = 0.0)      // стартовый и целевой узлы
        startNode.h = heur(startRow, startCol, endRow, endCol)  //задаем эвристику - сколько осталось до цели
        startNode.f = startNode.g + startNode.h
        openSet.add(startNode)

        val nodeMap = mutableMapOf<Pair<Int, Int>, Node>()  // карта для быстрого доступа к узлам по координатам
        nodeMap[startRow to startCol] = startNode

        while (openSet.isNotEmpty()) {  // пока у нас есть узлы по которым не прошлись
            val current = openSet.minByOrNull { it.f } ?: break // minByOrNull находит элемент с самым маленьким значением того что в скобках, ломаем цикл если ссписок пуст
            openSet.remove(current)
            closedSet.add(current)  // текущий узел переносим в закрытый список

            if (current.row == endRow && current.col == endCol) {   // если текущий узел это цель - возвращаем путь, цикл закроется на следующей итерации
                return@withContext reconstructPath(current)
            }

            for ((dr, dc) in directions) {  // для каждого узла проверяем соседей по 8ми направлениям
                val newRow = current.row + dr
                val newCol = current.col + dc

                if (newRow !in (0 until rows) || newCol !in (0 until cols)) continue    //until создает диапазон range, здесь мы проверяем не выходим ли мы за границы матрицы
                if (matrix[newRow][newCol] == 1) continue   // проверяем проходима ли клетко

                val neighbor = nodeMap.getOrPut(newRow to newCol) { // getorput получает значение по ключу или создает новое - узел по координатам матрицы
                    Node(newRow, newCol)                                // он здесь нужен чтобы не добавлять один и тот же узел в мапу по нескольку раз
                }                                                     //а вообще здесь мы ищем соседний узел

                if (closedSet.contains(neighbor)) continue  //если сосед в закрытом списке, то есть мы уже проходились по нему - скип

                val stepCost = if (dr != 0 && dc != 0) 1.414 else 1.0   //стоимость шага - 1 по прямой и √2 для диагонали
                val gDir = current.g + stepCost   //стоимость хода по текущему направлению

                //обновляем данные узла соседа
                if (gDir < neighbor.g || !openSet.contains(neighbor)) { //если g соседа больше - значит мы нашли более короткий путь до старта,
                    neighbor.parent = current     // если соседа нет в открытом списке или его g был больше - заполняем его данные
                    neighbor.g = gDir
                    neighbor.h = heur(neighbor.row, neighbor.col, endRow, endCol)
                    neighbor.f = neighbor.g + neighbor.h

                    if (!openSet.contains(neighbor)) {  //добавляем соседа в открытый список
                        openSet.add(neighbor)
                    }
                }
            }
        }
        println("AStar.findPath: path not found")
        return@withContext null //если мы не нашли целевой узел - возвращаем null
    }

    private fun reconstructPath(endNode: Node): List<Pair<Int, Int>> {  // восстанавливаем путь чтобы потом нарисовать его на карте
        val path = mutableListOf<Pair<Int, Int>>()
        var current: Node? = endNode
        while (current != null) {
            path.add(current.row to current.col)
            current = current.parent
        }
        return path.reversed()  // от старта к цели
    }

    //функция для отображения пути на карте
    fun displayPath(mapViewRef: MutableState<MapView?>, path: List<Pair<Int, Int>>) {
        val map = mapViewRef.value ?: return    //если нет мапы - возвращаем ничего

        removeLines(mapViewRef)

        val geoPoints = path.map { (row, col) ->    //переводим каждый узел из элемента матрицы обратно в координаты
            matrixToGeoPoint(row, col)
        }

        val polyline = Polyline()   // polyline это osmd-шная линия которая рисуется по гео координатам
        polyline.setPoints(geoPoints)
        val paint = polyline.getOutlinePaint()
        paint.color = android.graphics.Color.CYAN   //настрйока цвета и ширины линии
        paint.strokeWidth = 5.0f

        map.overlays.add(polyline)
        map.invalidate()
    }

    fun removeLines(mapViewRef: MutableState<MapView?>) {   //функция чтобы убрать линии
        val map = mapViewRef.value ?: return
        val existingLines = map.overlays.filterIsInstance<Polyline>()
        map.overlays.removeAll(existingLines)
    }

    fun reset() {
        currentOverlay = null
        isInitialized = false
    }
}

@Composable
fun AzvezdochkaScreen(modifier: Modifier = Modifier, mapViewRef: MutableState<MapView?>, matrix: Array<IntArray>?){
    val coroutineScope = rememberCoroutineScope()
    var markers by remember { mutableStateOf<List<GeoPoint>>(emptyList()) }

    LaunchedEffect(Unit) {
        Azvezdochka_algoritm.reset()
        Azvezdochka_algoritm.enableMarkers(mapViewRef, onMarkerAdded = { geoPoint ->
            if (matrix == null) {   // проверка загрузилась ли матрица
                Toast.makeText(mapViewRef.value?.context, "Матрица еще загружается", Toast.LENGTH_SHORT).show()
                return@enableMarkers
            }

            println("Поставляенный geoPoint: ${geoPoint.latitude}, ${geoPoint.longitude}")

            Azvezdochka_algoritm.addMarker(mapViewRef.value!!, geoPoint)    // наносим марккер на карту кастомной функцией
            markers = markers + geoPoint //добавляем маркер который пользователь поставил в лист маркеров

            when (markers.size) {
                2 -> {
                    val startMatrix = geoPointToMatrix(markers[0])  //преобразуем маркеры в элемент матрицы
                    val endMatrix = geoPointToMatrix(markers[1])

                    val startGeo = matrixToGeoPoint(startMatrix.first, startMatrix.second)
                    val endGeo = matrixToGeoPoint(endMatrix.first, endMatrix.second)

                    println("Перевод в матрицу стартовой точк: ${startMatrix.first}, ${startMatrix.second}")
                    println("Перевод в матрицу конечной точки: ${endMatrix.first}, ${endMatrix.second}")
                    println("Обратно старт: ${startGeo.latitude}, ${startGeo.longitude}")
                    println("Обратно конец: ${endGeo.latitude}, ${endGeo.longitude}")
                    println("Разница старт: ${geoPoint.latitude - startGeo.latitude}, ${geoPoint.longitude - startGeo.longitude}")

                    coroutineScope.launch {
                        val path = Azvezdochka_algoritm.findPath(
                            matrix,
                            startMatrix.first,
                            startMatrix.second,
                            endMatrix.first,
                            endMatrix.second
                        )

                        withContext(Dispatchers.Main) {
                            if (path != null) {
                                Azvezdochka_algoritm.displayPath(mapViewRef, path)
                            } else {
                                Toast.makeText( //toast это виджет для высплывающих сообщений
                                    mapViewRef.value?.context,
                                    "Путь не найден",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
                3 -> {
                    Azvezdochka_algoritm.clearAllMarkers(mapViewRef)
                    Azvezdochka_algoritm.removeLines(mapViewRef)
                    markers = emptyList()

                }
            }
        })
    }

    DisposableEffect(Unit) {    //выключаем мектки на карте при переключении экрана
        onDispose {
            Azvezdochka_algoritm.disableMarkers(mapViewRef)
            Azvezdochka_algoritm.removeLines(mapViewRef)
        }
    }

    //  !!это пока не трогаем!!
    /*Column {
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
    }*/

}
