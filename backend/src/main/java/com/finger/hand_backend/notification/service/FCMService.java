package com.finger.hand_backend.notification.service;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * FCM 전송 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FCMService {

    /**
     * 단일 토큰으로 알림 전송
     */
    public boolean sendToToken(String token, String title, String body, Map<String, String> data) {
        try {
            Message.Builder messageBuilder = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build());

            // 데이터가 있으면 추가
            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            Message message = messageBuilder.build();
            String response = FirebaseMessaging.getInstance().send(message);

            log.info("FCM sent successfully: {}", response);
            return true;

        } catch (FirebaseMessagingException e) {
            log.error("Failed to send FCM to token: {}", token, e);
            return false;
        }
    }

    /**
     * 여러 토큰으로 알림 전송
     */
    public Map<String, Boolean> sendToTokens(List<String> tokens, String title, String body, Map<String, String> data) {
        if (tokens == null || tokens.isEmpty()) {
            log.warn("No tokens provided for FCM");
            return Map.of();
        }

        try {
            MulticastMessage.Builder messageBuilder = MulticastMessage.builder()
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .addAllTokens(tokens);

            // 데이터가 있으면 추가
            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            MulticastMessage message = messageBuilder.build();
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);

            log.info("FCM multicast sent: success={}, failure={}",
                    response.getSuccessCount(), response.getFailureCount());

            // 각 토큰별 성공/실패 결과 매핑
            return tokens.stream()
                    .collect(Collectors.toMap(
                            token -> token,
                            token -> {
                                int index = tokens.indexOf(token);
                                return response.getResponses().get(index).isSuccessful();
                            }
                    ));

        } catch (FirebaseMessagingException e) {
            log.error("Failed to send FCM multicast", e);
            // 전부 실패로 처리
            return tokens.stream()
                    .collect(Collectors.toMap(token -> token, token -> false));
        }
    }
}
