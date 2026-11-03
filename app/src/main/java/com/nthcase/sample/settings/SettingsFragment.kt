package com.nthcase.sample.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.nthcase.sample.BuildConfig
import com.nthcase.sample.Notifications
import com.nthcase.sample.R
import com.nthcase.sample.data.AppEvent
import com.nthcase.sample.data.AppState
import com.nthcase.sample.data.ThemeMode
import com.nthcase.sample.databinding.DialogResetBinding
import com.nthcase.sample.databinding.FragmentSettingsBinding

/**
 * Settings, in classic Android Views: an XML layout, a Fragment, Material Components widgets. Every control has an
 * android:id, so Appium sees `com.nthcase.sample:id/settings_slow_loading` and so on. It holds the app's one system
 * permission prompt (notifications, Android 13 and later).
 */
class SettingsFragment : Fragment() {
    private var binding: FragmentSettingsBinding? = null

    private val askNotifications = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        AppState.orderUpdates = granted
        render()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        FragmentSettingsBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val b = binding!!
        b.settingsVersion.text = getString(R.string.settings_version, BuildConfig.VERSION_NAME, SPEC_VERSION)
        b.settingsOrders.setOnClickListener { AppState.events.tryEmit(AppEvent.OpenOrders) }
        b.settingsSignOut.setOnClickListener {
            AppState.signOut()
            AppState.events.tryEmit(AppEvent.SignedOut)
        }
        b.settingsTheme.setOnCheckedChangeListener { _, checkedId ->
            AppState.themeMode = when (checkedId) {
                R.id.settings_theme_light -> ThemeMode.Light
                R.id.settings_theme_dark -> ThemeMode.Dark
                else -> ThemeMode.System
            }
        }
        b.settingsSlowLoading.setOnCheckedChangeListener { _, on -> AppState.slowLoading = on }
        b.settingsFailLoading.setOnCheckedChangeListener { _, on -> AppState.failFirstLoad = on }
        b.settingsNotifications.setOnClickListener {
            val wantsOn = b.settingsNotifications.isChecked
            if (wantsOn && !notificationsAllowed()) {
                b.settingsNotifications.isChecked = false
                askNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                AppState.orderUpdates = wantsOn
                render()
            }
        }
        b.settingsSendNotification.setOnClickListener {
            Notifications.post(requireContext(), getString(R.string.notification_test_title), getString(R.string.notification_test_body))
            Snackbar.make(b.root, R.string.notification_sent, Snackbar.LENGTH_LONG).show()
        }
        b.settingsReset.setOnClickListener { confirmReset() }
        render()
    }

    override fun onResume() {
        super.onResume()
        render()
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    private fun notificationsAllowed(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    /** Puts AppState on the widgets. Listeners only fire on user changes, because the values set here are the current ones. */
    private fun render() {
        val b = binding ?: return
        b.settingsAccountEmail.text = AppState.signedInEmail ?: getString(R.string.settings_signed_out)
        b.settingsTheme.check(
            when (AppState.themeMode) {
                ThemeMode.System -> R.id.settings_theme_system
                ThemeMode.Light -> R.id.settings_theme_light
                ThemeMode.Dark -> R.id.settings_theme_dark
            },
        )
        b.settingsSlowLoading.isChecked = AppState.slowLoading
        b.settingsFailLoading.isChecked = AppState.failFirstLoad
        val allowed = notificationsAllowed()
        if (!allowed) AppState.orderUpdates = false
        b.settingsNotifications.isChecked = AppState.orderUpdates
        b.settingsNotificationsStatus.setText(
            when {
                !allowed -> R.string.settings_notifications_not_allowed
                AppState.orderUpdates -> R.string.settings_notifications_on
                else -> R.string.settings_notifications_off
            },
        )
        b.settingsSendNotification.isEnabled = allowed && AppState.orderUpdates
    }

    /** The XML screen's modal: its own buttons, so they carry the spec's ids instead of android:id/button1. */
    private fun confirmReset() {
        val dialogBinding = DialogResetBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.settings_reset_title)
            .setView(dialogBinding.root)
            .create()
        dialogBinding.settingsResetCancel.setOnClickListener { dialog.dismiss() }
        dialogBinding.settingsResetConfirm.setOnClickListener {
            AppState.resetData()
            dialog.dismiss()
            binding?.let { Snackbar.make(it.root, R.string.settings_reset_done, Snackbar.LENGTH_LONG).show() }
        }
        dialog.show()
    }

    companion object {
        const val SPEC_VERSION = "1.0"
    }
}
