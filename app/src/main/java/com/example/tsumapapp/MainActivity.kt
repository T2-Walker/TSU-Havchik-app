package com.example.tsumapapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import com.example.tsumapapp.ui.theme.TSUmapappTheme

import com.example.tsumapapp.ui.screens.AzvezdochkaScreen
import com.example.tsumapapp.ui.screens.OSMDMap
import com.example.tsumapapp.ui.screens.ClusterScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TSUmapappTheme {
                TSUmapappApp()
            }
        }
    }
}

@PreviewScreenSizes /* аннотация для того чтобы функция работала на разных размерах экрана (вроде бы) */
@Composable /* аннотация для того чтобы функция могла создать интерфейс (компилятор перерисовает интерфейс при изменении этой функции */
fun TSUmapappApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.A) } /* текущая страница - после by запоминание страницы даже после поворота экрана */

    NavigationSuiteScaffold(    /* здесь навигация */
        navigationSuiteItems = {    /* кнопки для навигации */
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            painterResource(it.icon),
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {     /* сам экран */
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize()) {    /* Box это вариант группировки контента в котором штуки в нем накладываются друг на друга */
                OSMDMap(modifier = Modifier.fillMaxSize())

                when (currentDestination) { /* переключение между экранами */
                    AppDestinations.A -> AzvezdochkaScreen(
                        modifier = Modifier.padding(
                            innerPadding
                        )
                    )

                    AppDestinations.CLUSTER -> ClusterScreen(
                        modifier = Modifier.padding(
                            innerPadding
                        )
                    )
                    /* здесь будут другие экраны */
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TSUmapappTheme {
        Greeting("Android")
    }
}