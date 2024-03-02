package com.application.bghit.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
public class FileUploadController {

    private static String UPLOAD_DIR = "src/main/resources/static/images/";

    @PostMapping("/images/uploadMultipleFiles")
    public String uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        StringBuilder fileNames = new StringBuilder();
        for (MultipartFile file : files) {
            // Génération d'un nom de fichier unique pour chaque fichier
            String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path fileNameAndPath = Paths.get(UPLOAD_DIR, uniqueFileName);
            fileNames.append(uniqueFileName).append(" "); // Utilisation du nom de fichier unique pour le retour

            try {
                Files.write(fileNameAndPath, file.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
                return "Erreur lors de la sauvegarde des fichiers";
            }
        }
        return "Fichiers sauvegardés avec succès : " + fileNames.toString();
    }
}