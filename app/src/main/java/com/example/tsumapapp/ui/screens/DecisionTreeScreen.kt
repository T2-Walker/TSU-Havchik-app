package com.example.tsumapapp.ui.screens

import android.R
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.xml.sax.Attributes

//струтура нашего дерева
//узел дерева — либо вопрос, либо ответ
sealed class  Treenode{//это как enum но каждый вариант может иметь
    // when — компилятор знает все возможные варианты
    //свои поля "запечатанный" — все варианты известны заранее
    //без sealed — компилятор не знает все варианты

    data class Question( // узел-вопрос - знает по какому признаку делит
    // и куда идти при каждом ответе
        val attributes: String, // название признака, например "budget"
        val children: Map<String, Treenode>// ответ - следующий узел
        //это словарик где ключ это строка (ответ пользователя)
        //значение — следующий узел дерева например: "low" - Leaf("Ярче")
    ) : Treenode()

    data class Leaf( // узел-ответ - типо лист дерева: просто название кафе когда алгоритм доходит до листа
        val recommendation: String    // например "Старбукс"
    ) : Treenode()
}
//строка из CSV набор признаков и целевой ответ
data class DataRow(
    val attrributes: Map<String, String>,//"budget" - "low"
    //словарь признаков из строки
    val label: String//"Старбукс" тобишь уже целевой отвте этйо строки
)
//парсинг CSV

fun parseCsv(scvText: String) : List<DataRow>{//функция принимает весь CSV как одну боооольшую строку и возвращает список DataRow
    //разбиваем текст ны строки убирая бустые
    val lines = scvText.trim().split("\n").filter {it.isNotBlank() }
    //тримом убираем пробелы и переносы строк по краям всего текста(если случайно добавили пустую строку в начале или конце файла)
    //сплитом разрезает строку по символу переноса строки превращает один большой текст в список строк
    //фильтр убирает пустые строки из списка а isNotBlank() возвращает `true` если строка содержит хоть какой-то символ кроме пробелов
    //фильтр оставляет только те элементы для которых лямбда вернула `true`

    if(lines.isEmpty()) return emptyList()
    //если после всех операций список пустой — файл был пустым
    //возвращаем пустой список и выходим из функции


    val headers = lines[0].split(",").map {it.trim()}
    //берем заголовок/сплитом режем по запятой/убираем тримом лишние пробелы у каждого заголовка

    val labelColum = headers.last()
    //то что мы предсказываем тоибшь recommended_place

    val attributeColumns  = headers.dropLast(1)
    //возвращает список без последнего элементав все заголовки кроме целевого

    return lines.drop(1).mapNotNull { line -> //список строк без заголовков
        //нужны тока строки с данными
        //как обычный мап но если лямбда вернула `null` — этот элемент выбрасывается** из результата

        val values = line.split(",").map { it.trim() }//тож самое режем по запятым

        if(values.size != headers.size) return@mapNotNull null//@ указывает метку — из какого блока выходим
        //проверяем — количество значений в строке совпадает с количеством заголовков?
        //если нет — строка повреждена ну пользователь забыл запятую
        //возвращаем null и mapNotNull автоматически выбросит эту строку

        val attributes = attributeColumns.mapIndexed { index, column ->
            //проходим по заголовкам-признакам `index` — номер заголовка, `column` — сам заголовок
            column to values[index]//создаём пару заголовок - значение
        }.toMap()//превращает список пар в словарь `Map`

        val label = values.last()//последнее значение строки — это рекомендация кафе

        //ФИНАЛУС
        DataRow(attributes, label)
        //создаём объект `DataRow` из собранных данных и возвращаем его это последняя строка лямбды
    }
}
@Composable
fun DecisionTreeScreen(modifier: Modifier = Modifier) {

}