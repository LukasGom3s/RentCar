package com.example.rentcar.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.example.rentcar.exception.SendEmailException;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class SendEmailService {

    private final JavaMailSender mailSender;

    // Injeta automaticamente o e-mail definido nas properties
    @Value("${spring.mail.username}")
    private String remetente;

    public SendEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            
            // Utiliza a variável injetada em vez do e-mail fixado
            helper.setFrom(remetente);

            mailSender.send(message);

        } catch (MailException | MessagingException e) {
            throw new SendEmailException("Falha ao enviar e-mail: " + e.getMessage());
        }
    }
}