package com.alertfire.communication;

import com.alertfire.model.LoRaMessage;
import javafx.application.Platform;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Gère la transmission des messages LoRa
 */
public class LoRaTransmitter {
    private static final double LORA_RANGE_KM = 1.0; // Portée LoRa de 1 km
    private static final String BACKEND_URL = "http://localhost:5000/api/receive-alert";

    /**
     * Envoie un message LoRa
     * @param message Message à envoyer
     */
    public void sendMessage(LoRaMessage message) {
        // Envoyer le message au récepteur globa l
        LoRaReceiver.getInstance().receiveMessage(message);

        System.out.println("Message LoRa envoyé par UUID: " + message.getUuid() +
                " | Temp: " + String.format("%.1f", message.getTemperature()) +
                "°C | CO2: " + String.format("%.0f", message.getCO2Level()) + " ppm");

        // Si c'est un projet maître, envoyer au backend Flask
        sendToBackend(message);
    }

    /**
     * Envoie les données au backend Flask (seulement si projet maître)
     * @param message Message à envoyer
     */
    private void sendToBackend(LoRaMessage message) {
        // Le nom de classe contient "Master" pour les projets maîtres
        String className = message.getUuid().toString();

        System.out.println("TEST1");

        System.out.println("TEST2");

        // Exécuter l'envoi dans un thread séparé pour ne pas bloquer l'UI
        new Thread(() -> {
            try {
                // Configurer la connexion HTTP
                System.out.println("TEST3");
                URL url = new URL(BACKEND_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // Préparer les données JSON
                String jsonPayload = message.toJson();
                System.out.println(jsonPayload);

                // Envoyer les données
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                // Lire la réponse
                int responseCode = connection.getResponseCode();

                // Afficher le résultat dans l'interface utilisateur
                final String result = "Backend API response: " + responseCode;
                Platform.runLater(() -> {
                    System.out.println(result);
                });

                connection.disconnect();
            } catch (IOException e) {
                final String error = "Erreur d'envoi au backend: " + e.getMessage();
                Platform.runLater(() -> {
                    System.out.println(error);
                });
            }
        }).start();
    }

    /**
     * Vérifie si deux projets sont à portée LoRa l'un de l'autre
     * @param sourceRow Ligne du projet source
     * @param sourceCol Colonne du projet source
     * @param targetRow Ligne du projet cible
     * @param targetCol Colonne du projet cible
     * @param cellSizeKm Taille d'une cellule en kilomètres
     * @return true si les projets sont à portée, false sinon
     */
    public static boolean isInRange(int sourceRow, int sourceCol, int targetRow, int targetCol, double cellSizeKm) {
        double distance = Math.sqrt(Math.pow(targetRow - sourceRow, 2) + Math.pow(targetCol - sourceCol, 2)) * cellSizeKm;
        return distance <= LORA_RANGE_KM;
    }
}
