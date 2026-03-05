package com.davanok.dvnkquizz.di

import com.davanok.dvnkquizz.ui.di.AppGraph
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph

@DependencyGraph(AppScope::class)
interface IosAppGraph: AppGraph