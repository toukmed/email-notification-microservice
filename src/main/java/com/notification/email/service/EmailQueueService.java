package com.notification.email.service;

import com.notification.email.dto.*;
import com.notification.email.entity.EmailDeliveryLog;
import com.notification.email.entity.EmailQueue;
import com.notification.email.entity.EmailStatus;
import com.notification.email.entity.EmailTemplate;
import com.notification.email.exception.ResourceNotFoundException;
import com.notification.email.exception.ValidationException;
import com.notification.email.repository.EmailDeliveryLogRepository;
import com.notification.email.repository.EmailQueueRepository;
import com.notification.email.repository.EmailTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailQueueService {

    private final EmailQueueRepository queueRepository;
    private final EmailTemplateRepository templateRepository;
    private final EmailDeliveryLogRepository deliveryLogRepository;
    private final EmailTemplateService templateService;

    @Transactional
    public EmailResponse queueEmail(SendEmailRequest request) {
        String subject;
        String body;
        EmailTemplate template = null;

        if (request.getTemplateName() != null && !request.getTemplateName().isBlank()) {
            template = templateRepository.findByName(request.getTemplateName())
                    .orElseThrow(() -> new ResourceNotFoundException("Template not found: " + request.getTemplateName()));

            subject = templateService.renderTemplate(template.getSubjectTemplate(), request.getVariables());
            body = templateService.renderTemplate(template.getBodyTemplate(), request.getVariables());
        } else {
            if (request.getSubject() == null || request.getSubject().isBlank()) {
                throw new ValidationException("Subject is required when not using a template");
            }
            if (request.getBody() == null || request.getBody().isBlank()) {
                throw new ValidationException("Body is required when not using a template");
            }
            subject = templateService.renderTemplate(request.getSubject(), request.getVariables());
            body = templateService.renderTemplate(request.getBody(), request.getVariables());
        }

        EmailQueue email = EmailQueue.builder()
                .template(template)
                .recipient(request.getRecipient())
                .cc(request.getCc())
                .bcc(request.getBcc())
                .subject(subject)
                .body(body)
                .variables(request.getVariables())
                .status(EmailStatus.PENDING)
                .scheduledAt(request.getScheduledAt() != null ? request.getScheduledAt() : OffsetDateTime.now())
                .build();

        email = queueRepository.save(email);
        log.info("Queued email {} to {}", email.getId(), email.getRecipient());

        return mapToResponse(email);
    }

    @Transactional(readOnly = true)
    public EmailStatusResponse getEmailStatus(UUID id) {
        EmailQueue email = queueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Email not found with id: " + id));

        List<EmailDeliveryLog> logs = deliveryLogRepository.findByEmailIdOrderByCreatedAtDesc(id);

        List<EmailStatusResponse.DeliveryLogEntry> history = logs.stream()
                .map(log -> EmailStatusResponse.DeliveryLogEntry.builder()
                        .status(log.getStatus())
                        .errorMessage(log.getErrorMessage())
                        .timestamp(log.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return EmailStatusResponse.builder()
                .id(email.getId())
                .recipient(email.getRecipient())
                .subject(email.getSubject())
                .currentStatus(email.getStatus())
                .retryCount(email.getRetryCount())
                .errorMessage(email.getErrorMessage())
                .createdAt(email.getCreatedAt())
                .deliveryHistory(history)
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<EmailResponse> getEmails(
            String recipient,
            EmailStatus status,
            OffsetDateTime fromDate,
            OffsetDateTime toDate,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<EmailQueue> emailPage = queueRepository.findWithFilters(recipient, status, fromDate, toDate, pageable);

        List<EmailResponse> content = emailPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<EmailResponse>builder()
                .content(content)
                .page(emailPage.getNumber())
                .size(emailPage.getSize())
                .totalElements(emailPage.getTotalElements())
                .totalPages(emailPage.getTotalPages())
                .first(emailPage.isFirst())
                .last(emailPage.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public List<EmailQueue> getPendingEmails(int batchSize) {
        return queueRepository.findPendingEmails(
                EmailStatus.PENDING,
                OffsetDateTime.now(),
                PageRequest.of(0, batchSize)
        );
    }

    @Transactional
    public boolean markAsProcessing(UUID id) {
        return queueRepository.updateStatus(id, EmailStatus.PENDING, EmailStatus.PROCESSING) > 0;
    }

    @Transactional
    public void markAsSent(EmailQueue email) {
        email.setStatus(EmailStatus.SENT);
        queueRepository.save(email);

        EmailDeliveryLog log = EmailDeliveryLog.builder()
                .email(email)
                .status(EmailStatus.SENT)
                .build();
        deliveryLogRepository.save(log);

        log.info("Email {} sent successfully to {}", email.getId(), email.getRecipient());
    }

    @Transactional
    public void markAsFailed(EmailQueue email, String errorMessage, int maxRetries) {
        email.setRetryCount(email.getRetryCount() + 1);
        email.setErrorMessage(errorMessage);

        if (email.getRetryCount() >= maxRetries) {
            email.setStatus(EmailStatus.FAILED);
            log.error("Email {} permanently failed after {} retries: {}", email.getId(), maxRetries, errorMessage);
        } else {
            email.setStatus(EmailStatus.PENDING);
            log.warn("Email {} failed, will retry (attempt {}/{}): {}", 
                    email.getId(), email.getRetryCount(), maxRetries, errorMessage);
        }

        queueRepository.save(email);

        EmailDeliveryLog log = EmailDeliveryLog.builder()
                .email(email)
                .status(email.getStatus())
                .errorMessage(errorMessage)
                .build();
        deliveryLogRepository.save(log);
    }

    private EmailResponse mapToResponse(EmailQueue email) {
        return EmailResponse.builder()
                .id(email.getId())
                .recipient(email.getRecipient())
                .subject(email.getSubject())
                .status(email.getStatus())
                .retryCount(email.getRetryCount())
                .errorMessage(email.getErrorMessage())
                .scheduledAt(email.getScheduledAt())
                .createdAt(email.getCreatedAt())
                .updatedAt(email.getUpdatedAt())
                .build();
    }
}
