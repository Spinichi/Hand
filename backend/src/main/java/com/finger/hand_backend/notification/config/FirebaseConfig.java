package com.finger.hand_backend.notification.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Firebase 초기화 설정
 */
@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${fcm.credentials.path:firebase-service-account.json}")
    private String credentialsPath;

    @PostConstruct
    public void initialize() {
        try {
            // Firebase 서비스 계정 JSON 파일 로드
            InputStream serviceAccount = new ClassPathResource(credentialsPath).getInputStream();

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            // Firebase 앱 초기화 (중복 초기화 방지)
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("Firebase initialized successfully");
            } else {
                log.info("Firebase already initialized");
            }

        } catch (IOException e) {
            log.error("Failed to initialize Firebase", e);
            // Firebase 파일이 없어도 서버는 정상 시작되도록 함
            log.warn("FCM features will not be available without proper Firebase credentials");
        }
    }
}
