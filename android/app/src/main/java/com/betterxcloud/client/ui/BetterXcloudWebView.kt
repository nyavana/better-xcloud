package com.betterxcloud.client.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.betterxcloud.client.ControllerPreferences
import com.betterxcloud.client.R

@SuppressLint("SetJavaScriptEnabled")
class BetterXcloudWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : WebView(context, attrs, defStyleAttr) {

    private val scriptRepository = ScriptRepository(context)
    private var currentKernel = ControllerPreferences.Kernel.NATIVE
    private var onBridgeReady: (() -> Unit)? = null

    init {
        setBackgroundColor(Color.BLACK)
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = false
            displayZoomControls = false
            mediaPlaybackRequiresUserGesture = false
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            userAgentString = buildUserAgent(userAgentString)
        }

        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                injectBridgeScripts()
            }
        }

        webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                callback?.onCustomViewHidden() // We enforce immersive fullscreen ourselves.
            }
        }

        addJavascriptInterface(BridgeCallbacks(), "BetterXcloudNative")
    }

    fun setKernel(kernel: ControllerPreferences.Kernel) {
        currentKernel = kernel
        evaluateJavascript(
            "window.BetterXcloudAndroidBridge?.setKernel('${kernel.prefValue}')",
            null
        )
    }

    fun ensureBridgeReady(onReady: () -> Unit) {
        onBridgeReady = onReady
        evaluateJavascript("window.BetterXcloudAndroidBridge?.readyState", null)
    }

    private fun injectBridgeScripts() {
        val bridgeScript = scriptRepository.getAsset("bx-android-bridge.js")
        val betterXcloudScript = scriptRepository.getAsset("better-xcloud.user.js")

        evaluateJavascript(bridgeScript.toInjectionSnippet(), null)
        evaluateJavascript(betterXcloudScript.toInjectionSnippet(), null)
        setKernel(currentKernel)
    }

    private fun String.toInjectionSnippet(): String {
        val encoded = android.util.Base64.encodeToString(toByteArray(), android.util.Base64.NO_WRAP)
        return "(function(){const parent=document.documentElement;const script=document.createElement('script');script.type='text/javascript';script.text=atob('$encoded');parent.appendChild(script);})();"
    }

    private fun buildUserAgent(original: String): String {
        val suffix = context.getString(R.string.user_agent_suffix)
        return if (original.contains(suffix)) original else "$original $suffix"
    }

    private inner class BridgeCallbacks {
        @JavascriptInterface
        fun onBridgeReady() {
            post { onBridgeReady?.invoke() }
        }
    }
}
