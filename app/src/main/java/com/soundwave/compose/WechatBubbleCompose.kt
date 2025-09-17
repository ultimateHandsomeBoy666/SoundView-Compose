package com.soundwave.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 微信气泡样式的容器组件
 * 
 * @param modifier 修饰符
 * @param bubbleColor 气泡背景色，默认为微信绿色
 * @param cornerRadius 圆角半径
 * @param triangleSize 右下角三角形尖尖的大小
 * @param paddingHorizontal 水平内边距
 * @param paddingVertical 垂直内边距  
 * @param paddingBottom 底部内边距，需要为三角形留出空间
 * @param content 容器内容
 * 
 * 使用示例：
 * ```
 * WechatBubbleFrame(
 *     bubbleColor = Color(0xFF95D75B)
 * ) {
 *     Text("Hello World")
 * }
 * ```
 */
@Composable
fun WechatBubbleFrame(
    modifier: Modifier = Modifier,
    bubbleColor: Color = Color(0xFF95D75B), // 微信绿色
    cornerRadius: Dp = 12.dp,
    triangleSize: Dp = 8.dp,
    paddingHorizontal: Dp = 16.dp,
    paddingVertical: Dp = 12.dp,
    paddingBottom: Dp = 20.dp, // 底部留出三角形空间
    content: @Composable BoxScope.() -> Unit
) {
    val density = LocalDensity.current
    
    Box(
        modifier = modifier
            .wrapContentSize()
    ) {
        // 背景气泡
        Canvas(
            modifier = Modifier
                .matchParentSize()
        ) {
            drawWechatBubble(
                bubbleColor = bubbleColor,
                cornerRadius = with(density) { cornerRadius.toPx() },
                triangleSize = with(density) { triangleSize.toPx() }
            )
        }
        
        // 内容
        Box(
            modifier = Modifier
                .padding(
                    start = paddingHorizontal,
                    top = paddingVertical,
                    end = paddingHorizontal,
                    bottom = paddingBottom
                )
        ) {
            content()
        }
    }
}

private fun DrawScope.drawWechatBubble(
    bubbleColor: Color,
    cornerRadius: Float,
    triangleSize: Float
) {
    if (size.width <= 0 || size.height <= 0) return
    
    // 主体圆角矩形
    val mainRect = androidx.compose.ui.geometry.Rect(
        offset = Offset.Zero,
        size = Size(size.width, size.height - triangleSize)
    )
    
    // 绘制主体圆角矩形
    drawRoundRect(
        color = bubbleColor,
        topLeft = mainRect.topLeft,
        size = mainRect.size,
        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
    )
    
    // 绘制右下角的三角形尖尖 - 完全按照原版算法
    val triangleStartX = size.width * 0.8f // 三角形起始位置
    val triangleStartY = size.height - triangleSize
    val triangleEndX = triangleStartX + triangleSize * 0.8f
    val triangleEndY = size.height
    
    val trianglePath = Path().apply {
        moveTo(triangleStartX, triangleStartY)
        lineTo(triangleEndX, triangleEndY)
        lineTo(triangleStartX + triangleSize, triangleStartY)
        close()
    }
    
    drawPath(
        path = trianglePath,
        color = bubbleColor
    )
}