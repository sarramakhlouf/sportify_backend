package com.app.sportify_backend.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name:Sportify}")
    private String appName;

    public void sendOtpEmail(String to, String otp){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Réinitialisation de mot de passe Sportify");
        message.setText("Bonjour,\n\nVotre code OTP pour réinitialiser votre mot de passe est : " + otp +
                "\nIl est valide pendant 5 minutes.\n\nSportify");
        mailSender.send(message);
    }

    public void sendManagerActivationByAdminEmail(String to, String firstname, int pitchCount) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Compte activé - Bienvenue sur " + appName + " !");
            message.setText(String.format(
                    "Bonjour %s,\n\n" +
                            "Excellente nouvelle ! 🎉\n\n" +
                            "Votre compte responsable terrain a été validé et activé par notre équipe.\n\n" +
                            "📍 %d terrain(s) ont été ajouté(s) à la plateforme et sont maintenant visibles par les utilisateurs.\n\n" +
                            "Vous pouvez maintenant :\n" +
                            "- Gérer vos terrains\n" +
                            "- Recevoir et gérer des réservations\n" +
                            "- Suivre l'activité de vos terrains\n\n" +
                            "Connectez-vous dès maintenant pour commencer !\n\n" +
                            "Cordialement,\n" +
                            "L'équipe %s",
                    firstname, pitchCount, appName
            ));

            mailSender.send(message);
            log.info("Email d'activation envoyé à : {}", to);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email d'activation à : {}", to, e);
        }
    }

    public void sendManagerDeactivationEmail(String to, String firstname) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Compte désactivé - " + appName);
            message.setText(String.format(
                    "Bonjour %s,\n\n" +
                            "Nous vous informons que votre compte responsable terrain a été désactivé.\n\n" +
                            "Vos terrains ne sont plus visibles sur la plateforme.\n\n" +
                            "Si vous pensez qu'il s'agit d'une erreur, veuillez contacter notre support.\n\n" +
                            "Cordialement,\n" +
                            "L'équipe %s",
                    firstname, appName
            ));

            mailSender.send(message);
            log.info("Email de désactivation envoyé à : {}", to);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de l'email de désactivation à : {}", to, e);
        }
    }

    public void notifyAdminNewManagerRegistration(String adminEmail, String managerName, String managerEmail) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(adminEmail);
            message.setSubject("[Admin] Nouvelle inscription Manager - " + appName);
            message.setText(String.format(
                    "Bonjour Admin,\n\n" +
                            "Une nouvelle inscription de responsable terrain est en attente de validation :\n\n" +
                            "Nom : %s\n" +
                            "Email : %s\n\n" +
                            "Veuillez vous connecter au backoffice pour valider ou rejeter cette demande.\n\n" +
                            "Système %s",
                    managerName, managerEmail, appName
            ));

            mailSender.send(message);
            log.info("Notification admin envoyée pour nouveau manager : {}", managerEmail);
        } catch (Exception e) {
            log.error("Erreur lors de l'envoi de la notification admin", e);
        }
    }
}
