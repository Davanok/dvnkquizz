package com.davanok.dvnkquizz.ui.di

import com.davanok.dvnkquizz.AppClass
import com.davanok.dvnkquizz.core.di.CoreGraph
import dev.zacsweers.metrox.viewmodel.ViewModelGraph

interface AppGraph: ViewModelGraph, CoreGraph {
    val app: AppClass
}