package com.pindou.app.state

import android.content.Context
import com.pindou.core.generate.DenoiseStrength

/**
 * Persists the denoise switch and strength across processes, mirroring
 * [com.pindou.app.ml.CutoutPrefs]. The setting is generation-time only and is
 * never written into a `.pindou` document, so nothing here affects the file
 * format.
 */
object DenoisePrefs {

    private const val FILE = "denoise"
    private const val KEY_ENABLED = "enabled"
    private const val KEY_STRENGTH = "strength"

    /** Default when unset: enabled at the medium strength. */
    fun enabled(context: Context): Boolean = prefs(context).getBoolean(KEY_ENABLED, true)

    fun setEnabled(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_ENABLED, value).apply()
    }

    fun strength(context: Context): DenoiseStrength =
        DenoiseStrength.fromId(prefs(context).getString(KEY_STRENGTH, null) ?: DenoiseStrength.DEFAULT.id)

    fun setStrength(context: Context, value: DenoiseStrength) {
        prefs(context).edit().putString(KEY_STRENGTH, value.id).apply()
    }

    private fun prefs(context: Context) = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
}
