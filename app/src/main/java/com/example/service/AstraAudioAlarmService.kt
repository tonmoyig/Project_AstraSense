package com.example.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Real-time audio warning & distress alarm subsystem for AstraSense.
 * Synthesizes audible emergency siren tones and warning beeps for critical spaceflight hazards,
 * supporting mute controls, emergency overrides, and tactile vibration alerts.
 */
object AstraAudioAlarmService {
  private const val TAG = "AstraAudioAlarm"

  private val _isMuted = MutableStateFlow(false)
  val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

  private val _isEmergencyAlarmActive = MutableStateFlow(false)
  val isEmergencyAlarmActive: StateFlow<Boolean> = _isEmergencyAlarmActive.asStateFlow()

  private val alarmScope = CoroutineScope(Dispatchers.Default)
  private var sirenJob: Job? = null

  fun initialize(context: Context) {
    try {
      val prefs = context.getSharedPreferences("astrasense_audio_prefs", Context.MODE_PRIVATE)
      _isMuted.value = prefs.getBoolean("is_muted", false)
    } catch (e: Exception) {
      Log.w(TAG, "Audio prefs initialization fallback: ${e.message}")
    }
  }

  fun toggleMute(context: Context? = null) {
    setMuted(!_isMuted.value, context)
  }

  fun setMuted(muted: Boolean, context: Context? = null) {
    _isMuted.value = muted
    if (muted && _isEmergencyAlarmActive.value) {
      // If user explicitly mutes during emergency, silence current siren
      stopEmergencyAlarm()
    }
    context?.let { ctx ->
      try {
        ctx.getSharedPreferences("astrasense_audio_prefs", Context.MODE_PRIVATE)
          .edit()
          .putBoolean("is_muted", muted)
          .apply()
      } catch (e: Exception) {
        Log.w(TAG, "Failed to persist audio mute pref: ${e.message}")
      }
    }
  }

  /**
   * Triggers the high-priority Emergency SOS distress alarm.
   * By mission protocol, emergency alarms auto-unmute so crew is alerted.
   */
  fun playEmergencyAlarm(context: Context? = null) {
    // High-priority emergency defaults to active audio
    _isMuted.value = false
    _isEmergencyAlarmActive.value = true

    // Trigger physical haptic distress vibration pulse
    context?.let { triggerEmergencyVibration(it) }

    sirenJob?.cancel()
    sirenJob = alarmScope.launch {
      try {
        while (isActive && _isEmergencyAlarmActive.value && !_isMuted.value) {
          // Play synthesized alternating dual-tone emergency siren
          playTonePattern(880, 1760, 400)
          delay(100)
          playTonePattern(1760, 880, 400)
          delay(150)
        }
      } catch (e: Exception) {
        Log.w(TAG, "Emergency siren loop ended: ${e.message}")
      }
    }
  }

  /**
   * Halts any active emergency siren alarm.
   */
  fun stopEmergencyAlarm() {
    _isEmergencyAlarmActive.value = false
    sirenJob?.cancel()
    sirenJob = null
  }

  /**
   * Plays a double warning tone for elevated clinical risk (e.g. SANS threshold or CO₂ spike).
   */
  fun playThreatWarningSound(context: Context? = null) {
    if (_isMuted.value) return
    context?.let { triggerWarningVibration(it) }

    alarmScope.launch {
      try {
        playSynthesizedTone(750, 180)
        delay(80)
        playSynthesizedTone(920, 220)
      } catch (e: Exception) {
        fallbackTone(ToneGenerator.TONE_PROP_BEEP2, 250)
      }
    }
  }

  /**
   * Plays a confirmation chime when self-exam or clearance authentication succeeds.
   */
  fun playConfirmationChime(context: Context? = null) {
    if (_isMuted.value) return
    alarmScope.launch {
      try {
        playSynthesizedTone(523, 120) // C5
        delay(40)
        playSynthesizedTone(659, 120) // E5
        delay(40)
        playSynthesizedTone(784, 200) // G5
      } catch (e: Exception) {
        fallbackTone(ToneGenerator.TONE_PROP_ACK, 200)
      }
    }
  }

  /**
   * Synthesizes PCM 16-bit audio waveform and streams via AudioTrack.
   * Completely self-contained: works on Android without needing any external audio assets.
   */
  private fun playSynthesizedTone(frequencyHz: Int, durationMs: Int) {
    try {
      val sampleRate = 44100
      val numSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(1)
      val generatedSnd = ShortArray(numSamples)
      val angularFreq = 2.0 * Math.PI * frequencyHz / sampleRate

      for (i in 0 until numSamples) {
        // Apply envelope fade-in / fade-out to prevent audio clicking
        val envelope = when {
          i < 400 -> i / 400.0
          i > numSamples - 400 -> (numSamples - i) / 400.0
          else -> 1.0
        }
        val sample = Math.sin(angularFreq * i) * 32767 * 0.7 * envelope
        generatedSnd[i] = sample.toInt().toShort()
      }

      val bufferSize = AudioTrack.getMinBufferSize(
        sampleRate,
        AudioFormat.CHANNEL_OUT_MONO,
        AudioFormat.ENCODING_PCM_16BIT
      ).coerceAtLeast(numSamples * 2)

      val audioTrack = AudioTrack.Builder()
        .setAudioAttributes(
          AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        )
        .setAudioFormat(
          AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
            .build()
        )
        .setBufferSizeInBytes(bufferSize)
        .setTransferMode(AudioTrack.MODE_STATIC)
        .build()

      audioTrack.write(generatedSnd, 0, numSamples)
      audioTrack.play()
      Thread.sleep(durationMs.toLong() + 30L)
      audioTrack.stop()
      audioTrack.release()
    } catch (e: Throwable) {
      Log.d(TAG, "AudioTrack synth notice: ${e.message}")
    }
  }

  private fun playTonePattern(freq1: Int, freq2: Int, durationMs: Int) {
    playSynthesizedTone(freq1, durationMs / 2)
    playSynthesizedTone(freq2, durationMs / 2)
  }

  private fun fallbackTone(toneType: Int, durationMs: Int) {
    try {
      val toneGen = ToneGenerator(AudioManager.STREAM_ALARM, 85)
      toneGen.startTone(toneType, durationMs)
      alarmScope.launch {
        delay(durationMs.toLong() + 50L)
        try { toneGen.release() } catch (_: Exception) {}
      }
    } catch (e: Throwable) {
      Log.d(TAG, "ToneGenerator fallback notice: ${e.message}")
    }
  }

  private fun triggerEmergencyVibration(context: Context) {
    try {
      val vibrator = getVibrator(context) ?: return
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val pattern = longArrayOf(0, 300, 150, 300, 150, 600)
        val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
        vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
      } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(longArrayOf(0, 300, 150, 300), -1)
      }
    } catch (e: Exception) {
      Log.w(TAG, "Vibration notice: ${e.message}")
    }
  }

  private fun triggerWarningVibration(context: Context) {
    try {
      val vibrator = getVibrator(context) ?: return
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(180, VibrationEffect.DEFAULT_AMPLITUDE))
      } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(180)
      }
    } catch (e: Exception) {
      Log.w(TAG, "Warning vibration notice: ${e.message}")
    }
  }

  private fun getVibrator(context: Context): Vibrator? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
      manager?.defaultVibrator
    } else {
      @Suppress("DEPRECATION")
      context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
  }
}
