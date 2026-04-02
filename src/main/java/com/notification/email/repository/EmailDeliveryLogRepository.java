package com.notification.email.repository;

import com.notification.email.entity.EmailDeliveryLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EmailDeliveryLogRepository extends JpaRepository<EmailDeliveryLog, UUID> {
    List<EmailDeliveryLog> findByEmailIdOrderByCreatedAtDesc(UUID emailId);
}
