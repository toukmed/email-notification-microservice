package com.notification.email.repository;

import com.notification.email.entity.EmailQueue;
import com.notification.email.entity.EmailStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface EmailQueueRepository extends JpaRepository<EmailQueue, UUID> {

    @Query("SELECT e FROM EmailQueue e WHERE e.status = :status AND e.scheduledAt <= :now ORDER BY e.scheduledAt ASC")
    List<EmailQueue> findPendingEmails(
            @Param("status") EmailStatus status,
            @Param("now") OffsetDateTime now,
            Pageable pageable
    );

    @Modifying
    @Query("UPDATE EmailQueue e SET e.status = :newStatus WHERE e.id = :id AND e.status = :currentStatus")
    int updateStatus(
            @Param("id") UUID id,
            @Param("currentStatus") EmailStatus currentStatus,
            @Param("newStatus") EmailStatus newStatus
    );

    Page<EmailQueue> findByRecipient(String recipient, Pageable pageable);

    Page<EmailQueue> findByStatus(EmailStatus status, Pageable pageable);

    @Query("SELECT e FROM EmailQueue e WHERE " +
            "(:recipient IS NULL OR e.recipient = :recipient) AND " +
            "(:status IS NULL OR e.status = :status) AND " +
            "(:fromDate IS NULL OR e.createdAt >= :fromDate) AND " +
            "(:toDate IS NULL OR e.createdAt <= :toDate)")
    Page<EmailQueue> findWithFilters(
            @Param("recipient") String recipient,
            @Param("status") EmailStatus status,
            @Param("fromDate") OffsetDateTime fromDate,
            @Param("toDate") OffsetDateTime toDate,
            Pageable pageable
    );
}
