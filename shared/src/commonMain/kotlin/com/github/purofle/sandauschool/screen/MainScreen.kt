package com.github.purofle.sandauschool.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.github.purofle.sandauschool.res.Res
import com.github.purofle.sandauschool.res.app_name
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.ExperimentalUuidApi

sealed class Screen {
    object Home : Screen()
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
fun MainScreen() {

    val backStack = remember {
        mutableStateListOf<Screen>(Screen.Home)
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(Res.string.app_name)) }
                )
            }
        ) { pd ->

            NavDisplay(
                backStack = backStack,
                onBack = {
                    if (backStack.size > 1) {
                        backStack.removeLastOrNull()
                    }
                },
                entryProvider = entryProvider {
                    entry(Screen.Home) {
                        HomeScreen()
                    }
                },
                modifier = Modifier.padding(pd)
            )
        }
    }
}