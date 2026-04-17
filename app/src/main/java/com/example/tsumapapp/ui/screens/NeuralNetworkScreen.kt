package com.example.tsumapapp.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlin.math.exp
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp



//наши оубчающее данные — цифры от 0-9 в сетке 5x5 - слева направо сверху вниз
val trainingData = listOf(//список пар цифр
    //Зиро
    Pair(
        doubleArrayOf(// 25 чисел, это пиксели цифры
            0.0,1.0,1.0,1.0,0.0,
            1.0,0.0,0.0,0.0,1.0,
            1.0,0.0,0.0,0.0,1.0,
            1.0,0.0,0.0,0.0,1.0,
            0.0,1.0,1.0,1.0,0.0
        ), 0
    ),
    //Ван
    Pair(
        doubleArrayOf(
            0.0,0.0,1.0,0.0,0.0,
            0.0,1.0,1.0,0.0,0.0,
            0.0,0.0,1.0,0.0,0.0,
            0.0,0.0,1.0,0.0,0.0,
            0.0,1.0,1.0,1.0,0.0
        ), 1
    ),
    //Ту
    Pair(
        doubleArrayOf(
            0.0,1.0,1.0,1.0,0.0,
            1.0,0.0,0.0,0.0,1.0,
            0.0,0.0,1.0,1.0,0.0,
            0.0,1.0,0.0,0.0,0.0,
            1.0,1.0,1.0,1.0,1.0
        ), 2
    ),
    //Фри
    Pair(
        doubleArrayOf(
            1.0,1.0,1.0,1.0,0.0,
            0.0,0.0,0.0,0.0,1.0,
            0.0,1.0,1.0,1.0,0.0,
            0.0,0.0,0.0,0.0,1.0,
            1.0,1.0,1.0,1.0,0.0
        ), 3
    ),
    //Фор
    Pair(
        doubleArrayOf(
            1.0,0.0,0.0,1.0,0.0,
            1.0,0.0,0.0,1.0,0.0,
            1.0,1.0,1.0,1.0,1.0,
            0.0,0.0,0.0,1.0,0.0,
            0.0,0.0,0.0,1.0,0.0
        ), 4
    ),
    //Файф
    Pair(
        doubleArrayOf(
            1.0,1.0,1.0,1.0,1.0,
            1.0,0.0,0.0,0.0,0.0,
            1.0,1.0,1.0,1.0,0.0,
            0.0,0.0,0.0,0.0,1.0,
            1.0,1.0,1.0,1.0,0.0
        ), 5
    ),
    //Сикс
    Pair(
        doubleArrayOf(
            0.0,1.0,1.0,1.0,0.0,
            1.0,0.0,0.0,0.0,0.0,
            1.0,1.0,1.0,1.0,0.0,
            1.0,0.0,0.0,0.0,1.0,
            0.0,1.0,1.0,1.0,0.0
        ), 6
    ),
    //Сэвен
    Pair(
        doubleArrayOf(
            1.0,1.0,1.0,1.0,1.0,
            0.0,0.0,0.0,0.0,1.0,
            0.0,0.0,0.0,1.0,0.0,
            0.0,0.0,1.0,0.0,0.0,
            0.0,0.0,1.0,0.0,0.0
        ), 7
    ),
    //Эйт
    Pair(
        doubleArrayOf(
            0.0,1.0,1.0,1.0,0.0,
            1.0,0.0,0.0,0.0,1.0,
            0.0,1.0,1.0,1.0,0.0,
            1.0,0.0,0.0,0.0,1.0,
            0.0,1.0,1.0,1.0,0.0
        ), 8
    ),
    //Найн
    Pair(
        doubleArrayOf(
            0.0,1.0,1.0,1.0,0.0,
            1.0,0.0,0.0,0.0,1.0,
            0.0,1.0,1.0,1.0,1.0,
            0.0,0.0,0.0,0.0,1.0,
            0.0,1.0,1.0,1.0,0.0
        ), 9
    )
)


class neuralNetwork{

    private val inputSize = 25 //5x5 нейронов(пикселей) значение либо 0.0 либо 1.0
    private val hiddenSize = 16//слои пикселей в которых нейронка учитса (заначения 0 или 1)
    private val outputSize = 10//циферки наши(каждый нейрон уверенность в цифре нейрон_3 = 0.9 знаичт 90% шо это тройка

    //скорость обучения нейронки - насколько сильно меняем вес нейрона за раз
    //большая - сеть не сходится, маленькая - долго учитса
    //изменение = learningRate * ошибка * входной_сигнал
    private val learningRate = 0.1

    //DoubleArray - быстрее для математики
    //weights1 — матрица весов входной-скрытый
    private val weights1 = Array(hiddenSize) {
        //маленькие веса дают sigmoid работать в "чувствительной" зоне около 0.5
        DoubleArray(inputSize) { Math.random() * 0.5 - 0.25 }
        //случайное число от -0.25 до 0.25
    }
    //насколько пиксель j важен для нейрона i
    //или насколько каждый пиксель важен для каждого паттерн
    //после обучения нейрон который ищет "вертикальную линию по центру" будет иметь большие веса для пкселей
    //2, 7, 12, 17, 22 (центральный столбец)

    //две матрицы - веса меду входным и скрытным слоем и скрытым и входным
    //weights1[i][j] - вес связи от вохода j к скрытому нейрону i


    //скрытый-выходной
    private val weights2 = Array(outputSize) {
        DoubleArray(hiddenSize) { Math.random() * 0.5 - 0.25 }
        //рандомные они потому что сеть ничего не знает
        //и ы процессе обучения веса подбираются так что бы сеть давала правильные ответы
    }
    //насколько скрытый нейрон j важен для распознавания цифры i
    //или насколько каждый паттерн важен для каждой цифры"

    //веса - показывают насколько важен каждый входной сигнал

    //пиксель_1 (0.8) - нейрон    - этот пиксель важен
    //пиксель_2 (0.1) - нейрон    - этот почти не важен
    //пиксель_3 (-0.5) - нейрон    - этот мешает активации

    //а если отрицательный вес — значит этот вход подавляет нейрон

    //смещения - доп веса для каждого нейрона шоб он смог активироватса дае если все входы = 0
    //это дополнительное слагаемое которое сдвигает нейрон
    private val bias1 = DoubleArray(hiddenSize) { 0.0 }
    private val bias2 = DoubleArray(outputSize) { 0.0 }
    //в обучении Bias тоже обнволяетса как обычный вес — просто его "вход" всегда равен 1

    //вроде как самый оптимальныйй вариант обучения
    private fun sigmoid(x: Double): Double = 1.0 / (1.0 + exp(-x))

    //нейрон посчитал сумму входов
    //сума может быть любой — от -100 до +100
    //нам нужно привести это к числу от 0 до 1

    private fun sigmoidDerivative(s: Double): Double = s * (1.0 - s)
    //производная от сигмоиды покажет насолько чуйсвителен нейрон
    //если выдает почти 1 его трудо заменить менее 0.5 - легко
    //и поитогу выводит нам максимально чувситвельный нейрон
    //а если 0 - полностью насыщен, не учится


    fun forward(input: DoubleArray): Pair<DoubleArray, DoubleArray> {
        //25 пикселей которые нарисовал пользователь
        //возвращаем ДВА массива от скрытый слой и выходной слой для обучения
        //он распространяет сигнал через сеть
        //Forward считает что из них получается

        //создаем массив из 16 чисел i — индекс текущего нейрона
        //если бы была линия по центру hidden[0] = sigmoid(4.5) ≈ 0.99  - нейрон оч активен
        val hidden = DoubleArray(hiddenSize) { i ->
            var sum = bias1[i]
            for (j in 0 until inputSize) {
                sum += weights1[i][j] * input[j]
            }
            sigmoid(sum)
        }

        //то же самое для выходного слоя — но входы уже не пксели а скрытый слой
        val output = DoubleArray(outputSize) { i ->
            var sum = bias2[i]
            for (j in 0 until hiddenSize) {
                sum += weights2[i][j] * hidden[j]
            }
            sigmoid(sum)
        }
        return Pair(hidden, output)
    }

    fun train(input: DoubleArray, label: Int) {
        //label — правильеный ответ 0-9

        //смотрим что нейронка думает ПРЯМО сейчас
        val (hidden, output) = forward(input)
        //от например output = [0.1, 0.8, 02, 0.1, 0.1, 0.1, 0.1, 0.1 0.1, 0.1]
        //сетка дуамет что это 1 из-за пикселя 0.8

        //строим целевой вектор где it индекс нейрона
        val target = DoubleArray(outputSize) { if (it == label) 1.0 else 0.0 }
        //для правильной цифры ставим 1.0 а остальным 0.0
        //пример лдя 3 target = [0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]
        //one-hot encoding —  только один нейрон должен быть активен
        //на какой нам вектор а патамуша если бы мы сказали "правильный ответ = 3"
        //то непонятно как изменить 10 выходных нейронов  а вектор говорит точно что должен делать каждый нейрон

        //ошибка выходного слоя
        //мы ищем наскока каждый выходной нейрон ошибся ии в какую сторону его нужно подвинуть
        val outputDelta = DoubleArray(outputSize) { i ->
            (target[i] - output[i]) * sigmoidDerivative(output[i])// разница между правильным и нашим ответом
            //умножаем на чувствительность нейрона если нейрон уже насыщен
            //(output примерно 1) — его трудно изменить, поэтому delta маленькая
        }

        //распространяем ошибку на скрытый слой
        val hiddenDelta = DoubleArray(hiddenSize) { j ->
            var error = 0.0
            for (i in 0 until outputSize) {
                error += outputDelta[i] * weights2[i][j]
            }
            //перебираем все выходные нейроны и для каждого
            //outputDelta[i] — наскока выходной нейрон i ошибся
            //weights2[i][j] — наскока скрытый нейрон j влиял на выходной нейрон i
            error * sigmoidDerivative(hidden[j])

            //тобишь если скрытый нейрон j сильно влиял (большой вес) на
            //ошибившийся выходной нейрон — значит j тоже виноват
            //это hiddenDelta "вина" каждого скрытого нейрона используем ее для обновления весов первого слоя
        }

        //обновляем веса
        //новый_вес = старый_вес + learningRate * delta * активация
        //вес уменьшился — скрытый нейрон 3 теперь меньше влияет на "цифра 1" правильно — он слишком сильно его активировал
        for (i in 0 until outputSize) {
            bias2[i] += learningRate * outputDelta[i]
            for (j in 0 until hiddenSize) {
                weights2[i][j] += learningRate * outputDelta[i] * hidden[j]
            }
        }

        //то же самое для весов пиксели-скрытый слой
        for (j in 0 until hiddenSize) {
            bias1[j] += learningRate * hiddenDelta[j]
            for (k in 0 until inputSize) {
                weights1[j][k] += learningRate * hiddenDelta[j] * input[k]
            }
        }
    }
    //принимает 25 пикселей - возвращает одно число (какая цифра нарисована)
    fun predict(input: DoubleArray): Int {
        val (_, output) = forward(input)
        //деструктуризация пары
        //запускаем прямой проход через сеть
        //Pair(скрытый_слой, выходной_слой)
        return output.indices.maxByOrNull { output[it] }!!
        //диапазон индексов массива output 0-9
        //it=1: output[1] = 0.92  - максимум
    }

    //обучает сеть на всех данных оч много раз это главная функция запуска обучения
    fun fit(data: List<Pair<DoubleArray, Int>>, epochs: Int = 5000) {//список обучающих примеров и количество эпох оубчения
        repeat(epochs) {
            data.shuffled().forEach { (input, label) ->
                //перемешивает список в случайном порядке каждую эпоху порядок разный
                //без перемешивания сеть видит всегда: 0, 1, 2, 3, 4, 5, 6, 7, 8, 9
                //она может начать ожидать следующую цифру вместо реального распознавания
                //перемешивание заставляет сеть учиться независимо от порядка
                train(input, label)//обучаем сеть на одном примере
            }
        }
    }
}
@Composable
fun NeuralNetworkScreen(modifier: Modifier = Modifier) {

    //сеетка 5x5 — true = черный пиксель, false = белый
    var pixels by remember {
        mutableStateOf(Array(5) { BooleanArray(5) { false } })
    }
    //результат распознавания (null = ещё не распознавали)
    var recognizedDigit by remember { mutableStateOf<Int?>(null) }
    //сообщение о статусе обучения
    var statusMessage by remember { mutableStateOf("Нажми кнопку для обучения") }
    //обученная нейросеть (null = пака не обучена)
    var neuralNet by remember { mutableStateOf<neuralNetwork?>(null) }
    //обучена ли сеть
    var isTrained by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        //заголовок
        Text(
            text = "Дьякую",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Бахни цифру от 0 до 9",
            fontSize = 14.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        //кнопка обучения
        Button(
            onClick = {
                statusMessage = "Обучаю сеть..."
                val net = neuralNetwork()
                net.fit(trainingData, epochs = 5000)
                neuralNet = net
                isTrained = true
                statusMessage = "Сеть обучена! теперь рисуй цифру ✓"
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isTrained) Color(0xFF388E3C) else Color(0xFF1565C0)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isTrained) "✓ Сеть обучена" else "Обучить нейросеть")
        }

        Text(
            text = statusMessage,
            fontSize = 13.sp,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        //сетка 5x5
        Text(
            text = "Нарисуй цифру:",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        //размер одной клетки
        val cellSize = 56.dp
        val cellSizePx = with(LocalDensity.current) { cellSize.toPx() }

        //сетка рисуется через Box с pointerInput
        Box(
            modifier = Modifier
                .size(cellSize * 5)
                .border(2.dp, Color.Gray)
                //detectTapGestures — ловим касания пальца
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        //определяем какую клетку нажали
                        val col = (offset.x / cellSizePx).toInt().coerceIn(0, 4)
                        val row = (offset.y / cellSizePx).toInt().coerceIn(0, 4)

                        //переключаем пиксель
                        val newPixels = Array(5) { r ->
                            BooleanArray(5) { c -> pixels[r][c] }
                        }
                        newPixels[row][col] = !newPixels[row][col]
                        pixels = newPixels

                        //сбрасываем результат при изменении рисунка
                        recognizedDigit = null
                    }
                }
        ) {
            //рисуем каждую клетку
            for (row in 0 until 5) {
                for (col in 0 until 5) {
                    Box(
                        modifier = Modifier
                            .offset(
                                x = cellSize * col,
                                y = cellSize * row
                            )
                            .size(cellSize)
                            .background(
                                if (pixels[row][col]) Color.Black
                                else Color.White
                            )
                            .border(0.5.dp, Color.LightGray)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        //кнопки управления
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            //кнопка очистки
            Button(
                onClick = {
                    pixels = Array(5) { BooleanArray(5) { false } }
                    recognizedDigit = null
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF757575)
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text("Очистить")
            }

            //кнопка распознавания
            Button(
                onClick = {
                    if (!isTrained) {
                        statusMessage = "Сначала обучи сеть!"
                        return@Button
                    }

                    //превращаем сетку пикселей в массив для нейросети
                    val input = DoubleArray(25) { idx ->
                        val row = idx / 5
                        val col = idx % 5
                        if (pixels[row][col]) 1.0 else 0.0
                    }

                    recognizedDigit = neuralNet?.predict(input)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Распознать")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        //резы
        recognizedDigit?.let { digit ->

            Text(
                text = "Распознана цифра:",
                fontSize = 16.sp,
                color = Color.Gray
            )

            Text(
                text = digit.toString(),
                fontSize = 72.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Ваша оценка заведению:",
                fontSize = 16.sp,
                color = Color.Gray
            )

            //пказываем звездочки — от 0 до 9
            //делим на 2 чтобы показать из 5 звеезд (округляем)
            val stars = (digit / 2.0).let { Math.round(it).toInt() }
            Row {
                repeat(5) { index ->
                    Text(
                        text = if (index < stars) "⭐" else "☆",
                        fontSize = 32.sp
                    )
                }
            }

            Text(
                text = "$digit из 9",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1B5E20)
            )
        }
    }
}