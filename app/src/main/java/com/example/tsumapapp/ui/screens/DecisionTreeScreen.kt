package com.example.tsumapapp.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.*
import androidx.compose.material3.*
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


//попробуем по типу Энтропии сделать логику
fun entropy(labels: List<String>): Double {//даем список кафэ, получаем дробное число тобишь энтропию

    if(labels.isEmpty()) return 0.0//проверка на пустой списсок

    //it - группируме элементы сами по себе (тобишь теующий элмент)
    // mapValues it.value.size - заменяем каждое значение Map на его размер
    // it здесь это запись Map пара типо ключ-значение
    // it.value — список, а .size — его длина
    val counts = labels.groupBy { it }.mapValues { it.value.size }

    val total = labels.size.toDouble()
    //общее количество меток

    return counts.values.sumOf { count ->//counts.values берём только значения Map, без ключей
        //sumOf суммирует результаты лямбды для каждого элемента а count — количество одной метки

        val p = count / total//вероятност этой метки ну например 3 / 4.0 = 0.75

        //минус перед p патамуша логарифм числа меньше 1 сегда отрицательный а минус на минус = плюс, ну и энтропия положительная
        -p * Math.log(p) / Math.log(2.0)//логарифм по основанию 2
    }
}

//
fun informationGain(rows: List<DataRow>, attribute: String): Double {//берем список строк данных и название признака по которому бдуем проверять например budget


    val totalEntropy = entropy(rows.map {it.label})//rows.map { it.label } — берём только метки из всех строк
    //и считаем энтропию до разделения

    val total = rows.size.toDouble()//общ кол-во строк для подсчёта весов групп

    //группируем строки по значению признака attribute
    val groups = rows.groupBy { it.attrributes[attribute] ?: ""}
    //it.attributes[attribute] — берем из Map значение по ключу например есл attribute = "budget" и строка имеет "budget" = "low" то вернет "low"
    //?: "" нуу а если вдруг такого ключа нет — используем пустую стрку

    //ызвешенная энтропия — среднее энтропий групп с учетом их размера
    val weightedEntropy = groups.values.sumOf { group -> //доля сия группы от всех данных большие группы влияют сильнее
        val weight = group.size / total//энтропия внутри этой группы
        weight * entropy(group.map { it.label })//вклад группы в общую энтропию
    }
    return totalEntropy - weightedEntropy//насколько уменьшилась энтропия после разделения
}

//строим дерево Рекурсива
fun buildTree(rows: List<DataRow>, attributes: List<String>):Treenode //rows текущие строки данных атрибуты не шо не юзали иии
//возвращаем узел дерева Question или Leaf
{
    //Остановка намбер One
    val labels = rows.map {it.label}//список всех меток текущих строк

    if(labels.toSet().size == 1){//преобразуем список в множество а вот множество хранит только уникальные элементы
        return Treenode.Leaf(labels.first())//если было ну 3 ярче вернем УНИКАЛЬНЕО ЯРЧЕ  но 1 size = 1
    }

    //Остоновка намбер two
    if(attributes.isEmpty()){//если признаки закончились но метки ещё разные — беремс самую частую метку
        val mostCommon = labels.groupBy { it }//группируем метки
            .maxByOrNull { it.value.size}!!//находим групуп с большущим рамзером а ну и уверены шо не null
            .key//берем ключ (название кафе), ЭТО не значение (список строк)
        return Treenode.Leaf(mostCommon)
    }

    //ГОЛОСОВАНИЕ ТУДУМ ТУДУМ ТУДУМ ПАПАПАПАМпапам
    val bestAttribute = attributes.maxByOrNull { attribute ->//для каждого признака считаем информационный выгрыш и выбираем максимальный
        informationGain(rows, attribute)
    }!!

    //выкидываем использованный признак
    val remaininAttributes = attributes.filter {it != bestAttribute}//ставляем только те признаки которые не равны лучшему
    //использованный признак больше не нужен — мы уже разделили по нему

    //Рекурсивно строим
    val groups = rows.groupBy { it.attrributes[bestAttribute] ?: ""}//строки разбитые по значению лучшего признака спартанцы кароч
    val children =groups.mapValues { (_, groupRows) -> buildTree(groupRows, remaininAttributes)//и тепреь для каждой группы строим поддерево
        //(_, groupRows) это деструктуризация.наш мапис хранит пары (ключ, значение)
    //а мы разбираем пару на две переменные _ (ключ — нам не нужен) и groupRows (список строк группы)

        //-> buildTree(groupRows, remaininAttributes -рекурсия
    // функция вызывает сама себя для меньшего набора данных каждый раз данных меньше и признаков меньше — потому рекурсия полюбому остановится
    }

    //ФИНАЛУС
    return Treenode.Question(bestAttribute, children)//возвращаем узел-вопрос с лучшим признаком и всеми поддеревьями
}
//🚜Часть юая перепишем дерево в список строк для визуала
//depth - глубина узлища, нужна для отступов
fun treeToLines(node: Treenode, depth: Int = 0): List<String> {

    val indent = "  ".repeat(depth)//отступ - 2 пробела на каждый уровень глубины

    return when (node) {

        //если лист — просто выводим рекомендаци
        is Treenode.Leaf -> listOf("$indent🍽 ${node.recommendation}")

        //а если вопрос — выводим признак и все ветки
        is Treenode.Question -> {
            val lines = mutableListOf<String>()

            //заголовок узла
            lines.add("$indent📂 ${node.attributes}?")

            //для каждого ответа рекурсивно выводим поддерево
            node.children.forEach { (answer, child) ->
                lines.add("$indent  $answer →")
                lines.addAll(treeToLines(child, depth + 1))
            }
            lines
        }
    }
}

//пРЕДИКктим рекомендацию по ответам пользователя и путь по узлам
fun predict(
    node: Treenode,//текущий узел начинаем с корня, при рекурсии идём глубже
    userInput: Map<String, String>,//ответы пользователя
    path: List<String> = emptyList()//путь который уже протопали по умолчанию пуст — в начале мы ещё нигде не были а при рекурсии даем накопленный путь

): Pair<String, List<String>> {//возвращаем пару - название кафе + список шагов пути
    return when (node) {
        //проверяем тип узла
        is Treenode.Leaf -> Pair(node.recommendation, path)//дошли до листа — нашли ответ вернули пару
        //это условие остановки рекурсии

        is Treenode.Question -> {//
            val userAnswer = userInput[node.attributes]//что пользователь выбрал для текющего признака например бюджетаа
            val nextNode = node.children[userAnswer]//ищем в трентусу ветку которая соответствует ответу пользователя
            //node.children - Map всех вето


            //Ответ мы не нашли
            if (nextNode == null) {
                val firstChild = node.children.values.first()//берем первую ветку как запасной вариант
                val newPath = path + "${node.attributes} = $userAnswer (Шота нету:( )"//Добавим шаг в путь шо не нашли
                predict(firstChild, userInput, newPath)//рекурсия с запасной веткой
            } else {//НАШЛИ
                val newPath = path + "${node.attributes} = $userAnswer"//добавляем шаг в путь наприемр "budget = low"
                predict(nextNode, userInput, newPath)//рекккккурсия идем в следующий узел с обновлённым путем
            }
        }
    }
}


@Composable

//🚜Экранус
fun DecisionTreeScreen(modifier: Modifier = Modifier) {

    //🚜тут текст CSV который загрузил пользователь
    var csvText by remember { mutableStateOf("") }
    //🚜построенно дерево (null = еще не построено)
    var tree by remember { mutableStateOf<Treenode?>(null) }
    //🚜 список признаков из CSV
    var attributes by remember { mutableStateOf<List<String>>(emptyList()) }
    //🚜вероятныей значения каждого признака
    var attributeValues by remember { mutableStateOf<Map<String, List<String>>>(emptyMap()) }
    //🚜ответы  пользователя для предсказания
    var userInput by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    //🚜наш предикт
    var prediction by remember { mutableStateOf("") }
    //🚜путь по дереву
    var predictionPath by remember { mutableStateOf<List<String>>(emptyList()) }
    //🚜лог об ошибке
    var errorMessage by remember { mutableStateOf("") }
    //🚜 нужен для загрузки файла
    val context = androidx.compose.ui.platform.LocalContext.current

    //🚜TL лаунчер типа для выбора файла из памяти вашего сяоми нищенского
    val filePicker = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        //uri — путь к выбранному файлу
        if (uri != null) {
            //читаем
            val stream = context.contentResolver.openInputStream(uri)
            csvText = stream?.bufferedReader()?.readText() ?: ""
            stream?.close()
        }
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0072BC))
    ) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .background(
                Color(0xFFE0F5FF),//фон в ихдрасиыль
                shape = RoundedCornerShape(24.dp)
            )
    ) {
        //загоооловок

        Text(
            text = "                    Я есть Гхрут",//мега отступ типо чтоб посередине было
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF0072BC),//цвет текста

        )
        Spacer(modifier = Modifier.height(12.dp))

        //кнопачка загрузки CSV
        Button(
            onClick = { filePicker.launch("text/*") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0072BC)) //фон кнопачки

        ) {
            Text("📂 Наэнвелоупить CSV")
        }

        //показываем сколько символов загружено по рофлу
        if (csvText.isNotEmpty()) {
            Text(
                //прикольная галочка
                text = "✓ Залито ${csvText.length} символов",
                fontSize = 13.sp,
                color = Color(0xFF1F2256)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        //осеменяем гхрута
        Button(
            onClick = {
                errorMessage = ""
                prediction = ""
                predictionPath = emptyList()

                //но но но
                if (csvText.isBlank()) {
                    errorMessage = "Сначала загрузите CSV файл!"
                    return@Button
                }

                //о великий парс работай CSV
                val rows = parseCsv(csvText)

                if (rows.isEmpty()) {
                    errorMessage = "Ни шо не пойму("
                    return@Button
                }

                //собираем список признаков из первой строки
                val attrs = rows.first().attrributes.keys.toList()
                attributes = attrs
                //собираем возможные значения каждого признака
                attributeValues = attrs.associateWith { attr ->
                    rows.map { it.attrributes[attr] ?: "" }.distinct()
                }
                //Энвелоупинг грута
                tree = buildTree(rows, attrs)

                //ферст предикт
                userInput = attrs.associateWith { attr ->
                    attributeValues[attr]?.firstOrNull() ?: ""
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0072BC))//фон кнопачки
        ) {
            Text("🌳 Слепить буратино")
        }
    }
        //🚜если шота не так
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = androidx.compose.ui.graphics.Color.Red,
                fontSize = 14.sp
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        //холый хрут
        tree?.let { currentTree ->
            Text(
                text = "Структура дерева:",
                fontSize = 18.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))
            //та самая функция шо дерево в список строк бацает и показываем каждую строку
            treeToLines(currentTree).forEach { line ->
                Text(
                    text = line,
                    fontSize = 14.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            //блок предикта
            Text(
                text = "Получить рекомендацию:",
                fontSize = 18.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            //кнопачки выбора ля каждоого признака
            attributes.forEach { attr ->
                Text(
                    text = attr,
                    fontSize = 14.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                )
                androidx.compose.foundation.layout.FlowRow {
                    attributeValues[attr]?.forEach { value ->
                        val isSelected = userInput[attr] == value
                        Button(
                            onClick = {
                                userInput = userInput.toMutableMap()
                                    .apply { put(attr, value) }
                            },
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = if (isSelected)
                                    androidx.compose.ui.graphics.Color(0xFF1565C0)
                                else
                                    androidx.compose.ui.graphics.Color(0xFF90CAF9)
                            ),
                            modifier = Modifier.padding(2.dp)
                        ) {
                            Text(value, fontSize = 12.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))

            //кнпка предикта
            Button(
                onClick = {
                    val (result, path) = predict(currentTree, userInput)
                    prediction = result
                    predictionPath = path
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                //прикорльаня лупа
                Text("🔍 Найти кафе")
            }

            //предикт
            if (prediction.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Рекомендуем: $prediction",
                    fontSize = 18.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = androidx.compose.ui.graphics.Color(0xFF1B5E20)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Путь по дереву:",
                    fontSize = 14.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                )

                //каждый шаг пути
                predictionPath.forEach { step ->
                    Text(
                        text = "→ $step",
                        fontSize = 13.sp,
                        color = androidx.compose.ui.graphics.Color.Gray
                    )
                }
            }
        }
    }
}