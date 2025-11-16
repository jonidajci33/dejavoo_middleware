package com.dejavoo.middleware.repository;

import com.dejavoo.middleware.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    /**
     * Find settlement by settlement ID
     * @param settlementId the unique settlement ID
     * @return Optional containing the settlement if found
     */
    Optional<Settlement> findBySettlementId(String settlementId);

    /**
     * Check if a settlement already exists by settlement ID
     * @param settlementId the unique settlement ID
     * @return true if exists, false otherwise
     */
    boolean existsBySettlementId(String settlementId);
}
