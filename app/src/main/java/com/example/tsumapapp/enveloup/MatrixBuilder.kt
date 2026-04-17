package com.example.tsumapapp.enveloup

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint


object Matrix { // object для того чтобы создать матрицу один раз и всегда обращаться к одной и той же матрице
    private var matrix: Array<IntArray>? = null // private чтобы случайно не изменить матрицу

    suspend fun load(
        context: Context,
        resId: Int
    ): Array<IntArray>? {   // suspend это маркер который помечает чтофункция может выполняться в фоне, чтобы приложение не фризило
        matrix?.let { return it }   // если null то выполнить все что написано ниже, если не null - let выполняет все что в скобках - возвращает it то есть саму матрицу чтобы не читать файл еще раз

        return withContext(Dispatchers.IO) {    //withContext это переключение на поток IO, IO это поток для чтения и вывода файлов
            val input = context.resources.openRawResource(resId)    //вызываем файл из res/raw
            val reader =
                input.bufferedReader() // bufferedreader загружает куски файла в оперативу и читает быстрее

            val lines = reader.readLines()
            val result =
                Array(lines.size) { rowIndex ->    //здесь массив Array который мы заполняем массивами IntArray
                    val row = lines[rowIndex].split(",").map { it.trim().toInt() }
                        .toIntArray() //здесь map применяет все что внутри скобок к каждому элементу списка it
                    row
                }

            matrix = result //первый индекс - линия, второй - колонка
            return@withContext matrix
        }
    }
}

/*
fun geoPointToMatrix(geoPoint: GeoPoint): Pair<Int, Int> {  //функция для получения позиции точки в матрице по геопоинту
    val west = 84.939221
    val south = 56.464447
    val cellSizeDegrees = 0.000023
    val cellSizeLng = cellSizeDegrees / Math.cos(Math.toRadians(56.47))

    val col = ((geoPoint.longitude - west) / cellSizeLng).toInt()
    val row = ((geoPoint.latitude - south) / cellSizeDegrees).toInt()

    return Pair(row, col)
}

fun matrixToGeoPoint(row: Int, col: Int): GeoPoint {    //и наоборот
    val west = 84.939221
    val south = 56.464447
    val cellSizeDegrees = 0.000023
    val cellSizeLng = cellSizeDegrees / Math.cos(Math.toRadians(56.47))

    val latitude = south + row * cellSizeDegrees
    val longitude = west + col * cellSizeLng

    return GeoPoint(latitude, longitude)
}
*/

fun geoPointToMatrix(geoPoint: GeoPoint): Pair<Int, Int> {
    val north = 56.473322
    val east = 84.951942
    val south = 56.464447
    val west = 84.939221

    val rows = 385
    val cols = 305

    val cellSizeLat = (north - south) / rows
    val cellSizeLng = (east - west) / cols

    val row = ((geoPoint.latitude - south) / cellSizeLat).toInt()
    val col = ((geoPoint.longitude - west) / cellSizeLng).toInt()

    val invertedrow =
        rows - row    //инвертируем строки потому что счет идет от юга к северу, а нам нужны координаты от севера к югу

    return Pair(invertedrow, col)
}

fun matrixToGeoPoint(row: Int, col: Int): GeoPoint {
    val north = 56.473322
    val east = 84.951942
    val south = 56.464447
    val west = 84.939221

    val rows = 385
    val cols = 305

    val invertedrow = rows - row    //опять инвертируем потому что здесь счет тоже идет от юга

    val cellSizeLat = (north - south) / rows
    val cellSizeLng = (east - west) / cols

    val latitude = south + (invertedrow) * cellSizeLat
    val longitude = west + (col) * cellSizeLng

    return GeoPoint(latitude, longitude)
}
