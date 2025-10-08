package com.betterxcloud.client

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.betterxcloud.client.databinding.ActivityLauncherBinding

/**
 * First screen shown when the application launches. Presents a simplified
 * launcher with "Start XCloud" and "Options" actions.
 */
class LauncherActivity : ComponentActivity() {
    private lateinit var binding: ActivityLauncherBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLauncherBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonStart.setOnClickListener {
            startActivity(Intent(this, XCloudActivity::class.java))
        }

        binding.buttonOptions.setOnClickListener {
            startActivity(Intent(this, OptionsActivity::class.java))
        }
    }
}
