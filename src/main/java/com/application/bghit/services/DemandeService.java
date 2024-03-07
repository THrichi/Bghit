package com.application.bghit.services;

import com.application.bghit.dtos.DemandeCreateDto;
import com.application.bghit.dtos.DemandeListDto;
import com.application.bghit.dtos.UserDto;
import com.application.bghit.entities.Demande;
import com.application.bghit.entities.Image;
import com.application.bghit.entities.PhotoCollection;
import com.application.bghit.entities.User;
import com.application.bghit.exceptions.AppException;
import com.application.bghit.repositories.DemandeRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    public Demande createDemande(DemandeCreateDto demandeDto) throws IOException, AppException {
        Demande demande = new Demande();
        System.out.println("demande :" + demande);
        demande.setTitre(demandeDto.titre());
        demande.setDescription(demandeDto.description());
        demande.setEstPayant(demandeDto.estPayant());
        demande.setSurDevis(demandeDto.surDevis());
        demande.setPrix(demandeDto.prix());
        demande.setCategorie(demandeDto.categorie());
        demande.setLieu(demandeDto.lieu());
        demande.setLatitude(demandeDto.latitude());
        demande.setLongitude(demandeDto.longitude());

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
            /*for (MultipartFile file : demandeDto.images()) {
                String imageUrl = saveImage(file); // Supposons que cette méthode sauvegarde l'image et retourne l'URL
                Image image = new Image(imageUrl, demande); // Assurez-vous que le constructeur de Image définit correctement les relations
                demande.addImage(image);
            }*/

            for (MultipartFile file : demandeDto.images()) {
                // Génération d'un nom de fichier unique pour chaque fichier

                Path userDirectory = Paths.get(UPLOAD_DIR, user.getId().toString());
                if (!Files.exists(userDirectory)) {
                    try {
                        Files.createDirectories(userDirectory);
                    } catch (IOException e) {
                        throw new AppException("Impossible de créer le répertoire pour l'utilisateur", HttpStatus.BAD_REQUEST);
                    }
                }

                String uniqueFileName = user.getId()+"/"+UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path fileNameAndPath = Paths.get(UPLOAD_DIR, uniqueFileName);// Utilisation du nom de fichier unique pour le retour

                try {
                    Files.write(fileNameAndPath, file.getBytes());
                    demande.addImage(new Image(uniqueFileName,demande));
                } catch (IOException e) {
                    throw new AppException("Erreur lors de la sauvegarde des fichiers", HttpStatus.BAD_REQUEST);
                }
            }
        }

        return demandeRepository.save(demande);
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
   public  DemandeListDto convertToDto(Demande demande) {
            if(demande != null)
            {
                List<String> imagesUrls = demande.getImages().stream()
                        .map(Image::getUrl) // Utilisation directe de l'URL de l'image
                        .collect(Collectors.toList());
                return new DemandeListDto(
                        demande.getIdDemande(),
                        demande.getTitre(),
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
                        demande.getReservedToIdUser()
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
            throw new AppException("La photo n'appartient pas à l'utilisateur", HttpStatus.BAD_REQUEST);
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

