package com.pindou.app.ml

import android.content.Context

/**
 * Persists the two bits of subject-cutout state that outlive a process: the
 * user's last enable choice, and whether the model has been found too slow on
 * this device (the capability gate is keyed by the model asset, so a future
 * lighter model starts ungated).
 */
object CutoutPrefs {

    private const val FILE = "cutout"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_UNAVAILABLE = "unavailable_" + SaliencyMaskSource.MODEL_ASSET
    private const val KEY_PRESET = "preset"

    fun enabled(context: Context): Boolean = prefs(context).getBoolean(KEY_ENABLED, false)

    fun setEnabled(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_ENABLED, value).apply()
    }

    fun unavailable(context: Context): Boolean = prefs(context).getBoolean(KEY_UNAVAILABLE, false)

    fun setUnavailable(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_UNAVAILABLE, value).apply()
    }

    fun preset(context: Context): String? = prefs(context).getString(KEY_PRESET, null)

    fun setPreset(context: Context, id: String) {
        prefs(context).edit().putString(KEY_PRESET, id).apply()
    }

    private fun prefs(context: Context) = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
}
