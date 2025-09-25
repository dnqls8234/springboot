package com.mindshift.ums.repository;

import com.mindshift.ums.domain.entity.Message;
import com.mindshift.ums.domain.enums.MessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    Optional<Message> findByRequestId(String requestId);

    Optional<Message> findByIdempotencyKey(String idempotencyKey);

    Optional<Message> findByProviderMessageId(String providerMessageId);

    Page<Message> findAllByTenantId(String tenantId, Pageable pageable);

    Page<Message> findAllByTenantIdAndStatus(String tenantId, MessageStatus status, Pageable pageable);

    @Query("SELECT m FROM Message m " +
           "WHERE m.status = :status " +
           "AND m.createdAt >= :startDate " +
           "AND m.createdAt <= :endDate " +
           "ORDER BY m.priority DESC, m.createdAt ASC")
    Page<Message> findPendingMessages(@Param("status") MessageStatus status,
                                     @Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate,
                                     Pageable pageable);

    @Query("SELECT m FROM Message m " +
           "WHERE m.status = 'FAILED' " +
           "AND m.retries < 3 " +
           "AND m.lastRetryAt < :retryThreshold " +
           "ORDER BY m.retries ASC, m.lastRetryAt ASC")
    Page<Message> findRetryableMessages(@Param("retryThreshold") LocalDateTime retryThreshold,
                                       Pageable pageable);

    @Query("SELECT m FROM Message m " +
           "WHERE m.status IN ('PENDING', 'PROCESSING') " +
           "AND m.ttlExpiresAt IS NOT NULL " +
           "AND m.ttlExpiresAt < :now")
    Page<Message> findExpiredMessages(@Param("now") LocalDateTime now, Pageable pageable);

    @Modifying
    @Query("UPDATE Message m " +
           "SET m.status = 'EXPIRED', m.updatedAt = :now " +
           "WHERE m.status IN ('PENDING', 'PROCESSING') " +
           "AND m.ttlExpiresAt IS NOT NULL " +
           "AND m.ttlExpiresAt < :now")
    int expireMessages(@Param("now") LocalDateTime now);

    @Query("SELECT COUNT(m) FROM Message m " +
           "WHERE m.tenantId = :tenantId " +
           "AND m.createdAt >= :startTime")
    long countMessagesSince(@Param("tenantId") String tenantId,
                           @Param("startTime") LocalDateTime startTime);

    @Query("SELECT COUNT(m) FROM Message m " +
           "WHERE JSON_EXTRACT(m.toJson, '$.phone') = :phone " +
           "AND m.createdAt >= :startTime")
    long countMessagesByPhoneSince(@Param("phone") String phone,
                                  @Param("startTime") LocalDateTime startTime);

    @Query("SELECT COUNT(m) FROM Message m " +
           "WHERE JSON_EXTRACT(m.toJson, '$.email') = :email " +
           "AND m.createdAt >= :startTime")
    long countMessagesByEmailSince(@Param("email") String email,
                                  @Param("startTime") LocalDateTime startTime);

    boolean existsByIdempotencyKey(String idempotencyKey);

    // Additional methods for monitoring and statistics
    long countByCreatedAtAfter(LocalDateTime date);

    long countByStatus(MessageStatus status);

    long countByChannel(com.mindshift.ums.domain.enums.ChannelType channel);

    long countByStatusAndCreatedAtAfter(MessageStatus status, LocalDateTime date);
}