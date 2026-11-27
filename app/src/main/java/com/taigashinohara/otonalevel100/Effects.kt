package com.taigashinohara.otonalevel100

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlin.math.PI
import kotlin.math.sin

/**
 * Celebration effects (v0.13.0, user request): haptics and a soft chime.
 * Everything is best-effort and wrapped so a missing vibrator or a busy
 * audio device can never break the quiz flow. Wrong answers get nothing —
 * celebration is a reward, never a punishment signal.
 */
object Haptics {
    private fun vibrator(context: Context): Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    } catch (e: Exception) {
        null
    }

    /** Short happy tick for a correct answer. */
    fun tick(context: Context) {
        try {
            val v = vibrator(context) ?: return
            if (!v.hasVibrator()) return
            v.vibrate(VibrationEffect.createOneShot(35, VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (e: Exception) {
            // Best effort only.
        }
    }

    /** Little fanfare pattern for level-ups and perfect sets. */
    fun fanfare(context: Context) {
        try {
            val v = vibrator(context) ?: return
            if (!v.hasVibrator()) return
            v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 60, 70, 60, 70, 160), -1))
        } catch (e: Exception) {
            // Best effort only.
        }
    }
}

/** Soft sine chimes synthesized on the fly — no audio assets needed. */
object Chime {
    private const val SAMPLE_RATE = 44100

    private fun render(notes: List<Pair<Double, Int>>, gain: Double = 0.10): ShortArray {
        val totalMs = notes.sumOf { it.second }
        val out = ShortArray(SAMPLE_RATE * totalMs / 1000)
        var offset = 0
        for ((freq, ms) in notes) {
            val n = SAMPLE_RATE * ms / 1000
            for (i in 0 until n) {
                // Short attack/decay envelope so the tone never clicks.
                val env = minOf(1.0, i / (SAMPLE_RATE * 0.01), (n - i) / (SAMPLE_RATE * 0.03))
                val v = sin(2 * PI * freq * i / SAMPLE_RATE) * gain * env
                out[offset + i] = (v * Short.MAX_VALUE).toInt().toShort()
            }
            offset += n
        }
        return out
    }

    private fun play(notes: List<Pair<Double, Int>>) {
        Thread {
            try {
                val data = render(notes)
                val track = AudioTrack(
                    AudioManager.STREAM_MUSIC, SAMPLE_RATE,
                    AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT,
                    data.size * 2, AudioTrack.MODE_STATIC
                )
                track.write(data, 0, data.size)
                track.play()
                Thread.sleep(notes.sumOf { it.second }.toLong() + 120)
                track.release()
            } catch (e: Exception) {
                // Best effort only.
            }
        }.start()
    }

    /** Two bright notes for a correct answer. */
    fun correct() = play(listOf(880.0 to 90, 1318.5 to 140))

    /** Rising three notes for a level-up / perfect set. */
    fun levelUp() = play(listOf(659.3 to 90, 880.0 to 90, 1318.5 to 200))
}
