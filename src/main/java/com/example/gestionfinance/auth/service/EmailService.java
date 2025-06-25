package com.example.gestionfinance.auth.service;

import jakarta.annotation.PostConstruct;
import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;



import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final String baseUrl;
    private final String fromEmail;
    private final String senderName;
    private final Environment environment;

    public EmailService(JavaMailSender mailSender,
                        TemplateEngine templateEngine,
                        @Value("${app.base-url}") String baseUrl,
                        @Value("${spring.mail.username}") String fromEmail,
                        @Value("${app.email.sender-name}") String senderName, Environment environment) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.baseUrl = baseUrl;
        this.fromEmail = fromEmail;
        this.senderName = senderName;
        this.environment = environment;
    }

    @PostConstruct
    public void init() {
        // Ne rien faire en mode test
        if (Arrays.stream(environment.getActiveProfiles()).anyMatch(profile -> profile.equalsIgnoreCase("test"))) {
            return;
        }
        try {
            //testSmtpConnection();
            System.out.println();
        } catch (Exception e) {
            logger.error("SMTP initialization failed", e);
            throw new RuntimeException("SMTP initialization failed", e);
        }
    }

    @Async
    public CompletableFuture<Void> sendPasswordResetEmail(String to, String token) {
        try {
            logger.debug("Début préparation email de réinitialisation pour {}", to);

            // 1. Test du template avant de construire le message
            try {
                String testContent = templateEngine.process("password-reset", new Context());
                logger.debug("Test du template réussi - Contenu généré:\n{}", testContent);
            } catch (Exception e) {
                logger.error("Échec du traitement du template email/password-reset", e);
                throw new RuntimeException("Erreur dans le template d'email", e);
            }

            // 2. Construction des variables pour le vrai email
            Map<String, Object> templateVariables = new HashMap<>();
            templateVariables.put("resetUrl", baseUrl + "/api/auth/reset-password?token=" + token);

            templateVariables.put("expirationTime", "1 heure");
            templateVariables.put("senderName", senderName);
            templateVariables.put("supportEmail", fromEmail);

            Context context = new Context();
            context.setVariables(templateVariables);

            // 3. Génération du contenu final
            String htmlContent = templateEngine.process("password-reset", context);
            logger.debug("Contenu final généré avec succès");

            sendHtmlMessage(to, "Réinitialisation de votre mot de passe", htmlContent);

            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            logger.error("Échec d'envoi d'email de réinitialisation à {}", to, e);
            throw new RuntimeException("Échec de l'envoi de l'email de réinitialisation", e);
        }
    }

    private void sendHtmlMessage(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            // En-têtes anti-spam
            message.addHeader("Precedence", "bulk");
            message.addHeader("X-Mailer", "YourAppName");
            message.addHeader("X-Priority", "3"); // Priorité normale
            message.addHeader("X-MSMail-Priority", "Normal");
            message.addHeader("X-AntiAbuse", "This is a legit email");

            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(fromEmail); // Juste l'email sans nom
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("Email envoyé avec succès à {}", to);

        } catch (Exception e) {
            logger.error("Échec d'envoi", e);
            throw new RuntimeException(e);
        }
    }
    public void testSmtpConnection() {
        if (!(mailSender instanceof JavaMailSenderImpl)) {
            logger.warn("Test SMTP non disponible - Implémentation non standard");
            return;
        }

        JavaMailSenderImpl mailSenderImpl = (JavaMailSenderImpl) mailSender;
        logger.info("Test de connexion SMTP vers {}:{}",
                mailSenderImpl.getHost(),
                mailSenderImpl.getPort());

        try {
            Session session = mailSenderImpl.getSession();
            Transport transport = session.getTransport();

            try {
                transport.connect(
                        mailSenderImpl.getHost(),
                        mailSenderImpl.getPort(),
                        mailSenderImpl.getUsername(),
                        mailSenderImpl.getPassword()
                );
                logger.info("Connexion SMTP réussie");
            } finally {
                transport.close();
            }
        } catch (AuthenticationFailedException e) {
            String errorMsg = "Échec d'authentification SMTP pour " + mailSenderImpl.getUsername();
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        } catch (MessagingException e) {
            String errorMsg = "Échec de connexion à " + mailSenderImpl.getHost() + ":" + mailSenderImpl.getPort();
            logger.error(errorMsg, e);
            throw new RuntimeException(errorMsg, e);
        }
    }

    private void logSmtpConfiguration() {
        if (!(mailSender instanceof JavaMailSenderImpl)) {
            return;
        }

        JavaMailSenderImpl mailSenderImpl = (JavaMailSenderImpl) mailSender;
        Properties props = mailSenderImpl.getJavaMailProperties();

        logger.info("Configuration SMTP actuelle:");
        logger.info("- Serveur: {}:{}", mailSenderImpl.getHost(), mailSenderImpl.getPort());
        logger.info("- Utilisateur: {}", mailSenderImpl.getUsername());
        logger.info("- Protocole: {}", mailSenderImpl.getProtocol());
        logger.info("- Authentification: {}", props.getProperty("mail.smtp.auth"));
        logger.info("- StartTLS: {}", props.getProperty("mail.smtp.starttls.enable"));
        logger.info("- Timeout: {}", props.getProperty("mail.smtp.timeout"));
    }
}