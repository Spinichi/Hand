package com.finger.hand_backend.user.repository;


import com.finger.hand_backend.user.entity.IndividualUser;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;


public interface IndividualUserRepository extends JpaRepository<IndividualUser, Integer> {
    Optional<IndividualUser> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
    void deleteByUserId(Long userId);
}
