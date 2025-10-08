package com.betterxcloud.client

import android.content.SharedPreferences
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import com.betterxcloud.client.ui.BetterXcloudWebView
import org.json.JSONObject

/**
 * Handles controller input and forwards the events to the injected JavaScript
 * bridge. The dispatcher only processes events when a physical gamepad is the
 * source and ignores keyboards or touch input.
 */
class ControllerEventDispatcher(
    private val webView: BetterXcloudWebView,
    private val prefs: SharedPreferences,
) {
    private val logTag = "BXController"
    private var axisState = AxisState()

    fun handleKeyEvent(event: KeyEvent): Boolean {
        if (!event.isGamepadButton) return false
        val button = ControllerPreferences.findButtonForKeyCode(event.keyCode, prefs)
        if (button == null) {
            android.util.Log.d(logTag, "Unmapped keyCode=${event.keyCode}")
            return false
        }
        if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount > 0) {
            return true
        }
        val pressed = event.action == KeyEvent.ACTION_DOWN
        dispatchButton(button, pressed)
        return true
    }

    fun handleMotionEvent(event: MotionEvent): Boolean {
        if (!event.isFromSource(InputDevice.SOURCE_JOYSTICK) && !event.isFromSource(InputDevice.SOURCE_GAMEPAD)) {
            return false
        }
        if (event.actionMasked != MotionEvent.ACTION_MOVE) {
            return false
        }
        axisState = axisState.update(event)
        dispatchAxes(axisState)
        return true
    }

    fun syncMappings() {
        val mappingJson = JSONObject()
        ControllerPreferences.Button.STANDARD.forEach { button ->
            mappingJson.put(button.jsName, ControllerPreferences.getKeyCodeFor(button, prefs))
        }
        val js = "window.BetterXcloudAndroidBridge?.updateMapping(${mappingJson});"
        webView.evaluateJavascript(js, null)
    }

    private val KeyEvent.isGamepadButton: Boolean
        get() {
            val source = source
            if (!isFromSource(InputDevice.SOURCE_GAMEPAD) && !isFromSource(InputDevice.SOURCE_JOYSTICK)) {
                return false
            }
            return keyCode != KeyEvent.KEYCODE_UNKNOWN
        }

    private fun dispatchButton(button: ControllerPreferences.Button, pressed: Boolean) {
        val payload = JSONObject().apply {
            put("type", "button")
            put("button", button.jsName)
            put("pressed", pressed)
            put("timestamp", System.currentTimeMillis())
        }
        val js = "window.BetterXcloudAndroidBridge?.onNativeEvent(${payload});"
        webView.evaluateJavascript(js, null)
    }

    private fun dispatchAxes(state: AxisState) {
        val payload = JSONObject().apply {
            put("type", "axes")
            put("timestamp", System.currentTimeMillis())
            put("lx", state.leftX)
            put("ly", state.leftY)
            put("rx", state.rightX)
            put("ry", state.rightY)
            put("lt", state.leftTrigger)
            put("rt", state.rightTrigger)
        }
        val js = "window.BetterXcloudAndroidBridge?.onNativeEvent(${payload});"
        webView.evaluateJavascript(js, null)
    }

    private data class AxisState(
        val leftX: Float = 0f,
        val leftY: Float = 0f,
        val rightX: Float = 0f,
        val rightY: Float = 0f,
        val leftTrigger: Float = 0f,
        val rightTrigger: Float = 0f,
    ) {
        fun update(event: MotionEvent): AxisState {
            val device = event.device ?: return this
            val range = { axis: Int -> device.getMotionRange(axis, event.source) }
            fun MotionEvent.normalize(axis: Int, center: Float = 0f): Float {
                val motionRange = range(axis) ?: return 0f
                val value = getAxisValue(axis)
                val normalized = if (motionRange.max > 1f) value / motionRange.max else value
                return normalized - center
            }
            fun MotionEvent.normalizeWithFallback(primary: Int, fallback: Int? = null): Float {
                val primaryValue = normalize(primary)
                if (primaryValue != 0f || fallback == null) return primaryValue
                return normalize(fallback)
            }
            return copy(
                leftX = event.normalizeWithFallback(MotionEvent.AXIS_X),
                leftY = event.normalizeWithFallback(MotionEvent.AXIS_Y),
                rightX = event.normalizeWithFallback(MotionEvent.AXIS_Z, MotionEvent.AXIS_RX),
                rightY = event.normalizeWithFallback(MotionEvent.AXIS_RZ, MotionEvent.AXIS_RY),
                leftTrigger = event.normalizeWithFallback(MotionEvent.AXIS_LTRIGGER, MotionEvent.AXIS_BRAKE),
                rightTrigger = event.normalizeWithFallback(MotionEvent.AXIS_RTRIGGER, MotionEvent.AXIS_GAS),
            )
        }
    }
}
