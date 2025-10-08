package com.betterxcloud.client

import android.content.SharedPreferences
import android.view.KeyEvent

/**
 * Centralizes preference keys and the default mapping for controller related
 * features. The mapping targets the standard Xbox layout and stores the
 * physical key code used for each logical button.
 */
object ControllerPreferences {
    private const val KEY_KERNEL = "controller.kernel"

    enum class Kernel(val prefValue: String) {
        NATIVE("native"),
        WEB("web");

        companion object {
            fun fromValue(value: String?): Kernel =
                entries.firstOrNull { it.prefValue == value } ?: NATIVE
        }
    }

    enum class Button(
        val prefKey: String,
        val label: String,
        val defaultKeyCode: Int,
        val jsName: String
    ) {
        A("controller.button.a", "A", KeyEvent.KEYCODE_BUTTON_A, "a"),
        B("controller.button.b", "B", KeyEvent.KEYCODE_BUTTON_B, "b"),
        X("controller.button.x", "X", KeyEvent.KEYCODE_BUTTON_X, "x"),
        Y("controller.button.y", "Y", KeyEvent.KEYCODE_BUTTON_Y, "y"),
        LB("controller.button.lb", "Left bumper", KeyEvent.KEYCODE_BUTTON_L1, "lb"),
        RB("controller.button.rb", "Right bumper", KeyEvent.KEYCODE_BUTTON_R1, "rb"),
        LT("controller.button.lt", "Left trigger", KeyEvent.KEYCODE_BUTTON_L2, "lt"),
        RT("controller.button.rt", "Right trigger", KeyEvent.KEYCODE_BUTTON_R2, "rt"),
        VIEW("controller.button.view", "View", KeyEvent.KEYCODE_BUTTON_SELECT, "view"),
        MENU("controller.button.menu", "Menu", KeyEvent.KEYCODE_BUTTON_START, "menu"),
        LS("controller.button.ls", "Left stick press", KeyEvent.KEYCODE_BUTTON_THUMBL, "ls"),
        RS("controller.button.rs", "Right stick press", KeyEvent.KEYCODE_BUTTON_THUMBR, "rs"),
        DPAD_UP("controller.button.dpad_up", "D-Pad up", KeyEvent.KEYCODE_DPAD_UP, "dpad_up"),
        DPAD_DOWN("controller.button.dpad_down", "D-Pad down", KeyEvent.KEYCODE_DPAD_DOWN, "dpad_down"),
        DPAD_LEFT("controller.button.dpad_left", "D-Pad left", KeyEvent.KEYCODE_DPAD_LEFT, "dpad_left"),
        DPAD_RIGHT("controller.button.dpad_right", "D-Pad right", KeyEvent.KEYCODE_DPAD_RIGHT, "dpad_right");

        companion object {
            val STANDARD = entries.toList()
        }
    }

    fun ensureDefaults(prefs: SharedPreferences) {
        if (!prefs.contains(KEY_KERNEL)) {
            prefs.edit().putString(KEY_KERNEL, Kernel.NATIVE.prefValue).apply()
        }
        val editor = prefs.edit()
        var commitNeeded = false
        for (button in Button.STANDARD) {
            if (!prefs.contains(button.prefKey)) {
                editor.putInt(button.prefKey, button.defaultKeyCode)
                commitNeeded = true
            }
        }
        if (commitNeeded) {
            editor.apply()
        }
    }

    fun getKernel(prefs: SharedPreferences): Kernel =
        Kernel.fromValue(prefs.getString(KEY_KERNEL, null))

    fun setKernel(prefs: SharedPreferences, kernel: Kernel) {
        prefs.edit().putString(KEY_KERNEL, kernel.prefValue).apply()
    }

    fun getKeyCodeFor(button: Button, prefs: SharedPreferences): Int =
        prefs.getInt(button.prefKey, button.defaultKeyCode)

    fun setKeyCodeFor(button: Button, keyCode: Int, prefs: SharedPreferences) {
        prefs.edit().putInt(button.prefKey, keyCode).apply()
    }

    fun findButtonForKeyCode(keyCode: Int, prefs: SharedPreferences): Button? =
        Button.STANDARD.firstOrNull { getKeyCodeFor(it, prefs) == keyCode }
}
