package com.soundwave.compose.lib

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Density
import kotlin.random.Random

private val random = Random(1)

fun DrawScope.drawVolumeBar(
    density: Density,
    volumeBar: VolumeBar,
    x: Float,
    index: Int,
    state: SoundWaveState,
    currentTime: Long,
    currentVolume: Int,
    config: SoundWaveConfig
) {
    // 非 idle 状态下需要重置 idleStartTime
    if (state != SoundWaveState.IDLE) {
        volumeBar.idleStartTime = 0
    }

    when (state) {
        SoundWaveState.IDLE -> {
            drawIdleBar(density, volumeBar, x, index, currentTime, config)
        }
        SoundWaveState.DANCE -> {
            drawDanceBar(density, volumeBar, x, index, currentTime, currentVolume, config)
        }
        SoundWaveState.INIT, SoundWaveState.STOP -> {
            drawInitBar(density, config, x)
        }
    }
}

fun DrawScope.drawInitBar(
    density: Density,
    config: SoundWaveConfig,
    x: Float
) {
    with(density) {
        val drawHeight = config.minVolumeBarHeight.toPx()
        val volumeBarHalfWidthPx = config.volumeBarHalfWidth.toPx()
        val color = config.volumeBarColor

        drawRoundRect(
            color = color,
            topLeft = Offset(x - volumeBarHalfWidthPx, 0f - drawHeight / 2),
            size = Size(volumeBarHalfWidthPx * 2, drawHeight),
            cornerRadius = CornerRadius(volumeBarHalfWidthPx, volumeBarHalfWidthPx)
        )
    }
}

fun DrawScope.drawIdleBar(
    density: Density,
    volumeBar: VolumeBar,
    x: Float,
    index: Int,
    currentTime: Long,
    config: SoundWaveConfig,
) {
    with(density) {
        val volumeBarHalfWidthPx = config.volumeBarHalfWidth.toPx()
        val minVolumeBarHeightPx = config.minVolumeBarHeight.toPx()
        val volumeCount = config.volumeCount
        val volumeIdleCount = config.volumeIdleCount
        val idleDuration = config.idleDuration
        val idleHeightGetter = config.idleHeightGetter
        val color = config.volumeBarColor

        if (currentTime - volumeBar.idleStartTime > idleDuration) {
            volumeBar.idleStartTime = currentTime
        }

        val idleStart = (1f * (currentTime - volumeBar.idleStartTime) / idleDuration *
                (volumeCount / 2 + volumeIdleCount + 4)).toInt()
        val mid = volumeCount / 2
        val getterX = if (index >= mid) {
            mid + idleStart - index
        } else {
            index - (mid - idleStart)
        }
        val drawHeight = when (getterX) {
            in 0..volumeIdleCount / 2 -> {
                idleHeightGetter(getterX).toPx()
            }
            in volumeIdleCount / 2 + 1..volumeIdleCount -> {
                idleHeightGetter(volumeIdleCount - getterX).toPx()
            }
            else -> {
                minVolumeBarHeightPx
            }
        }

        drawRoundRect(
            color = color,
            topLeft = Offset(x - volumeBarHalfWidthPx, 0f - drawHeight / 2),
            size = Size(volumeBarHalfWidthPx * 2, drawHeight),
            cornerRadius = CornerRadius(volumeBarHalfWidthPx, volumeBarHalfWidthPx)
        )
    }
}

fun DrawScope.drawDanceBar(
    density: Density,
    volumeBar: VolumeBar,
    x: Float,
    index: Int,
    currentTime: Long,
    currentVolume: Int,
    config: SoundWaveConfig,
) {
    with(density) {
        val volumeBarHalfWidthPx = config.volumeBarHalfWidth.toPx()
        val maxVolumeBarHeightPx = config.maxVolumeBarHeight.toPx()
        val minVolumeBarHeightPx = config.minVolumeBarHeight.toPx()
        val volumeHeightDistribution = config.volumeHeightDistribution
        val minVolume = config.minVolume
        val maxVolume = config.maxVolume
        val danceDuration = config.danceDuration
        val maxDanceDelay = config.maxDanceDelay
        val color = config.volumeBarColor
        val interpolator = config.interpolator

        if (currentTime - volumeBar.danceStartTime > danceDuration) {
            volumeBar.danceStartDelay = random.nextInt(maxDanceDelay.toInt())
            volumeBar.danceStartTime = currentTime + volumeBar.danceStartDelay
            val volume = currentVolume.coerceIn(minVolume, maxVolume)
            val rawHeight = 1f * (volume - minVolume) / (maxVolume - minVolume) *
                    (maxVolumeBarHeightPx - minVolumeBarHeightPx) + minVolumeBarHeightPx
            val heightPortion = if (index < volumeHeightDistribution.size) {
                volumeHeightDistribution[index]
            } else {
                1f
            }
            volumeBar.height = (rawHeight * heightPortion)
        }

        val fraction = 1f * (currentTime - volumeBar.danceStartTime) / danceDuration
        val drawHeight = if (fraction in 0f..1f) {
            (interpolator.getInterpolation(fraction) * volumeBar.height)
                .coerceIn(minVolumeBarHeightPx, maxVolumeBarHeightPx)
        } else {
            minVolumeBarHeightPx
        }

        // 绘制圆角矩形
        drawRoundRect(
            color = color,
            topLeft = Offset(x - volumeBarHalfWidthPx, 0f - drawHeight / 2),
            size = Size(volumeBarHalfWidthPx * 2, drawHeight),
            cornerRadius = CornerRadius(volumeBarHalfWidthPx, volumeBarHalfWidthPx)
        )
    }
}