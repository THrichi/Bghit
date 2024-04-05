package com.application.bghit.services;

import com.application.bghit.config.UserAuthProvider;
import com.application.bghit.dtos.ConfirmationResponse;
import com.application.bghit.dtos.UserDto;
import com.application.bghit.entities.User;
import com.application.bghit.exceptions.AppException;
import com.application.bghit.repositories.UserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmailService {
    @Value("${security.jwt.token.secret-key:secret}")
    private String secretKey;
    @Value("${spring.mail.username}")
    private String principalEmail;
    private final JavaMailSender emailSender;
    private final UserAuthProvider userAuthProvider;
    private final UserRepository userRepository;

    public void sendConfirmationEmail(String to, String confirmationUrl, String temporaryPassword) throws MessagingException {
        // Création d'un message MimeMessage
        MimeMessage message = emailSender.createMimeMessage();

        // Helper pour faciliter la création du message
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(principalEmail);
        helper.setTo(to);
        helper.setSubject("Confirmation de votre inscription");

        String htmlContent = "<html>"
                + "<body style='margin: 0; padding: 0;'>"
                + "<div style='background-color: #f4f4f4; padding: 20px;'>"
                + "<div style='max-width: 600px; margin: auto; background: white; padding: 20px; font-family: Helvetica, Arial, sans-serif; font-size: 14px; color: #333;'>"
                + "<h2 style='color: #0056b3; font-size: 22px;'>Confirmation de l'inscription</h2>"
                + "<p>Cher(e) abonné(e),</p>"
                + "<p>Nous vous remercions de vous être inscrit(e) sur notre plateforme. Pour finaliser votre inscription et activer votre compte, veuillez cliquer sur le lien ci-dessous :</p>"
                + "<a href='" + confirmationUrl + "' style='background-color: #0056b3; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>Confirmer l'inscription</a>"
                + "<p>Voici votre mot de passe temporaire pour accéder à votre compte. Veuillez le changer dès votre première connexion :</p>"
                + "<div style='background-color: #f2f2f2; color: #333; padding: 10px; border-left: 3px solid #0056b3; font-family: monospace;'>"
                + temporaryPassword
                + "</div>"
                + "<div style='margin-top: 20px; text-align: center;'>"
                + "<img src='cid:logoImage' style='width: 100px;'/>"
                + "</div>"
                + "<p>Cordialement,</p>"
                + "<p>L'équipe de support</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";


        helper.setText(htmlContent, true);

        // Ajout du logo en tant que ressource inline
        helper.addInline("logoImage", new ClassPathResource("static/images/logo/L2.png"));

        // Envoi de l'email
        emailSender.send(message);
    }
    public void sendConfirmationUpdateEmail(User user,String newEmail, String confirmationUrl) throws MessagingException {
        // Création d'un message MimeMessage
        MimeMessage message = emailSender.createMimeMessage();

        // Helper pour faciliter la création du message
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(principalEmail);
        helper.setTo(newEmail);
        helper.setSubject("Confirmation de changement d'adresse e-mail");
        String htmlContent = "<html>"
                + "<body style='margin: 0; padding: 0;'>"
                + "<div style='background-color: #f4f4f4; padding: 20px;'>"
                + "<div style='max-width: 600px; margin: auto; background: white; padding: 20px; font-family: Helvetica, Arial, sans-serif; font-size: 14px; color: #333;'>"
                + "<h2 style='color: #0056b3; font-size: 22px;'>Confirmation de changement d'email</h2>"
                + "<p>Cher(e), "+ user.getLastName() +"</p>"
                + "<p>Nous avons reçu une demande pour changer l'adresse email associée à votre compte pour celle-ci: [" + newEmail + "]. Si vous avez initié cette demande, veuillez confirmer le changement en cliquant sur le lien ci-dessous :</p>"
                + "<a href='" + confirmationUrl + "' style='background-color: #0056b3; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>Confirmer l'inscription</a>"
                + "<p>Si vous n'êtes pas à l'origine de cette demande, veuillez ignorer cet email ou nous contacter immédiatement si vous suspectez une activité frauduleuse sur votre compte.</p>"
                + "<div style='margin-top: 20px; text-align: center;'>"
                + "<img src='cid:logoImage' style='width: 100px;'/>"
                + "</div>"
                + "<p>Cordialement,</p>"
                + "<p>L'équipe de support</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";

        helper.setText(htmlContent, true);

        // Ajout du logo en tant que ressource inline
        helper.addInline("logoImage", new ClassPathResource("static/images/logo/L2.png"));

        // Envoi de l'email
        emailSender.send(message);
    }


    public ConfirmationResponse confirmUser(String token) {
        try {
            Authentication authentication = userAuthProvider.validateToken(token);
            if (authentication == null) {
                return new ConfirmationResponse(false, ConfirmationResponse.confirmStatus.TOKEN_EXPIRED);
            }
            // Ici, nous supposons que l'objet Authentication contient l'email de l'utilisateur en tant que principal
            // ou vous pourriez avoir besoin d'ajuster cette partie selon la manière dont votre token est décodé
            String userEmail = userAuthProvider.getEmailFromToken(token); // Ou une autre méthode pour obtenir l'email depuis l'Authentication
            Optional<User> user = userRepository.findByEmail(userEmail);
            if (user.isEmpty()) {
                return new ConfirmationResponse(false, ConfirmationResponse.confirmStatus.EMAIL_NOT_FOUND);
            }
            if (user.get().isEmailValidated()) {
                return new ConfirmationResponse(false, ConfirmationResponse.confirmStatus.EMAIL_ALREADY_CONFIRMED);
            }

            user.get().setEmailValidated(true);
            user.get().getVerified().setEmailVerified(true);
            userRepository.save(user.get());

            return new ConfirmationResponse(true, ConfirmationResponse.confirmStatus.SUCCESS);
        } catch (Exception e) {
            return new ConfirmationResponse(false, ConfirmationResponse.confirmStatus.UNKNOWN);
        } catch (AppException e) {
            return new ConfirmationResponse(false, ConfirmationResponse.confirmStatus.TOKEN_EXPIRED);
        }
    }
    public ConfirmationResponse confirmUpdateEmail(String token) {
        try {
            Authentication authentication = userAuthProvider.validateToken(token);
            if (authentication == null) {
                return new ConfirmationResponse(false, ConfirmationResponse.confirmStatus.TOKEN_EXPIRED);
            }

            String userEmail = userAuthProvider.getEmailFromToken(token);

            Long userId = userAuthProvider.getUserIdFromToken(token);// Ou une autre méthode pour obtenir l'email depuis l'Authentication
            Optional<User> userCheck = userRepository.findByEmail(userEmail);
            if (userCheck.isPresent()) {
                return new ConfirmationResponse(false, ConfirmationResponse.confirmStatus.EMAIL_EXIST);
            }
            Optional<User> user = userRepository.findById(userId);
            if (user.isEmpty()) {
                return new ConfirmationResponse(false, ConfirmationResponse.confirmStatus.UNKNOWN);
            }

            user.get().setEmail(userEmail);
            user.get().setEmailValidated(true);
            user.get().getVerified().setEmailVerified(true);
            userRepository.save(user.get());

            return new ConfirmationResponse(true, ConfirmationResponse.confirmStatus.SUCCESS);
        } catch (Exception e) {
            return new ConfirmationResponse(false, ConfirmationResponse.confirmStatus.UNKNOWN);
        } catch (AppException e) {
            return new ConfirmationResponse(false, ConfirmationResponse.confirmStatus.TOKEN_EXPIRED);
        }
    }

    public void sendResetPasswordEmail(String to, String resetPasswordUrl) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(principalEmail);
        helper.setTo(to);
        helper.setSubject("Réinitialisation de votre mot de passe");

        String htmlContent = "<html>"
                + "<body style='margin: 0; padding: 0;'>"
                + "<div style='background-color: #f4f4f4; padding: 20px;'>"
                + "<div style='max-width: 600px; margin: auto; background: white; padding: 20px; font-family: Helvetica, Arial, sans-serif; font-size: 14px; color: #333;'>"
                + "<h2 style='color: #0056b3; font-size: 22px;'>Réinitialisation de votre mot de passe</h2>"
                + "<p>Vous avez demandé à réinitialiser votre mot de passe. Veuillez utiliser le lien ci-dessous pour définir un nouveau mot de passe. Ce lien sera valide pour <strong>15 minutes</strong> après la réception de cet email.</p>"
                + "<a href='" + resetPasswordUrl + "' style='display: inline-block; background-color: #0056b3; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; font-weight: bold;'>Réinitialiser mon mot de passe</a>"
                + "<p>Si vous n'avez pas demandé à réinitialiser votre mot de passe, veuillez ignorer cet email ou nous contacter.</p>"
                + "<div style='margin-top: 20px; text-align: center;'>"
                + "<img src='cid:logoImage' style='width: 100px;'/>"
                + "</div>"
                + "<p>Cordialement,</p>"
                + "<p>L'équipe de support</p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";

        helper.setText(htmlContent, true);
        helper.addInline("logoImage", new ClassPathResource("static/images/logo/L2.png"));
        emailSender.send(message);
    }

}