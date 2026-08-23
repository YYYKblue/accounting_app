package com.yyykblue.accounting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.yyykblue.accounting.ui.AccountingApp
import com.yyykblue.accounting.ui.AccountingTheme
import com.yyykblue.accounting.ui.AccountingViewModel

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<AccountingViewModel> {
        AccountingViewModel.Factory((application as AccountingApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AccountingTheme {
                AccountingApp(viewModel)
            }
        }
    }
}
