package com.betterxcloud.client

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Hosts the preference fragment used to configure the controller and browser
 * options.
 */
class OptionsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_options)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.options_container, OptionsFragment())
                .commit()
        }
    }
}
