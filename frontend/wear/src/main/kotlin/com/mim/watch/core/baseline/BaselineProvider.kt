package com.mim.watch.core.baseline
// 베이스라인 제공/갱신 담당

import android.content.Context
import com.mim.watch.data.local.BaselineDao
import com.mim.watch.data.local.BaselineEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 활성 Baseline을 제공/갱신.
 * - 없으면 부트스트랩(고정값) 사용
 */
class BaselineProvider(
    private val context: Context,   // (Room 초기화 등에서 사용 가능)
    private val dao: BaselineDao    // Baseline을 저장/조회하는 DAO
) {
    // 임시 부트스트랩(개인화 전 기본값)
    // 개인화 이전의 부트스트랩(고정) 값. std가 0이면 안 되므로 현실적인 값으로 설정.
    // ⚠️ 실제 측정값에 맞춰 조정 (IBI 데이터가 제한적인 환경)
    private val bootstrap = BaseLine(
        hrvSdnnMean = 15.0,   hrvSdnnStd = 10.0,   // 측정 SDNN: 2~30ms 범위
        hrvRmssdMean = 20.0,  hrvRmssdStd = 15.0,  // 측정 RMSSD: 3~43ms 범위
        hrMean = 95.0,        hrStd = 5.0,         // 측정 HR: 94~97 bpm
        objTempMean = 36.0,   objTempStd = 0.2     // 측정 Temp: 36.0~36.2°C
    )

    // 활성Baseline 반환(없으면 bootstrap)
    suspend fun getActiveBaseline(): BaseLine = withContext(Dispatchers.IO) {
        dao.getActive()?.toModel() ?: bootstrap
    }

    // 새로운 Baseline을 활성으로 저장(기존 활성은 비활성 처리)
    suspend fun upsertActive(from: BaseLine) = withContext(Dispatchers.IO) {
        dao.deactivateAll()     // 먼저 전부 비활성
        dao.insertOrReplace(    // 새 베이스라인 저장(활성 플래그 on)
            BaselineEntity(
                version = from.version,
                isActive = true,
                hrvSdnnMean = from.hrvSdnnMean, hrvSdnnStd = from.hrvSdnnStd,
                hrvRmssdMean = from.hrvRmssdMean, hrvRmssdStd = from.hrvRmssdStd,
                hrMean = from.hrMean, hrStd = from.hrStd,
                objTempMean = from.objTempMean, objTempStd = from.objTempStd
            )
        )
    }
}
