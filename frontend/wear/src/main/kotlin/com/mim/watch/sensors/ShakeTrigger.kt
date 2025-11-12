//


// 패키지 경로, sensors와 관련된 코드들 가져오기
package com.mim.watch.sensors

// 안드로이드 센서 API 가져오기
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.abs
import kotlin.math.sqrt

// 싱클톤 오브젝트 선언
// SensorEventListener를 구현해서 센서 값이 바뀔 때, 콜백 받아오는 역할
object WristShakeTrigger : SensorEventListener {

    // 설정값(튜닝 파라미터)
    data class Config(
        val accelThreshold: Float = 12.0f, // 가속도 크기 임계값(중력 제거 후): 이 값이 크게 흔들려야 흔들었다고 판한함
        val minAlternations: Int = 3, // 방향 전환 횟수, 최소치 3 "좌-우-좌" 처럼 왕복 1.5회 필요
        val windowMs: Long = 600L, // 위 전환들이 얼마나 짧은 시간 안에 일어나야 하는지(밀리초), 흔들림이 짧고 빠르게 일어나야 인식 가능
        val cooldownMs: Long = 2000L, // 트리거가 한 번 발동한 후, 재인식까지 쉬는 시간 (연속 오인식 방지)
        // 회전 속도 보조 신호로 사용 useGyroAssist, gyroThreshold
        val useGyroAssist: Boolean = true,
        val gyroThreshold: Float = 2.0f,  // 회전이 이 정도 이상이면 흔든 것 같다의 기준
        val signThreshold: Float = 0.5f // 방향 전환 셀 때, 축의 부호(+, -)를 판단하는 민감도, 값이 낮을 수록 작은 방향 변환도 전환으로 인정해줌
    )

    // 내부 상태 변수들
    // 센서 매니저 / 가속도 / 자이로 핸들, 트리거가 발생했을 때 호출할 콜백 저장, 현재 설정값 보관
    private var sensorManager: SensorManager? = null
    private var accel: Sensor? = null
    private var gyro: Sensor? = null
    private var onTrigger: (() -> Unit)? = null
    private var config = Config()

    // 로우패스 + 하이패스 계산용 버퍼
    // -> alpha는 필터 계수: 중력 성분을 천천히 추적해 빼내는 데 사용
    private var lastAccel = FloatArray(3)
    private var initialized = false
    private val alpha = 0.8f

    // lastSign: 직전 방향(+1/-1/0) alternations: 전환 횟수 누적 windowStartMs: 지금 이 창 시작 시간(기억용)
    private var lastSign: Int = 0
    private var alternations = 0
    private var windowStartMs = 0L

    // 최근 자이로 스파크 시간 (회전 감지)
    private var gyroSpikeMs = 0L
    // 최근 트리거 발생 시간 (쿨다운 체크용)
    private var lastTriggerMs = 0L

    // 시작 / 중지 API
    // 시작 전에 혹시 등록된 리스너가 있다? stop()으로 정리 -> 중복 등록 방지
    // 최신 설정과 트리거 콜백 저장
    fun start(context: Context, config: Config = Config(), onTrigger: () -> Unit) {
        stop()
        this.config = config
        this.onTrigger = onTrigger

        // 시스템에서 센서 매니저를 가져오고, 가속도/자이로 센서 얻음
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accel = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyro  = sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        // 가속도(필수)와 자이로(옵션) 리스너 등록 -> SENSOR_DELAY_GAME은 샘플링 속도/배터리의 균형
        // 더 민감하게 하고싶으면 FASTTEST 사용해보기??
        accel?.let { sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
        if (config.useGyroAssist) {
            gyro?.let { sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
        }
        // 새 윈도우 시작 (전환 카운트 리셋)
        resetWindow()
    }

    // 센서 구독 해제, 핸들/콜백 정리, 초기화 플래그 리셋
    fun stop() {
        sensorManager?.unregisterListener(this)
        sensorManager = null
        onTrigger = null
        initialized = false
    }

    // 센서 콜백(핵심 로직임!!)
    // 쿨다운 중이면 아무것도 안함
    // 쿨다운 중 가속도 샘플이 왔을 때, initialized=false로 해두는 건, 다시 시작할 때 첫 프레임을 초기값으로 삼기 위함임
    override fun onSensorChanged(event: android.hardware.SensorEvent) {
        val now = System.currentTimeMillis()
        if (now - lastTriggerMs < config.cooldownMs) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) initialized = false
            return
        }

        // 자이로/가속도 각각의 처리로 분기
        when (event.sensor.type) {
            Sensor.TYPE_GYROSCOPE -> {
                // 자이로 처리
                // 회전 속도 벡터의 크리를 계산해서 임계치를 넘으면 "최근 회전이 있었음"으로 표시
                // 나중에 가속도 판단할 때, "회전이 최근 150ms 안에 있었는지"를 위한 보조판단
                if (!config.useGyroAssist) return
                val wx = event.values[0]; val wy = event.values[1]; val wz = event.values[2]
                val wMag = sqrt(wx*wx + wy*wy + wz*wz)
                if (wMag > config.gyroThreshold) gyroSpikeMs = now
            }
            Sensor.TYPE_ACCELEROMETER -> {
                // 가속도 처리
                // 첫 프레임이 필터 기준점 세팅 후 종류(다음 프레임부터 정상 계산)
                val ax = event.values[0]; val ay = event.values[1]; val az = event.values[2]
                if (!initialized) {
                    lastAccel[0] = ax; lastAccel[1] = ay; lastAccel[2] = az
                    initialized = true; resetWindow(); return
                }
                // 로우패스 필터로 lastAccel 업데이트
                // lastAccel은 천천히 변하는 성분(대체로 중력)을 따라감
                lastAccel[0] = alpha * lastAccel[0] + (1 - alpha) * ax
                lastAccel[1] = alpha * lastAccel[1] + (1 - alpha) * ay
                lastAccel[2] = alpha * lastAccel[2] + (1 - alpha) * az

                // 하이패스(중력 제거): 실제 흔들림(빠르게 변하는 성분)을 얻음
                // linMag = 흔들림의 순가속 크기
                val lx = ax - lastAccel[0]; val ly = ay - lastAccel[1]; val lz = az - lastAccel[2]
                val linMag = sqrt(lx*lx + ly*ly + lz*lz)
                // x, y, z 중 가장 크게 흔들린 축의 값 선택("지배축")
                val dominant = when {
                    abs(lx) >= abs(ly) && abs(lx) >= abs(lz) -> lx
                    abs(ly) >= abs(lx) && abs(ly) >= abs(lz) -> ly
                    else -> lz
                }

                // 지배축의 값이 +t 보다 크면 + 방향, -t 보다 작으면 - 방향, 그 사이면 0 (무시!!)
                val t = config.signThreshold
                val sign = when {
                    dominant >  t -> 1
                    dominant < -t -> -1
                    else -> 0
                }

                // 윈도우 시간이 지나면 전환 카운트 초기화 후 새 창 시작
                // 자이로 보조를 쓰면, 최근 150ms 내에 회전 스파이크가 있어야 "흔들림 인정"
                if (now - windowStartMs > config.windowMs) resetWindow()
                val gyroOk = !config.useGyroAssist || (now - gyroSpikeMs <= 150)
                // 충분히 큰 흔들림(linMag 임계 초과) & (자이로 조건 충족) 일 때만 밑의 로직 진행
                // 부호가 바뀌면, 전환 1회 추가 (이전 부호가 0이 아니고, 현재 부호도 0이 아니며, 서로 반대일 때만)
                // 전환 수가 minAlternations 이상이면 -> 트리거 발동
                // 1. 마지막 트리거 시간 저장(쿨다운 시작)
                // 2. 윈도우 리셋(다음 인식을 위해)
                // 3. onTrigger?.involve() 로 콜백 실행(진동, UI 표시)
                if (linMag > config.accelThreshold && gyroOk) {
                    if (sign != 0 && lastSign != 0 && sign != lastSign) alternations += 1
                    if (sign != 0) lastSign = sign
                    if (alternations >= config.minAlternations) {
                        lastTriggerMs = now
                        resetWindow()
                        onTrigger?.invoke()
                    }
                }
            }
        }
    }

    // 정확도 콜백/윈도우 리셋
    // 필요 없어서 비운 콜백
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // 새 윈도우 시작: 전환 수/부호 초기화
    private fun resetWindow() {
        windowStartMs = System.currentTimeMillis()
        alternations = 0
        lastSign = 0
    }
}
