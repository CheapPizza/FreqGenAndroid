package com.example.frequencygenerator

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.sin

class MainActivity : AppCompatActivity() {

    private lateinit var frequencyEditText: EditText
    private lateinit var frequencyTextView: TextView
    private lateinit var playButton: Button
    private lateinit var stopButton: Button
    private lateinit var resetButton: Button
    private var audioTrack: AudioTrack? = null
    private var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        frequencyEditText = findViewById(R.id.frequencyEditText)
        frequencyTextView = findViewById(R.id.frequencyTextView)
        playButton = findViewById(R.id.playButton)
        stopButton = findViewById(R.id.stopButton)
        resetButton = findViewById(R.id.resetButton)

        playButton.setOnClickListener {
            val frequency = frequencyEditText.text.toString().toIntOrNull()
            if (frequency != null && frequency in 1..20000) {
                frequencyTextView.text = "Current Frequency: $frequency Hz"
                playFrequency(frequency)
            } else {
                frequencyTextView.text = "Invalid frequency. Enter a value between 1 and 20000 Hz."
            }
        }

        stopButton.setOnClickListener {
            stopFrequency()
        }

        resetButton.setOnClickListener {
            frequencyEditText.text.clear()
            if (!isPlaying) {
                frequencyTextView.text = "Enter a frequency"
            }
        }
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
            samples[i] = (sin(2.0 * Math.PI * frequency * i / sampleRate) * Short.MAX_VALUE).toInt().toShort()
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

        audioTrack?.write(samples, 0, samples.size) ?: -1
        audioTrack?.setVolume(AudioTrack.getMaxVolume())
        audioTrack?.play()

        isPlaying = true
    }

    private fun stopFrequency() {
        if (isPlaying) {
            audioTrack?.stop()
            audioTrack?.release()
            isPlaying = false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopFrequency()
    }
}
