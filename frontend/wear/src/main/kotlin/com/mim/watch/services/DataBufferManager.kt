package com.mim.watch.services

import com.mim.watch.data.model.BioSample
import com.mim.watch.data.model.BioSampleBatch
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * 10초 버퍼링 매니저
 * - 센서 샘플을 버퍼에 모음
 * - 10개(=10초) 모이면 배치로 반환
 */
object DataBufferManager {

    private val buffer = ConcurrentLinkedQueue<BioSample>()
    private const val BUFFER_SIZE = 10  // 10초

    @Volatile
    var deviceId: String = "unknown"
        private set

    /**
     * 디바이스 ID 설정 (한 번만 호출)
     */
    fun setDeviceId(id: String) {
        deviceId = id
    }

    /**
     * 샘플 추가
     * @return 10개 모였으면 BioSampleBatch 반환, 아니면 null
     */
    @Synchronized
    fun addSample(sample: BioSample): BioSampleBatch? {
        buffer.add(sample)

        // 10개 모였는지 확인
        if (buffer.size >= BUFFER_SIZE) {
            return flush()
        }

        return null
    }

    /**
     * 버퍼를 강제로 비우고 배치 생성
     */
    @Synchronized
    fun flush(): BioSampleBatch? {
        if (buffer.isEmpty()) return null

        val samples = mutableListOf<BioSample>()
        while (samples.size < BUFFER_SIZE && buffer.isNotEmpty()) {
            buffer.poll()?.let { samples.add(it) }
        }

        if (samples.isEmpty()) return null

        return BioSampleBatch(
            deviceId = deviceId,
            batchTimestamp = System.currentTimeMillis(),
            samples = samples
        )
    }

    /**
     * 현재 버퍼 사이즈
     */
    fun currentSize(): Int = buffer.size
}