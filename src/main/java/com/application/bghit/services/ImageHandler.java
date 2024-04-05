package com.application.bghit.services;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.*;
import com.google.cloud.vision.v1.Image;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.protobuf.ByteString;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;

@Service
public class ImageHandler {

    @Value("${google.cloud.vision.api.key}")
    private String apiKey;
    private ImageAnnotatorClient imageAnnotatorClient;

    public ImageHandler() throws IOException {
        this.initImageAnnotatorClient();
    }

    public void initImageAnnotatorClient() throws IOException {
        // Chemin vers le fichier JSON contenant vos clés d'API
        ClassPathResource resource = new ClassPathResource("static/images/vision/e-dragon-414300-459dc1c377be.json");

        // Charger les informations d'identification
        GoogleCredentials credentials = GoogleCredentials.fromStream(resource.getInputStream())
                .createScoped(ImageAnnotatorSettings.getDefaultServiceScopes());

        ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                .build();

        this.imageAnnotatorClient = ImageAnnotatorClient.create(settings);
    }
    public byte[] compressImage(MultipartFile originalImage) throws IOException {
        BufferedImage image = ImageIO.read(originalImage.getInputStream());

        int newWidth = image.getWidth() / 2; // Réduction de la largeur par deux pour l'exemple
        int newHeight = (int) (((double) image.getHeight() / (double) image.getWidth()) * newWidth);

        BufferedImage compressedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = compressedImage.createGraphics();
        g2d.drawImage(image, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(compressedImage, "jpeg", os);
        return os.toByteArray();
    }
    public byte[] compressImage(BufferedImage originalImage) throws IOException {
        int newWidth = originalImage.getWidth() / 2; // Réduction de la largeur par deux pour l'exemple
        int newHeight = (int) (((double) originalImage.getHeight() / (double) originalImage.getWidth()) * newWidth);

        BufferedImage compressedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = compressedImage.createGraphics();
        g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g2d.dispose();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(compressedImage, "jpeg", os);
        return os.toByteArray();
    }
    public byte[] addWatermark(MultipartFile originalImage, String watermarkText) throws IOException {
        BufferedImage image = ImageIO.read(originalImage.getInputStream());


        Graphics2D g2d = (Graphics2D) image.getGraphics();
        AlphaComposite alphaChannel = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f);
        g2d.setComposite(alphaChannel);
        g2d.setColor(Color.BLUE);
        g2d.setFont(new Font("Arial", Font.BOLD, 64));
        FontMetrics fontMetrics = g2d.getFontMetrics();
        Rectangle2D rect = fontMetrics.getStringBounds(watermarkText, g2d);

        // Positionne le filigrane en bas à gauche de l'image
        // On laisse une petite marge pour que le texte ne soit pas complètement collé aux bords
        int x = 10; // Marge à gauche
        int y = image.getHeight() - (int) rect.getHeight() - fontMetrics.getDescent() + 10; // Marge en bas, en tenant compte de la descente du texte

        g2d.drawString(watermarkText, x, y);
        g2d.dispose();

        return compressImage(image);
    }

    public boolean analyseImage(byte[] imageData) {
        ByteString imgBytes = ByteString.copyFrom(imageData);
        Image img = Image.newBuilder().setContent(imgBytes).build();
        Feature feat = Feature.newBuilder().setType(Feature.Type.SAFE_SEARCH_DETECTION).build();
        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addFeatures(feat)
                .setImage(img)
                .build();

        BatchAnnotateImagesResponse response = this.imageAnnotatorClient.batchAnnotateImages(java.util.Collections.singletonList(request));
        for (AnnotateImageResponse res : response.getResponsesList()) {
            if (res.hasError()) {
                System.out.printf("Erreur : %s\n", res.getError().getMessage());
                return false; // Si erreur, on considère que l'analyse n'a pas pu conclure à l'inapproprié
            }
            SafeSearchAnnotation annotation = res.getSafeSearchAnnotation();
            if (annotation != null) {
                // Conditions pour juger une image comme inappropriée
                if (annotation.getAdult().getNumber() >= Likelihood.POSSIBLE.getNumber() ||
                        annotation.getViolence().getNumber() >= Likelihood.POSSIBLE.getNumber() ||
                        annotation.getRacy().getNumber() >= Likelihood.POSSIBLE.getNumber()) {
                    return true; // Image inappropriée
                }
            }
        }
        return false; // Si aucune condition inappropriée n'est remplie, l'image est considérée comme appropriée
    }



    /*public void analyzeImage(byte[] imageData) { // Ou chargez-la de vos configurations
        String endpoint = "https://vision.googleapis.com/v1/images:annotate?key=" + apiKey;

        String jsonRequest = buildJsonRequest(imageData);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonRequest))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String responseBody = response.body();
            System.out.println("Réponse de l'API Vision: " + responseBody);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private String buildJsonRequest(byte[] imageData) {
        String encodedImage = java.util.Base64.getEncoder().encodeToString(imageData);
        // La requête se concentre sur la détection de contenu inapproprié
        return "{\"requests\":[{\"image\":{\"content\":\"" + encodedImage + "\"},\"features\":[{\"type\":\"SAFE_SEARCH_DETECTION\"}]}]}";
    }*/
}
