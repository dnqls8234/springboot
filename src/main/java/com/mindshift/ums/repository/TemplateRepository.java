package com.mindshift.ums.repository;

import com.mindshift.ums.domain.entity.Template;
import com.mindshift.ums.domain.enums.ChannelType;
import com.mindshift.ums.domain.enums.TemplateStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Long> {

    Optional<Template> findByCode(String code);

    Optional<Template> findByCodeAndStatus(String code, TemplateStatus status);

    Optional<Template> findByCodeAndLocaleAndStatus(String code, String locale, TemplateStatus status);

    @Query("SELECT t FROM Template t " +
           "WHERE t.code = :code " +
           "AND t.locale = :locale " +
           "AND t.status = 'ACTIVE' " +
           "AND t.channel = :channel")
    Optional<Template> findActiveTemplate(@Param("code") String code,
                                         @Param("locale") String locale,
                                         @Param("channel") ChannelType channel);

    List<Template> findAllByStatus(TemplateStatus status);

    List<Template> findAllByChannel(ChannelType channel);

    List<Template> findAllByChannelAndStatus(ChannelType channel, TemplateStatus status);

    boolean existsByCode(String code);

    boolean existsByCodeAndIdNot(String code, Long id);

    // Method needed by TemplateService - add tenantId support
    @Query("SELECT t FROM Template t " +
           "WHERE t.code = :code " +
           "AND t.channel = :channel " +
           "AND t.locale = :locale " +
           "AND t.status = 'ACTIVE'")
    Optional<Template> findByTenantIdAndCodeAndChannelAndLocale(@Param("tenantId") String tenantId,
                                                               @Param("code") String code,
                                                               @Param("channel") ChannelType channel,
                                                               @Param("locale") String locale);
}