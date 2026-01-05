package com.app.sportify_backend.services;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender){
        this.mailSender = mailSender;
    }

    // Méthode pour envoyer l'OTP
    public void sendOtpEmail(String to, String otp){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Réinitialisation de mot de passe Sportify");
        message.setText("Bonjour,\n\nVotre code OTP pour réinitialiser votre mot de passe est : " + otp +
                "\nIl est valide pendant 5 minutes.\n\nSportify");
        mailSender.send(message);
    }
}
