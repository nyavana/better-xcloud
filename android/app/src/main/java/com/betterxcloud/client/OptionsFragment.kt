package com.betterxcloud.client

import android.content.SharedPreferences
import android.os.Bundle
import android.view.KeyEvent
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager

/**
 * Displays controller and browser preferences. Each preference is bound to a
 * value stored in [SharedPreferences], allowing the controller kernel and button
 * mapping to be customized without leaving the app.
 */
class OptionsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var prefs: SharedPreferences

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
        configureListeners()
        updateSummaries()
    }

    override fun onResume() {
        super.onResume()
        prefs.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        prefs.unregisterOnSharedPreferenceChangeListener(this)
        super.onPause()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            ControllerPreferences.Button.A.prefKey,
            ControllerPreferences.Button.B.prefKey,
            ControllerPreferences.Button.X.prefKey,
            ControllerPreferences.Button.Y.prefKey,
            ControllerPreferences.Button.LB.prefKey,
            ControllerPreferences.Button.RB.prefKey,
            ControllerPreferences.Button.LT.prefKey,
            ControllerPreferences.Button.RT.prefKey,
            ControllerPreferences.Button.VIEW.prefKey,
            ControllerPreferences.Button.MENU.prefKey,
            ControllerPreferences.Button.LS.prefKey,
            ControllerPreferences.Button.RS.prefKey,
            ControllerPreferences.Button.DPAD_UP.prefKey,
            ControllerPreferences.Button.DPAD_DOWN.prefKey,
            ControllerPreferences.Button.DPAD_LEFT.prefKey,
            ControllerPreferences.Button.DPAD_RIGHT.prefKey -> updateButtonSummary(key)
            ControllerPreferences.Kernel.NATIVE.prefValue,
            ControllerPreferences.Kernel.WEB.prefValue,
            "controller.kernel" -> updateKernelSummary()
        }
    }

    private fun updateSummaries() {
        updateKernelSummary()
        ControllerPreferences.Button.STANDARD.forEach { updateButtonSummary(it.prefKey) }
    }

    private fun configureListeners() {
        findPreference<ListPreference>("controller.kernel")?.setOnPreferenceChangeListener { _, newValue ->
            val value = newValue as? String ?: return@setOnPreferenceChangeListener false
            ControllerPreferences.setKernel(prefs, ControllerPreferences.Kernel.fromValue(value))
            updateKernelSummary()
            true
        }

        ControllerPreferences.Button.STANDARD.forEach { button ->
            findPreference<ListPreference>(button.prefKey)?.setOnPreferenceChangeListener { pref, newValue ->
                val value = (newValue as? String)?.toIntOrNull() ?: return@setOnPreferenceChangeListener false
                ControllerPreferences.setKeyCodeFor(button, value, prefs)
                (pref as? ListPreference)?.value = value.toString()
                updateButtonSummary(button.prefKey)
                false
            }
        }
    }

    private fun updateKernelSummary() {
        val kernelPref = findPreference<ListPreference>("controller.kernel") ?: return
        val kernel = ControllerPreferences.getKernel(prefs)
        val summaryRes = when (kernel) {
            ControllerPreferences.Kernel.NATIVE -> R.string.kernel_native_summary
            ControllerPreferences.Kernel.WEB -> R.string.kernel_web_summary
        }
        kernelPref.summary = getString(summaryRes)
    }

    private fun updateButtonSummary(prefKey: String) {
        val pref = findPreference<ListPreference>(prefKey) ?: return
        val keyCode = prefs.getInt(prefKey, KeyEvent.KEYCODE_UNKNOWN)
        pref.value = keyCode.toString()
        pref.summary = KeyCodeFormatter.describeKeyCode(keyCode, requireContext())
    }
}
