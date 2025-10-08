package com.betterxcloud.client.ui

import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Lazily reads JavaScript files stored in the app assets. The scripts are
 * cached in memory because the same assets are injected on every navigation.
 */
class ScriptRepository(private val context: Context) {
    private val cache = mutableMapOf<String, String>()

    fun getAsset(name: String): String = cache.getOrPut(name) {
        context.assets.open(name).use { input ->
            BufferedReader(InputStreamReader(input)).readText()
        }
    }
}
