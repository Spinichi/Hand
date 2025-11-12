package com.mim.watch.healthdebug

import android.app.Activity
import android.content.Context
import android.util.Log
import com.samsung.android.service.health.tracking.ConnectionListener
import com.samsung.android.service.health.tracking.HealthTracker
import com.samsung.android.service.health.tracking.HealthTrackerException
import com.samsung.android.service.health.tracking.HealthTrackingService
import com.samsung.android.service.health.tracking.data.DataPoint
import com.samsung.android.service.health.tracking.data.ValueKey
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs
import kotlin.math.sqrt

object HealthDebugManager {

    private const val TAG = "HealthDebug"

    @Volatile
    private var service: HealthTrackingService? = null

    // Samsung Health Platform 권한/동의 UI를 띄울 때 사용할 Activity (서비스 모드에서는 null 가능)
    @Volatile
    private var uiActivity: Activity? = null

    // 현재 listener 달아둔 tracker 들 (stopAll() 때 해제용)
    private val activeTrackers = ConcurrentHashMap<String, HealthTracker>()

    // UI에 보여줄 최근 값(디버그용): "트래커명 -> 사람이 읽을 문자열"
    @Volatile
    var latestByTracker: Map<String, String> = emptyMap()
        private set
    private val latestMutable = ConcurrentHashMap<String, String>()

    // ★ 서비스가 실시간 샘플을 받아 전송하기 위한 콜백
    @Volatile
    var onSample: ((tsMs: Long, hrBpm: Float?, ibiMs: Float?, movement: Float?, extras: Map<String, Any?>) -> Unit)? = null

    // 가속도 → movement(0~1) 계산용 상태(EMA)
    @Volatile
    private var movementEma: Float? = null

    // 각 센서별 최신 데이터 병합용 (모든 센서 데이터를 한 곳에 모음)
    private val latestSensorDataInternal = ConcurrentHashMap<String, Any?>()

    // 외부에서 읽기 전용으로 접근 가능
    fun getLatestSensorData(): Map<String, Any?> = HashMap(latestSensorDataInternal)

    // 안전하게 trackerType 이름 뽑는 헬퍼
    private fun itName(typeObj: Any): String = try {
        val m = typeObj.javaClass.getMethod("name")
        val v = m.invoke(typeObj)
        v?.toString() ?: typeObj.toString()
    } catch (_: Throwable) {
        typeObj.toString()
    }

    /**
     * Samsung HealthTrackingService 연결
     * - activity 는 null 가능 (서비스 모드)
     */
    fun connect(context: Context, activity: Activity? = null) {
        if (service != null) {
            Log.d(TAG, "connect(): service already exists, skip")
            return
        }

        uiActivity = activity
        Log.d(TAG, "connect(): creating HealthTrackingService")

        val listener = object : ConnectionListener {
            override fun onConnectionSuccess() {
                Log.d(TAG, "onConnectionSuccess() ✅")
                // HR/IBI 스트리밍
                startHeartRateTracker()
                // 가속도 스트리밍 (화면 꺼져도 동작)
                startAccelerometerTracker()
                // (옵션) 전체 디스커버리
                startAllTrackersForDiscovery()
            }

            override fun onConnectionFailed(e: HealthTrackerException) {
                Log.e(TAG, "onConnectionFailed() code=${e.errorCode} hasResolution=${e.hasResolution()}", e)
                if (e.hasResolution()) {
                    uiActivity?.let { act ->
                        try {
                            e.resolve(act)
                            Log.d(TAG, "called e.resolve() -> launched Samsung Health setup UI")
                        } catch (t: Throwable) {
                            Log.e(TAG, "resolve() call failed: $t", t)
                        }
                    } ?: Log.e(TAG, "No activity available for resolve()")
                }
                try { service?.disconnectService() } catch (_: Throwable) {}
                service = null
            }

            override fun onConnectionEnded() {
                Log.d(TAG, "onConnectionEnded() ❌")
                service = null
            }
        }

        service = HealthTrackingService(listener, context)
        try {
            Log.d(TAG, "connect(): calling connectService()")
            service?.connectService()
        } catch (t: Throwable) {
            Log.e(TAG, "connectService() threw $t", t)
            try { service?.disconnectService() } catch (_: Throwable) {}
            service = null
        }
    }

    /**
     * 서비스에서 편하게 부르도록 공개 start 래퍼
     */
    fun startAllTrackers() {
        startHeartRateTracker()
        startAccelerometerTracker()
        startAllTrackersForDiscovery()
    }

    /**
     * 심박/IBI 트래커
     * 우선 HEART_RATE, 불가하면 CONTINUOUS/기타 HEART*
     */
    private fun startHeartRateTracker() {
        val s = service ?: return

        val capability = s.trackingCapability
        val supported = capability.supportHealthTrackerTypes
        Log.d(TAG, "startHeartRateTracker(): supported trackers = $supported")

        val hrType =
            supported.find { itName(it).equals("HEART_RATE", ignoreCase = true) && !itName(it).contains("CONTINUOUS", true) }
                ?: supported.find { itName(it).contains("HEART_RATE_CONTINUOUS", true) }
                ?: supported.find { itName(it).contains("HEART", true) }

        if (hrType == null) {
            Log.e(TAG, "no HEART-like tracker found on this device")
            return
        }

        val trackerName = itName(hrType)
        Log.d(TAG, "chosen heart tracker = $trackerName")

        try {
            val tracker = s.getHealthTracker(hrType)
            tracker.setEventListener(object : HealthTracker.TrackerEventListener {

                override fun onDataReceived(dataPoints: List<DataPoint>) {
                    val latestPoint = dataPoints.lastOrNull() ?: return

                    val sb = StringBuilder().apply {
                        append("TS: ").append(latestPoint.timestamp).append("\n")
                    }

                    // 사람이 읽는 로그/화면용
                    fun <T : Any> grab(key: ValueKey<T>, label: String) {
                        try {
                            val v = latestPoint.getValue(key)
                            if (v != null) sb.append(label).append(": ").append(v.toString()).append("\n")
                        } catch (e: Throwable) {
                            Log.d(TAG, "[$trackerName] Failed to get $label: ${e.message}")
                        }
                    }

                    grab(ValueKey.HeartRateSet.HEART_RATE,        "HR")
                    grab(ValueKey.HeartRateSet.IBI_LIST,          "IBI(ms)")
                    grab(ValueKey.HeartRateSet.HEART_RATE_STATUS, "HR_Q")
                    grab(ValueKey.HeartRateSet.IBI_STATUS_LIST,   "IBI_Q")

                    // 화면용 최신값
                    latestMutable[trackerName] = sb.toString()
                    latestByTracker = HashMap(latestMutable)
                    Log.d(TAG, "[$trackerName] update:\n$sb")

                    // ★ CSV용 실데이터 추출 → onSample 콜백 (대체 계산 포함)
                    val tsMs: Long = latestPoint.timestamp

                    // 1) 원시 HR
                    val hrRaw: Float? = try {
                        (latestPoint.getValue(ValueKey.HeartRateSet.HEART_RATE) as? Number)?.toFloat()
                    } catch (_: Throwable) { null }

                    // 2) IBI 리스트의 마지막 하나(ms 가정)
                    val ibiMs: Float? = try {
                        when (val any = latestPoint.getValue(ValueKey.HeartRateSet.IBI_LIST)) {
                            is FloatArray  -> any.lastOrNull()?.toFloat()
                            is DoubleArray -> any.lastOrNull()?.toFloat()
                            is IntArray    -> any.lastOrNull()?.toFloat()
                            is LongArray   -> any.lastOrNull()?.toFloat()
                            is List<*>     -> (any.lastOrNull() as? Number)?.toFloat()
                            is Number      -> any.toFloat()
                            else           -> null
                        }
                    } catch (_: Throwable) { null }

                    // 3) 품질값 읽기 (0=좋음, 그 외=불량으로 가정)
                    var hrQInt: Int? = null
                    try {
                        val v = latestPoint.getValue(ValueKey.HeartRateSet.HEART_RATE_STATUS)
                        if (v is Number) hrQInt = v.toInt() else if (v != null) {
                            val s = v.toString()
                            try { hrQInt = s.toInt() } catch (_: Throwable) {}
                        }
                    } catch (_: Throwable) { }

                    var ibiQInt: Int? = null
                    try {
                        val v = latestPoint.getValue(ValueKey.HeartRateSet.IBI_STATUS_LIST)
                        when (v) {
                            is IntArray    -> ibiQInt = v.lastOrNull()
                            is LongArray   -> ibiQInt = v.lastOrNull()?.toInt()
                            is FloatArray  -> ibiQInt = v.lastOrNull()?.toInt()
                            is DoubleArray -> ibiQInt = v.lastOrNull()?.toInt()
                            is List<*>     -> {
                                val last = v.lastOrNull()
                                if (last is Number) ibiQInt = last.toInt() else if (last != null) {
                                    try { ibiQInt = last.toString().toInt() } catch (_: Throwable) {}
                                }
                            }
                            is Number      -> ibiQInt = v.toInt()
                            else -> { }
                        }
                    } catch (_: Throwable) { }

                    // 4) 최종 HR 결정 (규칙: HR 없거나 품질 나쁘면 IBI로 대체, IBI도 나쁘면 비움)
                    var hrFilled: Float? = null
                    var hrSrc = "HTS"

                    val isHrValid = (hrRaw != null && hrRaw > 0f)
                    val isIbiUsable = (ibiMs != null && ibiMs > 0f)
                    val isHrQualityBad = (hrQInt != null && hrQInt != 0)
                    val isIbiQualityBad = (ibiQInt != null && ibiQInt != 0)

                    if (isHrValid && !isHrQualityBad) {
                        hrFilled = hrRaw
                        hrSrc = "HTS"
                    } else {
                        if (isIbiUsable && !isIbiQualityBad) {
                            // HR = 60000 / IBI(ms)
                            hrFilled = 60000f / ibiMs!!
                            hrSrc = "IBI_FALLBACK"
                        } else {
                            hrFilled = null
                            hrSrc = "NONE"
                        }
                    }

                    // (선택) 이상치 필터
                    // if (hrFilled != null) {
                    //     if (hrFilled < 25f || hrFilled > 220f) {
                    //         hrFilled = null
                    //         hrSrc = "NONE"
                    //     }
                    // }

                    // ⭐ Heart Rate 콜백에서도 최신 센서 데이터를 latestSensorDataInternal에 저장
                    if (hrFilled != null) latestSensorDataInternal["heartRate"] = hrFilled
                    if (ibiMs != null) latestSensorDataInternal["ibi"] = ibiMs

                    // ⭐ extras에 모든 최신 센서 데이터 포함 (병합, 순서 중요)
                    val extras = buildMap<String, Any?> {
                        // 1. 먼저 저장된 모든 센서 데이터 추가
                        putAll(latestSensorDataInternal)

                        // 2. 현재 Heart Rate 메타데이터로 덮어쓰기
                        put("tracker", trackerName)
                        put("src", "HTS")
                        put("hr_src", hrSrc)
                        if (hrQInt != null) put("hr_q", hrQInt)
                        if (ibiQInt != null) put("ibi_q", ibiQInt)
                    }

                    // movement는 가속도 트래커에서 별도로 계산하여 주입
                    onSample?.invoke(tsMs, hrFilled, ibiMs, /*movement*/ null, extras)
                }

                override fun onFlushCompleted() { /* optional */ }

                override fun onError(error: HealthTracker.TrackerError) {
                    Log.e(TAG, "[$trackerName] error=$error")
                    latestMutable[trackerName] = "ERROR: $error"
                    latestByTracker = HashMap(latestMutable)
                }
            })

            activeTrackers[trackerName] = tracker
            Log.d(TAG, "listener attached to $trackerName (heart)")

            // flush로 즉시 데이터 수집 트리거
            try {
                Log.d(TAG, "Calling flush() to start measurement for $trackerName")
                tracker.flush()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to flush $trackerName: $e", e)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to init heart tracker $trackerName : $e", e)
            latestMutable[trackerName] = "INIT_FAIL: ${e.message}"
            latestByTracker = HashMap(latestMutable)
        }
    }

    /**
     * 가속도 트래커 (movement 0~1 산출)
     * ACC X/Y/Z → |sqrt(x^2+y^2+z^2) - g| → /6.0 → [0,1] → EMA(α=0.2)
     */
    private fun startAccelerometerTracker() {
        val s = service ?: return

        val capability = s.trackingCapability
        val supported = capability.supportHealthTrackerTypes
        Log.d(TAG, "startAccelerometerTracker(): supported trackers = $supported")

        val accelType = supported.find { itName(it).contains("ACCELEROMETER", true) }
        if (accelType == null) {
            Log.e(TAG, "no ACCELEROMETER tracker found on this device")
            return
        }

        val trackerName = itName(accelType)
        // 이미 활성화면 스킵
        if (activeTrackers.containsKey(trackerName)) {
            Log.d(TAG, "accelerometer $trackerName already active, skip")
            return
        }

        try {
            val tracker = s.getHealthTracker(accelType)
            tracker.setEventListener(object : HealthTracker.TrackerEventListener {
                override fun onDataReceived(dataPoints: List<DataPoint>) {
                    val latestPoint = dataPoints.lastOrNull() ?: return

                    // 값 추출
                    var ax: Float? = null
                    var ay: Float? = null
                    var az: Float? = null
                    try { ax = (latestPoint.getValue(ValueKey.AccelerometerSet.ACCELEROMETER_X) as? Number)?.toFloat() } catch (_: Throwable) {}
                    try { ay = (latestPoint.getValue(ValueKey.AccelerometerSet.ACCELEROMETER_Y) as? Number)?.toFloat() } catch (_: Throwable) {}
                    try { az = (latestPoint.getValue(ValueKey.AccelerometerSet.ACCELEROMETER_Z) as? Number)?.toFloat() } catch (_: Throwable) {}

                    if (ax == null || ay == null || az == null) return

                    // movement 계산
                    val g = 9.80665f
                    val mag = sqrt(ax * ax + ay * ay + az * az)
                    val dyn = abs(mag - g)                   // 동적 성분(m/s^2)
                    val cap = 6.0f                           // 0~6 m/s^2 범위로 정규화 (활동시 충분)
                    var mv = dyn / cap
                    if (mv < 0f) mv = 0f
                    if (mv > 1f) mv = 1f

                    // EMA 평활(α=0.2)
                    val alpha = 0.2f
                    movementEma = if (movementEma == null) mv else (alpha * mv + (1 - alpha) * movementEma!!)

                    // 화면용 로그
                    val sb = StringBuilder().apply {
                        append("TS: ").append(latestPoint.timestamp).append("\n")
                        append("ACCEL_X: ").append(ax).append("\n")
                        append("ACCEL_Y: ").append(ay).append("\n")
                        append("ACCEL_Z: ").append(az).append("\n")
                        append("MAG: ").append(mag).append("\n")
                        append("movement(0-1): ").append(String.format("%.3f", movementEma))
                    }
                    latestMutable[trackerName] = sb.toString()
                    latestByTracker = HashMap(latestMutable)
                    Log.d(TAG, "[$trackerName] update:\n$sb")

                    // ⭐ 가속도 데이터를 latestSensorDataInternal에 저장
                    latestSensorDataInternal["accelX"] = ax
                    latestSensorDataInternal["accelY"] = ay
                    latestSensorDataInternal["accelZ"] = az
                    latestSensorDataInternal["movementIntensity"] = movementEma

                    Log.d(TAG, "⭐ ACCEL updated: X=$ax, Y=$ay, Z=$az, movement=$movementEma")

                    // ⭐ extras에 모든 최신 센서 데이터 병합 (순서 중요: putAll 먼저, 현재 값으로 덮어쓰기)
                    val extras = buildMap<String, Any?> {
                        // 1. 먼저 저장된 모든 센서 데이터 추가
                        putAll(latestSensorDataInternal)

                        // 2. 현재 가속도 값으로 덮어쓰기 (최신 값 보장)
                        put("tracker", trackerName)
                        put("src", "HTS")
                        put("accelX", ax)
                        put("accelY", ay)
                        put("accelZ", az)
                        put("movementIntensity", movementEma)
                    }
                    onSample?.invoke(latestPoint.timestamp, null, null, movementEma, extras)
                }

                override fun onFlushCompleted() { /* optional */ }

                override fun onError(error: HealthTracker.TrackerError) {
                    Log.e(TAG, "[$trackerName] error=$error")
                    latestMutable[trackerName] = "ERROR: $error"
                    latestByTracker = HashMap(latestMutable)
                }
            })

            activeTrackers[trackerName] = tracker
            Log.d(TAG, "listener attached to $trackerName (accelerometer)")

            try {
                Log.d(TAG, "Calling flush() to start measurement for $trackerName")
                tracker.flush()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to flush $trackerName: $e", e)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to init accelerometer tracker $trackerName : $e", e)
            latestMutable[trackerName] = "INIT_FAIL: ${e.message}"
            latestByTracker = HashMap(latestMutable)
        }
    }

    /**
     * 전체 디스커버리 (옵션)
     * 너무 시끄러워질 수 있으니, 필요할 때만 켬
     */
    private fun startAllTrackersForDiscovery() {
        val s = service ?: return

        val capability = s.trackingCapability
        val supported = capability.supportHealthTrackerTypes
        Log.d(TAG, "startAllTrackersForDiscovery(): supported = $supported")

        supported.forEach { trackerType ->
            val trackerName = itName(trackerType)

            if (activeTrackers.containsKey(trackerName)) {
                Log.d(TAG, "discovery: $trackerName already active, skip")
                return@forEach
            }
            if (trackerName.contains("PPG", true)) {
                Log.d(TAG, "discovery: skipping $trackerName (too verbose)")
                return@forEach
            }
            if (trackerName.contains("HEART_RATE_CONTINUOUS", true)) {
                Log.d(TAG, "discovery: skipping $trackerName (doesn't work properly)")
                return@forEach
            }
            if (trackerName.contains("ACCELEROMETER", true)) {
                Log.d(TAG, "discovery: skipping $trackerName (already handled by main tracker)")
                return@forEach
            }

            try {
                val tracker = s.getHealthTracker(trackerType)
                tracker.setEventListener(object : HealthTracker.TrackerEventListener {
                    override fun onDataReceived(dataPoints: List<DataPoint>) {
                        val latestPoint = dataPoints.lastOrNull() ?: return
                        val sb = StringBuilder().apply {
                            append("TS: ").append(latestPoint.timestamp).append("\n")
                        }

                        fun <T : Any> grab(key: ValueKey<T>, label: String) {
                            try {
                                val v = latestPoint.getValue(key)
                                if (v != null) sb.append(label).append(": ").append(v.toString()).append("\n")
                            } catch (_: Throwable) { /* 없는 키면 무시 */ }
                        }

                        // Heart / HRV
                        grab(ValueKey.HeartRateSet.HEART_RATE,        "HR")
                        grab(ValueKey.HeartRateSet.IBI_LIST,          "IBI(ms)")
                        grab(ValueKey.HeartRateSet.HEART_RATE_STATUS, "HR_Q")
                        grab(ValueKey.HeartRateSet.IBI_STATUS_LIST,   "IBI_Q")

                        // Skin temperature
                        grab(ValueKey.SkinTemperatureSet.AMBIENT_TEMPERATURE, "AmbTemp(C)")
                        grab(ValueKey.SkinTemperatureSet.OBJECT_TEMPERATURE,  "ObjTemp(C)")
                        grab(ValueKey.SkinTemperatureSet.STATUS,              "Temp_Q")

                        // Accelerometer
                        grab(ValueKey.AccelerometerSet.ACCELEROMETER_X, "ACCEL_X")
                        grab(ValueKey.AccelerometerSet.ACCELEROMETER_Y, "ACCEL_Y")
                        grab(ValueKey.AccelerometerSet.ACCELEROMETER_Z, "ACCEL_Z")

                        // ECG
                        grab(ValueKey.EcgSet.ECG_MV,           "ECG(mV)")
                        grab(ValueKey.EcgSet.SEQUENCE,         "ECG_SEQ")
                        grab(ValueKey.EcgSet.MAX_THRESHOLD_MV, "ECG_MAX")
                        grab(ValueKey.EcgSet.MIN_THRESHOLD_MV, "ECG_MIN")
                        grab(ValueKey.EcgSet.LEAD_OFF,         "ECG_LEAD_OFF")

                        latestMutable["DISCOVERY:$trackerName"] = sb.toString()
                        latestByTracker = HashMap(latestMutable)
                        Log.d(TAG, "[DISCOVERY:$trackerName] update:\n$sb")

                        // ⭐ 온도 데이터를 저장 및 콜백 전달 (SKIN_TEMPERATURE 트래커만)
                        if (trackerName.contains("SKIN_TEMPERATURE", ignoreCase = true)) {
                            var objTemp: Float? = null
                            var ambTemp: Float? = null
                            try {
                                objTemp = (latestPoint.getValue(ValueKey.SkinTemperatureSet.OBJECT_TEMPERATURE) as? Number)?.toFloat()
                            } catch (_: Throwable) {}
                            try {
                                ambTemp = (latestPoint.getValue(ValueKey.SkinTemperatureSet.AMBIENT_TEMPERATURE) as? Number)?.toFloat()
                            } catch (_: Throwable) {}

                            // latestSensorDataInternal에 저장
                            latestSensorDataInternal["objectTemp"] = objTemp
                            latestSensorDataInternal["ambientTemp"] = ambTemp

                            // ⭐ extras에 모든 최신 센서 데이터 병합 (순서 중요)
                            val extras = buildMap<String, Any?> {
                                // 1. 먼저 저장된 모든 센서 데이터 추가
                                putAll(latestSensorDataInternal)

                                // 2. 현재 온도 값으로 덮어쓰기 (최신 값 보장)
                                put("tracker", trackerName)
                                put("src", "HTS")
                                put("objectTemp", objTemp)
                                put("ambientTemp", ambTemp)
                            }
                            onSample?.invoke(latestPoint.timestamp, null, null, null, extras)
                        }
                    }

                    override fun onFlushCompleted() { /* optional */ }

                    override fun onError(error: HealthTracker.TrackerError) {
                        Log.e(TAG, "[DISCOVERY:$trackerName] error=$error")
                        latestMutable["DISCOVERY:$trackerName"] = "ERROR: $error"
                        latestByTracker = HashMap(latestMutable)
                    }
                })

                activeTrackers[trackerName] = tracker
                Log.d(TAG, "discovery: listener attached to $trackerName")
            } catch (e: Exception) {
                Log.e(TAG, "discovery: Failed to init tracker $trackerName : $e", e)
                latestMutable["DISCOVERY:$trackerName"] = "INIT_FAIL: ${e.message}"
                latestByTracker = HashMap(latestMutable)
            }
        }
    }

    /**
     * 정리
     */
    fun stopAll() {
        Log.d(TAG, "stopAll()")

        activeTrackers.forEach { (name, tracker) ->
            try {
                tracker.unsetEventListener()
                Log.d(TAG, "unsetEventListener() for $name")
            } catch (t: Throwable) {
                Log.e(TAG, "unsetEventListener() error for $name : $t", t)
            }
        }
        activeTrackers.clear()

        try { service?.disconnectService() } catch (t: Throwable) {
            Log.e(TAG, "disconnectService() threw $t", t)
        }

        service = null
        uiActivity = null
        latestMutable.clear()
        latestByTracker = emptyMap()
        movementEma = null
    }
}
