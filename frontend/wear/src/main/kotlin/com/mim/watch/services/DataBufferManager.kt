package com.mim.watch.services

import com.mim.watch.data.model.BioSample
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * 10초 버퍼링 매니저
 * - 센서 샘플을 버퍼에 모음
 * - 10개(=10초) 모이면 결측치 제외하고 평균 계산하여 대표값 1개 반환
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
     * @return 10개 모였으면 대표 BioSample 1개 반환, 아니면 null
     */
    @Synchronized
    fun addSample(sample: BioSample): BioSample? {
        buffer.add(sample)

        // 10개 모였는지 확인
        if (buffer.size >= BUFFER_SIZE) {
            return createRepresentativeSample()
        }

        return null
    }

    /**
     * 버퍼의 10개 샘플로부터 대표 샘플 1개 생성 (결측치 제외하고 평균)
     */
    @Synchronized
    private fun createRepresentativeSample(): BioSample? {
        if (buffer.isEmpty()) return null

        // 10개 샘플 수집
        val samples = mutableListOf<BioSample>()
        while (samples.size < BUFFER_SIZE && buffer.isNotEmpty()) {
            buffer.poll()?.let { samples.add(it) }
        }

        if (samples.isEmpty()) return null

        // 각 필드별로 null 제외하고 평균 계산
        val validHeartRates = samples.mapNotNull { it.heartRate }
        val validHrvSdnn = samples.mapNotNull { it.hrvSdnn }
        val validHrvRmssd = samples.mapNotNull { it.hrvRmssd }
        val validObjectTemp = samples.mapNotNull { it.objectTemp }
        val validAmbientTemp = samples.mapNotNull { it.ambientTemp }
        val validAccelX = samples.mapNotNull { it.accelX }
        val validAccelY = samples.mapNotNull { it.accelY }
        val validAccelZ = samples.mapNotNull { it.accelZ }
        val validMovementIntensity = samples.mapNotNull { it.movementIntensity }
        val validStressIndex = samples.mapNotNull { it.stressIndex }
        val validStressLevel = samples.mapNotNull { it.stressLevel }
        val validTotalSteps = samples.mapNotNull { it.totalSteps }
        val validStepsPerMinute = samples.mapNotNull { it.stepsPerMinute }

        // ⭐ 이상치 판정: 원본 10개 샘플 중 stressLevel 4 이상이 6개 이상인지 체크
        val highStressCount = validStressLevel.count { it >= 4 }
        val isAnomaly = highStressCount >= 6

        // 대표 샘플 생성 (평균값 사용, 최신 타임스탬프)
        return BioSample(
            timestampMs = System.currentTimeMillis(),
            heartRate = validHeartRates.takeIf { it.isNotEmpty() }?.average()?.toFloat(),
            hrvSdnn = validHrvSdnn.takeIf { it.isNotEmpty() }?.average(),
            hrvRmssd = validHrvRmssd.takeIf { it.isNotEmpty() }?.average(),
            objectTemp = validObjectTemp.takeIf { it.isNotEmpty() }?.average()?.toFloat(),
            ambientTemp = validAmbientTemp.takeIf { it.isNotEmpty() }?.average()?.toFloat(),
            accelX = validAccelX.takeIf { it.isNotEmpty() }?.average()?.toFloat(),
            accelY = validAccelY.takeIf { it.isNotEmpty() }?.average()?.toFloat(),
            accelZ = validAccelZ.takeIf { it.isNotEmpty() }?.average()?.toFloat(),
            movementIntensity = validMovementIntensity.takeIf { it.isNotEmpty() }?.average()?.toFloat(),
            stressIndex = validStressIndex.takeIf { it.isNotEmpty() }?.average(),
            stressLevel = validStressLevel.takeIf { it.isNotEmpty() }?.average()?.toInt(),
            totalSteps = validTotalSteps.takeIf { it.isNotEmpty() }?.maxOrNull(),  // 걸음수는 최댓값 사용
            stepsPerMinute = validStepsPerMinute.takeIf { it.isNotEmpty() }?.average()?.toInt(),
            isAnomaly = isAnomaly  // 원본 10개 샘플 기준으로 판정
        )
    }

    /**
     * 현재 버퍼 사이즈
     */
    fun currentSize(): Int = buffer.size
}