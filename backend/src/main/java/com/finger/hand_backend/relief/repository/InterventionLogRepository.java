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

}
