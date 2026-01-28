package com.wtscards.ui.components

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Adds click-and-drag scrolling with inertia to any ScrollableState (ScrollState, LazyListState, etc.).
 */
fun Modifier.dragScrollable(scrollableState: ScrollableState): Modifier = this.pointerInput(Unit) {
    coroutineScope {
        awaitPointerEventScope {
            var lastY = 0f
            var velocity = 0f
            var lastTime = 0L

            while (true) {
                val event = awaitPointerEvent()
                when (event.type) {
                    PointerEventType.Press -> {
                        lastY = event.changes.first().position.y
                        velocity = 0f
                        lastTime = System.currentTimeMillis()
                    }
                    PointerEventType.Move -> {
                        if (event.changes.first().pressed) {
                            val currentY = event.changes.first().position.y
                            val currentTime = System.currentTimeMillis()
                            val delta = lastY - currentY
                            val timeDelta = (currentTime - lastTime).coerceAtLeast(1)

                            velocity = delta / timeDelta * 2000
                            lastY = currentY
                            lastTime = currentTime

                            launch {
                                scrollableState.scrollBy(delta)
                            }
                        }
                    }
                    PointerEventType.Release -> {
                        if (kotlin.math.abs(velocity) > 100) {
                            launch {
                                scrollableState.scroll {
                                    var remainingVelocity = velocity * 0.5f
                                    val decay = 0.95f
                                    while (kotlin.math.abs(remainingVelocity) > 1f) {
                                        scrollBy(remainingVelocity / 60f)
                                        remainingVelocity *= decay
                                        delay(16)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
