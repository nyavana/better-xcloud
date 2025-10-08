package com.betterxcloud.client

import android.content.SharedPreferences
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.preference.PreferenceManager
import com.betterxcloud.client.databinding.ActivityXcloudBinding
import com.betterxcloud.client.ui.BetterXcloudWebView

/**
 * Hosts the immersive WebView that loads xCloud with the Better xCloud user
 * script applied. The activity also brokers controller events between Android
 * and the injected web layer when the native kernel is enabled.
 */
class XCloudActivity : AppCompatActivity(), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var binding: ActivityXcloudBinding
    private lateinit var prefs: SharedPreferences
    private lateinit var controllerDispatcher: ControllerEventDispatcher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        binding = ActivityXcloudBinding.inflate(layoutInflater)
        setContentView(binding.root)

        controllerDispatcher = ControllerEventDispatcher(binding.webview, prefs)
        configureWebView(binding.webview)
        applyImmersiveMode()
    }

    override fun onResume() {
        super.onResume()
        prefs.registerOnSharedPreferenceChangeListener(this)
        applyImmersiveMode()
        binding.webview.onResume()
    }

    override fun onPause() {
        prefs.unregisterOnSharedPreferenceChangeListener(this)
        binding.webview.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        binding.webview.destroy()
        super.onDestroy()
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val kernel = ControllerPreferences.getKernel(prefs)
        if (kernel == ControllerPreferences.Kernel.NATIVE && controllerDispatcher.handleKeyEvent(event)) {
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    override fun dispatchGenericMotionEvent(ev: MotionEvent): Boolean {
        val kernel = ControllerPreferences.getKernel(prefs)
        if (kernel == ControllerPreferences.Kernel.NATIVE && controllerDispatcher.handleMotionEvent(ev)) {
            return true
        }
        return super.dispatchGenericMotionEvent(ev)
    }

    private fun configureWebView(webView: BetterXcloudWebView) {
        webView.setKernel(ControllerPreferences.getKernel(prefs))
        webView.ensureBridgeReady {
            controllerDispatcher.syncMappings()
        }
        webView.loadUrl(getString(R.string.xcloud_url))
    }

    private fun applyImmersiveMode() {
        val controller = ViewCompat.getWindowInsetsController(window.decorView) ?: return
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        window.decorView.keepScreenOn = true
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        if (key == null) return
        when (key) {
            "controller.kernel" -> binding.webview.setKernel(ControllerPreferences.getKernel(prefs))
            in ControllerPreferences.Button.STANDARD.map { it.prefKey } -> controllerDispatcher.syncMappings()
        }
    }
}
