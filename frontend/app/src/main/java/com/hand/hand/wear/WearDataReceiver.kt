package com.hand.hand.wear

import com.hand.hand.wear.model.BioSample
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 워치 데이터 수신 상태 관리
 * - WearListenerForegroundService에서 데이터 업데이트
 * - UI에서 StateFlow로 구독
 */
object WearDataReceiver {
    private val _lastReceivedData = MutableStateFlow<BioSample?>(null)
    val lastReceivedData: StateFlow<BioSample?> = _lastReceivedData.asStateFlow()

    fun updateData(sample: BioSample) {
        _lastReceivedData.value = sample
    }
}