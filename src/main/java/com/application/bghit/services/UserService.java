package com.application.bghit.services;

import com.application.bghit.dtos.*;
import com.application.bghit.entities.*;
import com.application.bghit.exceptions.AppException;
import com.application.bghit.repositories.PhotoCollectionRepository;
import com.application.bghit.repositories.SearchRepository;
import com.application.bghit.repositories.SettingsRepository;
import com.application.bghit.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
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
    private final SettingsRepository settingsRepository;

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

        List<DemandeListDto> demandeDto = user.getFavoris().stream()
                .map(this::convertToDto)
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
                user.getSearchFavoris(),
                photosDto,
                demandeDto,
                user.getSettings()
        ); // Exemple de DTO avec username et email
    }
    public DemandeListDto convertToDto(Demande demande) {
        if(demande != null)
        {
            List<String> imagesUrls = demande.getImages().stream()
                    .map(Image::getUrl) // Utilisation directe de l'URL de l'image
                    .collect(Collectors.toList());
            return new DemandeListDto(
                    demande.getIdDemande(),
                    demande.getDescription(),
                    demande.getDateCreation(),
                    demande.isEstPayant(),
                    demande.isSurDevis(),
                    demande.getPrix(),
                    demande.getCategorie(),
                    demande.getEtat(),
                    demande.getLieu(),
                    demande.getLatitude(),
                    demande.getLongitude(),
                    demande.getNombreDeVues(),
                    demande.getNombreDeReponses(),
                    imagesUrls,
                    demande.getTheme(),
                    convertToUserDto(demande.getUser()),
                    demande.getReservedToIdUser(),
                    demande.getType()
            );
        }
        return null;
    }
    public UserDto convertToUserDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setName(user.getName());
        userDto.setLastName(user.getLastName());
        userDto.setPicture(user.getPicture());
        userDto.setEmail(user.getEmail());
        userDto.setTelephone(user.getTelephone());
        userDto.setProfilCompleted(user.isProfileCompleted());
        userDto.setEmailVerified(user.isEmailValidated());
        userDto.setRating(user.getRating());
        userDto.setDateInscription(user.getDateInscription());
        userDto.setAffairesConcluses(user.getAffairesConcluses());
        return userDto;
    }
    public UserDto loginWithGoogle(String email) throws AppException {
        return UserDto.builder()
                .email(email)
                .build();
    }
    /*public UserDto firstLoginWithGoogle(String email) throws AppException {
        String passwordAsString = PasswordGenerator.generateRandomPassword();
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setName("");
        newUser.setLastName("");
        newUser.setPassword(passwordEncoder.encode(CharBuffer.wrap(passwordAsString)));
        newUser.setEmailValidated(true);
        newUser = userRepository.save(newUser);

        return convertToDto(newUser);
    }*/
    public String resetUserPassword(User user) {
        // Générer un nouveau mot de passe temporaire
        String randomPassword = PasswordGenerator.generateRandomPassword();
        // Hacher le mot de passe temporaire
        String hashedPassword = passwordEncoder.encode(randomPassword);

        // Récupérer l'utilisateur par son email et mettre à jour son mot de passe
        user.setPassword(hashedPassword);
        userRepository.save(user);
        return randomPassword;
    }
    @Transactional
    public UserDto register(String email,String randomPassword, boolean autoValidateEmail) throws AppException {
        Optional<User> oUser = userRepository.findByEmail(email);
        if(oUser.isPresent())
        {
            throw new AppException("Login Alrdy Exists", HttpStatus.BAD_REQUEST);
        }

        List<PhotoCollection> collections = new ArrayList<>();
        List<Search> searchFavoris = new ArrayList<>();
        User newUser = new User();
        newUser.setEmail(email);
        newUser.setName("");
        newUser.setLastName("");
        newUser.setPassword(passwordEncoder.encode(CharBuffer.wrap(randomPassword)));
        newUser.setPhotos(collections);
        newUser.setSearchFavoris(searchFavoris);
        newUser.setEmailValidated(autoValidateEmail);
        // Sauvegarder d'abord l'utilisateur sans la configuration pour obtenir un ID
        User savedUser = userRepository.save(newUser);

        // Créer et sauvegarder les paramètres de l'utilisateur
        Settings settings = new Settings();
        // settings.setUser(savedUser); // Inutile si vous sauvegardez `User` en premier et établissez la relation dans `User`
        settingsRepository.save(settings);

        // Établir la relation et sauvegarder à nouveau l'utilisateur
        savedUser.setSettings(settings);
        userRepository.save(savedUser);

        return convertToDto(savedUser);

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
    public User completeUser(User user, UserUpdateDto userDetails) {
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
        if (userDetails.password() != null) {
            String hashedPassword = passwordEncoder.encode(userDetails.password());
            user.setPassword(hashedPassword);
        }
        if (userDetails.image() != null && !userDetails.image().isEmpty()) {
            user.setPicture(userDetails.image());
        }
        user.setProfileCompleted(true);
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
    public boolean addDemandeFavoris(Demande demande) throws AppException {
        String email = getCurrentUserEmail();
        Optional<User> optionalUser = findByEmail(email);
        if(optionalUser.isEmpty())throw new AppException("User Not Found",HttpStatus.NOT_FOUND);
        User user = optionalUser.get();
        if(user.getFavoris().contains(demande))throw new AppException("Deja ajouter",HttpStatus.NOT_FOUND);
        user.getFavoris().add(demande);
        saveUser(user);
        return true;
    }
    public boolean removeDemandeFavoris(Demande demande) throws AppException {
        String email = getCurrentUserEmail();
        Optional<User> optionalUser = findByEmail(email);
        if(optionalUser.isEmpty())throw new AppException("User Not Found",HttpStatus.NOT_FOUND);
        User user = optionalUser.get();
        if(!user.getFavoris().contains(demande))throw new AppException("n existe pas",HttpStatus.NOT_FOUND);
        user.getFavoris().remove(demande);
        saveUser(user);
        return true;
    }

    public boolean addSearchFavoris(Search search) throws AppException {
        String email = getCurrentUserEmail();
        Optional<User> optionalUser = findByEmail(email);
        if(optionalUser.isEmpty())throw new AppException("User Not Found",HttpStatus.NOT_FOUND);
        User user = optionalUser.get();
        search.setUserId(user.getId());
        for (Search existingSearch : user.getSearchFavoris()) {
            if (isSearchEquivalent(existingSearch, search)) {
                throw new AppException("Déjà ajouté", HttpStatus.CONFLICT); // Modifier le code de statut pour refléter le conflit
            }
        }
        if(search.getNickName() == null)
        {
            search.setNickName("Favoris ( " + (user.getSearchFavoris().size()+1) + " )");
        }
        searchRepository.save(search);
        user.getSearchFavoris().add(search);
        saveUser(user);
        return true;
    }

    public boolean removeSearchFavoris(Search search) throws AppException {
        String email = getCurrentUserEmail();
        Optional<User> optionalUser = findByEmail(email);
        if(optionalUser.isEmpty())throw new AppException("User Not Found",HttpStatus.NOT_FOUND);
        User user = optionalUser.get();
        if(!user.getSearchFavoris().contains(search))throw new AppException("n existe pas",HttpStatus.NOT_FOUND);
        user.getSearchFavoris().remove(search);
        saveUser(user);
        return true;
    }

    private boolean isSearchEquivalent(Search s1, Search s2) {
        return Objects.equals(s1.getUserId(), s2.getUserId()) &&
                Objects.equals(s1.getKeyword(), s2.getKeyword()) &&
                Objects.equals(s1.getSearchCategory(), s2.getSearchCategory()) &&
                s1.getSearchDistance() == s2.getSearchDistance() &&
                Objects.equals(s1.getSearchLatitude(), s2.getSearchLatitude()) &&
                Objects.equals(s1.getSearchLongitude(), s2.getSearchLongitude()) &&
                Objects.equals(s1.getSearchPriceMin(), s2.getSearchPriceMin()) &&
                Objects.equals(s1.getSearchPriceMax(), s2.getSearchPriceMax()) &&
                s1.isSearchGratuit() == s2.isSearchGratuit() &&
                s1.isSearchSurDevis() == s2.isSearchSurDevis();
    }

    public void updateSettings(Settings settings) throws AppException {
        String email = getCurrentUserEmail();
        Optional<User> optionalUser = findByEmail(email);
        if(optionalUser.isEmpty())throw new AppException("User Not Found",HttpStatus.NOT_FOUND);
        User user = optionalUser.get();
        settings.setId(user.getSettings().getId());
        settingsRepository.save(settings);
        user.setSettings(settings);
        userRepository.save(user);
    }

    public List<User> findAllByFavorisContaining(Demande demande) {
        return userRepository.findAllByFavorisContaining(demande);
    }
}
