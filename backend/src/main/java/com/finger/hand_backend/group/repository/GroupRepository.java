package com.finger.hand_backend.group.repository;

import com.finger.hand_backend.group.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findByInviteCode(String code);
    boolean existsByInviteCode(String code);
}
