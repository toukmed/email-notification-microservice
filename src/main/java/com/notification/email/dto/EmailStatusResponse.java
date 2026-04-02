package com.notification.email.dto;

import com.notification.email.entity.EmailStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailStatusResponse {
    private UUID id;
    private String recipient;
    private String subject;
    private EmailStatus currentStatus;
    private Integer retryCount;
    private String errorMessage;
    private OffsetDateTime createdAt;
    private List<DeliveryLogEntry> deliveryHistory;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryLogEntry {
        private EmailStatus status;
        private String errorMessage;
        private OffsetDateTime timestamp;
    }
}
