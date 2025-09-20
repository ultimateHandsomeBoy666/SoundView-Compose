package com.soundwave.compose.lib

import android.view.animation.Interpolator
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.pow

/**
 * 音波状态枚举
 */
enum class SoundWaveState {
    INIT, IDLE, DANCE, STOP
}

/**
 * 音波配置数据类
 * @param volumeCount 音柱总数量
 * @param volumeIdleCount 缓动状态下的音柱数量
 * @param maxVolume 最大音量值
 * @param minVolume 最小音量值
 * @param danceDuration 跳跃动画持续时间（毫秒）
 * @param maxDanceDelay 最大跳跃延迟时间（毫秒），代表了每个音柱间跳跃的相位差，如果为0则所有音柱同时整齐跳跃
 * @param idleDuration 一次完整缓动循环持续时间（毫秒）
 * @param maxIdleHeight 最大缓动高度
 * @param minVolumeBarHeight 音柱最小高度
 * @param maxVolumeBarHeight 音柱最大高度
 * @param volumeBarMargin 音柱间距
 * @param volumeBarHalfWidth 音柱半宽
 * @param volumeBarColor 音柱颜色
 * @param enableIdle 是否启用缓动状态
 * @param idleHeightGetter 缓动高度计算函数，输入索引返回对应高度
 * @param interpolator 插值器，用于控制单根音柱跳跃动画
 * @param volumeHeightDistribution 音柱高度分布，在音柱原有跳跃高度上乘以一个系数，达到不同区间音柱高度不同的效果
 */
data class SoundWaveConfig(
    val volumeCount: Int = 37,
    val volumeIdleCount: Int = 16,
    val minVolume: Int = 7,
    val maxVolume: Int = 45,
    val danceDuration: Long = 250L,
    val maxDanceDelay: Long = 80L,
    val idleDuration: Long = 3500L,
    val maxIdleHeight: Dp = 16.dp,
    val minVolumeBarHeight: Dp = 4.dp,
    val maxVolumeBarHeight: Dp = 36.dp,
    val volumeBarMargin: Dp = 3.dp,
    val volumeBarHalfWidth: Dp = 1.5.dp,
    val volumeBarColor: Color = Color(0XFF11192D),
    val enableIdle: Boolean = true,
    val idleHeightGetter: (Int) -> Dp = { x -> (x + 4).dp },
    val interpolator: Interpolator = VolumeDanceInterpolator(),
    val volumeHeightDistribution: FloatArray = floatArrayOf(
        0.6f, 0.6f, 0.6f, 0.8f, 1f, 1.1f, 0.95f, 0.9f, 0.8f,
        0.75f, 0.8f, 0.9f, 0.95f, 1f, 1.1f, 1.2f, 1.5f, 1.4f, 1.3f,
        1.4f, 1.5f, 1.2f, 1.1f, 1f, 0.95f, 0.9f, 0.8f, 0.75f, 0.8f,
        0.9f, 0.95f, 1.1f, 1f, 0.8f, 0.6f, 0.6f, 0.6f
    )
)

@Stable
class VolumeBar {
    var height: Float = 0f
    var danceStartTime: Long = 0L
    var danceStartDelay: Int = 0
    var idleStartTime: Long = 0L
}

class VolumeDanceInterpolator: Interpolator {
    override fun getInterpolation(input: Float): Float {
        val a = -2.8f
        val b = 3.8f
        return a * input.pow(2) + b * input
    }
}