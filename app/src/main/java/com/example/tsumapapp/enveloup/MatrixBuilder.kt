package com.example.tsumapapp.enveloup

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object Matrix { // object для того чтобы создать матрицу один раз и всегда обращаться к одной и той же матрице
    private var matrix: Array<IntArray>? = null // private чтобы случайно не изменить матрицу

    suspend fun load(context: Context, resId: Int): Array<IntArray>? {   // suspend это маркер который помечает чтофункция может выполняться в фоне, чтобы приложение не фризило
        matrix?.let { return it }   // если null то выполнить все что написано ниже, если не null - let выполняет все что в скобках - возвращает it то есть саму матрицу чтобы не читать файл еще раз

        return withContext(Dispatchers.IO) {    //withContext это переключение на поток IO, IO это поток для чтения и вывода файлов
            val input = context.resources.openRawResource(resId)    //вызываем файл из res/raw
            val reader = input.bufferedReader() // bufferedreader загружает куски файла в оперативу и читает быстрее

            val lines = reader.readLines()
            val result = Array(lines.size) { rowIndex ->    //здесь массив Array который мы заполняем массивами IntArray
                val row = lines[rowIndex].split(",").map { it.trim().toInt() }.toIntArray() //здесь map применяет все что внутри скобок к каждому элементу списка it
                row
            }

            matrix = result
            return@withContext matrix
        }
    }
}