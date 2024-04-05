package com.application.bghit.controllers;

import com.application.bghit.config.UserAuthProvider;
import com.application.bghit.dtos.*;
import com.application.bghit.entities.User;
import com.application.bghit.exceptions.AppException;
import com.application.bghit.services.EmailService;
import com.application.bghit.services.PasswordGenerator;
import com.application.bghit.services.UserService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class AuthController {

    @Value("${spring.security.oauth2.resourceserver.opaquetoken.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.resourceserver.opaquetoken.client-secret}")
    private String clientSecret;
    @Value("${environment.default.profile.image}")
    private String defaultImage;
    private final EmailService emailService;
    private final UserService userService;
    private final UserAuthProvider userAuthProvider;
    private final PasswordEncoder passwordEncoder;
    @GetMapping("/auth/url")
    public ResponseEntity<UrlDto> auth(){
    String url = new GoogleAuthorizationCodeRequestUrl(
            clientId,
            "http://localhost:4200",
            Arrays.asList("email","profile","openid")
    ).build();
    return ResponseEntity.ok(new UrlDto(url));
    }

    @GetMapping("/auth/callback")
    public ResponseEntity<TokenDto> callback(@RequestParam("code") String code){
        try {
            // Échange le code contre un token
            GoogleTokenResponse response = new GoogleAuthorizationCodeTokenRequest(
                    new NetHttpTransport(),
                    new GsonFactory(),
                    clientId,
                    clientSecret,
                    code,
                    "http://localhost:4200" // Redirect URI
            ).execute();
            String accessToken = response.getAccessToken();
            UserDto userDto = userAuthProvider.fetchUserInfoFromToken(accessToken);
            Optional<User> updateUser = userService.findByEmail(userDto.getEmail());
            UserDto user;
            if(updateUser.isEmpty())
            {
                String randomPassword = PasswordGenerator.generateRandomPassword();
                user = userService.register(userDto.getEmail(),randomPassword,true);
            }else{
                User u = updateUser.get();
                if(u.getPicture().equals(defaultImage))
                {
                    u.setPicture(userDto.getPicture());
                    userService.saveUser(u);
                }
                user = userService.convertToUserDto(u);
            }

            return ResponseEntity.ok(new TokenDto(userAuthProvider.createToken(user,false)));

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (AppException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto> login ( @RequestBody CredentialsDto credentialsDto) throws AppException {
        UserDto user = userService.login(credentialsDto);
        user.setToken(userAuthProvider.createToken(user,credentialsDto.rememberMe()));
        return ResponseEntity.ok(user);
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> login ( @RequestBody SignUpDto signUpDto) throws AppException, MessagingException {
        String randomPassword = PasswordGenerator.generateRandomPassword();
        UserDto user = userService.register(signUpDto.email(),randomPassword,false);
        user.setToken(userAuthProvider.createToken(user,false));
        String token = userAuthProvider.createEmailConfirmationToken(1_800_000,user.getEmail(),null);
        String confirmationUrl = "http://localhost:4200/confirm?token=" + token;
        emailService.sendConfirmationEmail(
                user.getEmail(),
                confirmationUrl,
                randomPassword
        );
        return ResponseEntity.created(URI.create("/users/"+user.getId())).body(user);
    }

    @PostMapping("/send-confirmation-email")
    public String sendConfirmationEmail(@RequestParam String userEmail) throws MessagingException, AppException {
        String token = userAuthProvider.createEmailConfirmationToken(1_800_000,userEmail,null);
        String confirmationUrl = "http://localhost:4200/confirm?token=" + token;
        User user = userService.findByEmail(userEmail)
                .orElseThrow(() -> new AppException("Unknown User", HttpStatus.NOT_FOUND));
        if(!user.isEmailValidated())
        {
            String randomPassword = userService.resetUserPassword(user);
            emailService.sendConfirmationEmail(
                    userEmail,
                    confirmationUrl,
                    randomPassword
            );
            return confirmationUrl;
        }
        throw new AppException("Email Already validated", HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/confirm")
    public ResponseEntity<ConfirmationResponse> confirmRegistration(@RequestParam("token") String token) {
        ConfirmationResponse response = emailService.confirmUser(token);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/auth/request-password-reset")
    public ResponseEntity<?> requestPasswordReset(@RequestParam("email") String email) throws MessagingException, AppException {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new AppException("Utilisateur inconnu", HttpStatus.NOT_FOUND));
        String token = userAuthProvider.generateToken(user.getEmail());
        String resetLink = "http://localhost:4200/reset-password?token=" + token;
        emailService.sendResetPasswordEmail(email,resetLink);
        return ResponseEntity.ok(true);
    }
    @PostMapping("/auth/validate-reset-token")
    public ResponseEntity<ConfirmationResponse> validateResetToken(@RequestBody TokenDto tokenDto) throws AppException {
        if(!userAuthProvider.valideResetPasswordsToken(tokenDto.token()))throw new AppException("TOKEN EXPIRED",HttpStatus.UNAUTHORIZED);
        String email = userAuthProvider.getEmailFromToken(tokenDto.token());
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new AppException("Utilisateur inconnu", HttpStatus.NOT_FOUND));
        if (user.getResetPasswordToken() != null && user.getResetPasswordToken().equals(tokenDto.token())) throw new AppException("URLUSED",HttpStatus.UNAUTHORIZED);
        return ResponseEntity.ok(new ConfirmationResponse(true, ConfirmationResponse.confirmStatus.SUCCESS));
    }

    @PostMapping("/auth/reset-password")
    public ResponseEntity<UserDto> ResetPassword(@RequestBody ResetPasswordDto resetPasswordDto) throws AppException{
        String email = userAuthProvider.getEmailFromToken(resetPasswordDto.token());
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new AppException("Utilisateur inconnu", HttpStatus.NOT_FOUND));
        if(!userAuthProvider.valideResetPasswordsToken(resetPasswordDto.token()))throw new AppException("TOKEN_EXPIRED",HttpStatus.UNAUTHORIZED);
        if (user.getResetPasswordToken() != null && user.getResetPasswordToken().equals(resetPasswordDto.token())) throw new AppException("URLUSED",HttpStatus.UNAUTHORIZED);
        String hashedPassword = passwordEncoder.encode(resetPasswordDto.password());
        user.setPassword(hashedPassword);
        user.setResetPasswordToken(resetPasswordDto.token());
        userService.saveUser(user);
        CredentialsDto credentialsDto = new CredentialsDto(user.getEmail(), resetPasswordDto.password().toCharArray(),false);
        UserDto userDto = userService.login(credentialsDto);
        userDto.setToken(userAuthProvider.createToken(userDto,credentialsDto.rememberMe()));
        return ResponseEntity.ok(userDto);
    }
}