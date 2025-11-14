package com.finger.hand_backend.group.service;

import com.finger.hand_backend.group.dto.*;
import com.finger.hand_backend.group.entity.*;
import com.finger.hand_backend.group.repository.*;
import com.finger.hand_backend.group.util.InviteCodeGenerator;
import com.finger.hand_backend.measurement.Measurement;
import com.finger.hand_backend.measurement.MeasurementRepository;
import com.finger.hand_backend.risk.DailyRiskScoreService;
import com.finger.hand_backend.user.entity.IndividualUser;
import com.finger.hand_backend.user.repository.IndividualUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor @Transactional
public class GroupServiceImpl implements GroupService {
    private final GroupRepository groupRepo;
    private final GroupMemberRepository memberRepo;
    private final DailyRiskScoreService riskScoreService;
    private final MeasurementRepository measurementRepo;
    private final IndividualUserRepository individualUserRepo;

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

    /**
     * 그룹 멤버들의 평균 위험 점수 계산 (요청자 제외)
     */
    private Double calculateAvgMemberRiskScore(Long groupId, Long excludeUserId) {
        List<GroupMember> members = memberRepo.findByGroupId(groupId).stream()
                .filter(gm -> !gm.getUserId().equals(excludeUserId))
                .toList();

        if (members.isEmpty()) {
            return null; // 멤버가 없음
        }

        List<Double> riskScores = members.stream()
                .map(gm -> riskScoreService.getWeeklyAverageRiskScore(gm.getUserId()))
                .filter(score -> score != null)
                .toList();

        if (riskScores.isEmpty()) {
            return null; // 데이터 없음
        }

        return riskScores.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
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
                g.getCreatedBy(), g.getCreatedAt(), g.getUpdatedAt(), null, 1); // 본인만 있으므로 1명
    }

    @Override @Transactional(readOnly = true)
    public GroupResponse get(Long userId, Long groupId) {
        memberRepo.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new IllegalArgumentException("NOT_GROUP_MEMBER"));
        Group g = getOr404(groupId);
        Double avgRiskScore = calculateAvgMemberRiskScore(groupId, userId);
        Integer memberCount = (int) memberRepo.countByGroupId(groupId);
        return new GroupResponse(g.getId(), g.getName(), g.getGroupType(), g.getInviteCode(),
                g.getCreatedBy(), g.getCreatedAt(), g.getUpdatedAt(), avgRiskScore, memberCount);
    }

    @Override @Transactional(readOnly = true)
    public List<GroupResponse> myGroups(Long userId) {
        // 내가 MANAGER인 그룹만 조회
        return memberRepo.findByUserIdAndRole(userId, GroupRole.MANAGER).stream()
                .map(GroupMember::getGroup)
                .distinct()
                .map(g -> {
                    Double avgRiskScore = calculateAvgMemberRiskScore(g.getId(), userId);
                    Integer memberCount = (int) memberRepo.countByGroupId(g.getId());
                    return new GroupResponse(g.getId(), g.getName(), g.getGroupType(), g.getInviteCode(),
                            g.getCreatedBy(), g.getCreatedAt(), g.getUpdatedAt(), avgRiskScore, memberCount);
                })
                .toList();
    }

    @Override
    public GroupResponse update(Long userId, Long groupId, UpdateGroupRequest req) {
        requireManager(groupId, userId);
        Group g = getOr404(groupId);
        g.setName(req.name()); g.setGroupType(req.groupType());
        groupRepo.save(g);
        Double avgRiskScore = calculateAvgMemberRiskScore(groupId, userId);
        Integer memberCount = (int) memberRepo.countByGroupId(groupId);
        return new GroupResponse(g.getId(), g.getName(), g.getGroupType(), g.getInviteCode(),
                g.getCreatedBy(), g.getCreatedAt(), g.getUpdatedAt(), avgRiskScore, memberCount);
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

        String userName = individualUserRepo.findByUserId(userId)
                .map(IndividualUser::getName)
                .orElse("Unknown");
        Double weeklyAvg = riskScoreService.getWeeklyAverageRiskScore(userId);
        return new MemberResponse(gm.getUserId(), userName, gm.getRole(), gm.getSpecialNotes(),
                                 gm.getJoinedAt(), weeklyAvg);
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
                .map(gm -> {
                    String userName = individualUserRepo.findByUserId(gm.getUserId())
                            .map(IndividualUser::getName)
                            .orElse("Unknown");
                    Double weeklyAvg = riskScoreService.getWeeklyAverageRiskScore(gm.getUserId());
                    return new MemberResponse(gm.getUserId(), userName, gm.getRole(), gm.getSpecialNotes(),
                                             gm.getJoinedAt(), weeklyAvg);
                })
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

        String userName = individualUserRepo.findByUserId(targetUserId)
                .map(IndividualUser::getName)
                .orElse("Unknown");
        Double weeklyAvg = riskScoreService.getWeeklyAverageRiskScore(targetUserId);
        return new MemberResponse(gm.getUserId(), userName, gm.getRole(), gm.getSpecialNotes(),
                                 gm.getJoinedAt(), weeklyAvg);
    }

    @Override @Transactional(readOnly = true)
    public GroupAnomalyStatisticsResponse getGroupAnomalyStatistics(Long userId, Long groupId) {
        // 1. 권한 확인 (MANAGER만 가능)
        requireManager(groupId, userId);

        Group group = getOr404(groupId);

        // 2. 그룹 멤버 조회 (본인 제외)
        List<GroupMember> members = memberRepo.findByGroupId(groupId).stream()
                .filter(gm -> !gm.getUserId().equals(userId))
                .toList();

        if (members.isEmpty()) {
            // 멤버가 없는 경우
            return GroupAnomalyStatisticsResponse.builder()
                    .groupId(groupId)
                    .groupName(group.getName())
                    .memberCount(0)
                    .weeklyStatistics(null)
                    .topRiskMember(null)
                    .build();
        }

        // 3. 날짜 범위 설정 (오늘 기준 최근 7일)
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(6);

        // 4. 일별 평균 계산
        List<DailyAverageAnomaly> dailyAverages = new ArrayList<>();
        double totalSum = 0.0;

        for (LocalDate date = sevenDaysAgo; !date.isAfter(today); date = date.plusDays(1)) {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

            int totalAnomaliesForDay = 0;

            // 각 멤버의 해당 날짜 이상치 개수 합산
            for (GroupMember member : members) {
                List<Measurement> anomalies = measurementRepo
                        .findByUserIdAndIsAnomalyTrueAndMeasuredAtBetweenOrderByMeasuredAtAsc(
                                member.getUserId(), startOfDay, endOfDay);
                totalAnomaliesForDay += anomalies.size();
            }

            // 일별 평균 = 해당 날짜 전체 이상치 / 멤버 수
            double dailyAverage = (double) totalAnomaliesForDay / members.size();
            totalSum += dailyAverage;

            dailyAverages.add(DailyAverageAnomaly.builder()
                    .date(date)
                    .averageAnomalyCount(dailyAverage)
                    .build());
        }

        // 5. 주간 전체 평균 계산
        double weeklyAverage = totalSum / 7.0;

        // 6. 최고 위험 멤버 찾기 (주간 평균 이상치 횟수가 가장 높은 멤버)
        TopRiskMember topRiskMember = null;
        double maxAverage = 0.0;

        for (GroupMember member : members) {
            LocalDateTime weekStart = sevenDaysAgo.atStartOfDay();
            LocalDateTime weekEnd = today.plusDays(1).atStartOfDay();

            List<Measurement> weeklyAnomalies = measurementRepo
                    .findByUserIdAndIsAnomalyTrueAndMeasuredAtBetweenOrderByMeasuredAtAsc(
                            member.getUserId(), weekStart, weekEnd);

            double memberAverage = weeklyAnomalies.size() / 7.0;

            if (memberAverage > maxAverage) {
                maxAverage = memberAverage;

                // 사용자 이름 조회
                String userName = individualUserRepo.findByUserId(member.getUserId())
                        .map(IndividualUser::getName)
                        .orElse("Unknown");

                topRiskMember = TopRiskMember.builder()
                        .userId(member.getUserId())
                        .userName(userName)
                        .weeklyAverageAnomalyCount(memberAverage)
                        .build();
            }
        }

        // 7. 응답 생성
        WeeklyAnomalyStatistics weeklyStats = WeeklyAnomalyStatistics.builder()
                .startDate(sevenDaysAgo)
                .endDate(today)
                .totalAverageAnomalyCount(weeklyAverage)
                .dailyAverages(dailyAverages)
                .build();

        return GroupAnomalyStatisticsResponse.builder()
                .groupId(groupId)
                .groupName(group.getName())
                .memberCount(members.size())
                .weeklyStatistics(weeklyStats)
                .topRiskMember(topRiskMember)
                .build();
    }
}

