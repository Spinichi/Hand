package com.hand.hand.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.hand.hand.R
import com.hand.hand.ui.home.HomeActivity

/**
 * FCM 메시지 수신 서비스
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCM"
        private const val CHANNEL_ID = "hand_notifications"
        private const val CHANNEL_NAME = "Hand 알림"
        private const val NOTIFICATION_ID = 3001  // WearListener는 2001 사용
    }

    /**
     * FCM 토큰이 새로 생성되거나 갱신될 때 호출
     * - 앱 최초 실행
     * - 앱 재설치
     * - Firebase SDK가 주기적으로 갱신
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "새 FCM 토큰 생성: ${token.take(20)}...")

        // 1. 로컬에 저장
        FCMTokenManager.saveToken(this, token)

        // 2. 백엔드에 전송
        com.hand.hand.api.Notification.NotificationManager.registerToken(
            deviceToken = token,
            onSuccess = {
                Log.d(TAG, "백엔드에 토큰 등록 성공")
            },
            onFailure = { error ->
                Log.e(TAG, "백엔드에 토큰 등록 실패: ${error.message}")
            }
        )
    }

    /**
     * FCM 메시지 수신 시 호출
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d(TAG, "FCM 메시지 수신 from: ${message.from}")
        Log.d(TAG, "메시지 ID: ${message.messageId}")
        Log.d(TAG, "데이터: ${message.data}")

        // notification 페이로드가 있는 경우
        message.notification?.let {
            val title = it.title ?: "Hand"
            val body = it.body ?: ""
            showNotification(title, body, message.data)
        }

        // data 페이로드만 있는 경우 (백엔드에서 data 메시지로 보낼 때)
        if (message.notification == null && message.data.isNotEmpty()) {
            handleDataMessage(message.data)
        }
    }

    /**
     * 알림 표시
     * WearListenerForegroundService의 알림 패턴 참고
     */
    private fun showNotification(title: String, body: String, data: Map<String, String>) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Android 8.0+ 알림 채널 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Hand 앱 푸시 알림"
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 알림 클릭 시 HomeActivity로 이동
        val intent = Intent(this, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // 데이터 페이로드를 Intent에 추가 (선택사항)
            data.forEach { (key, value) ->
                putExtra(key, value)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 알림 빌드
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_launcher_foreground)  // TODO: 적절한 아이콘으로 변경
            .setAutoCancel(true)  // 탭하면 자동으로 사라짐
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
        Log.d(TAG, "알림 표시 완료: $title - $body")
    }

    /**
     * 데이터 메시지 처리
     * 백엔드에서 data 페이로드만 보낼 때 사용
     */
    private fun handleDataMessage(data: Map<String, String>) {
        Log.d(TAG, "데이터 메시지 처리: $data")

        // TODO: 데이터 타입에 따라 처리
        // 예시:
        // - type=ANOMALY_ALERT → 이상치 경고 알림
        // - type=GROUP_INVITE → 그룹 초대 알림
        // - type=DIARY_REMINDER → 일기 작성 리마인더

        val type = data["type"]
        val title = data["title"] ?: "Hand"
        val message = data["message"] ?: "새로운 알림이 있습니다"

        when (type) {
            "ANOMALY_ALERT" -> {
                showNotification("⚠️ 이상치 감지", message, data)
            }
            "GROUP_INVITE" -> {
                showNotification("📩 그룹 초대", message, data)
            }
            "DIARY_REMINDER" -> {
                showNotification("📝 일기 작성", message, data)
            }
            else -> {
                showNotification(title, message, data)
            }
        }
    }
}
