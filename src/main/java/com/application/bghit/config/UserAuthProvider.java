package com.application.bghit.config;

import com.application.bghit.dtos.UserDto;
import com.application.bghit.exceptions.AppException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.Person;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;

@RequiredArgsConstructor
@Component
public class UserAuthProvider {
    @Value("${security.jwt.token.secret-key:secret-key}")
    private String secretKey;

    @PostConstruct
    protected void init()
    {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }

    public String createToken(UserDto dto, boolean rememberMe)
    {
        Date now = new Date();
        long validityPeriodMs = 36_000_000; // 10 Heurs en millisecondes
        if (rememberMe) {
            // Augmentez la durée de validité pour "Se souvenir de moi", par exemple 7 jours
            validityPeriodMs = 604_800_000; // 7 jours en millisecondes
        }
        Date validity = new Date(now.getTime() + validityPeriodMs);
        return JWT.create()
                .withIssuer(dto.getEmail())
                .withIssuedAt(now)
                .withExpiresAt(validity)
                .sign(Algorithm.HMAC256(secretKey));
    }

    public String createEmailConfirmationToken(int time, String userEmail, Long userId) {
        Date now = new Date();
        long validityPeriodMs = time;
        Date validity = new Date(now.getTime() + validityPeriodMs);
        // Créer le builder de token JWT
        JWTCreator.Builder tokenBuilder = JWT.create()
                .withSubject(userEmail) // Utiliser 'subject' pour l'email de l'utilisateur
                .withIssuedAt(now)
                .withExpiresAt(validity);

        // Ajouter l'ID de l'utilisateur comme une claim personnalisée si non null
        tokenBuilder.withClaim("userEmail", userEmail);
        if (userId != null) {
            tokenBuilder.withClaim("userId", userId);
        }

        // Signer le token et le retourner
        return tokenBuilder.sign(Algorithm.HMAC256(secretKey));
    }
    /*public String getEmailFromToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getIssuer(); // L'email était stocké dans l'émetteur (issuer) du token
        } catch (JWTVerificationException exception) {
            // Logique de gestion des exceptions ou retourner null ou une chaîne vide
            System.err.println("Token invalide ou expiré: " + exception.getMessage());
            return null;
        }
    }*/
    public Long getUserIdFromToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token);
            // Récupération de l'ID de l'utilisateur comme une claim personnalisée
            return jwt.getClaim("userId").asLong();
        } catch (JWTVerificationException exception) {
            // Logique de gestion des exceptions ou retourner null
            System.err.println("Token invalide ou expiré: " + exception.getMessage());
            return null;
        }
    }
    public String getEmailFromToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token);
            // Récupération de l'ID de l'utilisateur comme une claim personnalisée
            return jwt.getClaim("userEmail").asString();
        } catch (JWTVerificationException exception) {
            // Logique de gestion des exceptions ou retourner null
            System.err.println("Token invalide ou expiré: " + exception.getMessage());
            return null;
        }
    }

    public Authentication validateToken(String token) throws AppException {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT decodedJWT = verifier.verify(token);

            UserDto user = UserDto.builder()
                    .email(decodedJWT.getIssuer())
                    .build();
            return new UsernamePasswordAuthenticationToken(user,null, Collections.emptyList());
        } catch (JWTVerificationException exception) {
            // Cette exception est levée si le token est invalide pour une quelconque raison (expiré, signature invalide, etc.)
            throw new AppException("Invalid or expired token", HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            // Gestion d'autres exceptions inattendues
            throw new AppException("Authentication failed", HttpStatus.UNAUTHORIZED);
        }
    }

    public String fetchUserEmailFromToken(String token) throws IOException {
        // Configure le transport HTTP et le JSON factory
        HttpTransport transport = new NetHttpTransport();
        JsonFactory jsonFactory = new GsonFactory();

        // Prépare la requête
        HttpRequestFactory requestFactory = transport.createRequestFactory();
        GenericUrl url = new GenericUrl("https://www.googleapis.com/oauth2/v2/userinfo");

        // Définit l'header d'autorisation avec le token
        HttpRequest request = requestFactory.buildGetRequest(url);
        request.getHeaders().setAuthorization("Bearer " + token);

        // Exécute la requête
        HttpResponse response = request.execute();

        // Traite la réponse pour extraire l'email
        String email = ""; // Initialise la variable email
        try {
            // Convertit la réponse en chaîne de caractères
            String userInfo = response.parseAsString();

            // Utilisez une bibliothèque JSON pour parser la réponse et extraire l'email
            // Exemple avec Gson (ajoutez la dépendance Gson à votre projet si nécessaire)
            com.google.gson.JsonParser parser = new com.google.gson.JsonParser();
            com.google.gson.JsonObject userInfoJson = parser.parse(userInfo).getAsJsonObject();

            if (userInfoJson.has("email")) {
                email = userInfoJson.get("email").getAsString();
            }
        } finally {
            response.disconnect();
        }

        return email;
    }
    public UserDto fetchUserInfoFromToken(String token) throws IOException {
        HttpTransport transport = new NetHttpTransport();
        JsonFactory jsonFactory = new GsonFactory();

        HttpRequestFactory requestFactory = transport.createRequestFactory();
        GenericUrl url = new GenericUrl("https://www.googleapis.com/oauth2/v2/userinfo");

        HttpRequest request = requestFactory.buildGetRequest(url);
        request.getHeaders().setAuthorization("Bearer " + token);

        HttpResponse response = request.execute();

        // Crée une nouvelle instance de UserDto
        UserDto user = new UserDto();

        try {
            String userInfo = response.parseAsString();
            com.google.gson.JsonParser parser = new com.google.gson.JsonParser();
            com.google.gson.JsonObject userInfoJson = parser.parse(userInfo).getAsJsonObject();

            if (userInfoJson.has("email")) {
                user.setEmail(userInfoJson.get("email").getAsString());
            }

            if (userInfoJson.has("name")) {
                user.setName(userInfoJson.get("name").getAsString());
            }

            if (userInfoJson.has("picture")) {
                user.setPicture(userInfoJson.get("picture").getAsString());
            }
        } finally {
            response.disconnect();
        }

        return user;
    }



}
