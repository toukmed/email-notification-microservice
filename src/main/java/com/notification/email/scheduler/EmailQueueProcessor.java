package com.notification.email.scheduler;

import com.notification.email.entity.EmailQueue;
import com.notification.email.service.EmailQueueService;
import com.notification.email.service.EmailSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailQueueProcessor {

    private final EmailQueueService queueService;
    private final EmailSenderService senderService;

    @Value("${email.queue.batch-size}")
    private int batchSize;

    @Value("${email.queue.max-retries}")
    private int maxRetries;

    @Scheduled(fixedDelayString = "${email.queue.poll-interval}")
    public void processQueue() {
        List<EmailQueue> pendingEmails = queueService.getPendingEmails(batchSize);

        if (pendingEmails.isEmpty()) {
            return;
        }

        log.debug("Processing {} pending emails", pendingEmails.size());

        for (EmailQueue email : pendingEmails) {
            processEmail(email);
        }
    }

    private void processEmail(EmailQueue email) {
        if (!queueService.markAsProcessing(email.getId())) {
            log.debug("Email {} already being processed by another instance", email.getId());
            return;
        }

        try {
            senderService.sendEmail(email);
            queueService.markAsSent(email);
        } catch (Exception e) {
            log.error("Failed to send email {}: {}", email.getId(), e.getMessage());
            queueService.markAsFailed(email, e.getMessage(), maxRetries);
        }
    }
}
