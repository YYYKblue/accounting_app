package com.yyykblue.accounting.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yyykblue.accounting.data.TransactionEntity

private enum class MainTab(val title: String) {
    HOME("账单"),
    ADD("记一笔"),
    STATS("统计"),
    SETTINGS("设置"),
}

@Composable
fun AccountingApp(viewModel: AccountingViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var tab by remember { mutableStateOf(MainTab.HOME) }
    var editing by remember { mutableStateOf<TransactionEntity?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                MainTab.entries.forEach { item ->
                    NavigationBarItem(
                        selected = tab == item,
                        onClick = {
                            tab = item
                            if (item != MainTab.ADD) editing = null
                        },
                        icon = {
                            Icon(
                                imageVector = when (item) {
                                    MainTab.HOME -> Icons.Default.Home
                                    MainTab.ADD -> Icons.Default.AddCircle
                                    MainTab.STATS -> Icons.Default.BarChart
                                    MainTab.SETTINGS -> Icons.Default.Settings
                                },
                                contentDescription = null,
                            )
                        },
                        label = { Text(item.title) },
                    )
                }
            }
        },
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            when (tab) {
                MainTab.HOME -> HomeScreen(
                    state = state,
                    onPreviousMonth = viewModel::previousMonth,
                    onNextMonth = viewModel::nextMonth,
                    onEdit = {
                        editing = it
                        tab = MainTab.ADD
                    },
                    onDelete = viewModel::delete,
                )
                MainTab.ADD -> AddTransactionScreen(
                    editing = editing,
                    onCancel = {
                        editing = null
                        tab = MainTab.HOME
                    },
                    onSave = { amount, type, category, merchant, note ->
                        viewModel.save(amount, type, category, merchant, note, editing) {
                            editing = null
                            tab = MainTab.HOME
                        }
                    },
                )
                MainTab.STATS -> StatsScreen(state)
                MainTab.SETTINGS -> SettingsScreen()
            }
        }
    }
}
