package com.example.tsumapapp.ui.screens

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.foundation.layout.*//дает нам colum row box padding
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text //юай компоненты взятые с Material3
import androidx.compose.runtime.*//работаем с состояними remember, mutableStateOf, by
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView//мост между старым Android View и новой Compose
import org.osmdroid.config.Configuration
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay
import java.io.File


//название и координаты мест
data class Cafe(
    val name: String,
    val lat: Double,
    val lon: Double,

    )
//Хачу безумку прям в ГК
val cafes = listOf(
    Cafe("Столовая ГК",        56.469445, 84.946860),
    Cafe("Кафе Минутка",        56.469368, 84.946882),
    Cafe("Сибирские Блины ГК",        56.469346, 84.946673),
    Cafe("Starbooks",        56.469603, 84.946139),
    Cafe("Автомат с едой ГК",        56.469309, 84.947649),
    Cafe("Сыр-Бор",        56.470781, 84.946160),
    Cafe("Столовая - 2 корупс",        56.468599, 84.945165),
    Cafe("Xo bakery",        56.468390, 84.945106),
    Cafe("Автомат с едой - 2 корпус(нексколько)",        56.468768, 84.945042),
    Cafe("Белка",        56.471141, 84.950165),
    Cafe("Укромное местечко",        56.472465, 84.948596),
    Cafe("Столовая номер 5",        56.469744, 84.942338),
    Cafe("Кафе - Научка",        56.467570, 84.949966),
    Cafe("Автомат с едой",        56.470431, 84.942636),
    Cafe("10-й корпус, тут едят людей",        56.468634, 84.943433),
    //🚜КАТЯ ТУТ МОЖНА ДОБОВЛЯТЬ НОВЫЕ МЕСТА ДЛЯ ПОЕСТЬ
    //намудрили вы канеш с этим ОпенСтриииитМэээп
)

//цвета для каждого кластера (максимум 5 кластеров)
val clusterColors = listOf(
    android.graphics.Color.RED,
    android.graphics.Color.BLUE,
    android.graphics.Color.GREEN,
    android.graphics.Color.MAGENTA,
    android.graphics.Color.CYAN
    //🚜КАТЯ ТУТ МОЖНА НАСТРОИТЬ ЦВЕТА КЛАСТЕРОВ
)

//шаблон для храенния данных о местах поесть
data class Cluster(
    val centerLat: Double,
    val centerLon: Double,
    val cafes: List<Cafe>
)

//создаем и просим вернуть наши кластеры
//ФУНКЦИЯ ЛОГИКИ КЛАСТЕРА
fun kMeans(cafes: List<Cafe>, k: Int): List<Cluster> {

    //берет первые k элементы из списка
    //map розодим по каждому элементу списка и преобразуем в новый список
    //it берем ЭТОТ же элемент из списка т.к у нашей лямбды 1 параметр, а тут храним как пару двух значений тобишь координаты
    var centers = cafes.take(k).map { Pair(it.lat, it.lon) }
    //от так оно выглядит
    //Старбукс  → Pair(56.4702, 84.9498)
    //Сибирские → Pair(56.4683, 84.9463)
    //Столовая  → Pair(56.4695, 84.9479)

    //создаем пустйо список кластеров
    var clusters = listOf<Cluster>()

    //сколько?
    repeat(67) {

        //разбиваем списки на группы что нам лямбда вернет то и будет ключом для группы
        val groups = cafes.groupBy { cafe ->
            //возвращаем диапазон индексов списка  тобишь если в enters centers 3 элемента вернет 0, 1, 2
            centers.indices.minByOrNull { i -> //находим элемент с минимальным значением(задаем индекс центра - i
                val dLat = cafe.lat - centers[i].first//берем i-й центр из списка и берем его first тобишь широту
                val dLon = cafe.lon - centers[i].second//аналогично с долготой
                dLat * dLat + dLon * dLon//квадрат расстояния между кафе и центром от это мы и вернем
            }!!//результат полюбому не null от прям Отвечаю
        }

        val newCenters = (0 until k).map { i ->//дял каждого числа из диапозона создаем элемент
            //спрашиваем новый центр дял кластера i
            //от 0 до k
            val group = groups[i] ?: emptyList()//берем группу с ключом i а если левая часть нуль то берем правую
            if (group.isEmpty()) {//проверяем пусой ли список
                centers[i]//оставляем старый центр
            } else {
                Pair(
                    group.map { it.lat }.average(),//берем все кафе в группе и создаем списко из их широт
                    // average даст нам среднее занчение шо и убдет новым ценитром кластера
                    group.map { it.lon }.average()//тоже самое и для долготы
                )
            }
        }
        clusters = (0 until k).map { i ->//от 0 до k  для каждого элемента создаем объект cluster,
            Cluster(
                //берем новый центр и его широту и долготу
                centerLat = newCenters[i].first,
                centerLon = newCenters[i].second,
                cafes = groups[i] ?: emptyList()
            )
        }
        if (newCenters == centers) return clusters//сравниваем старые и новые центры если они одинаковые — алгоритм сошёлся
        //кафе больше не перепрыгивают между кластерами, нет смысла продолжать
        centers = newCenters//обновляем центры для следующей иттерации
        //если центры не сдвинулись — выходим иаче обновляем центры и идём на следующую итерацию
    }
    return clusters
}

class CafeOverlay(
    private var clusters: List<Cluster>//список уже готовых кластеров для отрисовки
) : Overlay(){//наследуемся от Overlay OSMDroid

    //🚜КАТЯ это кистошка наша
    private val cafePaint = Paint().apply{
        style = Paint.Style.FILL//🚜заливка без FILL был бы контур
        strokeWidth = 8f
    }
    //🚜КАТЯ центр нашег кластера просто крестикс
    private val centerPaint = Paint().apply {
        color = android.graphics.Color.BLACK
        style = Paint.Style.STROKE//🚜Контур без заливки
        strokeWidth = 4f//🚜Толщина
    }
    //функция для перересовывания карты вызываем ИЗ вне
    fun updateClusters(newClusters: List<Cluster>, mapView: MapView){
        clusters = newClusters//обновляем данные
        mapView.invalidate()//говорим Android что этот View устарел, перерисуй - OSMDroid вызовет draw() заново
    }

    //Рисуем
    //override — переопределяем метод родителя
    //OSMDroid вызывает его при каждой перерисовке
    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        if(shadow) return//Black Lives Matter
        //projection — переводчик координат широта и долгота -> пиксели экрана с учётом зума и позиции
        val projection = mapView.projection

        //как форыч тока дает нам индекс index - номер кластера ну а клкстер сам объект
        clusters.forEachIndexed { index, cluster ->
            //если индекс выйдет за пределы списка цветов — начнёт сначала
            val color = clusterColors[index % clusterColors.size]
            cafePaint.color = color//меняем цвет кистошки перед рисованием

            //рисуем каждоое кафе в кластере
            cluster.cafes.forEach { cafe ->
                val point = projection.toPixels(GeoPoint(cafe.lat, cafe. lon), null)
                //переводим координаты в пиксели а null — можно передать готовый Point объект
                //круги кафешек
                canvas.drawCircle(
                    point.x.toFloat(),
                    point.y.toFloat(),
                    20f,//🚜радиус точки кафе
                    cafePaint
                    )
                }
            //чертим две линии и получаем крестик тобишь центер нашего кластера
            val center = projection.toPixels(
                GeoPoint(cluster.centerLat, cluster.centerLon), null
            )
            canvas.drawLine(
                center.x - 20f, center.y.toFloat(),
                center.x + 20f, center.y.toFloat(),
                centerPaint
            )
            canvas.drawLine(
                center.x.toFloat(), center.y - 20f,
                center.x.toFloat(), center.y + 20f,
                centerPaint
            )
            //🚜 20f в drawLine — половина длины крестика
            }
        }
    }


@Composable//говорим шо мы делаем ЮАЙ
//🚜 КАТЯ - Юай
fun ClusterScreen(modifier: Modifier = Modifier) {
    var k by remember { mutableStateOf(3) }//ремеберус запоминает значение между перерисовками Compose
    //когда меняется значение — Compose перерисовывает экран
    //by - делегирование, позволяет писать просто k вместо k.value

    //Три наши состояния - текущие кластеры, ссылка на overlay, ссылка на карту
    var clusters by remember { mutableStateOf(listOf<Cluster>()) }
    var cafeOverlay by remember { mutableStateOf<CafeOverlay?>(null) }//<CafeOverlay?> — ? значит может быть null пока карта не создана — null
    var mapView by remember { mutableStateOf<MapView?>(null) }

    //🚜Column — располагает дочерние элементы вертикально сверху вниз
    Column(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->//вызывается один раз при создании создаёт и возвращает MapView
                val config = Configuration.getInstance()
                config.load(
                    context,
                    context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
                )
                config.osmdroidBasePath = context.filesDir
                config.osmdroidTileCache = File(context.filesDir, "osmdroid_tiles")

                MapView(context).apply {
                    setUseDataConnection(true)
                    setTileSource(
                        org.osmdroid.tileprovider.tilesource.TileSourceFactory
                            .DEFAULT_TILE_SOURCE
                    )
                    controller.setZoom(15.5)
                    controller.setCenter(GeoPoint(56.4685, 84.9480))
                    //начальный зумус и центр карты кластеризации

                    setMultiTouchControls(true)

                    val bounds = BoundingBox(
                        56.480, 84.960,
                        56.455, 84.935
                    )
                    setScrollableAreaLimitDouble(bounds)
                    minZoomLevel = 14.0
                    maxZoomLevel = 19.0
                    val overlay = CafeOverlay(clusters)
                    overlays.add(overlay)
                    cafeOverlay = overlay
                    mapView = this
                    //🚜 создаём overlay, добавляем к карте, сохраняем ссылки в состояние Compose
                }
            },
            modifier = Modifier.weight(1f)//занимает всё свободное место в Column кроме панели снизу
        )
        Column(//🚜 второй колум уже панель управления снизу
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),//🚜отступпы СО ВСЕХ сторон
            horizontalAlignment = Alignment.CenterHorizontally//🚜центрируем по горизонатли
        ) {
            Text(
                text = "Количество клистерочков: $k",
                fontSize = 16.sp//🚜размерус шрифта
            )
            Slider(//🚜ползунок
                value = k.toFloat(),
                onValueChange = { k = it.toInt() },//лямбдус вызывается при движении it - новое значение флота конвертируме уже в интус
                valueRange = 2f..5f,//ренджа кластеров
                steps = 2,//премежуток шагов
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))//ратсояние между слайдеров и кнопкой
            Button(//вызов татального энвелоупа
                onClick = {
                    val result = kMeans(cafes, k)//запускаем нашу мега логику и поулчаем список призывников
                    mapView?.let { map ->
                        cafeOverlay?.updateClusters(result, map)//обновляем оверлей новыми кластерами
                    }
                }
            ) {
                Text("Энвелоупнуть")
            }
        }
    }
}
