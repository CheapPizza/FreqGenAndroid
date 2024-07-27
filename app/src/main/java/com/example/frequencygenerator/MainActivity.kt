package com.example.frequencygenerator

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.sin

class MainActivity : AppCompatActivity() {

    private lateinit var frequencyTextView: TextView
    private lateinit var seekBar: SeekBar
    private var audioTrack: AudioTrack? = null
    private var isPlaying = false
    private val TAG = "FrequencyGenerator"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        frequencyTextView = findViewById(R.id.frequencyTextView)
        seekBar = findViewById(R.id.seekBar)

        seekBar.max = 20000
        seekBar.progress = 200 // Set initial frequency

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val frequency = progress.coerceAtLeast(1)
                frequencyTextView.text = "$frequency Hz"
                playFrequency(frequency)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Start playing the initial frequency
        playFrequency(440) // Default to 440 Hz
    }

    private fun playFrequency(frequency: Int) {
        if (isPlaying) {
            audioTrack?.stop()
            audioTrack?.release()
        }

        val sampleRate = 44100
        val durationInSeconds = 60
        val numSamples = sampleRate * durationInSeconds
        val samples = ShortArray(numSamples)

        for (i in samples.indices) {
            samples[i] = (sin(2.0 * Math.PI * frequency * i / sampleRate) * Short.MAX_VALUE).toInt()
                .toShort()
        }

        audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(samples.size * 2)
            .build()

        val writeResult = audioTrack?.write(samples, 0, samples.size) ?: -1
        Log.d(TAG, "AudioTrack write result: $writeResult")
        audioTrack?.setVolume(AudioTrack.getMaxVolume())
        audioTrack?.play()
        Log.d(TAG, "Playing frequency: $frequency Hz")

        isPlaying = true
    }

    override fun onDestroy() {
        super.onDestroy()
        audioTrack?.stop()
        audioTrack?.release()
    }
}
