package com.ebf.smartattendanceapp.UltrasonicDetector

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max

@SuppressLint("MissingPermission")
class UltrasonicDetector {

    private val _isHearingUltrasonic = MutableStateFlow(false)
    val isHearingUltrasonic = _isHearingUltrasonic.asStateFlow()

    private var audioRecord: AudioRecord? = null
    private var recordingThread: Thread? = null
    private var isRecording = false

    // --- Configuration ---
    companion object {
        private const val SAMPLE_RATE = 44100 // Hz
        private val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val FFT_SIZE = 2048 // Must be a power of 2
        private const val AMPLITUDE_THRESHOLD = 30500.0
    }

    fun startListening() {
        if (isRecording) return
        _isHearingUltrasonic.value = false

        val bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
        if (bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e("UltrasonicDetector", "Invalid AudioRecord parameters.")
            return
        }

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            bufferSize
        )

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            Log.e("UltrasonicDetector", "AudioRecord could not be initialized.")
            return
        }

        isRecording = true
        audioRecord?.startRecording()

        recordingThread = Thread {
            val audioData = ShortArray(FFT_SIZE)
            val fft = FFT(FFT_SIZE)
            val real = DoubleArray(FFT_SIZE)
            val imag = DoubleArray(FFT_SIZE)

            while (isRecording) {
                val readResult = audioRecord?.read(audioData, 0, FFT_SIZE)
                if (readResult == FFT_SIZE) {
                    for (i in 0 until FFT_SIZE) {
                        real[i] = audioData[i].toDouble()
                        imag[i] = 0.0
                    }
                    fft.fft(real, imag)
                    if (isTargetFrequencyPresent(real, imag)) {
                        _isHearingUltrasonic.value = true
                        stopListening()
                    }
                }
            }
        }
        recordingThread?.start()
    }

    private fun isTargetFrequencyPresent(real: DoubleArray, imag: DoubleArray): Boolean {
        val binWidth = SAMPLE_RATE.toDouble() / FFT_SIZE

        // Define frequency ranges in Hz
        val ultrasonicStartHz = 19000.0
        val ultrasonicEndHz = 20000.0
        val voiceStartHz = 300.0
        val voiceEndHz = 4000.0

        // Convert Hz to FFT bin indices
        val ultrasonicStartBin = (ultrasonicStartHz / binWidth).toInt()
        val ultrasonicEndBin = (ultrasonicEndHz / binWidth).toInt()
        val voiceStartBin = (voiceStartHz / binWidth).toInt()
        val voiceEndBin = (voiceEndHz / binWidth).toInt()

        var maxUltrasonicAmplitude = 0.0
        var maxVoiceAmplitude = 0.0

        // We only need to check the first half of the bins (Nyquist theorem)
        for (i in 0 until FFT_SIZE / 2) {
            val magnitude = kotlin.math.sqrt(real[i] * real[i] + imag[i] * imag[i])

            // Check if the bin is in our ultrasonic range
            if (i in ultrasonicStartBin..ultrasonicEndBin) {
                maxUltrasonicAmplitude = max(maxUltrasonicAmplitude, magnitude)
            }

            // Check if the bin is in the human voice range
            if (i in voiceStartBin..voiceEndBin) {
                maxVoiceAmplitude = max(maxVoiceAmplitude, magnitude)
            }
        }

        // --- The Dominance Check Logic ---
        val isUltrasonicLoudEnough = maxUltrasonicAmplitude > AMPLITUDE_THRESHOLD
        // Check if ultrasonic amplitude is at least 5 times greater than voice amplitude
        // Added a small value (1.0) to avoid division by zero if voice amplitude is 0
        val isUltrasonicDominant = maxUltrasonicAmplitude > (maxVoiceAmplitude * 5.0) + 1.0

        if (isUltrasonicLoudEnough && isUltrasonicDominant) {
            Log.d("UltrasonicDetector", "SUCCESS: Dominant ultrasonic signal detected. UltraAmp: $maxUltrasonicAmplitude, VoiceAmp: $maxVoiceAmplitude")
            return true
        }

        return false
    }


    fun stopListening() {
        if (!isRecording) return
        isRecording = false
        recordingThread?.interrupt()
        recordingThread = null

        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }
}
