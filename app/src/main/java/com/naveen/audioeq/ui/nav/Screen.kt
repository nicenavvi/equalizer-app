package com.naveen.audioeq.ui.nav

sealed class Screen(val route: String, val label: String) {
    object Home : Screen("home", "Home")
    object Equalizer : Screen("equalizer", "Equalizer")
}
