package com.mim.watch.core.measurement

import android.content.Context
import java.io.File
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * 매초 만든 센서 샘플을 10분 동안 모아두는 초간단 레코더.
 * 기본은 메모리 버퍼만 사용하고, CSV 저장은 비활성화(no-op).
 */
object BioRecorder {

    data class Row(
        val tsMs: Long,
        val hrBpm: Double?,
        val ibiMsList: List<Double>?, // 예: [800.0, 810.0, ...]
        val objTempC: Double?,
        val totalSteps: Long?,
        val lastStepAtMs: Long?
    )

    private val buffer = mutableListOf<Row>()
    @Volatile var isRecording: Boolean = false
        private set
    @Volatile var startedAtMs: Long? = null
        private set

    private val dotSymbols = DecimalFormatSymbols(Locale.US)
    private val fmtHr    = DecimalFormat("0.0", dotSymbols)
    private val fmtIbi   = DecimalFormat("0", dotSymbols)
    private val fmtTemp  = DecimalFormat("0.000", dotSymbols)

    private fun isMissingHr(hr: Double?): Boolean {
        if (hr == null) return true
        if (!hr.isFinite()) return true
        if (hr <= 0.0) return true
        if (hr < 30.0 || hr > 230.0) return true
        return false
    }

    @Synchronized
    fun clear() {
        buffer.clear()
        startedAtMs = null
        isRecording = false
    }

    @Synchronized
    fun start() {
        buffer.clear()
        startedAtMs = System.currentTimeMillis()
        isRecording = true
    }

    @Synchronized
    fun stop() {
        isRecording = false
    }

    @Synchronized
    fun add(row: Row) {
        if (!isRecording) return
        buffer.add(row)
    }

    @Synchronized
    fun snapshot(): List<Row> = buffer.toList()

    /**
     * CSV 저장은 더 이상 사용하지 않음. (no-op)
     * 기존 호출부 호환을 위해 시그니처는 유지하되 항상 null을 반환.
     */
    @Deprecated("CSV 저장은 비활성화되었습니다. 네트워크 전송/메모리 버퍼만 사용하세요.")
    @Synchronized
    fun saveCsv(context: Context, fileNamePrefix: String = "bio_10min"): File? {
        return null
    }
}
