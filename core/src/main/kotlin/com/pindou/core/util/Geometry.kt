package com.pindou.core.util

/** Simple integer rectangle independent of Android geometry classes. */
data class RectI(val left: Int, val top: Int, val right: Int, val bottom: Int) {
    val width: Int get() = right - left
    val height: Int get() = bottom - top
    val area: Long get() = width.toLong() * height.toLong()
}

/** Integer size. */
data class SizeI(val width: Int, val height: Int)
