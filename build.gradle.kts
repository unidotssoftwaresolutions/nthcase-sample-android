// Root build: pins the plugins. AGP 9 compiles Kotlin itself, so the Kotlin plugin is only pinned here, never applied.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
