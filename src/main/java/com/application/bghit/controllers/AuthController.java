package com.application.bghit.controllers;

import com.application.bghit.config.UserAuthProvider;
import com.application.bghit.dtos.*;
import com.application.bghit.entities.User;
import com.application.bghit.exceptions.AppException;
import com.application.bghit.services.EmailService;
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

    private final EmailService emailService;
    private final UserService userService;
    private final UserAuthProvider userAuthProvider;
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
            UserDto user = userService.loginWithGoogle(userDto.getEmail());

            if(updateUser.isPresent())
            {
                User u = updateUser.get();
                if(u.getPicture().equals("/images/default-profile.jpg"))
                {
                    u.setPicture(userDto.getPicture());
                    //update database with new picture
                    userService.saveUser(u);

                }
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
        UserDto user = userService.register(signUpDto);
        user.setToken(userAuthProvider.createToken(user,false));
        String token = userAuthProvider.createEmailConfirmationToken(1_800_000,user.getEmail(),null);
        String confirmationUrl = "http://localhost:4200/confirm?token=" + token;
        emailService.sendConfirmationEmail(
                user.getEmail(),
                confirmationUrl
        );
        return ResponseEntity.created(URI.create("/users/"+user.getId())).body(user);
    }

    @PostMapping("/send-confirmation-email")
    public String sendConfirmationEmail(@RequestParam String userEmail) throws MessagingException {
        String token = userAuthProvider.createEmailConfirmationToken(1_800_000,userEmail,null);
        String confirmationUrl = "http://localhost:4200/confirm?token=" + token;
        emailService.sendConfirmationEmail(
                userEmail,
                confirmationUrl
        );
        return confirmationUrl;
    }

    @GetMapping("/confirm")
    public ResponseEntity<ConfirmationResponse> confirmRegistration(@RequestParam("token") String token) {
        ConfirmationResponse response = emailService.confirmUser(token);
        return ResponseEntity.ok(response);
    }

}
/*
*
* package com.application.bghit.controllers;

import com.application.bghit.config.UserAuthProvider;
import com.application.bghit.dtos.*;
import com.application.bghit.exceptions.AppException;
import com.application.bghit.services.UserService;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Date;

@RestController
@RequiredArgsConstructor
public class AuthController {

    @Value("${spring.security.oauth2.resourceserver.opaquetoken.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.resourceserver.opaquetoken.client-secret}")
    private String clientSecret;

    private final UserService userService;
    private final UserAuthProvider userAuthProvider;
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
    public ResponseEntity<UserDto> callback(@RequestParam("code") String code){

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
            UserDto user = UserDto.builder()
                    .email(userAuthProvider.fetchUserEmailFromToken(accessToken))
                    .build();
            user.setToken(userAuthProvider.createToken(user));
            return ResponseEntity.ok(user);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto> login ( @RequestBody CredentialsDto credentialsDto) throws AppException {
        UserDto user = userService.login(credentialsDto);
        user.setToken(userAuthProvider.createToken(user));
        return ResponseEntity.ok(user);
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> login ( @RequestBody SignUpDto signUpDto) throws AppException {
        UserDto user = userService.register(signUpDto);
        user.setToken(userAuthProvider.createToken(user));
        return ResponseEntity.created(URI.create("/users/"+user.getId())).body(user);
    }
}

*
* */