package com.notification.email.dto;

import com.notification.email.entity.EmailStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailResponse {
    private UUID id;
    private String recipient;
    private String subject;
    private EmailStatus status;
    private Integer retryCount;
    private String errorMessage;
    private OffsetDateTime scheduledAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
