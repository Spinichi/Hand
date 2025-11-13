package com.hand.hand.api.Measurements

import android.util.Log
import com.hand.hand.api.RetrofitClient
import com.hand.hand.wear.model.BioSample
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MeasurementsManager {
    companion object {
        private const val TAG = "MeasurementsManager"

        private val httpCall: MeasurementsInterface =
            RetrofitClient.getClient().create(MeasurementsInterface::class.java)

        /**
         * 워치에서 받은 BioSample을 백엔드로 전송
         *
         * @param sample 워치에서 받은 생체 데이터
         * @param onSuccess 성공 콜백 (저장된 측정 데이터 ID 반환)
         * @param onFailure 실패 콜백
         */
        fun sendBioData(
            sample: BioSample,
            onSuccess: ((MeasurementCreateResponse) -> Unit)? = null,
            onFailure: ((Throwable) -> Unit)? = null
        ) {
            // BioSample을 MeasurementRequest로 변환
            val request = MeasurementRequest.from(sample)

            Log.d(TAG, "📤 측정 데이터 전송: stressLevel=${request.stressLevel}, isAnomaly=${request.isAnomaly}")

            httpCall.createMeasurement(request).enqueue(object : Callback<MeasurementResponse> {
                override fun onResponse(
                    call: Call<MeasurementResponse>,
                    response: Response<MeasurementResponse>
                ) {
                    val body = response.body()
                    val errorBodyStr = try {
                        response.errorBody()?.string()
                    } catch (_: Exception) {
                        null
                    }

                    Log.d(
                        TAG,
                        "📥 응답코드=${response.code()} body=$body errorBody=$errorBodyStr"
                    )

                    if (response.isSuccessful && body != null && body.success) {
                        Log.d(TAG, "✅ 측정 데이터 저장 성공: ID=${body.data?.id}")
                        body.data?.let { onSuccess?.invoke(it) }
                    } else {
                        val msg = "측정 데이터 저장 실패: ${response.code()} - ${body?.message ?: response.message()}"
                        Log.e(TAG, msg)
                        onFailure?.invoke(Throwable(msg))
                    }
                }

                override fun onFailure(call: Call<MeasurementResponse>, t: Throwable) {
                    Log.e(TAG, "🚨 통신 실패: ${t.localizedMessage}", t)
                    onFailure?.invoke(t)
                }
            })
        }
    }
}
