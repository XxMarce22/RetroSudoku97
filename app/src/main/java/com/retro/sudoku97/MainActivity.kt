package com.retro.sudoku97

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.retro.sudoku97.ui.RetroSudokuApp
import com.retro.sudoku97.ui.Sudoku97Theme
import com.retro.sudoku97.ui.Win95
import androidx.compose.ui.graphics.toArgb

class MainActivity : ComponentActivity() {

    private val viewModel: SudokuViewModel by viewModels()
    private var fullscreen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Win95.Teal.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(Win95.Teal.toArgb())
        )
        super.onCreate(savedInstanceState)

        setContent {
            Sudoku97Theme {
                RetroSudokuApp(
                    viewModel = viewModel,
                    onExit = { finish() },
                    onMinimize = { moveTaskToBack(true) },
                    onMaximize = { toggleFullscreen() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.resumeGame()
    }

    override fun onPause() {
        viewModel.pauseGame()
        super.onPause()
    }

    override fun onStop() {
        viewModel.pauseGame()
        super.onStop()
    }

    private fun toggleFullscreen() {
        fullscreen = !fullscreen
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        if (fullscreen) {
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }
}
