// file: com/hand/hand/ui/model/OrgSource.kt
package com.hand.hand.ui.model

import com.hand.hand.ui.admin.sections.GroupMember
import com.hand.hand.ui.admin.sections.Mood
import kotlin.math.roundToInt

/**
 * 조직/멤버 단일 소스.
 * - 멤버는 7일치 점수(0..100, null=해당일 없음)를 가짐
 * - avgScore는 7일치의 "유효값 평균"으로 계산
 * - Mood는 avgScore로부터 파생
 * - advice(감정 개선 조언) / specialNote(특이사항) 분리 보관
 */
object OrgSource {

    // 내부 저장 구조: orgId -> (표시용 이름, 멤버들)
    private data class OrgData(
        val name: String,
        val members: List<MemberSeed>
    )

    /** 내부 전용 시드. UI에는 GroupMember로 변환해서 제공한다. */
    private data class MemberSeed(
        val id: String,
        val name: String,
        /** 주 7일치 점수 (월~일 순), null = 해당일 데이터 없음 */
        val week: List<Int?>,
        val advice: String? = null,        // 감정 개선 조언
        val specialNote: String? = null    // 특이사항
    ) {
        init { require(week.size == 7) { "week must have exactly 7 entries" } }
    }
//        val score: Int,           // 0..100
//        val note: String? = null
//    )

    // ───────────────────────── 샘플 데이터 ─────────────────────────
    private val orgs: Map<String, OrgData> = mapOf(
        "multipampus" to OrgData(
            name = "멀티캠퍼스",
            members = listOf(
                MemberSeed(
                    id = "1", name = "이희준",
                    week = listOf(90, 1, 80, 82, 1, 88, 82),
                    advice = "호흡을 길게 내쉬는 4-7-8 호흡을 오늘 2회만 해보세요.",
                    specialNote = null
                ),
                MemberSeed(
                    id = "2", name = "김민선",
                    week = listOf(62, 65, 60, 58, 66, null, 65),
                    advice = "점심 이후 10분 산책 루틴을 권장합니다.",
                    specialNote = "최근 피곤함 호소 잦음"
                ),
                MemberSeed(
                    id = "3", name = "박하늘",
                    week = listOf(44, 40, 52, null, 47, 49, 45),
                    advice = "수면 시간 고정(±30분) 유지 권장",
                    specialNote = null
                ),
                MemberSeed(
                    id = "4", name = "최도윤",
                    week = listOf(20, 28, null, 24, 22, 26, 19),
                    advice = "업무 중 50분 집중/10분 스트레칭 루틴",
                    specialNote = "담배 물리지 않으려면 조심할 것"
                ),
                MemberSeed(
                    id = "5", name = "정소은",
                    week = listOf(null, 4, 7, 3, null, 6, 1),
                    advice = "가벼운 산책과 햇빛 노출을 늘려보세요.",
                    specialNote = null
                )
//                MemberSeed("1",  "이희준", 83),
//                MemberSeed("2",  "김민선", 63),
//                MemberSeed("3",  "박하늘", 43),
//                MemberSeed("4",  "최도윤", 23, "담배 물리지 않으려면 조심할 것"),
//                MemberSeed("5",  "정소은",  3)
            )
        ),
        "gangnam_police" to OrgData(
            name = "강남 경찰서",
            members = listOf(
                MemberSeed("11", "김가온", listOf(78, 74, 72, 70, 76, null, 72),
                    advice = "교대 후 5분 기록 일지 작성해 보세요."
                ),
                MemberSeed("12", "박로이", listOf(59, 60, 57, 61, null, 55, 58),
                    advice = "퇴근 전 스트레칭 3종(목, 어깨, 허리)"
                ),
                MemberSeed("13", "최유빈", listOf(30, null, 35, 33, 31, 32, 34),
                    advice = "숙면 환경(온도·조도) 점검 권장",
                    specialNote = "최근 야근 증가, 피로 호소"
                ),
                MemberSeed("14", "정우성", listOf(90, 88, 86, 85, 92, 91, 89),
                    advice = "현 루틴 유지, 주 1회 휴식일 확보"
                )
//                MemberSeed("11", "김가온", 74),
//                MemberSeed("12", "박로이", 58),
//                MemberSeed("13", "최유빈", 32, "최근 야근 증가, 피로 호소"),
//                MemberSeed("14", "정우성", 88)
            )
        ),
        "seocho_police" to OrgData(
            name = "서초 경찰서",
            members = listOf(
                MemberSeed("21", "김가온", listOf(25, null, 22, 24, 23, 26, 25),
                    advice = "가벼운 유산소 15분 + 수분 섭취"
                ),
                MemberSeed("22", "박로이", listOf(18, 19, null, 20, 17, 18, 19),
                    advice = "점심 전 5분 호흡 루틴"
                ),
                MemberSeed("23", "최유빈", listOf(11, 12, 14, null, 10, 13, 12),
                    advice = "하루 1회 감사일기 3줄",
                    specialNote = "최근 야근 증가, 피로 호소"
                ),
                MemberSeed("24", "정우성", listOf(88, 90, 87, 89, 92, 91, 90),
                    advice = "현재 루틴 유지"
                )
//                MemberSeed("21", "김가온", 24),
//                MemberSeed("22", "박로이", 18),
//                MemberSeed("23", "최유빈", 12, "최근 야근 증가, 피로 호소"),
//                MemberSeed("24", "정우성", 88)
            )
        )
    )
    // ───────────────────────────────────────────────────────────────

    /** 모든 조직 목록 */
    fun organizations(): List<Organization> = orgs.map { (id, data) ->
        Organization(
            id = id,
            name = data.name,
            memberCount = data.members.size,
            averageScore = data.members.map { avgFromWeek(it.week).toFloat() }
                .ifEmpty { listOf(0f) }
                .average()
                .toFloat())
//            // 평균은 score로 계산
//            averageScore = data.members.map { it.score }.ifEmpty { listOf(0) }.average().toFloat()
//        )
    }

    /** 특정 조직 표시용 정보 */
    fun organization(orgId: String): Organization {
        val (resolvedId, data) = resolveOrg(orgId)
        val avg = data.members.map { avgFromWeek(it.week).toFloat() }
            .ifEmpty { listOf(0f) }
            .average()
            .toFloat()
//        val avg = data.members.map { it.score }.ifEmpty { listOf(0) }.average().toFloat()
        return Organization(
            id = resolvedId,
            name = data.name,
            memberCount = data.members.size,
            averageScore = avg
        )
    }

    /** 멤버 리스트 (avgScore = 7일 유효값 평균, mood = avgScore 기반)
     *  카드 하단 텍스트로 쓰는 note 는 '특이사항'을 매핑한다. */
    fun members(orgId: String): List<GroupMember> {
        val (_, data) = resolveOrg(orgId)
        return data.members.map { seed ->
            val avg = avgFromWeek(seed.week)
            GroupMember(
                id = seed.id,
                name = seed.name,
                mood = scoreToMood(avg),
                avgScore = avg,
                note = seed.specialNote     // ← 카드에는 특이사항을 노출
//                mood = scoreToMood(seed.score), // ✅ 점수→무드
//                avgScore = seed.score,          // ✅ 필드명 정확히: avgScore
//                note = seed.note
            )
        }
    }

    /** 특정 멤버 7일치 원시 점수 (null = 해당일 없음) */
    fun memberWeekScores(orgId: String, memberId: String): List<Int?> {
        val (_, data) = resolveOrg(orgId)
        return data.members.firstOrNull { it.id == memberId }?.week
            ?: List(7) { null }
    }

    /** 특정 멤버 7일치 정규화 점수(0f..1f, null 유지) */
    fun memberWeekScores01(orgId: String, memberId: String): List<Float?> =
        memberWeekScores(orgId, memberId).map { it?.coerceIn(0, 100)?.div(100f) }

    /** 특정 멤버 감정 개선 조언 */
    fun memberAdvice(orgId: String, memberId: String): String =
        resolveOrg(orgId).second.members.firstOrNull { it.id == memberId }?.advice.orEmpty()

    /** 특정 멤버 특이사항 */
    fun memberSpecialNote(orgId: String, memberId: String): String =
        resolveOrg(orgId).second.members.firstOrNull { it.id == memberId }?.specialNote.orEmpty()

    /** 조직의 경고(= SAD) 인원 수 (avgScore 기준) */
    fun sadCount(orgId: String): Int {
        val (_, data) = resolveOrg(orgId)
        return data.members.count { scoreToMood(avgFromWeek(it.week)) == Mood.SAD }
//        return data.members.count { scoreToMood(it.score) == Mood.SAD }
    }

    // ───────────────────────── helpers ─────────────────────────

    private fun resolveOrg(orgId: String): Pair<String, OrgData> {
        val data = orgs[orgId]
        return if (data != null) orgId to data else {
            val (firstId, firstData) = orgs.entries.first()
            firstId to firstData
        }
    }

    /** 7일치 평균(소수점 반올림). 유효값이 하나도 없으면 0. */
    private fun avgFromWeek(week: List<Int?>): Int {
        val valid = week.filterNotNull().map { it.coerceIn(0, 100) }
        return if (valid.isEmpty()) 0 else valid.average().roundToInt()
    }

    // avgScore → Mood (5단계)
    private fun scoreToMood(score: Int): Mood = when (score.coerceIn(0, 100)) {
        in 80..100 -> Mood.GREAT
        in 60..79  -> Mood.HAPPY
        in 40..59  -> Mood.OKAY
        in 20..39  -> Mood.DOWN
        else       -> Mood.SAD
    }
}
