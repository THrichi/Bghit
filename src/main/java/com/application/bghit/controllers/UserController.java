package com.application.bghit.controllers;

import com.application.bghit.config.UserAuthProvider;
import com.application.bghit.dtos.*;
import com.application.bghit.entities.*;
import com.application.bghit.exceptions.AppException;
import com.application.bghit.repositories.RatingRepository;
import com.application.bghit.repositories.SearchRepository;
import com.application.bghit.services.DemandeService;
import com.application.bghit.services.EmailService;
import com.application.bghit.services.TwilioSmsSenderService;
import com.application.bghit.services.UserService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
public class UserController {

    private final UserService userService;
    private final RatingRepository ratingRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final UserAuthProvider userAuthProvider;
    private final TwilioSmsSenderService smsSenderService;
    private final DemandeService demandeService;
    private final SearchRepository searchRepository;

    @GetMapping("/currentUser")
    public ResponseEntity<UserProfilDto> currentUser() throws AppException {

        String email = UserService.getCurrentUserEmail();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new AppException("Unknown User", HttpStatus.NOT_FOUND));
        UserProfilDto userDto = userService.convertUserToDto(user);
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/user/{idUser}")
    public ResponseEntity<UserProfilDto> currentUser(
            @PathVariable Long idUser
            ) throws AppException {
        User user = userService.findById(idUser)
                .orElseThrow(() -> new AppException("Unknown User", HttpStatus.NOT_FOUND));
        UserProfilDto userDto = userService.convertUserToDto(user);
        return ResponseEntity.ok(userDto);
    }
    @PostMapping("/user/addRating")
    public ResponseEntity<?> addRating(
            @RequestParam Long userId,
            @RequestBody Rating rating) throws AppException {
        try {
            User user = userService.findById(userId)
                    .orElseThrow(() -> new AppException("Unknown User", HttpStatus.NOT_FOUND));
            if(ratingRepository.existsByRaterIdAndUserId(rating.getRaterId(),rating.getUserId())){
                return ResponseEntity.status(HttpStatus.CONFLICT).body("La note pour cet utilisateur ne peut pas être dupliquée.");
            }
            ratingRepository.save(rating);
            Double userRating = ratingRepository.findAverageRating(userId);
            user.setRating(userRating);
            user.getRatings().add(rating);
            userService.saveUser(user);
            return ResponseEntity.ok(true);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("La note pour cet utilisateur ne peut pas être dupliquée.");
        } catch (Exception e) {
            // Gérer d'autres exceptions non spécifiques
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Une erreur interne est survenue.");
        }
    }

    @GetMapping("/user/checkRate/{raterId}/{userId}")
    public ResponseEntity<Boolean> checkUserHasRated(@PathVariable Long raterId, @PathVariable Long userId) {
        boolean hasRated = ratingRepository.existsByRaterIdAndUserId(raterId, userId);
        return ResponseEntity.ok(hasRated);
    }
    @GetMapping("/user/updateAboutMe")
    public ResponseEntity<Boolean> updateAboutMe(
            @RequestParam String aboutMe) throws AppException {

        String email = UserService.getCurrentUserEmail();

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new AppException("Unknown User", HttpStatus.NOT_FOUND));
        user.setAboutMe(aboutMe);
        userService.saveUser(user);
        return ResponseEntity.ok(true);
    }

    @PostMapping("/user/uploadPhotosCollection")
    public ResponseEntity<?> uploadUserPhotos(@RequestParam("files") MultipartFile[] files) throws AppException {
        if(files.length == 0) {
            return ResponseEntity.badRequest().body("Aucun fichier fourni.");
        }
        String email = UserService.getCurrentUserEmail();

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new AppException("Unknown User", HttpStatus.NOT_FOUND));

        try {
            userService.savePhotoCollention(files,user);

            return ResponseEntity.ok().body(true);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors du téléchargement des images.");
        }
    }
    @PostMapping("/user/uploadPhoto")
    public ResponseEntity<?> uploadUserPhoto(@RequestParam("file") MultipartFile file) throws AppException {
        if(file.isEmpty()) {
            return ResponseEntity.badRequest().body("Aucun fichier fourni.");
        }
        String email = UserService.getCurrentUserEmail();

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new AppException("Utilisateur inconnu", HttpStatus.NOT_FOUND));

        try {
            userService.savePhoto(file, user); // Assurez-vous que la méthode `savePhoto` est implémentée dans votre service

            return ResponseEntity.ok().body("Photo de profil mise à jour avec succès.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erreur lors du téléchargement de l'image.");
        }
    }

    @DeleteMapping("/user/removePhotoCollection")
    public ResponseEntity<?> deletePhoto(@RequestParam("photoId") Long photoId) {
        try {
            userService.deletePhoto(UserService.getCurrentUserEmail(), photoId);
            return ResponseEntity.ok().build();
        } catch (AppException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/user/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changePassword(@RequestBody PasswordChangeDto request) throws AppException {
        // Vous devriez valider la demande ici

        String userEmail = UserService.getCurrentUserEmail();
        User user = userService.findByEmail(userEmail)
                .orElseThrow(() -> new AppException("Unknown User", HttpStatus.NOT_FOUND));


        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new AppException("L'ancien mot de passe est incorrect", HttpStatus.BAD_REQUEST);
        }

        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new AppException("Confirmation de mot de passe incorrect", HttpStatus.BAD_REQUEST);
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userService.saveUser(user);
        return new ResponseEntity<>("Mot de passe changé avec succès", HttpStatus.OK);

    }

    @PutMapping("/user/update")
    public ResponseEntity<?> updateUser(@RequestBody UserUpdateDto userDetails) throws AppException {

        String email = UserService.getCurrentUserEmail();

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new AppException("Unknown User", HttpStatus.NOT_FOUND));
        userService.updateUser(user,userDetails);
        return ResponseEntity.ok(true);
    }

    @PatchMapping("/user/update-profile-image")
    public ResponseEntity<?> updateProfileImage(@RequestParam("image") String image) throws AppException {

        String email = UserService.getCurrentUserEmail();

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new AppException("Unknown User", HttpStatus.NOT_FOUND));
        user.setPicture(image);
        userService.saveUser(user);
        return ResponseEntity.ok(true);
    }

    @PutMapping("/user/complete-profil")
    public ResponseEntity<User> completeProfil(@RequestBody UserUpdateDto userDetails) throws AppException {

        String email = UserService.getCurrentUserEmail();

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new AppException("Unknown User", HttpStatus.NOT_FOUND));

        User updatedUser = userService.completeUser(user,userDetails);
        return ResponseEntity.ok(updatedUser);
    }


    @PostMapping("/user/send-confirmation-update-email")
    public ResponseEntity<?> sendConfirmationUpdateEmail(@RequestParam Long userId, @RequestParam String userEmail, @RequestParam String password) throws MessagingException, AppException {
        Optional<User> userCheck = userService.findByEmail(userEmail);
        if (userCheck.isPresent()) {
            return ResponseEntity.ok(new ConfirmationResponse(false, ConfirmationResponse.confirmStatus.EMAIL_EXIST)) ;
        }
        String email = UserService.getCurrentUserEmail();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new AppException("Unknown User", HttpStatus.NOT_FOUND));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AppException("L'ancien mot de passe est incorrect", HttpStatus.BAD_REQUEST);
        }
        String token = userAuthProvider.createEmailConfirmationToken(300_000,userEmail,userId);
        String confirmationUrl = "http://localhost:4200/confirm?token=" + token +"&update="+true;
        emailService.sendConfirmationUpdateEmail(
                user,
                userEmail,
                confirmationUrl
        );
        return ResponseEntity.ok(new ConfirmationResponse(true, ConfirmationResponse.confirmStatus.SUCCESS));
    }
    @GetMapping("/user/confirm-update-email")
    public ResponseEntity<ConfirmationResponse> confirmRegistration(@RequestParam("token") String token) {
        ConfirmationResponse response = emailService.confirmUpdateEmail(token);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/user/send-sms-verification")
    public void startVerification(@RequestBody String phoneNumber) {
        smsSenderService.startVerification(phoneNumber);
    }

    @PostMapping("/user/verify-sms")
    public ResponseEntity<?> verifyCode(@RequestBody SmsRequestDto smsRequest) throws AppException {
        String email = UserService.getCurrentUserEmail();
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new AppException("Unknown User", HttpStatus.NOT_FOUND));
        boolean verify = smsSenderService.verifyCode(smsRequest.from(), smsRequest.code());
        if(!verify)throw new AppException("Unknown User", HttpStatus.NOT_FOUND);

        Verified verified = user.getVerified();
        verified.setPhoneVerified(true);
        user.setVerified(verified);
        userService.saveUser(user);
        return ResponseEntity.ok(true);
    }
    @PatchMapping("/user/addAffaireConclus")
    public ResponseEntity<?> addAffaireConclus(@RequestParam("userId") Long userId) throws AppException {
        Optional<User> optionalUser = userService.findById(userId);
        if(optionalUser.isEmpty())throw new AppException("Unknown User", HttpStatus.NOT_FOUND);
        User user =  optionalUser.get();
        user.setAffairesConcluses(user.getAffairesConcluses()+1);
        userService.saveUser(user);
        return ResponseEntity.ok(true);
    }
    @PatchMapping("/user/addFavoris")
    public ResponseEntity<?> addFavoris(@RequestParam("demandeId") Long demandeId) throws AppException {
        try {
            Optional<Demande> optionalDemande = demandeService.findDemandeById(demandeId);
            if(optionalDemande.isEmpty())throw new AppException("Demande Not Found",HttpStatus.NOT_FOUND);
            Demande demande = optionalDemande.get();
            return ResponseEntity.ok(userService.addDemandeFavoris(demande));
        }catch (Exception e)
        {
            throw new AppException("Duplicated",HttpStatus.BAD_REQUEST);
        }
    }

    @PatchMapping("/user/removeFavoris")
    public ResponseEntity<?> removeFavoris(@RequestParam("demandeId") Long demandeId) throws AppException {
        try {
            Optional<Demande> optionalDemande = demandeService.findDemandeById(demandeId);
            if(optionalDemande.isEmpty())throw new AppException("Demande Not Found",HttpStatus.NOT_FOUND);
            Demande demande = optionalDemande.get();
            return ResponseEntity.ok(userService.removeDemandeFavoris(demande));
        }catch (Exception e)
        {
            throw new AppException("Duplicated",HttpStatus.BAD_REQUEST);
        }
    }

    @PatchMapping("/user/removeSearchFavoris")
    public ResponseEntity<?> removeSearchFavoris(@RequestParam("searchId") Long searchId) throws AppException {
        try {
            Optional<Search> optionalDemande = searchRepository.findById(searchId);
            if(optionalDemande.isEmpty())throw new AppException("Demande Not Found",HttpStatus.NOT_FOUND);
            Search search = optionalDemande.get();
            userService.removeSearchFavoris(search);
            searchRepository.delete(search);
            return ResponseEntity.ok(true);
        }catch (Exception e)
        {
            throw new AppException("Duplicated",HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/user/addSearchFavoris")
    public ResponseEntity<?> addSearchFavoris(@RequestBody Search search) throws AppException {
        try {
            return ResponseEntity.ok(userService.addSearchFavoris(search));
        }catch (Exception e){
            throw new AppException("Duplicated",HttpStatus.BAD_REQUEST);
        }
    }
    @PostMapping("/user/updateSettings")
    public ResponseEntity<?> updateSettings(@RequestBody Settings settings) throws AppException {
        try {
            userService.updateSettings(settings);
            return ResponseEntity.ok(true);
        }catch (Exception e)
        {
            throw new AppException("Settings Not Found",HttpStatus.BAD_REQUEST);
        }
    }


}
