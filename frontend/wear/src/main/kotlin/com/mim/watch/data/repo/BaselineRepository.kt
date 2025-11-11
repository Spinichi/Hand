package com.mim.watch.repo
// 도메인 저장소 레이어
import android.content.Context
import com.mim.watch.core.baseline.BaseLine
import com.mim.watch.core.baseline.BaselineProvider
import com.mim.watch.data.local.AppDb

class BaselineRepository(context: Context) {
    private val dao = AppDb.get(context).baselineDao()
    private val provider = BaselineProvider(context, dao) // Provider로 위임

    suspend fun active(): BaseLine = provider.getActiveBaseline() // 활성 Baseline 얻기
    suspend fun updateActive(b: BaseLine) = provider.upsertActive(b) // 활성 Baseline 갱신
}
