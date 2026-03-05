package com.davanok.dvnkquizz.ui.screens.packagePicker

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.davanok.dvnkquizz.core.domain.entities.GamePackage
import dev.zacsweers.metrox.viewmodel.metroViewModel
import dvnkquizz.sharedui.generated.resources.Res
import dvnkquizz.sharedui.generated.resources.ic_close
import dvnkquizz.sharedui.generated.resources.ic_search
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PackagePicker(
    onPackageSelected: (GamePackage) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PackagePickerViewModel = metroViewModel()
) {
    var searchBarExpanded by remember { mutableStateOf(false) }
    val query by viewModel.searchQuery.collectAsState()
    val pagingItems = viewModel.packages.collectAsLazyPagingItems()

    DockedSearchBar(
        modifier = modifier,
        inputField = {
            SearchBarDefaults.InputField(
                modifier = Modifier.fillMaxWidth(),
                query = query,
                onQueryChange = viewModel::onQueryChange,
                onSearch = { searchBarExpanded = false },
                expanded = searchBarExpanded,
                onExpandedChange = { searchBarExpanded = it },
                placeholder = { Text("Search for a game package...") },
                leadingIcon = {
                    Icon(painterResource(Res.drawable.ic_search), contentDescription = null)
                              },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                            Icon(painterResource(Res.drawable.ic_close), contentDescription = null)
                        }
                    }
                }
            )
        },
        expanded = searchBarExpanded,
        onExpandedChange = { searchBarExpanded = it },
    ) {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            // Initial Loading State
            if (pagingItems.loadState.refresh is LoadState.Loading) {
                item {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
                }
            }

            items(
                count = pagingItems.itemCount,
                key = pagingItems.itemKey { it.id }
            ) { index ->
                pagingItems[index]?.let { pkg ->
                    PackageItem(pkg, onClick = {
                        onPackageSelected(pkg)
                        searchBarExpanded = false
                    })
                }
            }

            // Append Loading/Error States
            item {
                when (val state = pagingItems.loadState.append) {
                    is LoadState.Loading -> LoadingIndicator(modifier = Modifier.padding(16.dp))
                    is LoadState.Error -> TextButton(onClick = { pagingItems.retry() }) {
                        Text("Error loading more. Retry?")
                    }
                    else -> {
                        if (pagingItems.itemCount == 0 && pagingItems.loadState.refresh is LoadState.NotLoading) {
                            Text("No packages found", modifier = Modifier.padding(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PackageItem(
    pkg: GamePackage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        modifier = modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        headlineContent = {
            Text(pkg.title, style = MaterialTheme.typography.titleMedium)
        },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "By ${pkg.authorId ?: "Anonymous"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = pkg.description,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        trailingContent = {
            SuggestionChip(
                onClick = { },
                label = { Text("Diff: ${pkg.difficulty}") },
                border = null,
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            )
        }
    )
}