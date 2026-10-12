package com.pindou.app.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pindou.app.Screen

/** What the crop screen produces: the pattern-generation source, or a reference image. */
enum class CropPurpose { SOURCE, REFERENCE }

/**
 * Navigation and transient UI state: which screen is showing, the one-shot
 * error/status dialogs, and the global busy flag. Owned by [com.pindou.app.PindouController]
 * and handed to the services that need to announce failures or busy periods.
 */
class NavigationState {

    var screen by mutableStateOf<Screen>(Screen.Home)
        private set

    /** Why the crop screen was opened; decides what a confirmed crop is used for. */
    var cropPurpose by mutableStateOf(CropPurpose.SOURCE)
        private set

    /**
     * Whether the grid reference image is shown over the pattern. Lives here (not
     * in the editor composable) so it survives the editor → crop → editor detour
     * when a reference is imported.
     */
    var showReference by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var statusMessage by mutableStateOf<String?>(null)
        private set

    var busy by mutableStateOf(false)
        private set

    fun go(to: Screen) {
        screen = to
    }

    fun beginCrop(purpose: CropPurpose) {
        cropPurpose = purpose
    }

    fun updateShowReference(value: Boolean) {
        showReference = value
    }

    fun showError(message: String) {
        errorMessage = message
    }

    fun clearError() {
        errorMessage = null
    }

    fun setStatus(message: String) {
        statusMessage = message
    }

    fun clearStatus() {
        statusMessage = null
    }

    fun markBusy(value: Boolean) {
        busy = value
    }
}
