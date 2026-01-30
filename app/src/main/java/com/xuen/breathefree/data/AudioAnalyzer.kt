package com.xuen.breathefree.data

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.IOException

class AudioAnalyzer(private val context: Context) {

    private var recorder: MediaRecorder? = null
    
    // We need a file to record to, even if we just discard it.
    // /dev/null is not always reliable on Android for MediaRecorder, so we use a cache file.
    private var outputFile: File? = null

    fun start() {
        if (recorder != null) return

        // Create a temporary file
        outputFile = File(context.cacheDir, "audio_temp.3gp")

        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            try {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(outputFile?.absolutePath)
                prepare()
                start()
            } catch (e: IOException) {
                Log.e("AudioAnalyzer", "prepare() failed", e)
            } catch (e: IllegalStateException) {
                 Log.e("AudioAnalyzer", "start() failed", e)
            } catch (e: Exception) {
                Log.e("AudioAnalyzer", "General error", e)
            }
        }
    }

    fun stop() {
        try {
            recorder?.stop()
        } catch (e: RuntimeException) {
            // stopping a recorder that wasn't successfully started can throw
            Log.e("AudioAnalyzer", "stop() failed", e)
        } finally {
             recorder?.release()
             recorder = null
             outputFile?.delete() // Clean up
        }
    }

    fun getAmplitude(): Float {
        val maxAmplitude = recorder?.maxAmplitude ?: 0
        // Normalize: maxAmplitude returns 0-32767 usually.
        // We want a float 0.0 - 1.0. 
        // We can clamp it or apply a log scale for better sensitivity.
        
        // Logarithmic scale is often better for audio.
        // But for visual reactivity, linear might be enough if we just want "loudness".
        // Let's try simple normalization first with a dynamic range.
        
        val max = 32767f
        return (maxAmplitude / max).coerceIn(0f, 1f)
    }
}
