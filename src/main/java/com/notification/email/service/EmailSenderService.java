package com.notification.email.service;

import com.notification.email.entity.EmailQueue;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailSenderService {

    private final JavaMailSender mailSender;

    @Value("${email.from.address}")
    private String fromAddress;

    @Value("${email.from.name}")
    private String fromName;

    public void sendEmail(EmailQueue email) throws MessagingException, MailException {
        log.debug("Preparing to send email {} to {}", email.getId(), email.getRecipient());

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromAddress, fromName);
        helper.setTo(email.getRecipient());
        helper.setSubject(email.getSubject());
        helper.setText(email.getBody(), true);

        if (StringUtils.hasText(email.getCc())) {
            helper.setCc(email.getCc().split(","));
        }

        if (StringUtils.hasText(email.getBcc())) {
            helper.setBcc(email.getBcc().split(","));
        }

        mailSender.send(message);
        log.debug("Email {} sent successfully", email.getId());
    }
}
