package com.application.bghit.services;

import com.application.bghit.dtos.*;
import com.application.bghit.entities.*;
import com.application.bghit.exceptions.AppException;
import com.application.bghit.repositories.PhotoCollectionRepository;
import com.application.bghit.repositories.SearchRepository;
import com.application.bghit.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private static String UPLOAD_DIR = "src/main/resources/static/images/users/collections/";
    private static String UPLOAD_PROFILe_DIR = "src/main/resources/static/images/users/profile/";
    private final Path rootLocation = Paths.get(UPLOAD_PROFILe_DIR);


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SearchRepository searchRepository;
    private final PhotoCollectionRepository photoCollectionRepository;

    public UserDto login(CredentialsDto credentialsDto) throws AppException {
        User user = userRepository.findByEmail(credentialsDto.login())
                .orElseThrow(() -> new AppException("Email not found", HttpStatus.NOT_FOUND));
        if(!passwordEncoder.matches(CharBuffer.wrap(credentialsDto.password()), user.getPassword())) {
            throw new AppException("Incorrect password", HttpStatus.UNAUTHORIZED);
        }
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                // .token(token) // Supposons que vous avez une manière de générer un token ici
                .build();
    }

    public Optional<User> findByEmail(String email)
    {
        return userRepository.findByEmail(email);
    }
    public Optional<User> findById(Long id)
    {
        return userRepository.findById(id);
    }

    public User saveUser(User user)
    {
        return userRepository.save(user);
    }
    public UserProfilDto convertUserToDto(User user) {
        // Conversion des favoris de recherche
        List<SearchDto> searchFavorisDto = user.getSearchFavoris().stream()
                .map(search -> new SearchDto(search.getId() /*, autres champs nécessaires*/))
                .collect(Collectors.toList());

        // Conversion des collections de photos
        List<PhotoCollectionDto> photosDto = user.getPhotos().stream()
                .map(photo -> new PhotoCollectionDto(photo.getId(), photo.getUrl()))
                .collect(Collectors.toList());

        // Conversion de l'objet Verified
        Verified verified = user.getVerified();
        VerifiedDto verifiedDto = new VerifiedDto(
                verified.isEmailVerified(),
                verified.isPhoneVerified(),
                verified.isProfileVerified(),
                verified.isIdentityVerified());

        // Calcul de la moyenne des ratings
        double averageRating = user.getRatings().isEmpty() ? 0.0 :
                user.getRatings().stream()
                        .mapToDouble(Rating::getRating)
                        .average()
                        .getAsDouble();

        //UserProfilDto dto = new UserProfilDto(user.getId(),user.getName(),user.getPicture(),user.getEmail(),user.ge)

        return new UserProfilDto(
                user.getId(),
                user.getName(),
                user.getLastName(),
                user.getDateNaissance(),
                user.getPicture(),
                user.getEmail(),
                user.isEmailValidated(),
                user.getTelephone(),
                user.getAdresse(),
                user.getLatitude(),
                user.getLongitude(),
                averageRating,
                user.getRatings(),
                user.getDateInscription(),
                user.isParticulier(),
                user.getAboutMe(),
                user.isProfileCompleted(),
                user.getAffairesConcluses(),
                user.isDisponibleALHeure(),
                StatusDto.valueOf(user.getStatus().name()), // Assurez-vous de convertir correctement l'énumération
                verifiedDto,
                searchFavorisDto,
                photosDto
        ); // Exemple de DTO avec username et email
    }

    public UserDto loginWithGoogle(String email) throws AppException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException("Unknown User", HttpStatus.NOT_FOUND));

        return UserDto.builder()
                .email(user.getEmail())
                .build();
    }
    public UserDto firstLoginWithGoogle(SignUpDto signUpDto) throws AppException {
        Optional<User> oUser = userRepository.findByEmail(signUpDto.email());
        if(oUser.isPresent())
        {
            this.loginWithGoogle(signUpDto.email());
        }
        String passwordAsString = new String(signUpDto.password());
        User newUser = new User();
        newUser.setEmail(signUpDto.email());
        newUser.setName(signUpDto.name());
        newUser.setPassword(passwordEncoder.encode(CharBuffer.wrap(passwordAsString)));

        newUser = userRepository.save(newUser);

        return convertToDto(newUser);
    }

    public UserDto register(SignUpDto signUpDto) throws AppException {
        Optional<User> oUser = userRepository.findByEmail(signUpDto.email());
        System.out.println(signUpDto);
        if(oUser.isPresent())
        {
            throw new AppException("Login Alrdy Exists", HttpStatus.BAD_REQUEST);
        }
        String passwordAsString = new String(signUpDto.password());

        List<PhotoCollection> collections = new ArrayList<>();
        List<Search> searchFavoris = new ArrayList<>();
        User newUser = new User();
        newUser.setEmail(signUpDto.email());
        newUser.setName(signUpDto.name());
        newUser.setPassword(passwordEncoder.encode(CharBuffer.wrap(passwordAsString)));
        newUser.setPhotos(collections);
        newUser.setSearchFavoris(searchFavoris);
        newUser = userRepository.save(newUser);

        return convertToDto(newUser);

    }
    public static UserDto convertToDto(User user) {
        if (user == null) {
            return null;
        }

        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                //.token("Votre logique pour générer/obtenir le token")
                .build();
    }

    public static String getCurrentUserEmail(){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;

        if (principal instanceof String) {
            // Le principal est directement l'email
            email = principal.toString();
        } else if (principal instanceof UserDto) {
            // Si le principal est un UserDto, extrayez l'email de cet objet
            email = ((UserDto) principal).getEmail();
        } else {
            // Gérez les autres types ou erreurs potentielles
            throw new IllegalStateException("Type de principal inconnu");
        }
        return email;
    }

    public void savePhotoCollention(MultipartFile[] images, User user) throws AppException {
        for (MultipartFile file : images) {
            // Génération d'un nom de fichier unique pour chaque fichier

            Path userDirectory = Paths.get(UPLOAD_DIR, user.getId().toString());
            if (!Files.exists(userDirectory)) {
                try {
                    Files.createDirectories(userDirectory);
                } catch (IOException e) {
                    throw new AppException("Impossible de créer le répertoire pour l'utilisateur", HttpStatus.BAD_REQUEST);
                }
            }

            String uniqueFileName = user.getId()+"/"+ UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path fileNameAndPath = Paths.get(UPLOAD_DIR, uniqueFileName);// Utilisation du nom de fichier unique pour le retour

            try {
                Files.write(fileNameAndPath, file.getBytes());
                PhotoCollection photoC = new PhotoCollection();
                photoC.setUrl(uniqueFileName);
                user.addPhotoCollection(photoC);
                photoCollectionRepository.save(photoC);
            } catch (IOException e) {
                throw new AppException("Erreur lors de la sauvegarde des fichiers", HttpStatus.BAD_REQUEST);
            }
        }
        saveUser(user);
    }

    public void deletePhoto(String userEmail, Long photoId) throws AppException {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new AppException("Utilisateur non trouvé", HttpStatus.NOT_FOUND));
        PhotoCollection photo = photoCollectionRepository.findById(photoId)
                .orElseThrow(() -> new AppException("Photo non trouvée", HttpStatus.NOT_FOUND));

        if (!user.getPhotos().contains(photo)) {
            throw new AppException("La photo n'appartient pas à l'utilisateur", HttpStatus.BAD_REQUEST);
        }

        user.removeImage(photo); // Suppression de la relation
        userRepository.save(user); // Sauvegarde de l'utilisateur mis à jour

        photoCollectionRepository.delete(photo); // Suppression de la photo de la base de données

        Path pathToFile = Paths.get(UPLOAD_DIR, photo.getUrl());
        try {
            Files.deleteIfExists(pathToFile); // Suppression du fichier
        } catch (IOException e) {
            throw new AppException("Erreur lors de la suppression du fichier", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public User updateUser(User user, UserUpdateDto userDetails) {
        if (userDetails.name() != null && !userDetails.name().equals(user.getName())) {
            user.setName(userDetails.name());
        }
        if (userDetails.lastName() != null && !userDetails.lastName().equals(user.getLastName())) {
            user.setLastName(userDetails.lastName());
        }
        if (userDetails.telephone() != null && !userDetails.telephone().equals(user.getTelephone())) {
            Verified verified = user.getVerified();
            verified.setPhoneVerified(false);
            user.setTelephone(userDetails.telephone());
            user.setVerified(verified);
        }
        if (userDetails.dateNaissance() != null && !userDetails.dateNaissance().equals(user.getDateNaissance())) {
            user.setDateNaissance(userDetails.dateNaissance());
        }
        if (userDetails.adresse() != null && !userDetails.adresse().equals(user.getAdresse())) {
            user.setAdresse(userDetails.adresse());
        }
        if (userDetails.latitude() != null && !userDetails.latitude().equals(user.getLatitude())) {
            user.setLatitude(userDetails.latitude());
        }
        if (userDetails.longitude() != null && !userDetails.longitude().equals(user.getLongitude())) {
            user.setLongitude(userDetails.longitude());
        }
        if (userDetails.disponibleALHeure() != user.isDisponibleALHeure()) {
            user.setDisponibleALHeure(userDetails.disponibleALHeure());
        }
        return userRepository.save(user);

    }
    public void savePhoto(MultipartFile image, User user) throws AppException {
        // Générer un nom de fichier unique pour l'image
        String uniqueFileName = user.getId() + "/" + UUID.randomUUID().toString() + "_" + image.getOriginalFilename();
        Path fileNameAndPath = Paths.get(UPLOAD_PROFILe_DIR, uniqueFileName);

        // Assurer l'existence du répertoire de l'utilisateur
        Path userDirectory = Paths.get(UPLOAD_PROFILe_DIR, user.getId().toString());
        if (!Files.exists(userDirectory)) {
            try {
                Files.createDirectories(userDirectory);
            } catch (IOException e) {
                throw new AppException("Impossible de créer le répertoire pour l'utilisateur.", HttpStatus.BAD_REQUEST);
            }
        }

        // Sauvegarder l'image
        try {
            Files.write(fileNameAndPath, image.getBytes());

            // Mettre à jour le chemin de la photo de profil dans l'objet User
            user.setPicture(uniqueFileName);
            saveUser(user); // Assurez-vous que cette méthode enregistre l'utilisateur dans la base de données
        } catch (IOException e) {
            throw new AppException("Erreur lors de la sauvegarde de l'image.", HttpStatus.BAD_REQUEST);
        }
    }
}
