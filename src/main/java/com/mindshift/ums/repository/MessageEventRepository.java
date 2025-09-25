package com.mindshift.ums.repository;

import com.mindshift.ums.domain.entity.MessageEvent;
import com.mindshift.ums.domain.enums.EventType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageEventRepository extends JpaRepository<MessageEvent, Long> {

    List<MessageEvent> findAllByMessage_IdOrderByOccurredAtDesc(Long messageId);

    List<MessageEvent> findAllByRequestIdOrderByOccurredAtDesc(String requestId);

    List<MessageEvent> findAllByTypeAndOccurredAtBetween(EventType type,
                                                        LocalDateTime startTime,
                                                        LocalDateTime endTime);

    @Query("SELECT e FROM MessageEvent e " +
           "WHERE e.message.id = :messageId " +
           "AND e.type IN :types " +
           "ORDER BY e.occurredAt DESC")
    List<MessageEvent> findByMessageIdAndTypes(@Param("messageId") Long messageId,
                                              @Param("types") List<EventType> types);

    boolean existsByMessage_IdAndType(Long messageId, EventType type);

    MessageEvent findFirstByMessage_IdAndTypeOrderByOccurredAtDesc(Long messageId, EventType type);
}