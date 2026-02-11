package io.celox.xclip.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.tween

object XClipAnimation {
    const val DURATION_SHORT = 150
    const val DURATION_MEDIUM = 300
    const val DURATION_LONG = 500
    const val STAGGER_DELAY = 50

    val springDefault = SpringSpec<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    fun <T> tweenMedium() = tween<T>(durationMillis = DURATION_MEDIUM)
    fun <T> tweenShort() = tween<T>(durationMillis = DURATION_SHORT)
    fun <T> tweenLong() = tween<T>(durationMillis = DURATION_LONG)
}
