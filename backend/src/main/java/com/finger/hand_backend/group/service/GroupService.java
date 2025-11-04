package com.finger.hand_backend.group.service;

import com.finger.hand_backend.group.dto.*;
import java.util.List;

public interface GroupService {
    GroupResponse create(Long userId, CreateGroupRequest req);
    GroupResponse get(Long userId, Long groupId);
    List<GroupResponse> myGroups(Long userId);
    GroupResponse update(Long userId, Long groupId, UpdateGroupRequest req);
    void delete(Long userId, Long groupId);

    InviteValidationResponse validate(String code);
    MemberResponse join(Long userId, JoinByInviteRequest req);
    String rotateInvite(Long userId, Long groupId);

    void leave(Long userId, Long groupId);
    List<MemberResponse> members(Long userId, Long groupId);
    void expel(Long userId, Long groupId, Long targetUserId);
    MemberResponse updateNotes(Long userId, Long groupId, Long targetUserId, UpdateMemberNotesRequest req);
}

