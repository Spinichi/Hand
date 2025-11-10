package com.finger.hand_backend.notification.repository;

import com.finger.hand_backend.notification.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    /**
     * 사용자의 활성화된 토큰 조회
     */
    List<DeviceToken> findByUserIdAndIsActive(Long userId, Boolean isActive);

    /**
     * 토큰으로 조회
     */
    Optional<DeviceToken> findByDeviceToken(String deviceToken);

    /**
     * 모든 활성화된 토큰 조회 (전체 푸시용)
     */
    List<DeviceToken> findByIsActive(Boolean isActive);
}
