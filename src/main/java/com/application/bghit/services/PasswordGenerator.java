package com.application.bghit.services;

import java.security.SecureRandom;
import java.util.Random;

public class PasswordGenerator {

    private static final String UPPER_CASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER_CASE = UPPER_CASE.toLowerCase();
    private static final String NUMBERS = "0123456789";
    private static final String SPECIAL_CHARACTERS = "!@#$%^&*";
    private static final String ALL_ALLOWED_CHARACTERS = UPPER_CASE + LOWER_CASE + NUMBERS + SPECIAL_CHARACTERS;
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateRandomPassword() {
        int length = 15;

        // Assurer que le mot de passe contient au moins un caractère de chaque type
        StringBuilder password = new StringBuilder(length);
        password.append(UPPER_CASE.charAt(RANDOM.nextInt(UPPER_CASE.length())));
        password.append(LOWER_CASE.charAt(RANDOM.nextInt(LOWER_CASE.length())));
        password.append(NUMBERS.charAt(RANDOM.nextInt(NUMBERS.length())));
        password.append(SPECIAL_CHARACTERS.charAt(RANDOM.nextInt(SPECIAL_CHARACTERS.length())));

        // Remplir le reste du mot de passe avec des caractères aléatoires de tous les types
        for (int i = 4; i < length; i++) {
            password.append(ALL_ALLOWED_CHARACTERS.charAt(RANDOM.nextInt(ALL_ALLOWED_CHARACTERS.length())));
        }

        // Mélanger le mot de passe pour ne pas avoir l'ordre des types de caractères prédéfini
        return shuffleString(password.toString());
    }

    private static String shuffleString(String string) {
        char[] characters = string.toCharArray();
        for (int i = 0; i < characters.length; i++) {
            int randomIndex = i + RANDOM.nextInt(characters.length - i);
            char temp = characters[i];
            characters[i] = characters[randomIndex];
            characters[randomIndex] = temp;
        }
        return new String(characters);
    }
}
