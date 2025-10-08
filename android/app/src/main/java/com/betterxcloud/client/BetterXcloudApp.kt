package com.betterxcloud.client

import android.app.Application
import androidx.preference.PreferenceManager

/**
 * Application entry point used to initialize shared preferences with defaults
 * on the first run of the app.
 */
class BetterXcloudApp : Application() {
    override fun onCreate() {
        super.onCreate()
        PreferenceManager.getDefaultSharedPreferences(this)
            .let { prefs ->
                ControllerPreferences.ensureDefaults(prefs)
            }
    }
}
