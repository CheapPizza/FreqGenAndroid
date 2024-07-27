package com.example.frequencygenerator

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.sign
import android.graphics.Color

class MainActivity : AppCompatActivity() {

    private lateinit var frequencyEditText: EditText
    private lateinit var frequencyTextView: TextView
    private lateinit var playButton: Button
    private lateinit var stopButton: Button
    private lateinit var resetButton: Button
    private lateinit var sineButton: Button
    private lateinit var squareButton: Button
    private lateinit var sawButton: Button

    private var activeButtonColor = Color.parseColor("#ff6cff41")
    private var inactiveButtonColor = Color.parseColor("#ffc9ffb9")

    private var currentFrequency = 0
    private var audioTrack: AudioTrack? = null
    private var isPlaying = false
    private var maxFrequency = 20000
    private var selectedWaveform = "sine"
    private val sampleRate = 44100
    private val durationInSeconds = 60

    private val numSamples = sampleRate * durationInSeconds
    private val samples = ShortArray(numSamples)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        frequencyEditText = findViewById(R.id.frequencyEditText)
        frequencyTextView = findViewById(R.id.frequencyTextView)
        playButton = findViewById(R.id.playButton)
        stopButton = findViewById(R.id.stopButton)
        resetButton = findViewById(R.id.resetButton)
        sineButton = findViewById(R.id.sineButton)
        sineButton.setBackgroundColor(activeButtonColor)
        squareButton = findViewById(R.id.squareButton)
        squareButton.setBackgroundColor(inactiveButtonColor)
        sawButton = findViewById(R.id.sawButton)
        sawButton.setBackgroundColor(inactiveButtonColor)

        playButton.setOnClickListener {
            val frequency = frequencyEditText.text.toString().toIntOrNull()
            if (frequency != null && frequency in 1..maxFrequency) {
                frequencyTextView.text = "Current Frequency: $frequency Hz"
                if (frequency != currentFrequency) {
                    updateSample(frequency)
                }
                playFrequency()
            } else {
                frequencyTextView.text = "Enter a frequency between 1 and 20000 Hz."
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

        // TODO: Figure out why other waveforms don't work. See updateSample function
        //sineButton.setOnClickListener {
        //    waveformSelection("sine")
        //}
        //squareButton.setOnClickListener {
        //    waveformSelection("square")
        //}
        //sawButton.setOnClickListener {
        //    waveformSelection("saw")
        //}
    }

    private fun waveformSelection(selection: String) {
        if (selection == selectedWaveform) {
            return
        }
        selectedWaveform = selection
        when (selection) {
            "sine" -> {
                sineButton.setBackgroundColor(activeButtonColor)
                squareButton.setBackgroundColor(inactiveButtonColor)
                sawButton.setBackgroundColor(inactiveButtonColor)
            }
            "square" -> {
                sineButton.setBackgroundColor(inactiveButtonColor)
                squareButton.setBackgroundColor(activeButtonColor)
                sawButton.setBackgroundColor(inactiveButtonColor)
            }
            "saw" -> {
                sineButton.setBackgroundColor(inactiveButtonColor)
                squareButton.setBackgroundColor(inactiveButtonColor)
                sawButton.setBackgroundColor(activeButtonColor)
            }
        }
        updateSample(currentFrequency)
        if (isPlaying) {
            playFrequency()
        }
    }

    private fun updateSample(frequency: Int) {
        for (i in samples.indices) {
            val t = i / sampleRate.toDouble()
            // Create the waveform
            // TODO: Currently the sawtooth and square waves don't work as intended and are disabled.
            samples[i] = when (selectedWaveform) {
                "sine" -> (sin(2.0 * Math.PI * frequency * t) * Short.MAX_VALUE).toInt().toShort()
                "sawtooth" -> ((2.0 * (t * frequency - floor(t * frequency + 0.5))) * Short.MAX_VALUE).toInt().toShort()
                "square" -> (sign(sin(2.0 * Math.PI * frequency * t)) * Short.MAX_VALUE).toInt().toShort()
                else -> 0
            }
        }
        currentFrequency = frequency
    }

    private fun playFrequency() {
        if (isPlaying) {
            audioTrack?.stop()
            audioTrack?.release()
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
