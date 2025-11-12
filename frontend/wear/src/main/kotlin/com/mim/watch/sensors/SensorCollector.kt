package com.mim.watch.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

/**
 * SensorCollector (steps-only)
 *
 * 수집 대상:
 *  - 누적 걸음 수 (TYPE_STEP_COUNTER)
 *  - 최근 한 걸음 시각 + 파생 카운트 (TYPE_STEP_DETECTOR)
 *
 * 다른 센서(HR/ACCEL 등)는 제거하고, 걸음 관련 값만 깔끔하게 유지했습니다.
 */
object SensorCollector {

    // 디바이스 부팅 이후 누적 걸음 수 (OS가 float로 제공)
    @Volatile
    var lastStepCount: Float? = null
        private set

    // 최근 "한 걸음 감지" 타임스탬프(ms)
    @Volatile
    var lastStepTimestampMs: Long? = null
        private set

    // TYPE_STEP_DETECTOR 이벤트를 기반으로 세는 파생 걸음 수(앱 실행 중 카운트)
    @Volatile
    var derivedStepCount: Int = 0
        private set

    // ⭐ 분당 걸음 수 계산용: 최근 1분간의 걸음 타임스탬프 저장
    private val stepTimestamps = mutableListOf<Long>()

    // 실행 상태
    @Volatile
    var isRunning: Boolean = false
        private set

    // 센서 존재 여부 플래그
    @Volatile
    var hasStepCounterSensor: Boolean = false
        private set

    @Volatile
    var hasStepDetectSensor: Boolean = false
        private set

    // 내부 핸들
    private var sensorManager: SensorManager? = null
    private var stepCounterListener: SensorEventListener? = null
    private var stepDetectListener: SensorEventListener? = null

    fun start(context: Context) {
        Log.d("SensorCollector", "start() steps-only")

        // 이미 돌고 있으면 중복 등록 방지
        if (stepCounterListener != null || stepDetectListener != null) {
            isRunning = true
            return
        }

        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // 1) 누적 걸음 센서
        val stepCounterSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        hasStepCounterSensor = stepCounterSensor != null

        if (stepCounterSensor == null) {
            Log.e("SensorCollector", "No STEP_COUNTER on this device")
        } else {
            stepCounterListener = object : SensorEventListener {
                override fun onSensorChanged(event: android.hardware.SensorEvent?) {
                    if (event == null || event.values.isEmpty()) return
                    val totalSteps = event.values[0]
                    lastStepCount = totalSteps
                    Log.d("SensorCollector", "STEP_COUNTER total=$totalSteps")
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }
            sensorManager?.registerListener(
                stepCounterListener,
                stepCounterSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            Log.d("SensorCollector", "STEP_COUNTER listener registered")
        }

        // 2) 걸음 감지 센서(이벤트 기반)
        val stepDetectSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        hasStepDetectSensor = stepDetectSensor != null

        if (stepDetectSensor == null) {
            Log.e("SensorCollector", "No STEP_DETECTOR on this device")
        } else {
            stepDetectListener = object : SensorEventListener {
                override fun onSensorChanged(event: android.hardware.SensorEvent?) {
                    if (event == null) return
                    val now = System.currentTimeMillis()
                    lastStepTimestampMs = now
                    derivedStepCount += 1

                    // ⭐ 분당 걸음 수 계산용: 타임스탬프 기록
                    synchronized(stepTimestamps) {
                        stepTimestamps.add(now)
                        // 1분(60초) 이전 데이터는 제거
                        stepTimestamps.removeAll { it < now - 60_000 }
                    }

                    Log.d(
                        "SensorCollector",
                        "STEP_DETECT at $now (derivedStepCount=$derivedStepCount, SPM=${getStepsPerMinute()})"
                    )
                }
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }
            sensorManager?.registerListener(
                stepDetectListener,
                stepDetectSensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
            Log.d("SensorCollector", "STEP_DETECTOR listener registered")
        }

        isRunning = true
    }

    /**
     * 분당 걸음 수(SPM) 반환
     * 최근 1분간 감지된 걸음 수
     */
    fun getStepsPerMinute(): Int {
        synchronized(stepTimestamps) {
            val now = System.currentTimeMillis()
            // 1분 이전 데이터 제거 (실시간 정확도 향상)
            stepTimestamps.removeAll { it < now - 60_000 }
            return stepTimestamps.size
        }
    }

    fun stop() {
        Log.d("SensorCollector", "stop() steps-only")
        stepCounterListener?.let { sensorManager?.unregisterListener(it) }
        stepDetectListener?.let { sensorManager?.unregisterListener(it) }
        stepCounterListener = null
        stepDetectListener = null
        sensorManager = null
        isRunning = false
        synchronized(stepTimestamps) {
            stepTimestamps.clear()
        }
    }
}
