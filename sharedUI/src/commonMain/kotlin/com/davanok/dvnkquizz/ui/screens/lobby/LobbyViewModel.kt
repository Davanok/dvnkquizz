package com.davanok.dvnkquizz.ui.screens.lobby

import androidx.lifecycle.ViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey

@Inject
@ViewModelKey(LobbyViewModel::class)
@ContributesIntoMap(AppScope::class)
class LobbyViewModel(

): ViewModel() {
}