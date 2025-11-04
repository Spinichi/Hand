package com.finger.hand_backend.group.controller;

import com.finger.hand_backend.common.dto.ApiResponse;
import com.finger.hand_backend.group.dto.*;
import com.finger.hand_backend.group.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/groups") // 전역 context-path: /api/v1
@RequiredArgsConstructor
public class GroupController {
    private final GroupService service;

    private Long userId(Authentication auth){ return Long.valueOf(auth.getName()); }

    @PostMapping
    public ResponseEntity<ApiResponse<GroupResponse>> create(Authentication a, @Valid @RequestBody CreateGroupRequest req){
        return ResponseEntity.ok(ApiResponse.success(service.create(userId(a), req), "ok"));
    }
    @GetMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GroupResponse>> get(Authentication a, @PathVariable Long groupId){
        return ResponseEntity.ok(ApiResponse.success(service.get(userId(a), groupId), "ok"));
    }
    @GetMapping
    public ResponseEntity<ApiResponse<List<GroupResponse>>> myGroups(Authentication a){
        return ResponseEntity.ok(ApiResponse.success(service.myGroups(userId(a)), "ok"));
    }
    @PutMapping("/{groupId}")
    public ResponseEntity<ApiResponse<GroupResponse>> update(Authentication a, @PathVariable Long groupId,
                                                             @Valid @RequestBody UpdateGroupRequest req){
        return ResponseEntity.ok(ApiResponse.success(service.update(userId(a), groupId, req), "ok"));
    }
    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> delete(Authentication a, @PathVariable Long groupId){
        service.delete(userId(a), groupId); return ResponseEntity.noContent().build();
    }

    @GetMapping("/invite/validate")
    public ResponseEntity<ApiResponse<InviteValidationResponse>> validate(@RequestParam String code){
        return ResponseEntity.ok(ApiResponse.success(service.validate(code), "ok"));
    }
    @PostMapping("/join")
    public ResponseEntity<ApiResponse<MemberResponse>> join(Authentication a, @Valid @RequestBody JoinByInviteRequest req){
        return ResponseEntity.ok(ApiResponse.success(service.join(userId(a), req), "ok"));
    }
    @PostMapping("/{groupId}/invite-code/rotate")
    public ResponseEntity<ApiResponse<Map<String,String>>> rotate(Authentication a, @PathVariable Long groupId){
        String code = service.rotateInvite(userId(a), groupId);
        return ResponseEntity.ok(ApiResponse.success(Map.of("groupId", String.valueOf(groupId), "inviteCode", code), "ok"));
    }
    @DeleteMapping("/{groupId}/leave")
    public ResponseEntity<Void> leave(Authentication a, @PathVariable Long groupId){
        service.leave(userId(a), groupId); return ResponseEntity.noContent().build();
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<ApiResponse<List<MemberResponse>>> members(Authentication a, @PathVariable Long groupId){
        return ResponseEntity.ok(ApiResponse.success(service.members(userId(a), groupId), "ok"));
    }
    @DeleteMapping("/{groupId}/members/{userId}")
    public ResponseEntity<Void> expel(Authentication a, @PathVariable Long groupId, @PathVariable("userId") Long target){
        service.expel(userId(a), groupId, target); return ResponseEntity.noContent().build();
    }
    @PutMapping("/{groupId}/members/{userId}/notes")
    public ResponseEntity<ApiResponse<MemberResponse>> updateNotes(Authentication a, @PathVariable Long groupId,
                                                                   @PathVariable("userId") Long target,
                                                                   @Valid @RequestBody UpdateMemberNotesRequest req){
        return ResponseEntity.ok(ApiResponse.success(service.updateNotes(userId(a), groupId, target, req), "ok"));
    }
}

