package com.finger.hand_backend.group.service;

import com.finger.hand_backend.group.dto.*;
import com.finger.hand_backend.group.entity.*;
import com.finger.hand_backend.group.repository.*;
import com.finger.hand_backend.group.util.InviteCodeGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service @RequiredArgsConstructor @Transactional
public class GroupServiceImpl implements GroupService {
    private final GroupRepository groupRepo;
    private final GroupMemberRepository memberRepo;

    private Group getOr404(Long id){
        return groupRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("GROUP_NOT_FOUND"));
    }
    private GroupMember requireManager(Long gid, Long uid){
        GroupMember gm = memberRepo.findByGroupIdAndUserId(gid, uid)
                .orElseThrow(() -> new IllegalArgumentException("NOT_GROUP_MEMBER"));
        if (gm.getRole() != GroupRole.MANAGER) throw new IllegalArgumentException("FORBIDDEN");
        return gm;
    }
    private String newCode(){
        for(int i=0;i<10;i++){ String c=InviteCodeGenerator.generate6(); if(!groupRepo.existsByInviteCode(c)) return c; }
        throw new IllegalArgumentException("INVITE_GENERATE_FAILED");
    }

    @Override
    public GroupResponse create(Long userId, CreateGroupRequest req) {
        Group g = new Group();
        g.setName(req.name()); g.setGroupType(req.groupType());
        g.setInviteCode(newCode()); g.setCreatedBy(userId);
        groupRepo.save(g);

        GroupMember owner = new GroupMember();
        owner.setGroup(g); owner.setUserId(userId); owner.setRole(GroupRole.MANAGER); owner.setSpecialNotes("");
        memberRepo.save(owner);

        return new GroupResponse(g.getId(), g.getName(), g.getGroupType(), g.getInviteCode(),
                g.getCreatedBy(), g.getCreatedAt(), g.getUpdatedAt());
    }

    @Override @Transactional(readOnly = true)
    public GroupResponse get(Long userId, Long groupId) {
        memberRepo.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new IllegalArgumentException("NOT_GROUP_MEMBER"));
        Group g = getOr404(groupId);
        return new GroupResponse(g.getId(), g.getName(), g.getGroupType(), g.getInviteCode(),
                g.getCreatedBy(), g.getCreatedAt(), g.getUpdatedAt());
    }

    @Override @Transactional(readOnly = true)
    public List<GroupResponse> myGroups(Long userId) {
        return memberRepo.findByUserId(userId).stream()
                .map(GroupMember::getGroup)
                .distinct()
                .map(g -> new GroupResponse(g.getId(), g.getName(), g.getGroupType(), g.getInviteCode(),
                        g.getCreatedBy(), g.getCreatedAt(), g.getUpdatedAt()))
                .toList();
    }

    @Override
    public GroupResponse update(Long userId, Long groupId, UpdateGroupRequest req) {
        requireManager(groupId, userId);
        Group g = getOr404(groupId);
        g.setName(req.name()); g.setGroupType(req.groupType());
        groupRepo.save(g);
        return new GroupResponse(g.getId(), g.getName(), g.getGroupType(), g.getInviteCode(),
                g.getCreatedBy(), g.getCreatedAt(), g.getUpdatedAt());
    }

    @Override public void delete(Long userId, Long groupId) {
        requireManager(groupId, userId);
        groupRepo.deleteById(groupId);
    }

    @Override @Transactional(readOnly = true)
    public InviteValidationResponse validate(String code) {
        return groupRepo.findByInviteCode(code)
                .map(g -> new InviteValidationResponse(true, g.getId(), g.getName()))
                .orElse(new InviteValidationResponse(false, null, null));
    }

    @Override
    public MemberResponse join(Long userId, JoinByInviteRequest req) {
        Group g = groupRepo.findByInviteCode(req.inviteCode())
                .orElseThrow(() -> new IllegalArgumentException("INVALID_INVITE_CODE"));
        if (memberRepo.existsByGroupIdAndUserId(g.getId(), userId))
            throw new IllegalArgumentException("ALREADY_JOINED");

        GroupMember gm = new GroupMember();
        gm.setGroup(g); gm.setUserId(userId); gm.setRole(GroupRole.MEMBER); gm.setSpecialNotes("");
        memberRepo.save(gm);
        return new MemberResponse(gm.getUserId(), gm.getRole(), gm.getSpecialNotes(), gm.getJoinedAt());
    }

    @Override
    public String rotateInvite(Long userId, Long groupId) {
        requireManager(groupId, userId);
        Group g = getOr404(groupId);
        g.setInviteCode(newCode()); groupRepo.save(g);
        return g.getInviteCode();
    }

    @Override
    public void leave(Long userId, Long groupId) {
        GroupMember me = memberRepo.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new IllegalArgumentException("NOT_GROUP_MEMBER"));
        if (me.getRole()==GroupRole.MANAGER &&
                memberRepo.countByGroupIdAndRole(groupId, GroupRole.MANAGER)==1)
            throw new IllegalArgumentException("ONLY_MANAGER_CANNOT_LEAVE");
        memberRepo.delete(me);
    }

    @Override @Transactional(readOnly = true)
    public List<MemberResponse> members(Long userId, Long groupId) {
        requireManager(groupId, userId);
        return memberRepo.findByGroupId(groupId).stream()
                .map(gm -> new MemberResponse(gm.getUserId(), gm.getRole(), gm.getSpecialNotes(), gm.getJoinedAt()))
                .toList();
    }

    @Override
    public void expel(Long userId, Long groupId, Long targetUserId) {
        requireManager(groupId, userId);
        GroupMember target = memberRepo.findByGroupIdAndUserId(groupId, targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("MEMBER_NOT_FOUND"));
        if (target.getRole()==GroupRole.MANAGER &&
                memberRepo.countByGroupIdAndRole(groupId, GroupRole.MANAGER)==1)
            throw new IllegalArgumentException("CANNOT_EXPEL_ONLY_MANAGER");
        memberRepo.delete(target);
    }

    @Override
    public MemberResponse updateNotes(Long userId, Long groupId, Long targetUserId, UpdateMemberNotesRequest req) {
        requireManager(groupId, userId);
        GroupMember gm = memberRepo.findByGroupIdAndUserId(groupId, targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("MEMBER_NOT_FOUND"));
        gm.setSpecialNotes(req.specialNotes()==null? "" : req.specialNotes().trim());
        memberRepo.save(gm);
        return new MemberResponse(gm.getUserId(), gm.getRole(), gm.getSpecialNotes(), gm.getJoinedAt());
    }
}

