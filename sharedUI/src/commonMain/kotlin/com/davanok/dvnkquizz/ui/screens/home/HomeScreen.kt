package com.davanok.dvnkquizz.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.davanok.dvnkquizz.core.domain.entities.GamePackage
import com.davanok.dvnkquizz.core.domain.enums.AppTheme
import com.davanok.dvnkquizz.ui.screens.packagePicker.PackagePicker
import com.davanok.dvnkquizz.ui.utils.enumStrings.drawableRes
import com.davanok.dvnkquizz.ui.utils.enumStrings.titleRes
import dev.zacsweers.metrox.viewmodel.metroViewModel
import dvnkquizz.sharedui.generated.resources.Res
import dvnkquizz.sharedui.generated.resources.app_name
import dvnkquizz.sharedui.generated.resources.change_profile_image
import dvnkquizz.sharedui.generated.resources.delete_profile_image
import dvnkquizz.sharedui.generated.resources.host_game
import dvnkquizz.sharedui.generated.resources.host_game_title
import dvnkquizz.sharedui.generated.resources.ic_check
import dvnkquizz.sharedui.generated.resources.ic_clear
import dvnkquizz.sharedui.generated.resources.ic_error
import dvnkquizz.sharedui.generated.resources.ic_logout
import dvnkquizz.sharedui.generated.resources.ic_person
import dvnkquizz.sharedui.generated.resources.invite_code_field_label
import dvnkquizz.sharedui.generated.resources.invite_code_field_placeholder
import dvnkquizz.sharedui.generated.resources.join_game_title
import dvnkquizz.sharedui.generated.resources.join_lobby
import dvnkquizz.sharedui.generated.resources.log_out
import dvnkquizz.sharedui.generated.resources.my_packages
import dvnkquizz.sharedui.generated.resources.nickname_field_label
import dvnkquizz.sharedui.generated.resources.no_profile_image
import dvnkquizz.sharedui.generated.resources.profile_image
import dvnkquizz.sharedui.generated.resources.submit_nickname
import dvnkquizz.sharedui.generated.resources.unselect_game_package
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.Uuid

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToLobby: (Uuid) -> Unit,
    navigateToUserPackages: () -> Unit,
    viewModel: HomeViewModel = metroViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var inviteCode by remember { mutableStateOf("") }
    var selectedPackage by remember { mutableStateOf<GamePackage?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.app_name)) },
                actions = {
                    ThemeSwitcher(
                        current = uiState.appSettings.theme,
                        onChange = viewModel::setAppTheme
                    )

                    FilledIconButton(onClick = viewModel::logOut) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_logout),
                            contentDescription = stringResource(Res.string.log_out)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = 12.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ProfilePart(
                isLoading = uiState.isProfileLoading,
                nickname = uiState.nickname,
                imageUrl = uiState.imageUrl,
                nicknameChanged = uiState.nicknameChanged,
                onNicknameChange = viewModel::setNickname,
                onImageChange = viewModel::setImage,
                submitNickname = viewModel::submitNickname,
                modifier = Modifier
            )

            TextButton(onClick = navigateToUserPackages) {
                Text(stringResource(Res.string.my_packages))
            }

            GamePart(
                inviteCode = inviteCode,
                onInviteCodeChange = { inviteCode = it.uppercase() },
                selectedPackage = selectedPackage,
                onSelectPackage = { selectedPackage = it },
                joinEnabled = inviteCode.length == 6,
                createEnabled = selectedPackage != null,
                onJoin = {
                    viewModel.onJoinClicked(inviteCode, onNavigateToLobby)
                },
                onCreate = {
                    selectedPackage?.let {
                        viewModel.onCreateGame(it.id, onNavigateToLobby)
                    }
                },
                modifier = Modifier
            )

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage.toString(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun ThemeSwitcher(
    current: AppTheme,
    onChange: (AppTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    fun nextAppTheme(current: AppTheme): AppTheme = when(current) {
        AppTheme.SYSTEM -> AppTheme.LIGHT
        AppTheme.LIGHT -> AppTheme.DARK
        AppTheme.DARK -> AppTheme.SYSTEM
    }

    IconButton(
        onClick = { onChange(nextAppTheme(current)) },
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(current.drawableRes),
            contentDescription = stringResource(current.titleRes)
        )
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ProfilePart(
    isLoading: Boolean,
    nickname: String,
    imageUrl: String?,
    nicknameChanged: Boolean,
    onNicknameChange: (String) -> Unit,
    onImageChange: (ByteArray?) -> Unit,
    submitNickname: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val launcher = rememberFilePickerLauncher(type = FileKitType.Image) { file ->
        if (file != null)
            scope.launch { onImageChange(file.readBytes()) }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .combinedClickable(
                    onClickLabel = stringResource(Res.string.change_profile_image),
                    onClick = { launcher.launch() },
                    onLongClickLabel = stringResource(Res.string.delete_profile_image),
                    onLongClick = { if (imageUrl != null) onImageChange(null) }
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> LoadingIndicator()
                imageUrl == null -> Icon(
                    painter = painterResource(Res.drawable.ic_person),
                    contentDescription = stringResource(Res.string.no_profile_image)
                )
                else -> AsyncImage(
                    model = imageUrl,
                    contentDescription = stringResource(Res.string.profile_image),
                    error = painterResource(Res.drawable.ic_error)
                )
            }
        }

        OutlinedTextField(
            value = nickname,
            onValueChange = onNicknameChange,
            label = { Text(stringResource(Res.string.nickname_field_label)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { submitNickname() }),
            singleLine = true,
            trailingIcon = {
                AnimatedVisibility(visible = nicknameChanged) {
                    IconButton(onClick = submitNickname) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_check),
                            contentDescription = stringResource(Res.string.submit_nickname)
                        )
                    }
                }
            }
        )
    }
}

@Composable
private fun GamePart(
    inviteCode: String,
    onInviteCodeChange: (String) -> Unit,
    selectedPackage: GamePackage?,
    onSelectPackage: (GamePackage?) -> Unit,
    joinEnabled: Boolean,
    createEnabled: Boolean,
    onJoin: () -> Unit,
    onCreate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState { 2 }
    val scope = rememberCoroutineScope()

    val pages = listOf(
        stringResource(Res.string.join_game_title),
        stringResource(Res.string.host_game_title)
    )

    Column(modifier = modifier) {
        PrimaryTabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier.fillMaxWidth()
        ) {
            pages.forEachIndexed { index, page ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.scrollToPage(index) } },
                    text = { Text(text = page) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { currentPage ->
            val isJoin = currentPage == 0

            if (isJoin)
                JoinGamePage(
                    inviteCode = inviteCode,
                    onInviteCodeChange = onInviteCodeChange,
                    onJoin = onJoin,
                    joinEnabled = joinEnabled,
                    modifier = Modifier.fillMaxSize()
                )
            else
                HostGamePage(
                    selectedPackage = selectedPackage,
                    onSelectPackage = onSelectPackage,
                    onCreate = onCreate,
                    createEnabled = createEnabled,
                    modifier = Modifier.fillMaxSize()
                )
        }
    }
}

@Composable
private fun JoinGamePage(
    inviteCode: String,
    onInviteCodeChange: (String) -> Unit,
    onJoin: () -> Unit,
    joinEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = inviteCode,
            onValueChange = onInviteCodeChange,
            label = { Text(stringResource(Res.string.invite_code_field_label)) },
            placeholder = { Text(stringResource(Res.string.invite_code_field_placeholder)) },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = onJoin,
            modifier = Modifier.fillMaxWidth(),
            enabled = joinEnabled
        ) {
            Text(stringResource(Res.string.join_lobby))
        }
    }
}
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun HostGamePage(
    selectedPackage: GamePackage?,
    onSelectPackage: (GamePackage?) -> Unit,
    onCreate: () -> Unit,
    createEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PackagePicker(
            onPackageSelected = onSelectPackage,
            modifier = Modifier.fillMaxWidth()
        )

        if (selectedPackage != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedPackage.title,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = { onSelectPackage(null) }) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_clear),
                        contentDescription = stringResource(Res.string.unselect_game_package)
                    )
                }
            }
        }

        Button(
            onClick = onCreate,
            modifier = Modifier.fillMaxWidth(),
            enabled = createEnabled
        ) {
            Text(stringResource(Res.string.host_game))
        }
    }
}