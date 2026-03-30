package com.example.tsumapapp.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


//название и координаты мест
data class Cafe(
    val name: String,
    val lat: Double,
    val lon: Double,

    )
//Хачу безумку прям в ГК
val cafes = listOf(
    Cafe("Старбукс",           56.4702, 84.9498),
    Cafe("Сибирские блины",    56.4683, 84.9463),
    Cafe("Столовая ГК",        56.4695, 84.9479),
    Cafe("Ярче",               56.4671, 84.9512),
    Cafe("Кофе у остановки",   56.4658, 84.9487),
    Cafe("Кафе 2го корпуса",   56.4712, 84.9445),
    Cafe("Вендинг автомат",    56.4689, 84.9501),
)

//шаблон для храенния данных о местах поесть
data class Cluster(
    val centerLat: Double,
    val centerLon: Double,
    val cafes: List<Cafe>
)

//создаем и просим вернуть наши кластеры
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
    }

    return clusters
}
@Composable
fun ClusterScreen(modifier: Modifier = Modifier) {
}
