package io.celox.xclip.ui.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.celox.xclip.R
import io.celox.xclip.data.ClipboardEntity
import io.celox.xclip.ui.ClipboardViewModel
import io.celox.xclip.ui.components.ClipboardItem
import io.celox.xclip.ui.components.EmptyState
import io.celox.xclip.ui.components.XClipSearchBar
import io.celox.xclip.ui.theme.XClipAnimation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClipboardScreen(
    viewModel: ClipboardViewModel,
    onNavigateToDetail: (ClipboardEntity) -> Unit
) {
    val clipboards by viewModel.clipboards.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val recentlyDeleted by viewModel.recentlyDeleted.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var searchVisible by rememberSaveable { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val undoLabel = stringResource(R.string.action_undo)
    val deletedMessage = stringResource(R.string.snackbar_deleted)
    val copiedMessage = stringResource(R.string.snackbar_copied)

    LaunchedEffect(recentlyDeleted) {
        recentlyDeleted?.let {
            val result = snackbarHostState.showSnackbar(
                message = deletedMessage,
                actionLabel = undoLabel,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDelete()
            } else {
                viewModel.clearRecentlyDeleted()
            }
        }
    }

    val pinnedItems = clipboards.filter { it.isPinned }
    val unpinnedItems = clipboards.filter { !it.isPinned }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    AnimatedContent(
                        targetState = searchVisible,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        label = "titleSearch"
                    ) { isSearching ->
                        if (isSearching) {
                            XClipSearchBar(
                                query = searchQuery,
                                onQueryChange = viewModel::onSearchQueryChanged,
                                visible = true,
                                onClose = {
                                    searchVisible = false
                                    viewModel.onSearchQueryChanged("")
                                }
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.app_name),
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                },
                actions = {
                    if (!searchVisible) {
                        IconButton(onClick = { searchVisible = true }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = stringResource(R.string.cd_search)
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    actionColor = MaterialTheme.colorScheme.primary
                )
            }
        }
    ) { padding ->
        if (clipboards.isEmpty()) {
            EmptyState(
                isSearching = searchQuery.isNotEmpty(),
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                if (pinnedItems.isNotEmpty()) {
                    item(key = "pinned_header") {
                        Text(
                            text = stringResource(R.string.section_pinned),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    itemsIndexed(
                        items = pinnedItems,
                        key = { _, entity -> entity.id }
                    ) { index, entity ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(XClipAnimation.tweenMedium()) +
                                    slideInVertically(XClipAnimation.tweenMedium()) { -it / 2 },
                            modifier = Modifier.animateItem()
                        ) {
                            ClipboardItem(
                                entity = entity,
                                onCopy = { copyToClipboard(context, it.text, copiedMessage) },
                                onDelete = viewModel::deleteEntry,
                                onTogglePin = viewModel::togglePin,
                                onClick = { onNavigateToDetail(it) }
                            )
                        }
                        if (index < pinnedItems.lastIndex) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    item(key = "pinned_spacer") {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                itemsIndexed(
                    items = unpinnedItems,
                    key = { _, entity -> entity.id }
                ) { index, entity ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(XClipAnimation.tweenMedium()) +
                                slideInVertically(XClipAnimation.tweenMedium()) { -it / 2 },
                        modifier = Modifier.animateItem()
                    ) {
                        ClipboardItem(
                            entity = entity,
                            onCopy = { copyToClipboard(context, it.text, copiedMessage) },
                            onDelete = viewModel::deleteEntry,
                            onTogglePin = viewModel::togglePin,
                            onClick = { onNavigateToDetail(it) }
                        )
                    }
                    if (index < unpinnedItems.lastIndex) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String, message: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("XClip", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
