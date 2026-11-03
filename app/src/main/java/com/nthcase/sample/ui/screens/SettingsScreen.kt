package com.nthcase.sample.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.fragment.compose.AndroidFragment
import com.nthcase.sample.settings.SettingsFragment
import com.nthcase.sample.ui.MossgroveTopBar

/** Compose draws the top bar; the body is the classic XML Fragment, so this one screen exercises Android Views. */
@Composable
fun SettingsScreen() {
    Scaffold(topBar = { MossgroveTopBar("Settings") }) { padding ->
        AndroidFragment<SettingsFragment>(modifier = Modifier.padding(padding).fillMaxSize())
    }
}
