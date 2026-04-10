package com.recrutement.recrutement.service.Impl;

import com.recrutement.recrutement.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendActivationEmail(String toEmail, String fullName, String activationToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Activation de votre compte - SmartRecruit");

            String activationLink = frontendUrl + "/verify-email?token=" + activationToken;
            helper.setText(buildActivationEmailTemplate(fullName, activationLink), true);

            mailSender.send(message);
            logger.info("Email d'activation envoye a: {}", toEmail);
        } catch (MessagingException e) {
            logger.error("Erreur lors de l'envoi de l'email d'activation a: {}", toEmail, e);
            throw new RuntimeException("Echec de l'envoi de l'email d'activation", e);
        }
    }

    @Override
    public void sendRecruiterPendingApprovalEmail(String toEmail, String adminName, String recruiterName, String recruiterEmail) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Nouveau compte recruteur en attente - SmartRecruit");
            helper.setText(buildRecruiterPendingApprovalTemplate(adminName, recruiterName, recruiterEmail), true);

            mailSender.send(message);
            logger.info("Email de notification admin envoye a: {}", toEmail);
        } catch (MessagingException e) {
            logger.error("Erreur lors de l'envoi de l'email de notification admin a: {}", toEmail, e);
            throw new RuntimeException("Echec de l'envoi de la notification admin", e);
        }
    }

    @Override
    public void sendRecruiterApprovedEmail(String toEmail, String fullName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Votre compte recruteur est actif - SmartRecruit");
            helper.setText(buildRecruiterApprovedTemplate(fullName), true);

            mailSender.send(message);
            logger.info("Email d'approbation recruteur envoye a: {}", toEmail);
        } catch (MessagingException e) {
            logger.error("Erreur lors de l'envoi de l'email d'approbation a: {}", toEmail, e);
            throw new RuntimeException("Echec de l'envoi de l'email d'approbation", e);
        }
    }

    @Override
    public void sendRecruiterRejectedEmail(String toEmail, String fullName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Votre demande recruteur a ete refusee - SmartRecruit");
            helper.setText(buildRecruiterRejectedTemplate(fullName), true);

            mailSender.send(message);
            logger.info("Email de refus recruteur envoye a: {}", toEmail);
        } catch (MessagingException e) {
            logger.error("Erreur lors de l'envoi de l'email de refus a: {}", toEmail, e);
            throw new RuntimeException("Echec de l'envoi de l'email de refus", e);
        }
    }

    @Override
    public void sendWelcomeEmail(String toEmail, String fullName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Bienvenue sur SmartRecruit");
            helper.setText(buildWelcomeEmailTemplate(fullName), true);

            mailSender.send(message);
            logger.info("Email de bienvenue envoye a: {}", toEmail);
        } catch (MessagingException e) {
            logger.error("Erreur lors de l'envoi de l'email de bienvenue a: {}", toEmail, e);
        }
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String fullName, String resetToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Reinitialisation de votre mot de passe - SmartRecruit");

            String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
            helper.setText(buildPasswordResetTemplate(fullName, resetLink), true);

            mailSender.send(message);
            logger.info("Email de reinitialisation envoye a: {}", toEmail);
        } catch (MessagingException e) {
            logger.error("Erreur lors de l'envoi de l'email de reinitialisation a: {}", toEmail, e);
            throw new RuntimeException("Echec de l'envoi de l'email de reinitialisation", e);
        }
    }

    private String buildActivationEmailTemplate(String fullName, String activationLink) {
        return baseTemplate(
                "Verification de votre email",
                "Bonjour " + fullName + ",",
                """
                <p>Merci de vous etre inscrit sur SmartRecruit. Cliquez sur le bouton ci-dessous pour activer votre compte.</p>
                """,
                activationLink,
                "Activer mon compte",
                """
                <p>Ce lien d'activation est valable pendant 24 heures.</p>
                """
        );
    }

    private String buildRecruiterPendingApprovalTemplate(String adminName, String recruiterName, String recruiterEmail) {
        String safeAdminName = (adminName == null || adminName.isBlank()) ? "Administrateur" : adminName;

        return baseTemplate(
                "Validation d'un nouveau recruteur",
                "Bonjour " + safeAdminName + ",",
                String.format(
                        """
                        <p>Un nouveau compte recruteur vient d'etre cree et attend votre validation.</p>
                        <ul>
                            <li><strong>Nom :</strong> %s</li>
                            <li><strong>Email :</strong> %s</li>
                        </ul>
                        <p>Veuillez verifier ce compte puis l'activer depuis l'espace administrateur.</p>
                        """,
                        recruiterName,
                        recruiterEmail
                ),
                frontendUrl + "/admin-dashboard",
                "Ouvrir le dashboard admin",
                """
                <p>Apres activation, le recruteur recevra automatiquement un email de confirmation.</p>
                """
        );
    }

    private String buildRecruiterApprovedTemplate(String fullName) {
        return baseTemplate(
                "Compte recruteur active",
                "Bonjour " + fullName + ",",
                """
                <p>Votre compte recruteur a ete verifie et active par l'administration.</p>
                <p>Vous pouvez maintenant vous connecter et utiliser toutes les fonctionnalites recruteur.</p>
                """,
                frontendUrl + "/auth/login",
                "Se connecter",
                """
                <p>Bienvenue sur SmartRecruit, votre espace recrutement est maintenant operationnel.</p>
                """
        );
    }

    private String buildRecruiterRejectedTemplate(String fullName) {
        return baseTemplate(
                "Demande recruteur refusee",
                "Bonjour " + fullName + ",",
                """
                <p>Apres verification par l'administration, votre demande de compte recruteur n'a pas ete retenue.</p>
                <p>Si vous pensez qu'il s'agit d'une erreur, vous pouvez contacter l'administration ou soumettre une nouvelle demande avec des informations completes.</p>
                """,
                frontendUrl + "/register",
                "Revenir a l'inscription",
                """
                <p>Merci pour votre interet pour SmartRecruit.</p>
                """
        );
    }

    private String buildPasswordResetTemplate(String fullName, String resetLink) {
        return baseTemplate(
                "Mot de passe oublie",
                "Bonjour " + fullName + ",",
                """
                <p>Nous avons recu une demande de reinitialisation de mot de passe pour votre compte SmartRecruit.</p>
                <p>Si vous etes a l'origine de cette demande, utilisez le bouton ci-dessous pour definir un nouveau mot de passe.</p>
                """,
                resetLink,
                "Reinitialiser mon mot de passe",
                """
                <p>Ce lien est valable pendant 30 minutes. Si vous n'avez pas demande cette action, ignorez simplement cet email.</p>
                """
        );
    }

    private String buildWelcomeEmailTemplate(String fullName) {
        return baseTemplate(
                "Compte active avec succes",
                "Felicitations " + fullName + ",",
                """
                <p>Votre compte SmartRecruit est maintenant actif. Vous pouvez des a present vous connecter a la plateforme.</p>
                """,
                frontendUrl + "/auth/login",
                "Se connecter",
                """
                <p>Merci de votre confiance et bienvenue sur SmartRecruit.</p>
                """
        );
    }

    private String baseTemplate(
            String title,
            String greeting,
            String introHtml,
            String actionLink,
            String actionLabel,
            String footerHtml
    ) {
        return String.format(
                """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; line-height: 1.6; color: #24324a; background: #f4f7fb; margin: 0; padding: 24px; }
                        .container { max-width: 640px; margin: 0 auto; background: #ffffff; border-radius: 20px; overflow: hidden; box-shadow: 0 14px 32px rgba(24, 39, 75, 0.12); }
                        .header { padding: 28px 32px; background: linear-gradient(135deg, #255fd0 0%%, #27b0d7 100%%); color: #ffffff; }
                        .content { padding: 32px; }
                        .button-wrap { text-align: center; margin: 28px 0; }
                        .btn { display: inline-block; padding: 14px 28px; background: linear-gradient(135deg, #255fd0, #27b0d7); color: #ffffff !important; text-decoration: none; border-radius: 999px; font-weight: 700; }
                        .link-box { margin-top: 24px; padding: 16px; background: #f4f8ff; border-radius: 14px; font-size: 14px; word-break: break-all; }
                        .footer { color: #6f7c95; font-size: 13px; margin-top: 24px; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>%s</h1>
                            <p>SmartRecruit</p>
                        </div>
                        <div class="content">
                            <h2>%s</h2>
                            %s
                            <div class="button-wrap">
                                <a href="%s" class="btn">%s</a>
                            </div>
                            %s
                            <div class="link-box">
                                <strong>Lien direct :</strong><br>
                                <a href="%s">%s</a>
                            </div>
                            <p class="footer">Cet email a ete envoye automatiquement. Merci de ne pas y repondre.</p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                title,
                greeting,
                introHtml,
                actionLink,
                actionLabel,
                footerHtml,
                actionLink,
                actionLink
        );
    }
}
