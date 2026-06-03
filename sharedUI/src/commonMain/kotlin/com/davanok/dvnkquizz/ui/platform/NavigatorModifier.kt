package com.davanok.dvnkquizz.ui.platform

import androidx.compose.runtime.Composable
import com.davanok.dvnkquizz.ui.navigation.Route

@Composable
expect fun NavigatorModifier(currentRoute: Route)