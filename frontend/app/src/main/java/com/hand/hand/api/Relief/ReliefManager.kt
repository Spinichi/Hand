//ReliefManager.kt

package com.hand.hand.api.Relief

import android.util.Log
import com.hand.hand.api.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ReliefManager {

    companion object {
        private const val TAG = "ReliefManager"

        private val httpCall: ReliefInterface =
            RetrofitClient.getClient().create(ReliefInterface::class.java)

        /**
         * 릴리프 세션 시작 요청
         *
         * @param token              JWT access token (순수 토큰 값만, "Bearer "는 여기서 붙임)
         * @param interventionId     개입 ID
         * @param triggerType        "AUTO_SUGGEST" 등
         * @param anomalyDetectionId 이상 감지 ID (없으면 null)
         * @param gestureCode        제스처 코드 (없으면 null)
         * @param startedAt          ISO8601 시간 문자열
         */
        fun startSession(
//            token: String,
            interventionId: Int,
            triggerType: String,
            anomalyDetectionId: Int?,
            gestureCode: String?,
            startedAt: String,
            onSuccess: (ReliefSessionStartResponse) -> Unit,
            onFailure: (Throwable) -> Unit
        ) {
            val req = ReliefSessionStartRequest(
                interventionId = interventionId,
                triggerType = triggerType,
                anomalyDetectionId = anomalyDetectionId,
                gestureCode = gestureCode,
                startedAt = startedAt
            )

//            val authHeader = "Bearer $token"

            Log.d(TAG, "startSession 요청: $req")

            httpCall.startReliefSession(req)
                .enqueue(object : Callback<ReliefSessionStartResponse> {
                    override fun onResponse(
                        call: Call<ReliefSessionStartResponse>,
                        response: Response<ReliefSessionStartResponse>
                    ) {
                        val body = response.body()
                        val errorBodyStr = try {
                            response.errorBody()?.string()
                        } catch (_: Exception) {
                            null
                        }

                        Log.d(
                            TAG,
                            "startSession 응답: code=${response.code()}, body=$body, errorBody=$errorBodyStr"
                        )

                        if (response.isSuccessful && body != null) {
                            onSuccess(body)
                        } else {
                            onFailure(
                                RuntimeException(
                                    "Relief startSession 실패: code=${response.code()}, error=$errorBodyStr"
                                )
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<ReliefSessionStartResponse>,
                        t: Throwable
                    ) {
                        Log.e(TAG, "startSession 네트워크 오류", t)
                        onFailure(t)
                    }
                })
        }

        fun endSession(
            sessionId: Long,
            endedAt: String,
            userRating: Int,
            onSuccess: () -> Unit,
            onFailure: (Throwable) -> Unit
        ) {
            val req = ReliefSessionEndRequest(
                endedAt = endedAt,
                userRating = userRating
            )

            Log.d(TAG, "endSession 요청: sessionId=$sessionId, req=$req")

            httpCall.endReliefSession(sessionId, req)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(
                        call: Call<Void>,
                        response: Response<Void>
                    ) {
                        val errorBodyStr = try {
                            response.errorBody()?.string()
                        } catch (_: Exception) {
                            null
                        }

                        Log.d(
                            TAG,
                            "endSession 응답: code=${response.code()}, errorBody=$errorBodyStr"
                        )

                        if (response.isSuccessful) {
                            onSuccess()
                        } else {
                            onFailure(
                                RuntimeException(
                                    "Relief endSession 실패: code=${response.code()}, error=$errorBodyStr"
                                )
                            )
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Log.e(TAG, "endSession 네트워크 오류", t)
                        onFailure(t)
                    }
                })
        }

    }
}
