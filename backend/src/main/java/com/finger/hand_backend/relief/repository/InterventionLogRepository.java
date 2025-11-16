// InterventionLogRepository.java
package com.finger.hand_backend.relief.repository;

import com.finger.hand_backend.relief.entity.InterventionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InterventionLogRepository extends JpaRepository<InterventionLog, Long> {

    List<InterventionLog> findByUserIdOrderByStartedAtDesc(Long userId);

    // 마이페이지용: 감소폭 평균 (after가 있는 데이터만)
    @Query("""
  SELECT il.interventionId,
         COUNT(il) AS sessions,
         AVG(il.afterStress - il.beforeStress) AS avgReduction
  FROM InterventionLog il
  WHERE il.userId = :userId AND il.afterStress IS NOT NULL
  GROUP BY il.interventionId
  ORDER BY avgReduction ASC
""")
    List<Object[]> statsByUser(Long userId);


    Optional<InterventionLog> findTop1ByUserIdAndAfterStressIsNullAndEndedAtIsNotNullOrderByEndedAtDesc(Long userId);

    // 완화 기록 화면용: 일수 포함 통계
    @Query(value = """
        SELECT il.intervention_id,
               COUNT(DISTINCT DATE(il.started_at)) AS days,
               COUNT(il.id) AS sessions,
               AVG(il.before_stress - il.after_stress) AS avg_reduction
        FROM intervention_logs il
        WHERE il.user_id = :userId AND il.after_stress IS NOT NULL
        GROUP BY il.intervention_id
        """, nativeQuery = true)
    List<Object[]> getStatisticsWithDays(Long userId);

    // 완화 기록 히스토리 (날짜별 세션 목록)
    List<InterventionLog> findByUserIdAndAfterStressIsNotNullOrderByStartedAtDesc(Long userId);

}
