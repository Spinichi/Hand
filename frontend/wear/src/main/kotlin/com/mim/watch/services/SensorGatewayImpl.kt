package com.mim.watch.services
// 상위에서 쓰는 파사드
import android.content.Context
import com.mim.watch.core.measurement.SensorSample
import com.mim.watch.core.measurement.StressScore
import com.mim.watch.repo.BaselineRepository

/**
 * 상위(예: MainActivity or HealthDebugManager)가 이 클래스만 호출하면
 * Baseline 로드 + 점수 계산을 한 번에 수행.
 */
class SensorGatewayImpl(private val context: Context) {

    // Baseline 저장소(필요 시 초기화). Lazy로 필요할 때 생성.
    private val baselineRepo by lazy { BaselineRepository(context) }

    // 1회 샘플을 처리해 결과 반환(저장/알림 등은 여기서 확장 가능)
    suspend fun processRealtimeSample(sample: SensorSample): StressScore.Result {
        val baseline = baselineRepo.active()    // 활성 Baseline 코드
        return StressScore.score(sample, baseline)  // 점수 계산 후 결과 반환
    }
}
