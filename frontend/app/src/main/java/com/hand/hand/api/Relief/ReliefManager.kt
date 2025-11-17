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

        // ═══════════════════════════════════════════════════════════
        // 워치 자동 완화법용 (Auto Relief from Watch)
        // ═══════════════════════════════════════════════════════════

        /**
         * 완화법 세션 시작 (워치 자동용)
         *
         * @param interventionId 완화법 ID (1~6)
         * @param triggerType "AUTO_SUGGEST" or "MANUAL"
         * @param gestureCode 제스처 코드 (예: "breathing")
         * @param onSuccess 성공 콜백 (sessionId, beforeStress 반환)
         * @param onFailure 실패 콜백
         */
        fun startSession(
            interventionId: Long,
            triggerType: String,
            gestureCode: String? = null,
            onSuccess: ((ReliefStartResponse) -> Unit)? = null,
            onFailure: ((Throwable) -> Unit)? = null
        ) {
            val request = ReliefStartRequest.create(
                interventionId = interventionId,
                triggerType = triggerType,
                gestureCode = gestureCode
            )

            Log.d(TAG, "📤 완화법 시작 요청: interventionId=$interventionId, triggerType=$triggerType")

            httpCall.startSession(request).enqueue(object : Callback<ReliefApiResponse<ReliefStartResponse>> {
                override fun onResponse(
                    call: Call<ReliefApiResponse<ReliefStartResponse>>,
                    response: Response<ReliefApiResponse<ReliefStartResponse>>
                ) {
                    val body = response.body()
                    val errorBodyStr = try {
                        response.errorBody()?.string()
                    } catch (_: Exception) {
                        null
                    }

                    Log.d(TAG, "📥 응답코드=${response.code()} body=$body errorBody=$errorBodyStr")

                    if (response.isSuccessful && body != null && body.success) {
                        Log.d(TAG, "✅ 완화법 세션 시작 성공: sessionId=${body.data?.sessionId}, beforeStress=${body.data?.beforeStress}")
                        body.data?.let { onSuccess?.invoke(it) }
                    } else {
                        val msg = "완화법 세션 시작 실패: ${response.code()} - ${body?.message ?: response.message()}"
                        Log.e(TAG, msg)
                        onFailure?.invoke(Throwable(msg))
                    }
                }

                override fun onFailure(call: Call<ReliefApiResponse<ReliefStartResponse>>, t: Throwable) {
                    Log.e(TAG, "🚨 통신 실패: ${t.localizedMessage}", t)
                    onFailure?.invoke(t)
                }
            })
        }

        /**
         * 완화법 세션 종료 (워치 자동용)
         *
         * @param sessionId 세션 ID (시작 시 반환받은 값)
         * @param userRating 사용자 별점 (1~5, null 가능)
         * @param onSuccess 성공 콜백
         * @param onFailure 실패 콜백
         */
        fun endSession(
            sessionId: Long,
            userRating: Int? = null,
            onSuccess: ((ReliefEndResponse) -> Unit)? = null,
            onFailure: ((Throwable) -> Unit)? = null
        ) {
            val request = ReliefEndRequest.create(userRating = userRating)

            Log.d(TAG, "📤 완화법 종료 요청: sessionId=$sessionId, userRating=$userRating")

            httpCall.endSession(sessionId, request).enqueue(object : Callback<ReliefApiResponse<ReliefEndResponse>> {
                override fun onResponse(
                    call: Call<ReliefApiResponse<ReliefEndResponse>>,
                    response: Response<ReliefApiResponse<ReliefEndResponse>>
                ) {
                    val body = response.body()
                    val errorBodyStr = try {
                        response.errorBody()?.string()
                    } catch (_: Exception) {
                        null
                    }

                    Log.d(TAG, "📥 응답코드=${response.code()} body=$body errorBody=$errorBodyStr")

                    if (response.isSuccessful && body != null && body.success) {
                        Log.d(TAG, "✅ 완화법 세션 종료 성공: afterStress=${body.data?.afterStress}, duration=${body.data?.durationSeconds}초")
                        body.data?.let { onSuccess?.invoke(it) }
                    } else {
                        val msg = "완화법 세션 종료 실패: ${response.code()} - ${body?.message ?: response.message()}"
                        Log.e(TAG, msg)
                        onFailure?.invoke(Throwable(msg))
                    }
                }

                override fun onFailure(call: Call<ReliefApiResponse<ReliefEndResponse>>, t: Throwable) {
                    Log.e(TAG, "🚨 통신 실패: ${t.localizedMessage}", t)
                    onFailure?.invoke(t)
                }
            })
        }

        // ═══════════════════════════════════════════════════════════
        // 앱 수동 완화법용 (Manual Relief from App - careSafeZone)
        // ═══════════════════════════════════════════════════════════

        /**
         * 릴리프 세션 시작 요청 (앱 수동용)
         *
         * @param interventionId     개입 ID
         * @param triggerType        "MANUAL" 등
         * @param anomalyDetectionId 이상 감지 ID (없으면 null)
         * @param gestureCode        제스처 코드 (없으면 null)
         * @param startedAt          ISO8601 시간 문자열
         */
        fun startReliefSession(
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
                            Log.d(TAG, "✅ 완화법 세션 시작 성공: sessionId=${body.data?.sessionId}, beforeStress=${body.data?.beforeStress}")
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

        /**
         * 릴리프 세션 종료 (앱 수동용)
         */
        fun endReliefSession(
            sessionId: Long,
            userRating: Int,
            onSuccess: () -> Unit,
            onFailure: (Throwable) -> Unit
        ) {
            val req = ReliefEndRequest.create(userRating = userRating)

            Log.d(TAG, "endReliefSession 요청: sessionId=$sessionId, req=$req")

            httpCall.endReliefSession(sessionId, req)
                .enqueue(object : Callback<ReliefApiResponse<ReliefEndResponse>> {
                    override fun onResponse(
                        call: Call<ReliefApiResponse<ReliefEndResponse>>,
                        response: Response<ReliefApiResponse<ReliefEndResponse>>
                    ) {
                        val body = response.body()
                        val errorBodyStr = try {
                            response.errorBody()?.string()
                        } catch (_: Exception) {
                            null
                        }

                        Log.d(
                            TAG,
                            "endReliefSession 응답: code=${response.code()}, body=$body, errorBody=$errorBodyStr"
                        )

                        if (response.isSuccessful && body != null && body.success) {
                            Log.d(TAG, "✅ 완화법 세션 종료 성공: afterStress=${body.data?.afterStress}, duration=${body.data?.durationSeconds}초")
                            onSuccess()
                        } else {
                            onFailure(
                                RuntimeException(
                                    "Relief endReliefSession 실패: code=${response.code()}, error=$errorBodyStr"
                                )
                            )
                        }
                    }

                    override fun onFailure(call: Call<ReliefApiResponse<ReliefEndResponse>>, t: Throwable) {
                        Log.e(TAG, "endReliefSession 네트워크 오류", t)
                        onFailure(t)
                    }
                })
        }

        /**
         * 오늘의 세션 개수 조회
         *
         * @param onSuccess 성공 콜백 (count 반환)
         * @param onFailure 실패 콜백
         */
        fun getTodaySessionCount(
            onSuccess: ((Long) -> Unit)? = null,
            onFailure: ((Throwable) -> Unit)? = null
        ) {
            Log.d(TAG, "📤 오늘의 세션 개수 조회 요청")

            httpCall.getTodaySessionCount().enqueue(object : Callback<ReliefApiResponse<TodayCountData>> {
                override fun onResponse(
                    call: Call<ReliefApiResponse<TodayCountData>>,
                    response: Response<ReliefApiResponse<TodayCountData>>
                ) {
                    val body = response.body()
                    Log.d(TAG, "📥 응답코드=${response.code()} body=$body")

                    if (response.isSuccessful && body != null && body.success) {
                        val count = body.data?.count ?: 0L
                        Log.d(TAG, "✅ 오늘의 세션 개수: $count")
                        onSuccess?.invoke(count)
                    } else {
                        val msg = "오늘의 세션 개수 조회 실패: ${response.code()} - ${body?.message ?: response.message()}"
                        Log.e(TAG, msg)
                        onFailure?.invoke(Throwable(msg))
                    }
                }

                override fun onFailure(call: Call<ReliefApiResponse<TodayCountData>>, t: Throwable) {
                    Log.e(TAG, "🚨 통신 실패: ${t.localizedMessage}", t)
                    onFailure?.invoke(t)
                }
            })
        }

    }
}
