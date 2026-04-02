package com.notification.email.dto;

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
public class EmailTemplateResponse {
    private UUID id;
    private String name;
    private String subjectTemplate;
    private String bodyTemplate;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
