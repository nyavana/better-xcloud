package com.betterxcloud.client

import android.content.Context
import android.view.KeyEvent

/**
 * Utility used to present a friendly representation of [KeyEvent] codes in the
 * preferences screen.
 */
object KeyCodeFormatter {
    private val namedKeys = mapOf(
        KeyEvent.KEYCODE_BUTTON_A to R.string.keycode_button_a,
        KeyEvent.KEYCODE_BUTTON_B to R.string.keycode_button_b,
        KeyEvent.KEYCODE_BUTTON_X to R.string.keycode_button_x,
        KeyEvent.KEYCODE_BUTTON_Y to R.string.keycode_button_y,
        KeyEvent.KEYCODE_BUTTON_L1 to R.string.keycode_button_lb,
        KeyEvent.KEYCODE_BUTTON_R1 to R.string.keycode_button_rb,
        KeyEvent.KEYCODE_BUTTON_L2 to R.string.keycode_button_lt,
        KeyEvent.KEYCODE_BUTTON_R2 to R.string.keycode_button_rt,
        KeyEvent.KEYCODE_BUTTON_THUMBL to R.string.keycode_button_ls,
        KeyEvent.KEYCODE_BUTTON_THUMBR to R.string.keycode_button_rs,
        KeyEvent.KEYCODE_BUTTON_START to R.string.keycode_button_menu,
        KeyEvent.KEYCODE_BUTTON_SELECT to R.string.keycode_button_view,
        KeyEvent.KEYCODE_DPAD_UP to R.string.keycode_dpad_up,
        KeyEvent.KEYCODE_DPAD_DOWN to R.string.keycode_dpad_down,
        KeyEvent.KEYCODE_DPAD_LEFT to R.string.keycode_dpad_left,
        KeyEvent.KEYCODE_DPAD_RIGHT to R.string.keycode_dpad_right,
    )

    fun describeKeyCode(keyCode: Int, context: Context): String =
        namedKeys[keyCode]?.let(context::getString)
            ?: context.getString(R.string.keycode_generic, keyCode)
}
