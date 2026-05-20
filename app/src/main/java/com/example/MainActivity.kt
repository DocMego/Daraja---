package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.screens.MainAppContainer
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.HabitViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: HabitViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val appTheme by viewModel.appTheme.collectAsState()

      MyApplicationTheme(appTheme = appTheme) {
        MainAppContainer(viewModel = viewModel)
      }
    }
  }
}

