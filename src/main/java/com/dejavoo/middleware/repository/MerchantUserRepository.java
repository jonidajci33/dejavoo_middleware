package com.dejavoo.middleware.repository;

import com.dejavoo.middleware.entity.MerchantUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MerchantUserRepository extends JpaRepository<MerchantUser, Long> {

    /**
     * Find user by username
     * @param username the username
     * @return Optional containing the user if found
     */
    Optional<MerchantUser> findByUsername(String username);

    /**
     * Find active user by username
     * @param username the username
     * @param active the active status
     * @return Optional containing the user if found and active
     */
    Optional<MerchantUser> findByUsernameAndActive(String username, Boolean active);
}
