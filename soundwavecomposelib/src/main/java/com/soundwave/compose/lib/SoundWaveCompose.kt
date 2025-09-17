package com.soundwave.compose.lib

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.android.awaitFrame
import kotlin.math.pow
import kotlin.random.Random

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
 * @param maxDanceDelay 最大跳跃延迟时间（毫秒）
 * @param idleDuration 缓动循环持续时间（毫秒）
 * @param maxIdleHeight 最大缓动高度
 * @param minVolumeBarHeight 音柱最小高度
 * @param maxVolumeBarHeight 音柱最大高度
 * @param volumeBarMargin 音柱间距
 * @param volumeBarHalfWidth 音柱半宽
 * @param volumeBarColor 音柱颜色
 * @param enableIdle 是否启用缓动状态
 */
data class SoundWaveConfig(
    val volumeCount: Int = 37,
    val volumeIdleCount: Int = 16,
    val maxVolume: Int = 35,
    val minVolume: Int = 4,
    val danceDuration: Long = 250L,
    val maxDanceDelay: Int = 80,
    val idleDuration: Long = 3500L,
    val maxIdleHeight: Dp = 16.dp,
    val minVolumeBarHeight: Dp = 4.dp,
    val maxVolumeBarHeight: Dp = 36.dp,
    val volumeBarMargin: Dp = 3.dp,
    val volumeBarHalfWidth: Dp = 1.5.dp,
    val volumeBarColor: Color = Color.Black.copy(alpha = 0.6f),
    val enableIdle: Boolean = true
)

@Stable
private class VolumeBar {
    var height: Float = 0f
    var danceStartTime: Long = 0L
    var danceStartDelay: Int = 0
    var idleStartTime: Long = 0L
}

private class VolumeDanceInterpolator {
    fun getInterpolation(input: Float): Float {
        val a = -2.8f
        val b = 3.8f
        return a * input.pow(2) + b * input
    }
}

/**
 * 音波可视化组件
 * 
 * @param volume 当前音量值，用于驱动音波动画
 * @param modifier 修饰符
 * @param config 音波配置，包含各种参数设置
 * @param idleHeightGetter 缓动高度计算函数，输入索引返回对应高度
 * 
 * 使用示例：
 * ```
 * SoundWaveView(
 *     volume = currentVolume,
 *     config = SoundWaveConfig(
 *         volumeCount = 37,
 *         volumeBarColor = Color.White
 *     )
 * )
 * ```
 */
@Composable
fun SoundWaveView(
    volume: Int,
    modifier: Modifier = Modifier,
    config: SoundWaveConfig = SoundWaveConfig(),
    idleHeightGetter: (Int) -> Float = { x -> (x + 4).toFloat() }
) {
    val density = LocalDensity.current
    
    // 将dp转换为px
    val minVolumeBarHeightPx = with(density) { config.minVolumeBarHeight.toPx() }
    val maxVolumeBarHeightPx = with(density) { config.maxVolumeBarHeight.toPx() }
    val volumeBarMarginPx = with(density) { config.volumeBarMargin.toPx() }
    val volumeBarHalfWidthPx = with(density) { config.volumeBarHalfWidth.toPx() }
    val maxIdleHeightPx = with(density) { config.maxIdleHeight.toPx() }
    
    // 音柱高度分布 - 完全按照原版
    val volumeHeightDistribution = remember {
        floatArrayOf(
            0.6f, 0.6f, 0.6f, 0.8f, 1f, 1.1f, 0.95f, 0.9f, 0.8f,
            0.75f, 0.8f, 0.9f, 0.95f, 1f, 1.1f, 1.2f, 1.5f, 1.4f, 1.3f,
            1.4f, 1.5f, 1.2f, 1.1f, 1f, 0.95f, 0.9f, 0.8f, 0.75f, 0.8f,
            0.9f, 0.95f, 1.1f, 1f, 0.8f, 0.6f, 0.6f, 0.6f
        )
    }
    
    // 状态管理
    var currentVolume by remember { mutableIntStateOf(-1) }
    var state by remember { mutableStateOf(SoundWaveState.INIT) }
    val volumeBars = remember { Array(config.volumeCount) { VolumeBar() } }
    val random = remember { Random(1) }
    val interpolator = remember { VolumeDanceInterpolator() }
    
    // 强制刷新状态
    var refreshTrigger by remember { mutableLongStateOf(0L) }
    
    // 处理音量变化 - 完全按照原版逻辑
    LaunchedEffect(volume) {
        if (currentVolume < 0) {
            currentVolume = volume
            return@LaunchedEffect
        }
        // 平滑音量变化
        currentVolume = (currentVolume + volume) / 2
        state = if ((currentVolume < config.minVolume) && config.enableIdle) {
            SoundWaveState.IDLE
        } else {
            SoundWaveState.DANCE
        }
    }
    
    // 60fps 动画循环 - 使用awaitFrame确保真正的60fps
    LaunchedEffect(Unit) {
        state = SoundWaveState.INIT
        while (true) {
            awaitFrame()
            refreshTrigger = System.currentTimeMillis()
        }
    }
    
    // 计算总宽度
    val totalWidth = with(density) {
        (config.volumeCount * volumeBarHalfWidthPx * 2 + 
         (config.volumeCount - 1) * volumeBarMarginPx).toDp()
    }
    
    Canvas(
        modifier = modifier.size(
            width = totalWidth,
            height = config.maxVolumeBarHeight
        )
    ) {
        // 触发刷新
        refreshTrigger
        
        val currentTime = System.currentTimeMillis()
        val centerX = size.width / 2
        val centerY = size.height / 2
        
        // 绘制中间的音量条
        val midPosition = (volumeBars.size - 1) / 2
        
        translate(centerX, centerY) {
            // 绘制中间音柱
            drawVolumeBar(
                volumeBars[midPosition],
                0f,
                0f,
                midPosition,
                state,
                currentTime,
                currentVolume,
                config,
                volumeHeightDistribution,
                minVolumeBarHeightPx,
                maxVolumeBarHeightPx,
                volumeBarHalfWidthPx,
                volumeBarMarginPx,
                maxIdleHeightPx,
                random,
                interpolator,
                idleHeightGetter
            )
            
            // 绘制右侧音量条
            var rightX = 0f
            for (i in midPosition + 1 until volumeBars.size) {
                rightX += volumeBarMarginPx + 2 * volumeBarHalfWidthPx
                drawVolumeBar(
                    volumeBars[i],
                    rightX,
                    0f,
                    i,
                    state,
                    currentTime,
                    currentVolume,
                    config,
                    volumeHeightDistribution,
                    minVolumeBarHeightPx,
                    maxVolumeBarHeightPx,
                    volumeBarHalfWidthPx,
                    volumeBarMarginPx,
                    maxIdleHeightPx,
                    random,
                    interpolator,
                    idleHeightGetter
                )
            }
            
            // 绘制左侧音量条 - 按照原版的镜像绘制方式
            var leftX = 0f
            for (i in midPosition - 1 downTo 0) {
                leftX -= volumeBarMarginPx + 2 * volumeBarHalfWidthPx
                drawVolumeBar(
                    volumeBars[i],
                    leftX,
                    0f,
                    i,
                    state,
                    currentTime,
                    currentVolume,
                    config,
                    volumeHeightDistribution,
                    minVolumeBarHeightPx,
                    maxVolumeBarHeightPx,
                    volumeBarHalfWidthPx,
                    volumeBarMarginPx,
                    maxIdleHeightPx,
                    random,
                    interpolator,
                    idleHeightGetter
                )
            }
        }
    }
}

private fun DrawScope.drawVolumeBar(
    volumeBar: VolumeBar,
    x: Float,
    y: Float,
    index: Int,
    state: SoundWaveState,
    currentTime: Long,
    currentVolume: Int,
    config: SoundWaveConfig,
    volumeHeightDistribution: FloatArray,
    minVolumeBarHeightPx: Float,
    maxVolumeBarHeightPx: Float,
    volumeBarHalfWidthPx: Float,
    volumeBarMarginPx: Float,
    maxIdleHeightPx: Float,
    random: Random,
    interpolator: VolumeDanceInterpolator,
    idleHeightGetter: (Int) -> Float
) {
    // 非 idle 状态下需要重置 idleStartTime
    if (state != SoundWaveState.IDLE) {
        volumeBar.idleStartTime = 0
    }
    
    val drawHeight = when (state) {
        SoundWaveState.IDLE -> {
            drawIdleBar(
                volumeBar,
                index,
                currentTime,
                config,
                minVolumeBarHeightPx,
                maxIdleHeightPx,
                idleHeightGetter
            )
        }
        SoundWaveState.DANCE -> {
            drawDanceBar(
                volumeBar,
                index,
                currentTime,
                currentVolume,
                config,
                volumeHeightDistribution,
                minVolumeBarHeightPx,
                maxVolumeBarHeightPx,
                random,
                interpolator
            )
        }
        SoundWaveState.INIT, SoundWaveState.STOP -> {
            minVolumeBarHeightPx
        }
    }
    
    // 绘制圆角矩形 - 完全按照原版的绘制方式
    drawRoundRect(
        color = config.volumeBarColor,
        topLeft = Offset(x - volumeBarHalfWidthPx, y - drawHeight / 2),
        size = Size(volumeBarHalfWidthPx * 2, drawHeight),
        cornerRadius = CornerRadius(volumeBarHalfWidthPx, volumeBarHalfWidthPx)
    )
}

private fun drawIdleBar(
    volumeBar: VolumeBar,
    index: Int,
    currentTime: Long,
    config: SoundWaveConfig,
    minVolumeBarHeightPx: Float,
    maxIdleHeightPx: Float,
    idleHeightGetter: (Int) -> Float
): Float {
    // 完全按照原版的IDLE算法
    if (currentTime - volumeBar.idleStartTime > config.idleDuration) {
        volumeBar.idleStartTime = currentTime
    }
    
    val idleStart = (1f * (currentTime - volumeBar.idleStartTime) / config.idleDuration * 
                    (config.volumeCount / 2 + config.volumeIdleCount + 4)).toInt()
    val mid = config.volumeCount / 2
    val x = if (index >= mid) {
        mid + idleStart - index
    } else {
        index - (mid - idleStart)
    }
    
    return if (x in 0..config.volumeIdleCount / 2) {
        idleHeightGetter(x)
    } else if (x in config.volumeIdleCount / 2 + 1..config.volumeIdleCount) {
        idleHeightGetter(config.volumeIdleCount - x)
    } else {
        minVolumeBarHeightPx
    }
}

private fun drawDanceBar(
    volumeBar: VolumeBar,
    index: Int,
    currentTime: Long,
    currentVolume: Int,
    config: SoundWaveConfig,
    volumeHeightDistribution: FloatArray,
    minVolumeBarHeightPx: Float,
    maxVolumeBarHeightPx: Float,
    random: Random,
    interpolator: VolumeDanceInterpolator
): Float {
    // 完全按照原版的DANCE算法
    if (currentTime - volumeBar.danceStartTime > config.danceDuration) {
        volumeBar.danceStartDelay = random.nextInt(config.maxDanceDelay)
        volumeBar.danceStartTime = currentTime + volumeBar.danceStartDelay
        val volume = currentVolume.coerceIn(config.minVolume, config.maxVolume)
        val rawHeight = 1f * (volume - config.minVolume) / (config.maxVolume - config.minVolume) * 
                       (maxVolumeBarHeightPx - minVolumeBarHeightPx) + minVolumeBarHeightPx
        val heightPortion = if (index < volumeHeightDistribution.size) {
            volumeHeightDistribution[index]
        } else {
            1f
        }
        volumeBar.height = (rawHeight * heightPortion)
    }
    
    val fraction = 1f * (currentTime - volumeBar.danceStartTime) / config.danceDuration
    return if (fraction in 0f..1f) {
        (interpolator.getInterpolation(fraction) * volumeBar.height)
            .coerceIn(minVolumeBarHeightPx, maxVolumeBarHeightPx)
    } else {
        minVolumeBarHeightPx
    }
}