package com.hand.hand.diary

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File

object RecordManager {

    private const val TAG = "RecordManager"

    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null

    fun startRecording(context: Context) {
        // 이전 녹음기 정리
        try {
            mediaRecorder?.reset()
            mediaRecorder?.release()
        } catch (_: Exception) { }
        mediaRecorder = null
        outputFile = null

        val dir = File(context.filesDir, "recordings").apply { mkdirs() }
        outputFile = File(dir, "diary_${System.currentTimeMillis()}.m4a")

        try {
            mediaRecorder =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MediaRecorder(context)   // API 31+
                } else {
                    MediaRecorder()          // 예전 방식
                }

            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioEncodingBitRate(128000)
                setAudioSamplingRate(44100)
                setOutputFile(outputFile!!.absolutePath)
                prepare()
                start()
            }

            Log.d(TAG, "🎙 startRecording 성공: ${outputFile?.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "startRecording 실패", e)
            try {
                mediaRecorder?.reset()
                mediaRecorder?.release()
            } catch (_: Exception) { }
            mediaRecorder = null
            outputFile?.delete()
            outputFile = null
        }
    }

    fun stopRecording(): File? {
        return try {
            mediaRecorder?.apply {
                stop()
                reset()
                release()
            }
            mediaRecorder = null

            val path = outputFile?.absolutePath
            val size = outputFile?.length() ?: -1L

            Log.d(TAG, "🎙 stopRecording 성공: path=$path, size=${size} bytes")
            Log.d(TAG, "🎙 stopRecording 성공: ${outputFile?.absolutePath}")
            outputFile
        } catch (e: Exception) {
            Log.e(TAG, "stopRecording 실패", e)
            try {
                mediaRecorder?.release()
            } catch (_: Exception) { }
            mediaRecorder = null
            outputFile = null
            null
        }
    }
}
