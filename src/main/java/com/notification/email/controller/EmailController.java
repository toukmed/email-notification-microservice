package com.notification.email.controller;

import com.notification.email.dto.*;
import com.notification.email.entity.EmailStatus;
import com.notification.email.service.EmailQueueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/emails")
@RequiredArgsConstructor
public class EmailController {

    private final EmailQueueService queueService;

    @PostMapping("/send")
    public ResponseEntity<EmailResponse> sendEmail(@Valid @RequestBody SendEmailRequest request) {
        EmailResponse response = queueService.queueEmail(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<EmailStatusResponse> getEmailStatus(@PathVariable UUID id) {
        EmailStatusResponse response = queueService.getEmailStatus(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<EmailResponse>> getEmails(
            @RequestParam(required = false) String recipient,
            @RequestParam(required = false) EmailStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<EmailResponse> response = queueService.getEmails(recipient, status, fromDate, toDate, page, size);
        return ResponseEntity.ok(response);
    }
}
