package com.finger.hand_backend.group.repository;

import com.finger.hand_backend.group.entity.GroupMember;
import com.finger.hand_backend.group.entity.GroupRole;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    Optional<GroupMember> findByGroupIdAndUserId(Long groupId, Long userId);
    boolean existsByGroupIdAndUserId(Long groupId, Long userId);
    List<GroupMember> findByUserId(Long userId);
    List<GroupMember> findByUserIdAndRole(Long userId, GroupRole role);
    List<GroupMember> findByGroupId(Long groupId);
    long countByGroupId(Long groupId);
    long countByGroupIdAndRole(Long groupId, GroupRole role);
    void deleteByGroupIdAndUserId(Long groupId, Long userId);
}

