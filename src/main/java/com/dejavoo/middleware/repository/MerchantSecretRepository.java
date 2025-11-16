package com.dejavoo.middleware.repository;

import com.dejavoo.middleware.entity.MerchantSecret;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MerchantSecretRepository extends JpaRepository<MerchantSecret, Long> {

    /**
     * Find merchant secret by merchant ID
     * @param merchantId the merchant ID
     * @return Optional containing the merchant secret if found
     */
    Optional<MerchantSecret> findByMerchantId(String merchantId);

    /**
     * Find active merchant secret by merchant ID
     * @param merchantId the merchant ID
     * @param active the active status
     * @return Optional containing the merchant secret if found and active
     */
    Optional<MerchantSecret> findByMerchantIdAndActive(String merchantId, Boolean active);
}
