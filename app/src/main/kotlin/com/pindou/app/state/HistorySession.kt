package com.pindou.app.state

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.pindou.app.data.HistoryStore
import com.pindou.app.data.PatternEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Loads and refreshes the saved-pattern history (the `.pindou` files under the
 * public Documents/PindouAssistant folder). Decoding runs on IO; the list is
 * exposed as Compose state for the home "recent" row and the history screen.
 */
class HistorySession(
    context: Context,
    private val scope: CoroutineScope,
) {
    private val store = HistoryStore(context)

    var items by mutableStateOf<List<PatternEntry>>(emptyList())
        private set

    var loading by mutableStateOf(false)
        private set

    fun refresh() {
        loading = true
        scope.launch {
            val list = withContext(Dispatchers.IO) { store.list() }
            items = list
            loading = false
        }
    }

    fun delete(entry: PatternEntry) {
        scope.launch {
            withContext(Dispatchers.IO) { store.delete(entry.uri) }
            refresh()
        }
    }
}
