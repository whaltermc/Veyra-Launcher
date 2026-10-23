package dev.whaltermc.veyra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.Lifecycle
import dev.whaltermc.veyra.ui.navigation.VeyraNavHost
import dev.whaltermc.veyra.ui.theme.Theme.Veyra
import kotlinx.coroutines.launch
import dev.whaltermc.veyra.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Veyra {
                VeyraNavHost()
            }
        }

        // Warm boot: pre-load last used instance data
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.warmBoot()
            }
        }
    }
}