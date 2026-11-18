package com.nthcase.sample.ui.screens

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.fragment.compose.AndroidFragment
import com.nthcase.sample.lobby.LobbyViewsFragment
import com.nthcase.sample.ui.MossgroveTopBar

/**
 * Lobby (Views): Compose draws the top bar, as on Settings; everything under it is the Android Views Lobby. The top
 * bar's Back goes through the system Back, so a detail or promo open in the fragment closes first.
 */
@Composable
fun LobbyViewsScreen() {
    val dispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    Scaffold(topBar = { MossgroveTopBar("Lobby (Views)", onBack = { dispatcher?.onBackPressed() }) }) { padding ->
        AndroidFragment<LobbyViewsFragment>(modifier = Modifier.padding(padding).fillMaxSize())
    }
}
