# SoundWave Compose Library

A beautiful, highly customizable sound wave visualization library for Jetpack Compose.

[![](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=24)
[![](https://img.shields.io/badge/Compose-Ready-blue.svg)](https://developer.android.com/jetpack/compose)

## Features

- 🎵 **Real-time sound wave visualization** with smooth 60fps animations
- 🎨 **Highly customizable** appearance with extensive configuration options  
- 📱 **Jetpack Compose native** - built specifically for modern Android UI
- 🌊 **Multiple animation states** - IDLE wave flow and DANCE volume response
- 🎯 **Easy integration** - just pass volume data and enjoy the visualization
- 💚 **WeChat-style bubble** container included for messaging apps

## Demo

The library provides smooth, responsive sound wave animations that react to audio input in real-time.

## Installation

### Gradle (Recommended)

Add this to your module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("io.github.ultimatehandsomeboy666:soundwavecomposelib:1.0.0")
}
```

### Local Module

Clone this repository and include the `soundwavecomposelib` module in your project.

## Usage

### Basic Usage

```kotlin
import com.soundwave.compose.lib.SoundWaveView
import com.soundwave.compose.lib.SoundWaveConfig

@Composable
fun MyScreen() {
    var currentVolume by remember { mutableIntStateOf(0) }
    
    SoundWaveView(
        volume = currentVolume,
        modifier = Modifier.wrapContentSize()
    )
}
```

### Advanced Configuration

```kotlin
SoundWaveView(
    volume = currentVolume,
    modifier = Modifier.wrapContentSize(),
    config = SoundWaveConfig(
        volumeCount = 37,                    // Number of volume bars
        volumeIdleCount = 16,               // Bars participating in idle animation
        maxVolume = 35,                     // Maximum volume value
        minVolume = 4,                      // Minimum volume value
        maxVolumeBarHeight = 45.dp,         // Maximum bar height
        minVolumeBarHeight = 4.dp,          // Minimum bar height
        volumeBarColor = Color.White,       // Bar color
        volumeBarHalfWidth = 1.5.dp,        // Bar width (half width)
        volumeBarMargin = 2.dp,             // Space between bars
        enableIdle = true                   // Enable idle wave animation
    ),
    idleHeightGetter = { x ->               // Custom idle height calculation
        0.07f * x * x * (12 - x) + 4
    }
)
```

### WeChat-Style Bubble Container

```kotlin
import com.soundwave.compose.lib.WechatBubbleFrame

WechatBubbleFrame(
    bubbleColor = Color(0xFF95D75B)  // WeChat green
) {
    SoundWaveView(
        volume = currentVolume,
        config = SoundWaveConfig(
            volumeBarColor = Color.Black.copy(alpha = 0.6f)
        )
    )
}
```

## Configuration Options

### SoundWaveConfig Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `volumeCount` | Int | 37 | Total number of volume bars |
| `volumeIdleCount` | Int | 16 | Number of bars in idle wave animation |
| `maxVolume` | Int | 35 | Maximum volume value for scaling |
| `minVolume` | Int | 4 | Minimum volume value for scaling |
| `danceDuration` | Long | 250L | Duration of volume bar dance animation (ms) |
| `maxDanceDelay` | Int | 80 | Maximum random delay for bar animations (ms) |
| `idleDuration` | Long | 3500L | Duration of one idle wave cycle (ms) |
| `maxIdleHeight` | Dp | 16.dp | Maximum height during idle animation |
| `minVolumeBarHeight` | Dp | 4.dp | Minimum bar height |
| `maxVolumeBarHeight` | Dp | 36.dp | Maximum bar height |
| `volumeBarMargin` | Dp | 3.dp | Space between volume bars |
| `volumeBarHalfWidth` | Dp | 1.5.dp | Half width of each volume bar |
| `volumeBarColor` | Color | Black (60% alpha) | Color of volume bars |
| `enableIdle` | Boolean | true | Enable idle wave animation when volume is low |

### WechatBubbleFrame Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `bubbleColor` | Color | #95D75B | Background color of the bubble |
| `cornerRadius` | Dp | 12.dp | Corner radius of the bubble |
| `triangleSize` | Dp | 8.dp | Size of the tail triangle |
| `paddingHorizontal` | Dp | 16.dp | Horizontal padding inside bubble |
| `paddingVertical` | Dp | 12.dp | Vertical padding inside bubble |
| `paddingBottom` | Dp | 20.dp | Bottom padding (leaves space for triangle) |

## Animation States

The sound wave has three main states:

1. **INIT/STOP** - Static minimum height bars
2. **IDLE** - Smooth wave flowing animation when volume is below threshold  
3. **DANCE** - Volume-responsive bouncing animation

## Integration with Audio

To integrate with real audio input:

```kotlin
class AudioCapture {
    private val dbCalculator = DBCalculator()
    
    fun processAudioData(audioData: ShortArray, readSize: Int): Int {
        val volume = dbCalculator.calculateDB(audioData, readSize).toInt() + 10
        return volume
    }
}

@Composable
fun SoundRecorderScreen() {
    var currentVolume by remember { mutableIntStateOf(0) }
    
    // Update currentVolume from your audio processing
    // (See the demo app for complete AudioRecord implementation)
    
    SoundWaveView(
        volume = currentVolume,
        config = SoundWaveConfig(
            minVolume = 4,
            maxVolume = 35
        )
    )
}
```

## Performance

- Optimized for 60fps animations using `awaitFrame()`
- Minimal recomposition with `@Stable` classes
- Efficient Canvas drawing operations
- Smooth volume change interpolation

## License

```
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Author

**bullfrog** - [ultimateHandsomeBoy666](https://github.com/ultimateHandsomeBoy666)

## Acknowledgments

- Inspired by the original SoundWave library for traditional Android Views
- Built with ❤️ using Jetpack Compose