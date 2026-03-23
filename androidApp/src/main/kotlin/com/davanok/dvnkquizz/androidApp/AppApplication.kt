package com.davanok.dvnkquizz.androidApp

import android.app.Application
import com.davanok.dvnkquizz.ui.di.AndroidAppGraph
import dev.zacsweers.metro.createGraphFactory

class AppApplication : Application() {
    val graph by lazy { createGraphFactory<AndroidAppGraph.Factory>().create(this) }
}