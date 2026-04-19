package com.davanok.dvnkquizz.ui.screens.userGamePackages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.davanok.dvnkquizz.core.domain.entities.GamePackage
import dev.zacsweers.metrox.viewmodel.metroViewModel
import dvnkquizz.sharedui.generated.resources.Res
import dvnkquizz.sharedui.generated.resources.back
import dvnkquizz.sharedui.generated.resources.create_new_game_package
import dvnkquizz.sharedui.generated.resources.drafts_section_title
import dvnkquizz.sharedui.generated.resources.external_section_title
import dvnkquizz.sharedui.generated.resources.ic_add
import dvnkquizz.sharedui.generated.resources.ic_arrow_back
import dvnkquizz.sharedui.generated.resources.ic_sync
import dvnkquizz.sharedui.generated.resources.no_user_game_packages
import dvnkquizz.sharedui.generated.resources.reload_packages
import dvnkquizz.sharedui.generated.resources.user_game_packages_list_title
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.char
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

private val dateTimeFormat = DateTimeComponents.Format {
    day()
    char('-')
    monthNumber()
    char('-')
    year()
}

@Composable
fun UserGamePackagesScreen(
    onBackClick: () -> Unit,
    navigateToPackage: (packageId: Uuid) -> Unit,
    navigateToNewPackage: () -> Unit,
    viewModel: UserGamePackagesViewModel = metroViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Content(
        uiState = uiState,
        onBackClick = onBackClick,
        onReloadPackagesClick = viewModel::loadData,
        onPackageClick = { navigateToPackage(it.id) },
        onNewPackageClick = navigateToNewPackage,
        modifier = Modifier.fillMaxSize()
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Content(
    uiState: UserGamePackagesScreenUiState,
    onBackClick: () -> Unit,
    onReloadPackagesClick: () -> Unit,
    onNewPackageClick: () -> Unit,
    onPackageClick: (GamePackage) -> Unit,
    modifier: Modifier = Modifier
) {
    val hasAnyPackages = uiState.drafts.isNotEmpty() || uiState.external.isNotEmpty()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(Res.string.user_game_packages_list_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_arrow_back),
                            contentDescription = stringResource(Res.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onReloadPackagesClick) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_sync),
                            contentDescription = stringResource(Res.string.reload_packages)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (hasAnyPackages) {
                FloatingActionButton(onClick = onNewPackageClick) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_add),
                        contentDescription = stringResource(Res.string.create_new_game_package)
                    )
                }
            }
        }
    ) { paddingValues ->
        val contentModifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()

        val isCompletelyEmpty = !hasAnyPackages
                && !uiState.isDraftsLoading
                && !uiState.isExternalLoading
                && uiState.externalError == null

        val isInitialLoading = !hasAnyPackages
                && uiState.isDraftsLoading
                && uiState.isExternalLoading

        when {
            // 1. Both lists are loading for the first time
            isInitialLoading -> Box(contentModifier) {
                LoadingIndicator(modifier = Modifier.align(Alignment.Center))
            }

            // 2. Both lists are entirely empty (no errors, no loading)
            isCompletelyEmpty -> Column(
                modifier = contentModifier.padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(Res.string.no_user_game_packages),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                FilledTonalButton(onClick = onNewPackageClick) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_add),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(Res.string.create_new_game_package))
                }
            }

            // 3. Display the grid with sections for Drafts and External packages
            else -> PackagesGridList(
                uiState = uiState,
                onPackageClick = onPackageClick,
                modifier = contentModifier
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PackagesGridList(
    uiState: UserGamePackagesScreenUiState,
    onPackageClick: (GamePackage) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Adaptive(160.dp),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // --- Drafts Section ---
        if (uiState.drafts.isNotEmpty() || uiState.isDraftsLoading) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                SectionHeader(text = stringResource(Res.string.drafts_section_title)) // Note: You'll need to define this string resource
            }

            if (uiState.isDraftsLoading && uiState.drafts.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp)) {
                        LoadingIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
            } else {
                items(
                    items = uiState.drafts,
                    key = { "draft:${it.id}" } // Prefixed to avoid ID collisions with external list
                ) { pkg ->
                    PackageItem(
                        gamePackage = pkg,
                        onClick = { onPackageClick(pkg) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // --- External Section ---
        if (uiState.external.isNotEmpty() || uiState.isExternalLoading || uiState.externalError != null) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                SectionHeader(
                    text = stringResource(Res.string.external_section_title), // Note: You'll need to define this string resource
                    modifier = Modifier.padding(top = if (uiState.drafts.isNotEmpty()) 16.dp else 0.dp)
                )
            }

            if (uiState.isExternalLoading && uiState.external.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp)) {
                        LoadingIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
            } else if (uiState.externalError != null && uiState.external.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = uiState.externalError,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth().padding(24.dp)
                    )
                }
            } else {
                items(
                    items = uiState.external,
                    key = { "ext:${it.id}" } // Prefixed to avoid ID collisions with drafts list
                ) { pkg ->
                    PackageItem(
                        gamePackage = pkg,
                        onClick = { onPackageClick(pkg) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(bottom = 4.dp, top = 8.dp)
    )
}

@Composable
private fun PackageItem(
    gamePackage: GamePackage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = gamePackage.title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = gamePackage.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // Pushes the footer to the bottom if cards in the same row have different heights
            Spacer(modifier = Modifier.weight(1f, fill = false))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val createdAt = gamePackage.createdAt
                if (createdAt != null) {
                    Text(
                        text = createdAt.format(dateTimeFormat),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                Text(
                    text = gamePackage.difficulty.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}