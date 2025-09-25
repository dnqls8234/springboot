package com.mindshift.ums.repository;

import com.mindshift.ums.domain.entity.RecipientPref;
import com.mindshift.ums.domain.enums.RecipientType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipientPrefRepository extends JpaRepository<RecipientPref, RecipientPref.RecipientPrefId> {

    Optional<RecipientPref> findByTenantIdAndRecipientKey(String tenantId, String recipientKey);

    List<RecipientPref> findAllByTenantId(String tenantId);

    List<RecipientPref> findAllByTenantIdAndOptedOut(String tenantId, Boolean optedOut);

    @Query("SELECT r FROM RecipientPref r " +
           "WHERE r.tenantId = :tenantId " +
           "AND r.recipientKey = :recipientKey " +
           "AND r.recipientType = :recipientType")
    Optional<RecipientPref> findByTenantAndRecipient(@Param("tenantId") String tenantId,
                                                    @Param("recipientKey") String recipientKey,
                                                    @Param("recipientType") RecipientType recipientType);

    boolean existsByTenantIdAndRecipientKey(String tenantId, String recipientKey);

    int deleteByTenantIdAndRecipientKey(String tenantId, String recipientKey);
}