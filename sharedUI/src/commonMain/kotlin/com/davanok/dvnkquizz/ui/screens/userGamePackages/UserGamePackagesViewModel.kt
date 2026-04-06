package com.davanok.dvnkquizz.ui.screens.userGamePackages

import androidx.lifecycle.ViewModel
import com.davanok.dvnkquizz.core.domain.repositories.UserGamePackagesRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey


@Inject
@ViewModelKey(UserGamePackagesViewModel::class)
@ContributesIntoMap(AppScope::class)
class UserGamePackagesViewModel(
    private val repository: UserGamePackagesRepository
): ViewModel() {

}