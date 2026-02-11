package io.celox.xclip

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import io.celox.xclip.data.ClipboardEntity
import io.celox.xclip.service.ClipboardService
import io.celox.xclip.ui.ClipboardViewModel
import io.celox.xclip.ui.screen.ClipboardScreen
import io.celox.xclip.ui.screen.DetailScreen
import io.celox.xclip.ui.theme.XClipTheme

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: ClipboardViewModel

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startClipboardService()
        } else {
            Toast.makeText(this, getString(R.string.permission_required), Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel = ViewModelProvider(this)[ClipboardViewModel::class.java]

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            startClipboardService()
        }

        setContent {
            XClipTheme {
                var selectedEntity by rememberSaveable { mutableStateOf<ClipboardEntity?>(null) }

                AnimatedContent(
                    targetState = selectedEntity,
                    transitionSpec = {
                        if (targetState != null) {
                            slideInHorizontally { it } + fadeIn() togetherWith
                                    slideOutHorizontally { -it / 3 } + fadeOut()
                        } else {
                            slideInHorizontally { -it } + fadeIn() togetherWith
                                    slideOutHorizontally { it / 3 } + fadeOut()
                        }
                    },
                    label = "navigation"
                ) { entity ->
                    if (entity != null) {
                        DetailScreen(
                            entity = entity,
                            onBack = { selectedEntity = null },
                            onDelete = {
                                viewModel.deleteEntry(it)
                                selectedEntity = null
                            },
                            onTogglePin = viewModel::togglePin
                        )
                    } else {
                        ClipboardScreen(
                            viewModel = viewModel,
                            onNavigateToDetail = { selectedEntity = it }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Notification tap brings app to front - Compose handles it
    }

    private fun startClipboardService() {
        val serviceIntent = Intent(this, ClipboardService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }
}
