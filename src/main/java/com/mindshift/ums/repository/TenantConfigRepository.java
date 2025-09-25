package com.mindshift.ums.repository;

import com.mindshift.ums.domain.entity.TenantConfig;
import com.mindshift.ums.domain.enums.TenantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantConfigRepository extends JpaRepository<TenantConfig, String> {

    Optional<TenantConfig> findByApiKey(String apiKey);

    Optional<TenantConfig> findByApiKeyAndStatus(String apiKey, TenantStatus status);

    @Query("SELECT t FROM TenantConfig t " +
           "WHERE t.apiKey = :apiKey " +
           "AND t.status = 'ACTIVE' " +
           "AND t.apiKeyStatus = 'ACTIVE'")
    Optional<TenantConfig> findActiveByApiKey(@Param("apiKey") String apiKey);

    List<TenantConfig> findAllByStatus(TenantStatus status);

    boolean existsByApiKey(String apiKey);

    boolean existsByTenantId(String tenantId);
}