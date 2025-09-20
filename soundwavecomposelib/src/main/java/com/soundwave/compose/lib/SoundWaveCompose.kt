package com.soundwave.compose.lib

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.android.awaitFrame

/**
 * 音波可视化组件
 * @param volume 当前音量值，用于驱动音波动画
 * @param modifier 修饰符
 * @param config 音波配置，包含各种参数设置
 */
@Composable
fun SoundWave(
    volume: Int,
    modifier: Modifier = Modifier,
    config: SoundWaveConfig = SoundWaveConfig()
) {
    val density = LocalDensity.current
    
    val volumeBarMarginPx = with(density) { config.volumeBarMargin.toPx() }
    val volumeBarHalfWidthPx = with(density) { config.volumeBarHalfWidth.toPx() }

    var currentVolume by remember { mutableIntStateOf(-1) }
    var state by remember { mutableStateOf(SoundWaveState.INIT) }
    val volumeBars = remember { Array(config.volumeCount) { VolumeBar() } }
    var refreshTrigger by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        state = SoundWaveState.INIT
        while (true) {
            awaitFrame()
            refreshTrigger = System.currentTimeMillis()
        }
    }

    // 处理音量变化
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
                density, volumeBars[midPosition], 0f, midPosition,
                state, currentTime, currentVolume, config
            )
            
            // 绘制右侧音柱
            var rightX = 0f
            for (i in midPosition + 1 until volumeBars.size) {
                rightX += volumeBarMarginPx + 2 * volumeBarHalfWidthPx
                drawVolumeBar(
                    density, volumeBars[i], rightX, i,
                    state, currentTime, currentVolume, config,
                )
            }
            
            // 绘制左侧音柱
            var leftX = 0f
            for (i in midPosition - 1 downTo 0) {
                leftX -= volumeBarMarginPx + 2 * volumeBarHalfWidthPx
                drawVolumeBar(
                    density, volumeBars[i], leftX, i,
                    state, currentTime, currentVolume, config,
                )
            }
        }
    }
}
