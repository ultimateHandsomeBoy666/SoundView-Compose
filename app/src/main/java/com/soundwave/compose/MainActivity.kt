package com.soundwave.compose

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.soundwave.compose.lib.SoundWaveConfig
import com.soundwave.compose.lib.SoundWave
import com.soundwave.compose.ui.theme.SoundWaveComposeTheme
import kotlin.concurrent.thread

class MainActivity : ComponentActivity() {
    
    private var audioRecord: AudioRecord? = null
    private var recordingThread: Thread? = null
    private var isRecording = false
    private val dbCalculator = DBCalculator()
    private var lastRecordTime = 0L
    
    // 当前音量状态 - 使用mutableStateOf而不是mutableIntStateOf，确保更新触发重组
    private val currentVolume = mutableStateOf(0)
    private val maxAmplitude = mutableStateOf(0)
    private val bufferSize = mutableStateOf(0)
    
    companion object {
        private const val SAMPLE_RATE = 44100
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            startRecording()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            SoundWaveComposeTheme {
                SoundWaveScreen(
                    volume = currentVolume.value,
                    maxAmplitude = maxAmplitude.value,
                    bufferSize = bufferSize.value
                )
            }
        }
        
        checkAndRequestPermission()
    }
    
    private fun checkAndRequestPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                startRecording()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }
    
    private fun startRecording() {
        val bufferSizeValue = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
        bufferSize.value = bufferSizeValue
        
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            bufferSizeValue
        )
        
        audioRecord?.startRecording()
        isRecording = true
        
        recordingThread = thread(start = true) {
            val buffer = ShortArray(bufferSizeValue)
            while (isRecording) {
                val readSize = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (readSize > 0 && System.currentTimeMillis() - lastRecordTime > 250L) {
                    lastRecordTime = System.currentTimeMillis()
                    val audioData = buffer.copyOf(readSize)
                    val maxAmp = audioData.maxOrNull()?.toFloat() ?: 0f
                    val volume = dbCalculator.calculateDB(audioData, audioData.size).toInt() + 10

                    // 更新UI状态
                    currentVolume.value = volume
                    maxAmplitude.value = maxAmp.toInt()
                }
            }
        }
    }
    
    override fun onStop() {
        super.onStop()
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }
}

@Composable
fun SoundWaveScreen(
    volume: Int,
    maxAmplitude: Int,
    bufferSize: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)) // 暗色背景
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        
        // 微信气泡样式的小音浪
        WechatBubbleFrame(
            modifier = Modifier.wrapContentSize(),
            bubbleColor = Color(0xFF95D75B)
        ) {
            SoundWave(
                volume = volume,
                modifier = Modifier.wrapContentSize(),
                config = SoundWaveConfig(
                    volumeCount = 37,
                    volumeIdleCount = 12,
                    volumeBarColor = Color.Black.copy(alpha = 0.625f),
                    maxVolumeBarHeight = 45.dp,
                    minVolumeBarHeight = 4.dp,
                    volumeBarHalfWidth = 0.9.dp,
                    volumeBarMargin = 0.8.dp,
                    idleHeightGetter = { x ->
                        (x + 6).dp
                    }
                )
            )
        }
        
        Spacer(modifier = Modifier.height(30.dp))
        
        // 大的音浪视图
        Box(
            modifier = Modifier
                .wrapContentSize()
                .background(
                    color = Color(0xA82196F3),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            SoundWave(
                volume = volume,
                modifier = Modifier.wrapContentSize(),
                config = SoundWaveConfig(
                    volumeCount = 38,
                    volumeIdleCount = 16,
                    maxVolumeBarHeight = 45.dp,
                    minVolumeBarHeight = 4.dp,
                    volumeBarColor = Color.White,
                    volumeBarHalfWidth = 1.5.dp,
                    volumeBarMargin = 2.dp,
                    idleHeightGetter = { x ->
                        (0.07f * x * x * (12 - x) + 4).dp
                    }
                ),
            )
        }
        
        Spacer(modifier = Modifier.height(30.dp))
        
        // 音量信息显示
        Text(
            text = "Volume: $volume (Max: $maxAmplitude)\nbufferSize = $bufferSize",
            fontSize = 24.sp,
            fontWeight = FontWeight.Normal,
            color = Color.White
        )
    }
}