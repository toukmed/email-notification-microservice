package com.notification.email.service;

import com.notification.email.dto.EmailTemplateRequest;
import com.notification.email.dto.EmailTemplateResponse;
import com.notification.email.entity.EmailTemplate;
import com.notification.email.exception.ResourceNotFoundException;
import com.notification.email.exception.DuplicateResourceException;
import com.notification.email.repository.EmailTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailTemplateService {

    private final EmailTemplateRepository templateRepository;
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{(\\w+)}}");

    @Transactional
    public EmailTemplateResponse createTemplate(EmailTemplateRequest request) {
        if (templateRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Template with name '" + request.getName() + "' already exists");
        }

        EmailTemplate template = EmailTemplate.builder()
                .name(request.getName())
                .subjectTemplate(request.getSubjectTemplate())
                .bodyTemplate(request.getBodyTemplate())
                .build();

        template = templateRepository.save(template);
        log.info("Created email template: {}", template.getName());
        return mapToResponse(template);
    }

    @Transactional(readOnly = true)
    public EmailTemplateResponse getTemplate(UUID id) {
        return templateRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public EmailTemplateResponse getTemplateByName(String name) {
        return templateRepository.findByName(name)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with name: " + name));
    }

    @Transactional(readOnly = true)
    public List<EmailTemplateResponse> getAllTemplates() {
        return templateRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public EmailTemplateResponse updateTemplate(UUID id, EmailTemplateRequest request) {
        EmailTemplate template = templateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Template not found with id: " + id));

        if (!template.getName().equals(request.getName()) && templateRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Template with name '" + request.getName() + "' already exists");
        }

        template.setName(request.getName());
        template.setSubjectTemplate(request.getSubjectTemplate());
        template.setBodyTemplate(request.getBodyTemplate());

        template = templateRepository.save(template);
        log.info("Updated email template: {}", template.getName());
        return mapToResponse(template);
    }

    @Transactional
    public void deleteTemplate(UUID id) {
        if (!templateRepository.existsById(id)) {
            throw new ResourceNotFoundException("Template not found with id: " + id);
        }
        templateRepository.deleteById(id);
        log.info("Deleted email template: {}", id);
    }

    public String renderTemplate(String template, Map<String, Object> variables) {
        if (template == null || variables == null || variables.isEmpty()) {
            return template;
        }

        StringBuffer result = new StringBuffer();
        Matcher matcher = VARIABLE_PATTERN.matcher(template);

        while (matcher.find()) {
            String variableName = matcher.group(1);
            Object value = variables.get(variableName);
            String replacement = value != null ? Matcher.quoteReplacement(value.toString()) : matcher.group(0);
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);

        return result.toString();
    }

    private EmailTemplateResponse mapToResponse(EmailTemplate template) {
        return EmailTemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .subjectTemplate(template.getSubjectTemplate())
                .bodyTemplate(template.getBodyTemplate())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
