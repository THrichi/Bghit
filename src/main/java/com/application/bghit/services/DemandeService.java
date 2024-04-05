package com.application.bghit.services;

import com.application.bghit.dtos.*;
import com.application.bghit.entities.*;
import com.application.bghit.exceptions.AppException;
import com.application.bghit.repositories.DemandeRepository;
import com.application.bghit.repositories.RoomRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DemandeService {

    private final DemandeRepository demandeRepository;
    private final RoomRepository roomRepository;
    private final ImageHandler imageHandler;

    private final UserService userService;
    private final Path rootLocation = Paths.get("src/main/resources/static/images/demandes");

    private static String UPLOAD_DIR = "src/main/resources/static/images/demandes/";




    /*public List<Demande> findAllDemandes() {
        return demandeRepository.findAll();
    }*/

    public Page<DemandeListDto> findAll(Specification<Demande> specification, Pageable pageable){
        Page<Demande> page = demandeRepository.findAll(specification, pageable);
        return page.map(this::convertToDto); // Utiliser map pour convertir chaque Demande en DemandeListDto
    }
    public List<DemandeListDto> massConvert(List<Demande> demandes){
        List<DemandeListDto> tmp = new ArrayList<>();
        for (Demande demande : demandes)
        {
            tmp.add(convertToDto(demande));
        }
        return  tmp;
    }
    public Optional<Demande> findDemandeById(Long id) {
        return demandeRepository.findById(id);
    }

    public Demande saveDemande(Demande demande) {
        return demandeRepository.save(demande);
    }

    /*public void deleteDemande(Long id) {
        demandeRepository.deleteById(id);
    }*/


    public List<Demande> findDemandesByUserEmail(String email) {
        return demandeRepository.findByUserEmail(email);
    }
    public List<Demande> findDemandesByUserId(Long id) {
        return demandeRepository.findByUserId(id);
    }

    public static Specification<Demande> demandeSpecification(String titre, String categorie, String etat) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (titre != null && !titre.isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("titre")), "%" + titre.toLowerCase() + "%"));
            }
            if (categorie != null && !categorie.isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("categorie")), categorie.toLowerCase()));
            }
            if (etat != null && !etat.isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("etat")), etat.toLowerCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    @Transactional
    public ConfirmationResponse createDemande(DemandeCreateDto demandeDto) throws IOException, AppException {
        Demande demande = new Demande();
        demande.setDescription(demandeDto.description());
        demande.setEstPayant(demandeDto.estPayant());
        demande.setSurDevis(demandeDto.surDevis());
        demande.setPrix(demandeDto.prix());
        demande.setCategorie(demandeDto.categorie());
        demande.setLieu(demandeDto.lieu());
        demande.setLatitude(demandeDto.latitude());
        demande.setLongitude(demandeDto.longitude());
        demande.setType(demandeDto.type());
        // Initialiser les champs manquants
        demande.setDateCreation(new Date());
        demande.setEtat(Demande.DemandeStatus.CREATED);
        demande.setNombreDeVues(0);
        demande.setNombreDeReponses(0);
        demande.setTheme("Thème par défaut"); // Adaptez à votre logique

        // Trouver l'utilisateur et le lier à la demande
        User user = userService.findByEmail(demandeDto.userEmail())
                .orElseThrow(() -> new AppException("Utilisateur Inconnu", HttpStatus.BAD_REQUEST));
        demande.setUser(user);

        // Gérer l'enregistrement des images
        if (demandeDto.images() != null) {
            for (MultipartFile file : demandeDto.images()) {
                if (!file.isEmpty()) {
                // Génération d'un nom de fichier unique pour chaque fichier

                    Path userDirectory = Paths.get(UPLOAD_DIR, user.getId().toString());
                    if (!Files.exists(userDirectory)) {
                        try {
                            Files.createDirectories(userDirectory);
                        } catch (IOException e) {
                            throw new AppException("Impossible de créer le répertoire pour l'utilisateur", HttpStatus.BAD_REQUEST);
                        }
                    }
                    String fileExtension = getFileExtension(Objects.requireNonNull(file.getOriginalFilename()));
                    String uniqueFileName = user.getId() + "/" + UUID.randomUUID() + (fileExtension.isEmpty() ? ".jpeg" : "." + fileExtension);
                    Path fileNameAndPath = Paths.get(UPLOAD_DIR, uniqueFileName);// Utilisation du nom de fichier unique pour le retour

                    try {

                        byte[] fileBytes = imageHandler.addWatermark(file, "Bghite.ma");
                        if(imageHandler.analyseImage(fileBytes)) return new ConfirmationResponse(false, ConfirmationResponse.confirmStatus.ERROR);;
                        Files.write(fileNameAndPath, fileBytes);
                        demande.addImage(new Image(uniqueFileName,demande));
                    } catch (IOException e) {
                        throw new AppException("Erreur lors de la sauvegarde des fichiers", HttpStatus.BAD_REQUEST);
                    }
                }
            }
        }
        demandeRepository.save(demande);
        return new ConfirmationResponse(true, ConfirmationResponse.confirmStatus.SUCCESS);
    }
    private String getFileExtension(String filename) {
        if (filename.contains(".")) {
            return filename.substring(filename.lastIndexOf(".") + 1);
        } else {
            // Gérer les fichiers sans extension ou attribuer une extension par défaut
            return ""; // ou retourner par exemple "png" ou "jpg" selon le contexte
        }
    }
    private String saveImage(MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            // Assurez-vous que le dossier des images existe ou est créé
            Files.createDirectories(this.rootLocation); // Ajoutez cette ligne pour créer le dossier si nécessaire

            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename(); // Pour éviter les doublons
            Files.copy(file.getInputStream(), this.rootLocation.resolve(filename));
            return rootLocation.resolve(filename).toString();
        }

        return null;
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

    public void deleteDemande(String  userEmail,Long demandeId) throws AppException {

        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new AppException("Demande non trouvée", HttpStatus.NOT_FOUND));

        if (!userEmail.equals(demande.getUser().getEmail())) {
            throw new AppException("La Demande n'appartient pas à l'utilisateur", HttpStatus.BAD_REQUEST);
        }
        if (!demande.getEtat().equals(Demande.DemandeStatus.ONLINE) && !demande.getEtat().equals(Demande.DemandeStatus.CLOSED)) {
            throw new AppException("La Demande ne peut pas etre supprimer", HttpStatus.BAD_REQUEST);
        }


        // Trouver toutes les Rooms associées à la Demande
        List<Room> rooms = roomRepository.findByDemande(demande);

        // Retirer la Demande de chaque Room
        for (Room room : rooms) {
            room.setStatus(Room.RoomStatus.CLOSED);
            room.setDemande(null); // Retirer la référence à la Demande
            roomRepository.save(room); // Sauvegarder la Room modifiée
        }

        List<User> usersWithDemandeInFavoris = userService.findAllByFavorisContaining(demande);

        // Retirer la demande des favoris de chaque utilisateur
        for (User user : usersWithDemandeInFavoris) {
            user.getFavoris().remove(demande);
            userService.saveUser(user); // Sauvegarder les changements pour chaque utilisateur
        }


        List<Image> tmpImages = demande.getImages();
        for (Image image : tmpImages)
        {
            Path pathToFile = Paths.get(UPLOAD_DIR, image.getUrl());
            try {
                Files.deleteIfExists(pathToFile); // Suppression du fichier
            } catch (IOException e) {
                throw new AppException("Erreur lors de la suppression du fichier", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        demande.getImages().clear(); // Clears the existing collection without replacing it

        // Suppression de la relation
        demandeRepository.delete(demande); // Sauvegarde de l'utilisateur mis à jour
    }
    public List<CategorieCountDTO> getTop5Categories(Demande.DemandeType type) {
        return demandeRepository.findTopCategories(type,PageRequest.of(0, 5));
    }
    public Demande changeDemandeStatus(Long demandeId, Demande.DemandeStatus newStatus, Long reservedToIdUser) {
        Optional<Demande> demandeOptional = demandeRepository.findById(demandeId);
        if (demandeOptional.isPresent()) {
            Demande demande = demandeOptional.get();
            demande.setEtat(newStatus);
            if(reservedToIdUser!=null)
            {
                demande.setReservedToIdUser(reservedToIdUser);
            }
            demandeRepository.save(demande);
            return demande;
        }
        return null;
    }

}

