// InterventionRepository.java
package com.finger.hand_backend.relief.repository;

import com.finger.hand_backend.relief.entity.Intervention;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterventionRepository extends JpaRepository<Intervention, Long> {
    boolean existsById(Long id);
}
